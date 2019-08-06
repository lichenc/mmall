package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailsVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    ProductMapper productMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    ICategoryService iCategoryService;

    @Override
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
    @Override
    public ServerResponse setSaleStatus(Integer productId ,Integer status) {
        //判断参数是否为空
        if(productId == null || status == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //封装类
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount > 0)
        {
            return ServerResponse.createBySuccessMessage("产品状态更新成功");
        }
        return ServerResponse.createBySuccessMessage("产品状态更新失败");
    }

    //获取商品详情
    @Override
    public ServerResponse<ProductDetailsVo> manageProductDetail(Integer productId ) {
        //判断参数是否空
        if(productId == null)
        {
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null)
        {
            return ServerResponse.createByErrorMessage("产品已经下架或者删除");
        }
        //封装一个VO value object,专门为了给前台展示用的
        ProductDetailsVo productDetailsVo = assembleProductDetailsVo(product);
        return ServerResponse.createBySuccess(productDetailsVo);
    }

    public  ProductDetailsVo assembleProductDetailsVo(Product product)
    {
        ProductDetailsVo productDetailsVo = new ProductDetailsVo();
        productDetailsVo.setId(product.getId());
        productDetailsVo.setCategoryId(product.getCategoryId());
        productDetailsVo.setDetail(product.getDetail());
        productDetailsVo.setMainImage(product.getMainImage());
        productDetailsVo.setSubTitle(product.getSubtitle());
        productDetailsVo.setSubImage(product.getSubImages());
        productDetailsVo.setName(product.getName());
        productDetailsVo.setPrice(product.getPrice());
        productDetailsVo.setStock(product.getStock());
        productDetailsVo.setStatus(product.getStatus());

        //hostName 从配置文件来读取
        productDetailsVo.setHostName(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));

        //categoryParentId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null)
        {
            productDetailsVo.setCategoryParentId(0);//如果查询为空，默认为根节点
        }else{
            productDetailsVo.setCategoryParentId(category.getParentId());//
        }
        //createTime
        productDetailsVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        //updateTime
        productDetailsVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailsVo;
    }

    //获取商品详情
    @Override
    public ServerResponse<PageInfo> getProductList(int pageNum,int pageSize ) {
        ////startPage--start
        ////填充自己的sql查询逻辑
        ////pageHelper--收尾
        PageHelper.startPage(pageNum,pageSize);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        List<Product> productList = productMapper.selectList();

        for(Product productItem : productList)
        {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    public ProductListVo assembleProductListVo(Product product)
    {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setSubImage(product.getSubImages());
        productListVo.setSubTitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        productListVo.setHostName(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        return productListVo;
    }
    @Override
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId,int pageNum,int pageSize) {
        //
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName))
        {
            productName = new StringBuffer().append("%").append(productName).append("%").toString();
        }
        List<ProductListVo> productListVoList = Lists.newArrayList();
        List<Product> productList = productMapper.selectByNameAndProductId(productName,productId);

        for(Product productItem : productList)
        {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    //前台的查询详情
    @Override
    public ServerResponse<ProductDetailsVo> getProductDetail(Integer productId ) {
        //判断参数是否空
        if(productId == null)
        {
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null)
        {
            return ServerResponse.createByErrorMessage("产品已经下架或者删除");
        }
        //为什么？？？还要判断一次呢，后台查询需要查看所有状态的产品，当前前台
        // 展示只能显示在线的产品
        if (!product.getStatus().equals(Const.ProductStatusEnum.ON_SALE.getCode()))
        {
            return ServerResponse.createByErrorMessage("产品已经下架或者删除");
        }
        //封装一个VO value object,专门为了给前台展示用的
        ProductDetailsVo productDetailsVo = assembleProductDetailsVo(product);
        return ServerResponse.createBySuccess(productDetailsVo);
    }

    public ServerResponse<PageInfo> getProductKeywordCategory(String keyword ,Integer categoryId ,Integer pageNum,Integer pageSize,String orderBy ) {

        //判断参数是否为空,如果都为空则返回参数错误提示
        // 为什么字段串用isBlank判断是否为空，而categoryId用 == null???
        if(StringUtils.isBlank(keyword) && categoryId == null)
        {
            return  ServerResponse.createByErrorCodeMessage(
                    ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //产品类型ID集，如果传入的产品类型ID是比较大的类型，
        // 那么其下面会有很多子类，那么使用sql语句查询的时候，就可以用id in () 查询所有的产品
        List<Integer> categoryIdList = new ArrayList<Integer>();

        //判断产品类型标识不为空
        if(categoryId != null)
        {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            //如果类型为空且关键字也为空，返回一个空的结果集，不报错
            if (StringUtils.isBlank(keyword) && category == null){
                //分页
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            //category不为空，而查询类型及子类型所有的ID
            categoryIdList = iCategoryService.getCategoryIdAndChildrenCategoryId(category.getId()).getData();
        }

        //如果关键字不为空
        if (StringUtils.isNotBlank(keyword)){
            keyword = new StringBuffer().append("%").append(keyword).append("%").toString();
        }
        //分页
        PageHelper.startPage(pageNum,pageSize);
        //排序处理
        //判断order是否为空
        if(StringUtils.isNotBlank(orderBy))
        {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                //截取
                String[] orderByArray = orderBy.split("_");
                //排序
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }
        //根据关键字和类型标识集查询
        List<ProductListVo> productListVoList = Lists.newArrayList();
        List<Product> productList = productMapper.selectByNameAndCategoryIds(
                StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);
        //组装PageInfo信息
        PageHelper.startPage(pageNum,pageSize);
        for(Product product : productList)
        {
            productListVoList.add(assembleProductListVo(product));
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
