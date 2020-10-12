package com.baidu.com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.com.baidu.shop.feign.BrandFeign;
import com.baidu.com.baidu.shop.feign.CategoryFeign;
import com.baidu.com.baidu.shop.feign.GoodsFeign;
import com.baidu.com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ShopElasticsearchServiceImpl
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/16
 * @Version V1.0
 **/
@RestController
@Slf4j
public class ShopElasticsearchServiceImpl extends BaseApiService implements ShopElasticsearchService {
    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private BrandFeign brandFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Result<JSONObject> saveData(Integer spuId) {
        //通过spuId查询数据
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);

        List<GoodsDoc> goodsDocs = this.esGoodsInfo(spuDTO);//在这行代码有可能是查询不到数据的
        elasticsearchRestTemplate.save(goodsDocs.get(0));

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> delData(Integer spuId) {
        return null;
    }

    @Override
    public GoodsResponse search(String search,Integer page, String filter) {

        System.out.println(filter);

        if (StringUtil.isEmpty(search)) throw new RuntimeException("查询内容不能为空");
        NativeSearchQuery query = this.getSearchQueryBuilder(search, page, filter).build();
        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(query, GoodsDoc.class);
        List<SearchHit<GoodsDoc>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits.getSearchHits());

        List<GoodsDoc> list = highLightHit.stream()
                .map(searchHit -> searchHit.getContent()).collect(Collectors.toList());

        long total = searchHits.getTotalHits();
        long totalPage = Double.valueOf(Math.ceil(Long.valueOf(total).doubleValue() / 10)).longValue();

        //获取聚合数据
        Aggregations aggregations = searchHits.getAggregations();

//        List<CategoryEntity> categoryList = this.getCategoryList(aggregations);//通过分类id获取分类集合

        Map<Integer, List<CategoryEntity>> map = this.getCategoryList(aggregations);

        List<CategoryEntity> categoryList = null;
        Integer hotCid = 0;

        for(Map.Entry<Integer,List<CategoryEntity>> mapEntry : map.entrySet()){
            hotCid = mapEntry.getKey();
            categoryList = mapEntry.getValue();
        }
        Map<String, List<String>> specParamValueMap  = this.getspecParam(hotCid, search);
        List<BrandEntity> brandList = this.getBrandList(aggregations);//通过品牌id获取品牌集合

