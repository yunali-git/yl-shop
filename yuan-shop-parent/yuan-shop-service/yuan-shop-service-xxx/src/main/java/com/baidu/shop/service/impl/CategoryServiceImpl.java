package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/8/28
 * @Version V1.0
 **/
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {
        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JSONObject> save(CategoryEntity categoryEntity) {
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
    public Result<JSONObject> edit(CategoryEntity categoryEntity) {
        categoryMapper.updateByPrimaryKeySelective(categoryEntity);
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> delete(Integer id) {
        //验证获取的id是否有效
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        if (categoryEntity == null) {
            return this.setResultError("当前id不存在");
        }

        //判断当前id是否是父节点
//        if(categoryEntity.getIsParent() == 1){
//            return this.setResultError("当前数据为父节点,不可以删除");
//        }

        //构建条件查询 根据 被删除的parentId查询数据
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());
        List<CategoryEntity> list = categoryMapper.selectByExample(example);

        //如果查询结果为一条 就将isParent状态改为0
        if(list.size() == 1){
            CategoryEntity parentEntity = new CategoryEntity();
            parentEntity.setId(categoryEntity.getParentId());
            parentEntity.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(parentEntity);
        }

        categoryMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }
}
