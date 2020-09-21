package com.mr.repostory;

import com.mr.entity.GoodsEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * @ClassName GoodsEsRepository
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/14
 * @Version V1.0
 **/
public interface GoodsEsRepository extends ElasticsearchRepository<GoodsEntity,Long> {
    List<GoodsEntity> findByTitle(String title);

    List<GoodsEntity> findByPriceBetween(Double start,Double end);
}
