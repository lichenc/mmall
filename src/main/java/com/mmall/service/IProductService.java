package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailsVo;

public interface IProductService {

     ServerResponse saveOrUpdateProcduct(Product product);

     ServerResponse setSaleStatus(Integer productId ,Integer status);

     ServerResponse<ProductDetailsVo> manageProductDetail(Integer productId );

     ServerResponse<PageInfo> getProductList(int pageNum, int pageSize );

     ServerResponse<PageInfo> searchProduct(String productName, Integer productId,int pageNum,int pageSize);

     ServerResponse<ProductDetailsVo> getProductDetail(Integer productId );

     ServerResponse<PageInfo> getProductKeywordCategory(String keyword ,Integer categoryId ,Integer pageNum,Integer pageSize,String orderBy );
}