        GoodsResponse goodsResponse = new GoodsResponse(total, totalPage, brandList, categoryList, list, specParamValueMap);
        return goodsResponse;
    }

    private Map<String, List<String>> getspecParam(Integer hotCid,String search){
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(hotCid);
        specParamDTO.setSearching(true);

        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParamInfo(specParamDTO);
        if(specParamResult.getCode() == 200){
            List<SpecParamEntity> specParamList = specParamResult.getData();

            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"brandName","category","title"));

            queryBuilder.withPageable(PageRequest.of(0,1));

            specParamList.stream().forEach(specParam ->{
                queryBuilder.addAggregation(AggregationBuilders.terms(specParam.getName()).field("specs." + specParam.getName() + ".keyword"));
            });
            SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);

            Map<String, List<String>> map = new HashMap<>();
            Aggregations aggregations = searchHits.getAggregations();

            specParamList.stream().forEach(specParam ->{
                Terms terms = aggregations.get(specParam.getName());
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                List<String> valueList = buckets.stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());

                map.put(specParam.getName(),valueList);

            });
            return map;
        }
        return null;
    }

    /**
     * 构建查询条件
     * @param search
     * @param page
     * @return
     */
    private NativeSearchQueryBuilder getSearchQueryBuilder(String search, Integer page, String filter){
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();

        if(StringUtil.isNotEmpty(filter) && filter.length() > 2){
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            Map<String, String> filterMap = JSONUtil.toMapValueString(filter);

            filterMap.forEach((key,value) -> {
                MatchQueryBuilder matchQueryBuilder = null;

                if(key.equals("cid3") || key.equals("brandId")){
                    matchQueryBuilder = QueryBuilders.matchQuery(key, value);
                }else{
                    matchQueryBuilder = QueryBuilders.matchQuery("specs." + key + ".keyword",value);
                }
                boolQueryBuilder.must(matchQueryBuilder);
            });
            searchQueryBuilder.withFilter(boolQueryBuilder);
        }

        searchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"brandName","categoryName","title"));
        //品牌  分类  聚合
        searchQueryBuilder.addAggregation(AggregationBuilders.terms("cid_agg").field("cid3"));
        searchQueryBuilder.addAggregation(AggregationBuilders.terms("brand_agg").field("brandId"));
        //高亮
        searchQueryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));
        //分页
        searchQueryBuilder.withPageable(PageRequest.of(page-1,10));
        return searchQueryBuilder;
    }

    /**
     * 获取品牌集合
     * @param aggregations
     * @return
     */
    private List<BrandEntity> getBrandList(Aggregations aggregations){
        Terms brand_agg = aggregations.get("brand_agg");
        List<String> brandIdList = brand_agg.getBuckets().stream()
                .map(brandBucket -> brandBucket.getKeyAsNumber().intValue() + "").collect(Collectors.toList());
        //通过品牌id集合去查询数据
        Result<List<BrandEntity>> brandResult = brandFeign.getBrandByIdList(String.join(",", brandIdList));
        return brandResult.getData();
    }

    /**
     * 获取分类集合
     * @param aggregations
     * @return
     */
    private Map<Integer, List<CategoryEntity>> getCategoryList(Aggregations aggregations){
        Terms cid_agg = aggregations.get("cid_agg");
        List<? extends Terms.Bucket> cidBuckets = cid_agg.getBuckets();

        List<Integer> hotCidArr = Arrays.asList(0);

        List<Long> maxCount = Arrays.asList(0L);

        Map<Integer, List<CategoryEntity>> map = new HashMap<>();

        List<String> cidList = cidBuckets.stream().map(cidbucket -> {
            Number keyAsNumber = cidbucket.getKeyAsNumber();

            if(cidbucket.getDocCount() > maxCount.get(0)){
                maxCount.set(0,cidbucket.getDocCount());
                hotCidArr.set(0,keyAsNumber.intValue());
            }
            return keyAsNumber.intValue() + "";
        }).collect(Collectors.toList());

//        List<String> cidList = cidBuckets.stream().map(cidbucket -> {
//            Number keyAsNumber = cidbucket.getKeyAsNumber();
//            return keyAsNumber.intValue() + "";
//        }).collect(Collectors.toList());

        String cidsStr = String.join(",", cidList);
        Result<List<CategoryEntity>> caterogyResult = categoryFeign.getCategoryByIdList(cidsStr);

        map.put(hotCidArr.get(0),caterogyResult.getData());

        return map;
    }

    @Override
    public Result<JSONObject> initGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(!indexOperations.exists()){
            indexOperations.create();
            log.info("索引创建成功");
            indexOperations.createMapping();
            log.info("映射创建成功");
        }

        //批量新增数据
        List<GoodsDoc> goodsDocs = this.esGoodsInfo(new SpuDTO());
        elasticsearchRestTemplate.save(goodsDocs);

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> clearGoodsEsData() {
        IndexOperations index = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(index.exists()){
            index.delete();
        }
        return this.setResultSuccess();
    }

    private List<GoodsDoc> esGoodsInfo(SpuDTO spuDTO) {
        //查询spu信息
        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);
        //查询出来的数据是多个spu
        List<GoodsDoc> goodsDocs = new ArrayList<>();
        if(spuInfo.getCode() == HTTPStatus.OK){
            //spu数据
            List<SpuDTO> spuList = spuInfo.getData();

            spuList.stream().forEach(spu -> {

                GoodsDoc goodsDoc = new GoodsDoc();

                goodsDoc.setId(spu.getId().longValue());
                goodsDoc.setTitle(spu.getTitle());
                goodsDoc.setSubTitle(spu.getSubTitle());
                goodsDoc.setBrandName(spu.getBrandName());
                goodsDoc.setCategoryName(spu.getCategoryName());
                goodsDoc.setBrandId(spu.getBrandId().longValue());
                goodsDoc.setCid1(spu.getCid1().longValue());
                goodsDoc.setCid2(spu.getCid2().longValue());
                goodsDoc.setCid3(spu.getCid3().longValue());
                goodsDoc.setCreateTime(spu.getCreateTime());

                //通过spuID查询skuList
                Map<List<Long>, List<Map<String, Object>>> skus = this.getSkusAndPriceList(spu.getId());

                skus.forEach((key,value) -> {
                    goodsDoc.setPrice(key);
                    goodsDoc.setSkus(JSONUtil.toJsonString(value));
                });

                //通过cid3查询规格参数
                Map<String, Object> specMap = this.getSpecMap(spu);

                goodsDoc.setSpecs(specMap);
                goodsDocs.add(goodsDoc);
            });
            System.out.println(goodsDocs);
        }

        return goodsDocs;
    }

    private Map<List<Long>, List<Map<String, Object>>> getSkusAndPriceList(Integer spuId){

        Map<List<Long>, List<Map<String, Object>>> hashMap = new HashMap<>();

        Result<List<SkuDTO>> skuResult = goodsFeign.getSkuBySpuId(spuId);
        List<Long> priceList = new ArrayList<>();
        List<Map<String, Object>> skuMap = null;

        if(skuResult.getCode() == HTTPStatus.OK){

            List<SkuDTO> skuList = skuResult.getData();
            skuMap = skuList.stream().map(sku -> {
                Map<String, Object> map = new HashMap<>();

                map.put("id", sku.getId());
                map.put("title", sku.getTitle());
                map.put("images", sku.getImages());
                map.put("price", sku.getPrice());

                priceList.add(sku.getPrice().longValue());

                return map;
            }).collect(Collectors.toList());
        }
        hashMap.put(priceList,skuMap);
        return hashMap;
    }

    private Map<String, Object> getSpecMap(SpuDTO spuDTO){

        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spuDTO.getCid3());
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParamInfo(specParamDTO);

        Map<String, Object> specMap = new HashMap<>();

        if (specParamResult.getCode() == HTTPStatus.OK) {
            //只有规格参数的id和规格参数的名字
            List<SpecParamEntity> paramList = specParamResult.getData();

            //通过spuid去查询spuDetail,detail里面有通用和特殊规格参数的值
            Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBydSpu(spuDTO.getId());
            //因为spu和spuDetail one --> one

            if(spuDetailResult.getCode() == HTTPStatus.OK){
                SpuDetailEntity spuDetaiInfo = spuDetailResult.getData();

                //通用规格参数的值
                String genericSpecStr = spuDetaiInfo.getGenericSpec();
                Map<String, String> genericSpecMap = JSONUtil.toMapValueString(genericSpecStr);

                //特有规格参数的值
                String specialSpecStr = spuDetaiInfo.getSpecialSpec();
                Map<String, List<String>> specialSpecMap = JSONUtil.toMapValueStrList(specialSpecStr);

                paramList.stream().forEach(param -> {

                    if (param.getGeneric()) {

                        if(param.getNumeric() && param.getSearching()){
                            specMap.put(param.getName(), this.chooseSegment(genericSpecMap.get(param.getId() + ""),param.getSegments(),param.getUnit()));
                        }else{
                            specMap.put(param.getName(), genericSpecMap.get(param.getId() + ""));
                        }
                    } else {
                        specMap.put(param.getName(), specialSpecMap.get(param.getId() + ""));
                    }
                });
            }
        }
        return specMap;
    }

    /**
     * 把具体的值转换成区间-->不做范围查询
     * @param value
     * @param segments
     * @param unit
     * @return
     */
    private String chooseSegment(String value, String segments, String unit) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + unit + "以上";
                }else if(begin == 0){
                    result = segs[1] + unit + "以下";
                }else{
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }

}
