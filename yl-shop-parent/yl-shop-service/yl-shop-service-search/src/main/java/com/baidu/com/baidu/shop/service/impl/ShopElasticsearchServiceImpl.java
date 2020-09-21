package com.baidu.com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.com.baidu.shop.feign.GoodsFeign;
import com.baidu.com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private SpecificationFeign specificationFeign;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Result<List<GoodsDoc>> search(String search,Integer page) {

        if (StringUtil.isEmpty(search)) return this.setResultError("查询内容不能为空");

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //多字段同时查询
        queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"title","brandName","categoryName"));

        //高亮
        queryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));

        //分页
        queryBuilder.withPageable(PageRequest.of(page-1,10));

        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);

        List<SearchHit<GoodsDoc>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits.getSearchHits());

        List<Object> list = highLightHit.stream().map(searchHit -> searchHit.getContent()).collect(Collectors.toList());

        long total = searchHits.getTotalHits();
        //Integer totalPage = total / 10;
        //double ceil = Math.ceil(Long.valueOf(total).doubleValue() / 10);
        long totalPage = Double.valueOf(Math.ceil(Long.valueOf(total).doubleValue() / 10)).longValue();

        Map<String, Long> messageMap = new HashMap<>();
        messageMap.put("total",total);
        messageMap.put("totalPage",totalPage);
        messageMap.toString();
        return this.setResult(HTTPStatus.OK,JSONUtil.toJsonString(messageMap),list);
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
        List<GoodsDoc> goodsDocs = this.esGoodsInfo();
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

    private List<GoodsDoc> esGoodsInfo() {
        //查询spu信息
        SpuDTO spuDTO = new SpuDTO();
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

                skus.forEach((key, value) -> {
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
