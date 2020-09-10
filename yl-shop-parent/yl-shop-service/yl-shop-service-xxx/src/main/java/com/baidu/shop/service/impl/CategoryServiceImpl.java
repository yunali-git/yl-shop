package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/8/27
 * @Version V1.0
 **/
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SpecGroupMapper specGroupMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpuMapper spuMapper;


    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {
        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    @Transactional //回滚 必须加
    @Override
    public Result<JSONObject> addCategory(CategoryEntity categoryEntity) {

        //通过新增节点的父级id将父级节点的parent状态改为1
        CategoryEntity parentEntity = new CategoryEntity();

        parentEntity.setId(categoryEntity.getParentId());
        parentEntity.setIsParent(1);
        categoryMapper.updateByPrimaryKeySelective(parentEntity);

        categoryMapper.insertSelective(categoryEntity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> editCategory(CategoryEntity categoryEntity) {
        categoryMapper.updateByPrimaryKeySelective(categoryEntity);
        return this.setResultSuccess();

    }

    @Transactional
    @Override
    public Result<JSONObject> deleteCategory(Integer id) {
        //验证获取的id是否有效
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        if (categoryEntity == null) {
            return this.setResultError("当前id不存在");
        }

        //构建条件查询 根据 被删除的parentId查询数据
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());
        List<CategoryEntity> list = categoryMapper.selectByExample(example);

        //分类绑定商品
        Example example3 = new Example(SpuEntity.class);
        example3.createCriteria().andEqualTo("cid3",id);
        List<SpuEntity> list3 = spuMapper.selectByExample(example3);
        if(list3.size() > 0) return this.setResultError("此分类已被商品绑定,不可删除");

        //分类绑定规格
        Example example1 = new Example(SpecGroupEntity.class);
        example1.createCriteria().andEqualTo("cid",id);
        List<SpecGroupEntity> list1 = specGroupMapper.selectByExample(example1);
        if(list1.size() >0) return this.setResultError("此分类已被规格绑定,不可删除");

        //分类绑定品牌
        Example example2 = new Example(CategoryBrandEntity.class);
        example2.createCriteria().andEqualTo("categoryId",id);
        List<CategoryBrandEntity> list2 = categoryBrandMapper.selectByExample(example2);
        if(list2.size() > 0) return this.setResultError("此分类已被品牌绑定,不可删除");

        //如果查询结果为一条 就将isParent状态改为0
        if(list.size() == 1){
            CategoryEntity parentEntity = new CategoryEntity();
            parentEntity.setId(categoryEntity.getParentId());
            parentEntity.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(parentEntity);
        }

        categoryMapper.deleteByPrimaryKey(id);//删除
        return this.setResultSuccess();
    }

    @Override
    public Result<List<CategoryEntity>> getBrand(Integer brandId) {
            List<CategoryEntity> byBrandId = categoryMapper.getBrandId(brandId);

            return this.setResultSuccess(byBrandId);
    }
}
