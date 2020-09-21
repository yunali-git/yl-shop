package com.baidu;

import com.mr.RunTestEsApplication;
import com.mr.entity.GoodsEntity;
import com.mr.repostory.GoodsEsRepository;
import com.mr.utils.ESHighLightUtil;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName testES
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/14
 * @Version V1.0
 **/
//让测试在Spring容器环境下执行
@RunWith(SpringRunner.class)
//声明启动类,当测试方法运行的时候会帮我们自动启动容器
@SpringBootTest(classes = { RunTestEsApplication.class})
public class testES {
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private GoodsEsRepository goodsEsRepository;

    /*
    创建索引
     */
    @Test
    public void createGoodsIndex(){
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(IndexCoordinates.of("indexname"));
        indexOperations.create();//创建索引
        //indexOperations.exists() 判断索引是否存在
        System.out.println(indexOperations.exists()?"索引创建成功":"索引创建失败");
    }

    /*
   创建映射
    */
    @Test
    public void createGoodsMapping(){

        //此构造函数会检查有没有索引存在,如果没有则创建该索引,如果有则使用原来的索引
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsEntity.class);

        //indexOperations.createMapping();//创建映射,不调用此函数也可以创建映射,这就是高版本的强大之处
        System.out.println("映射创建成功");
    }

    /*
   删除索引
    */
    @Test
    public void deleteGoodsIndex(){
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsEntity.class);

        indexOperations.delete();
        System.out.println("索引删除成功");
    }

    @Test
    public void saveData(){

        GoodsEntity entity = new GoodsEntity();
        entity.setId(1L);
        entity.setBrand("小米");
        entity.setCategory("手机");
        entity.setImages("xiaomi.jpg");
        entity.setPrice(1000D);
        entity.setTitle("小米3");

        goodsEsRepository.save(entity);

        System.out.println("新增成功");
    }

    /*
   批量新增文档
    */
    @Test
    public void saveAllData(){

        GoodsEntity entity = new GoodsEntity();
        entity.setId(2L);
        entity.setBrand("苹果");
        entity.setCategory("手机");
        entity.setImages("pingguo.jpg");
        entity.setPrice(5000D);
        entity.setTitle("iphone11手机");

        GoodsEntity entity2 = new GoodsEntity();
        entity2.setId(3L);
        entity2.setBrand("三星");
        entity2.setCategory("手机");
        entity2.setImages("sanxing.jpg");
        entity2.setPrice(3000D);
        entity2.setTitle("w2019手机");

        GoodsEntity entity3 = new GoodsEntity();
        entity3.setId(4L);
        entity3.setBrand("华为");
        entity3.setCategory("手机");
        entity3.setImages("huawei.jpg");
        entity3.setPrice(4000D);
        entity3.setTitle("华为mate30手机");

        goodsEsRepository.saveAll(Arrays.asList(entity,entity2,entity3));

        System.out.println("批量新增成功");
    }
    /*
   更新文档
    */
    @Test
    public void updateData(){

        GoodsEntity entity = new GoodsEntity();
        entity.setId(1L);
        entity.setBrand("三星");
        entity.setCategory("手机");
        entity.setImages("sanxing2.jpg");
        entity.setPrice(6000D);
        entity.setTitle("三星手机");

        goodsEsRepository.save(entity);

        System.out.println("修改成功");
    }

    /*
    删除文档
     */
    @Test
    public void delData(){

        GoodsEntity entity = new GoodsEntity();
        entity.setId(1L);

        goodsEsRepository.delete(entity);

        System.out.println("删除成功");
    }

    /*
    查询所有
     */
    @Test
    public void searchAll(){
        //查询总条数
        long count = goodsEsRepository.count();
        System.out.println(count);
        //查询所有数据
        Iterable<GoodsEntity> all = goodsEsRepository.findAll();
        all.forEach(goods -> {
            System.out.println(goods);
        });
    }
    /*
    条件查询
     */
    @Test
    public void searchByParam(){

        List<GoodsEntity> allByAndTitle = goodsEsRepository.findByTitle("小米2");
        System.out.println(allByAndTitle);

        System.out.println("===============================");
        List<GoodsEntity> byAndPriceBetween = goodsEsRepository.findByPriceBetween(1000D, 1001D);
        System.out.println(byAndPriceBetween);

    }

    /*
    自定义查询
     */
    @Test
    public void customizeSearch(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(
                QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("title","小米"))
        );
        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        search.getSearchHits().stream().forEach(hits -> {
            System.out.println(hits.getContent());
        });
    }

    /*
     高亮
     */
    @Test
    public void searchHighLight(){

        //构建高亮查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //构建高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field title = new HighlightBuilder.Field("title");//.Field静态内部类
        title.preTags("<span style=color:red'>");
        title.postTags("</span>");
        highlightBuilder.field(title);

        queryBuilder.withHighlightBuilder(highlightBuilder);//设置高亮

        //boolQuery把各种其它查询通过 must:与  must_not:非  should:或  的方式进行组合
        //matchQuery类型查询，会把查询条件进行分词，然后进行查询,多个词条之间是or的关系
        queryBuilder.withQuery(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("title","小米手机"))
                        .must(QueryBuilders.rangeQuery("price").gte(1000).lte(7000))
        );

        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        queryBuilder.withPageable(PageRequest.of(0,2));

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        List<SearchHit<GoodsEntity>> searchHits = search.getSearchHits();

        //重新设置title
        List<SearchHit<GoodsEntity>> goodsList = searchHits.stream().map(hit -> {
            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            hit.getContent().setTitle(highlightFields.get("title").get(0));
            return hit;
        }).collect(Collectors.toList());

        System.out.println(goodsList);
    }

    /*
   使用高亮工具类
    */
    @Test
    public void highLightUtilTest(){
        //构建高亮查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //构建高亮
        queryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));//设置高亮

        queryBuilder.withQuery(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("title","华为手机"))
                        .must(QueryBuilders.rangeQuery("price").gte(1000).lte(7000))
        );

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        List<SearchHit<GoodsEntity>> searchHits = search.getSearchHits();

        //重新设置title
        List<SearchHit<GoodsEntity>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits);
        System.out.println(highLightHit);
    }

    @Test
    public void searchAggs(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brand_agg").field("brand")
        );

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        Aggregations aggregations = search.getAggregations();

        Terms terms = aggregations.get("brand_agg");

        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount());
        });
        System.out.println(search);
    }

    /*
    聚合函数
     */
    @Test
    public void searchAggMethod(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brand_agg").field("brand")
                        //聚合函数
                        .subAggregation(AggregationBuilders.max("max_price").field("price"))
        );

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        Aggregations aggregations = search.getAggregations();

        Terms terms = aggregations.get("brand_agg");

        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount());

            //获取聚合
            Aggregations aggregations1 = bucket.getAggregations();
            //得到map
            Map<String, Aggregation> map = aggregations1.asMap();

            Max max_price = (Max) map.get("max_price");
            System.out.println(max_price.getValue());
        });
    }
}
