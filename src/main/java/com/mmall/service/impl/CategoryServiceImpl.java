package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.collections.CollectionUtils;
import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse<String> addGategory(Integer parentId , String categoryName)
    {
        //判断父节点和品类名是否为空
        if (parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("参数不正确");
        }
        //封装品类
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(categoryName);
        category.setStatus(true);//说明品类是可用的
        //调用
        int resultCount = categoryMapper.insert(category);
        if (resultCount > 0)
        {
            return ServerResponse.createBySuccessMessage("品类添加成功");
        }
        return ServerResponse.createByErrorMessage("品类添加失败");
    }


    public ServerResponse<String> setGategoryName(Integer categoryId , String categoryName)
    {
        //判断父节点和品类名是否为空
        if (categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("参数不正确");
        }
        //封装品类
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        //调用
        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (resultCount > 0)
        {
            return ServerResponse.createBySuccessMessage("品类修改成功");
        }
        return ServerResponse.createByErrorMessage("品类修改失败");
    }

    public ServerResponse<List<Category>> getChildrenParallelGategory(Integer categoryId)
    {
        //判断父节点是否为空
        if (categoryId == null){
            return ServerResponse.createByErrorMessage("参数不正确");
        }
        List<Category> categoryList = categoryMapper.selectChildrenParallelCategoryByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    public ServerResponse<List<Integer>> getCategoryIdAndChildrenCategoryId(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildrenCategoryId(categorySet ,categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null){
            for(Category categoryItem : categorySet){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    public Set<Category> findChildrenCategoryId(Set<Category> categorySet , Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null){
            categorySet.add(category);
        }

        //查找子节点,递归算法一定要有一个退出的条件
        List<Category> categoryList = categoryMapper.selectChildrenParallelCategoryByParentId(categoryId);
        for(Category categoryItem : categoryList)
        {
            findChildrenCategoryId(categorySet ,categoryItem.getId());
        }
        return categorySet;
    }
}
