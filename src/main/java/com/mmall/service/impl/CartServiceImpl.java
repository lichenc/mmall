package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    CartMapper cartMapper;

    @Autowired
    ProductMapper productMapper;


    public ServerResponse<CartVo> list(Integer userId){
        CartVo cartVo = this.getCartVo(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> add(Integer userId, Integer productId , Integer count){
        if(productId == null || count == 0)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //查询购物车是否存在，不存在则新增，存在则更新
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        //判断cart是否为空
        if(cart == null)
        {
            //为空则表示不存在，那么就新增一个
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartMapper.insert(cartItem);
        }else{
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKey(cart);
        }
        return this.list(userId);
    }


    public ServerResponse<CartVo> update(Integer userId, Integer productId , Integer count){
        if(productId == null || count == 0)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //查询购物车是否存在，存在则更新
        Cart cart = cartMapper.selectCartByUserIdProductId(userId ,productId);
        //判断cart是否为空
        if(cart != null)
        {
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKey(cart);
        }
        return this.list(userId);
    }

    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds){
       /* if(StringUtils.isBlank(productIds))
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //对productIds进行截取
        String[] productIdList = productIds.split(",");
        //遍历删除
        for (String productId : productIdList)
        {
            cartMapper.deleteByUserIdProductIds(,Integer.valueOf(productId));
        }*/
        //上面是我的写法，使用循环删除，这样效率不高，并且截取的方式是用遍历
        //下面是geely老师的方法,以逗号分隔，并转换成list类型
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productIdList))
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //我觉得这里需要记下日志，判断是否删除成功
        cartMapper.deleteProductByUserIdAndProductIds(userId,productIdList);
        return this.list(userId);
    }

    public ServerResponse<CartVo> select(Integer userId, Integer productId){

        if(productId == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //根据产品标识查询购物车是否存在
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        //判断cart是否为空
        if(cart != null)
        {
            cart.setChecked(Const.Cart.CHECKED);
            cartMapper.updateByPrimaryKey(cart);
        }

        return this.list(userId);
    }

    //这里业务逻辑判断我的做法是：
    /*
      1、判断参数是否为空，为空则返回参数不正确
      2、将四个方法都分开，每个方法对应一个mapper
      3、在sql里面写定了参数 checked = '0' 或者 checked = '1'
      现在经过geely 老师的提示后，四个方法合并一起共用，这样的好处是减少代码的开发，提高
      代码的复用，高复用。根据参数的变化来得到不同的结果
     */
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId,Integer checked){
        cartMapper.checkedcOrUcCheckedProduct(userId ,productId ,checked);
        return this.list(userId);
    }


    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if(userId == null)
        {
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    private CartVo getCartVo(Integer userId)
    {
        CartVo cartVo = new CartVo();
        //根据userId查询所有的购物车
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        //使用BogDecimal解决精度问题
        BigDecimal tatolPrice = new BigDecimal("0.00");
        //循环获取购物车
        for(Cart cart : cartList)
        {
            CartProductVo cartProductVo = new CartProductVo();
            cartProductVo.setId(cart.getId());
            cartProductVo.setUserId(cart.getUserId());
            cartProductVo.setProductId(cart.getProductId());

            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (product != null){
                cartProductVo.setProductName(product.getName());
                cartProductVo.setProductSubtitle(product.getSubtitle());
                cartProductVo.setProductMainImage(product.getMainImage());
                cartProductVo.setProductPrice(product.getPrice());
                cartProductVo.setProductStatus(product.getStatus());
                cartProductVo.setProductStock(product.getStock());
                cartProductVo.setProductChecked(cart.getChecked());
                int buyLimitCount = 0;
                if(cart.getQuantity() <= product.getStock())
                {
                    cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    buyLimitCount = cart.getQuantity();
                }else{
                    cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                    buyLimitCount = product.getStock();
                    Cart cartForQuantity = new Cart();
                    cartForQuantity.setId(cart.getId());
                    cartForQuantity.setQuantity(buyLimitCount);
                    //?? 为什么这里不做判断是否成功
                    cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                }
                cartProductVo.setQuantity(buyLimitCount);
                cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity().doubleValue()));
            }
            //要先判断产品是不是已经被选上了
            if(cart.getChecked() == Const.Cart.CHECKED)
            {
                tatolPrice = BigDecimalUtil.add(tatolPrice.doubleValue() , cartProductVo.getProductTotalPrice().doubleValue());
            }
            cartProductVoList.add(cartProductVo);
        }

        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(tatolPrice);
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        return cartVo;
    }

    //判断商品是否选中
    private boolean getAllCheckedStatus(Integer userId)
    {
        if(userId == null){
            return  false;
        }
        return cartMapper.selectProductIsCheckedByUserId(userId) == 0;
    }

}
