package com.mmall.service.impl;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    ProductMapper productMapper;

    public ServerResponse saveOrUpdateProcduct(Product product){
        //首先判断传进来的商品类是否为空
        if (product != null)
        {
            //获取图片主图
            if(StringUtils.isNotBlank(product.getSubImages()))
            {
                //对主图进行分割
                String[] subImageArray = product.getSubImages().split(",");
                //将一个子图赋值给主图
                product.setMainImage(subImageArray[0]);
            }
            //判断产品的ID是否为空，如果不为空表示更新，为空则是新增
            if(product.getId() != null)
            {
                int rowCount = productMapper.updateByPrimaryKey(product);
                if(rowCount > 0)
                {
                    return ServerResponse.createBySuccess("更新产品成功");
                }
                return ServerResponse.createByErrorMessage("更新产品失败");
            }else{
                int rowCount = productMapper.insert(product);
                if(rowCount > 0)
                {
                    return ServerResponse.createBySuccess("新增产品成功");
                }
                return ServerResponse.createByErrorMessage("更新产品失败");

            }
        }
        return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }
}
