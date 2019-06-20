package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.List;

public interface ICategoryService {

    ServerResponse<String> addGategory(Integer parentId , String categoryName);

    ServerResponse<String> setGategoryName(Integer categoryId , String categoryName);

    ServerResponse getChildrenParallelGategory(Integer categoryId);

    ServerResponse<List<Integer>> getCategoryIdAndChildrenCategoryId(Integer categoryId);
}
