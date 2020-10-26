package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.component.MrRabbitMQ;
import com.baidu.shop.constant.MqMessageConstant;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsServiceImpl
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/7
 * @Version V1.0
 **/
@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {
    @Resource
    private SpuMapper spuMapper;

    @Autowired
    private BrandService brandService;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Resource
    private StockMapper stockMapper;

    @Resource
    private MrRabbitMQ mrRabbitMQ;

    @Override
    public Result<SkuEntity> getSkuBySkuId(Long skuId) {

        SkuEntity skuEntity = skuMapper.selectByPrimaryKey(skuId);

        return this.setResultSuccess(skuEntity);
    }

    @Override
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {
        //分页
        if(ObjectUtil.isNotNull(spuDTO.getPage())
                && ObjectUtil.isNotNull(spuDTO.getRows()))
            PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());

        Example example = new Example(SpuEntity.class);

        //条件查询
        Example.Criteria criteria = example.createCriteria();
        if(StringUtil.isNotEmpty(spuDTO.getTitle()))
            criteria.andLike("title","%" + spuDTO.getTitle() + "%");

        if(ObjectUtil.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() != 2)
            criteria.andEqualTo("saleable",spuDTO. getSaleable());

        if (ObjectUtil.isNotNull(spuDTO.getId())) {
            criteria.andEqualTo("id",spuDTO.getId());
        }

        //排序
        if(ObjectUtil.isNotNull(spuDTO.getSort()))
            example.setOrderByClause(spuDTO.getOrderByClause());

        List<SpuEntity> list = spuMapper.selectByExample(example);

        List<SpuDTO> spuDtoList = list.stream().map(spuEntity -> {
            SpuDTO spuDTO1 = BaiduBeanUtil.copyProperties(spuEntity, SpuDTO.class);

            //设置品牌名称
            BrandDTO brandDTO = new BrandDTO();
            brandDTO.setId(spuEntity.getBrandId());
            Result<PageInfo<BrandEntity>> brandInfo = brandService.getBrandInfo(brandDTO);

            if (ObjectUtil.isNotNull(brandInfo)) {

                PageInfo<BrandEntity> data = brandInfo.getData();
                List<BrandEntity> list1 = data.getList();

                if (!list1.isEmpty() && list1.size() == 1) {
                    spuDTO1.setBrandName(list1.get(0).getName());
                }
            }
            //分类名称
            String caterogyName = categoryMapper.selectByIdList(
                    Arrays.asList(spuDTO1.getCid1(), spuDTO1.getCid2(), spuDTO1.getCid3()))
                    .stream().map(category -> category.getName())
                    .collect(Collectors.joining("/"));

            spuDTO1.setCategoryName(caterogyName);
            return spuDTO1;
        }).collect(Collectors.toList());

        PageInfo<SpuEntity> info = new PageInfo<>(list);

//        Map<String, Object> map = new HashMap<>();
//        map.put("list",spuDtoList);
//        map.put("total",info.getTotal());

        return this.setResult(HTTPStatus.OK,info.getTotal() + "",spuDtoList);
    }

    // @Transactional
    @Override
    public Result<JsonObject> spuAdd(SpuDTO spuDTO) {

        Integer spuId = addInfoTransaction(spuDTO);

        //发送消息
        mrRabbitMQ.send(spuId + "", MqMessageConstant.SPU_ROUT_KEY_SAVE);
        return this.setResultSuccess();
    }
    @Transactional
    public Integer addInfoTransaction(SpuDTO spuDTO){

        //JDK的动态代理
        //cglib动态代理
        //aspectj动态代理
        Date date = new Date();

        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        //新增spu
        spuMapper.insertSelective(spuEntity);

        Integer spuId = spuEntity.getId();
        //新增spudetail
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuId);
        spuDetailMapper.insertSelective(spuDetailEntity);

        this.addSkusAndStocks(spuDTO.getSkus(),spuId,date);
        return spuEntity.getId();
    }

    @Override
    public Result<SpuDetailEntity> getSpuDetailBydSpu(Integer spuId) {
        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);
        return this.setResultSuccess(spuDetailEntity);
    }

    @Override
    public Result<List<SkuDTO>> getSkuBySpuId(Integer spuId) {
        List<SkuDTO> list = skuMapper.selectSkuAndStockBySpuId(spuId);
        return this.setResultSuccess(list);
    }

//    @Transactional 修改
    @Override
    public Result<JsonObject> spuEdit(SpuDTO spuDTO) {
        this.editInfoTransaction(spuDTO);

        mrRabbitMQ.send(spuDTO.getId() + "", MqMessageConstant.SPU_ROUT_KEY_UPDATE);
        return this.setResultSuccess();
    }
    @Transactional
    public void editInfoTransaction(SpuDTO spuDTO){
        Date date = new Date();
        //修改spu信息
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);//!!!!!!!!!设置最后更新时间

        spuMapper.updateByPrimaryKeySelective(spuEntity);
        //修改spudetail
        spuDetailMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(),SpuDetailEntity.class));

        this.deleteSkuAndStocks(spuDTO.getId());

        //新增 sku和stock数据
        this.addSkusAndStocks(spuDTO.getSkus(),spuDTO.getId(),date);
    }



    @Transactional
    @Override
    public Result<JsonObject> upOrDownEdit(SpuDTO spuDTO) {
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setId(spuDTO.getId());
        if(spuEntity.getSaleable() == 1){
            spuEntity.setSaleable(0);
            spuMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess("下架成功");
        }else{
            spuEntity.setSaleable(1);
            spuMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess("上架成功");
        }
    }

//    @Transactional  删除
    @Override
    public Result<JsonObject> spuDelete(Integer spuId) {
        this.delInfoTransaction(spuId);

        mrRabbitMQ.send(spuId + "", MqMessageConstant.SPU_ROUT_KEY_DELETE);
        return this.setResultSuccess();
    }

    @Transactional
    public void delInfoTransaction(Integer spuId){
        //删除spu
        spuMapper.deleteByPrimaryKey(spuId);
        //删除detail
        spuDetailMapper.deleteByPrimaryKey(spuId);

        this.deleteSkuAndStocks(spuId);
    }

    private void deleteSkuAndStocks(Integer spuId){
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId",spuId);

        //通过spuId查询出来将要被删除的Sku数据
        List<Long> skuIdList = skuMapper.selectByExample(example).stream()
                .map(sku -> sku.getId()).collect(Collectors.toList());
        if(skuIdList.size() > 0){
            //通过skuId 删除sku
            skuMapper.deleteByIdList(skuIdList);

            //通过skuId 删除stock
            stockMapper.deleteByIdList(skuIdList);
        }
    }

    private void addSkusAndStocks(List<SkuDTO> skus, Integer spuId, Date date){
        skus.stream().forEach(skuDTO -> {
            //新增sku
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuId);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);
            //新增stock
            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            stockMapper.insertSelective(stockEntity);
        });
    }
}
