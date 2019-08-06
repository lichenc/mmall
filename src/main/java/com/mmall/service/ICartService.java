package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.vo.CartVo;

public interface ICartService {
    ServerResponse<CartVo> add(User user, Integer productId , Integer count);

    ServerResponse<CartVo> update(User user, Integer productId , Integer count);

    ServerResponse<CartVo> deleteProduct(User user, String productIds);
}
