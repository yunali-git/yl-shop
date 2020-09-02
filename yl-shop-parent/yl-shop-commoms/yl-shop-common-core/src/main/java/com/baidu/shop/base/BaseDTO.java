package com.baidu.shop.base;

import com.baidu.shop.utils.StringUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName BaseDTO
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/8/31
 * @Version V1.0
 **/
@ApiModel(value = "品牌BaseDTO")
@Data
public class BaseDTO {

    @ApiModelProperty(value = "当前页",example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页多少条",example = "5")
    private Integer rows;

    @ApiModelProperty(value = "排序字段")
    private String sort;

    @ApiModelProperty(value = "是否降序")
    private Boolean desc;

    @ApiModelProperty(hidden = true)
    public String getOrderByClause(){
        if(StringUtil.isNotEmpty(sort)) return sort + " " + (desc?"desc":"");
        return null;
    }
}
