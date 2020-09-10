package com.baidu.shop.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName SkuDTO
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/8
 * @Version V1.0
 **/
@ApiModel(value = "SKU属性数据传输类")
@Data
public class SkuDTO {
    @ApiModelProperty(value = "主键", example = "1")
    private Long id;

    @ApiModelProperty(value = "spu主键", example = "1")
    private Integer spuId;

    @ApiModelProperty(value = "商品标题")
    private String title;

    @ApiModelProperty(value = "商品的图片，多个图片以‘,’分割")
    private String images;

    @ApiModelProperty(value = "销售价格，单位为分", example = "1")
    private Integer price;

    @ApiModelProperty(value = "特有规格属性在spu属性模板中的对应下标组合")
    private String indexes;

    @ApiModelProperty(value = "sku的特有规格参数键值对，json格式，反序列化时请使用linkedHashMap，保证有序")
    private String ownSpec;

    //注意此处使用boolean值来接,在service中处理一下就可以了
    @ApiModelProperty(value = "是否有效，0无效，1有效", example = "1")
    private Boolean enable;

    @ApiModelProperty(value = "添加时间")
    private Date createTime;

    @ApiModelProperty(value = "最后修改时间")
    private Date lastUpdateTime;

    @ApiModelProperty(value = "库存")
    private Integer stock;
}
