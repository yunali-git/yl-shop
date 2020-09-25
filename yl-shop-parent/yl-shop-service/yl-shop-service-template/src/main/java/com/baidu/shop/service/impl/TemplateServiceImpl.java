package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.TemplateService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TemplateServiceImpl
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/25
 * @Version V1.0
 **/
@RestController
public class TemplateServiceImpl extends BaseApiService implements TemplateService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private BrandFeign brandFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    @Value(value = "${mrshop.static.html.path}")
    private String staticHTMLPath;

    //注入静态化模版
    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public Result<JSONObject> createStaticHTMLTemplate(Integer spuId) {
        Map<String, Object> map = this.getPageInfoSpuId(spuId);
        //创建模板引擎上下文
        Context context = new Context();
        //将所有准备的数据放到模板中
        context.setVariables(map);
        //创建文件 param1:文件路径 param2:文件名称
        File file = new File(staticHTMLPath, spuId + ".html");
        //构建文件输出流
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8");
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }finally {
            writer.close();
        }
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> initStaticHTMLTemplate() {
        //获取spu数据
        Result<List<SpuDTO>> spuInfoResult = goodsFeign.getSpuInfo(new SpuDTO());
        if (spuInfoResult.getCode() ==200) {
            List<SpuDTO> spuDTOList = spuInfoResult.getData();

            spuDTOList.stream().forEach(spuDTO -> {
                createStaticHTMLTemplate(spuDTO.getId());
            });
        }
        return this.setResultSuccess();
    }

    private Map<String, Object> getPageInfoSpuId(Integer spuId) {

        Map<String, Object> map = new HashMap<>();

        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<List<SpuDTO>> spuInfoResult = goodsFeign.getSpuInfo(spuDTO);
        if (spuInfoResult.getCode() == 200) {

            if (spuInfoResult.getData().size() == 1) {
                //spu信息
                SpuDTO spuInfo = spuInfoResult.getData().get(0);
                map.put("spuInfo",spuInfo);
                //品牌信息
                BrandDTO brandDTO = new BrandDTO();
                brandDTO.setId(spuInfo.getBrandId());
                Result<PageInfo<BrandEntity>> brandInfoResult = brandFeign.getBrandInfo(brandDTO);
                if(brandInfoResult.getCode() == 200){
                    PageInfo<BrandEntity> data = brandInfoResult.getData();

                    List<BrandEntity> brandList = data.getList();
                    if(brandList.size() == 1){
                        map.put("brandInfo",brandList.get(0));
                    }
                }
                //分类信息
                List<String> strings = Arrays.asList(spuInfo.getCid1() + "", spuInfo.getCid2() + "", spuInfo.getCid3() + "");
                Result<List<CategoryEntity>> categoryResult = categoryFeign.getCategoryByIdList(String.join(",",strings));
                if(categoryResult.getCode() == 200){
                    List<CategoryEntity> categoryEntityList = categoryResult.getData();
                    map.put("categoryList",categoryEntityList);
                }
                //skus
                Result<List<SkuDTO>> skusResult = goodsFeign.getSkuBySpuId(spuInfo.getId());
                if(skusResult.getCode() == 200){
                    List<SkuDTO> skuList = skusResult.getData();
                    map.put("skus",skuList);
                }
                //特有参数
                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setCid(spuInfo.getCid3());
                specParamDTO.setGeneric(false);

                Result<List<SpecParamEntity>> specParamInfoResult = specificationFeign.getSpecParamInfo(specParamDTO);

                if( specParamInfoResult.getCode() == 200) {
                    List<SpecParamEntity> specParamList = specParamInfoResult.getData();

                    Map<Integer, String> specParamMap = new HashMap<>();
                    specParamList.stream().forEach(param -> {
                        specParamMap.put(param.getId(),param.getName());
                    });
                    map.put("specParamMap",specParamMap);
                }
                //spuDetail
                Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBydSpu(spuInfo.getId());
                if(spuDetailResult.getCode() == 200){
                    SpuDetailEntity spuDetailList = spuDetailResult.getData();
                    map.put("spuDetailList",spuDetailList);
                }
                //规格
                SpecGroupDTO specGroupDTO = new SpecGroupDTO();
                specGroupDTO.setCid(spuInfo.getCid3());
                Result<List<SpecGroupEntity>> sepcGroupInfoResult = specificationFeign.getSepcGroupInfo(specGroupDTO);

                if (sepcGroupInfoResult.getCode() == 200) {
                    List<SpecGroupEntity> specGroupEntityList = sepcGroupInfoResult.getData();

                    List<SpecGroupDTO> specGroupDTOList = specGroupEntityList.stream().map(specGroupEntity -> {

                        SpecGroupDTO specGroup = BaiduBeanUtil.copyProperties(specGroupEntity, SpecGroupDTO.class);
                        SpecParamDTO paramDTO = new SpecParamDTO();

                        paramDTO.setGroupId(specGroup.getId());
                        paramDTO.setGeneric(true);
                        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParamInfo(paramDTO);

                        if(specParamResult.getCode() == 200){
                            specGroup.setParamList(specParamResult.getData());
                        }
                        return specGroup;
                    }).collect(Collectors.toList());
                    map.put("specGroupDTOList",specGroupDTOList);
                }
            }
        }
        return map;
    }
}
