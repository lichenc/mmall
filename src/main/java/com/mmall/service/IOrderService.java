package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

public interface IOrderService {

    ServerResponse pay(Integer userId , Long orderNo , String path);

    ServerResponse alipayCallBack(Map<String ,String> params);

    ServerResponse queryOrderPayStatus(Integer userId ,Long orderNo);

    ServerResponse createOrder(Integer userId ,Integer shippingId);

    ServerResponse cancelOrder(Integer userId ,Long orderNo);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse<OrderVo> getOrderDetail(Integer userId , Long orderNo);

    ServerResponse<PageInfo> getOrderList(Integer userId , int pageNum , int pageSize);

    //backend
    ServerResponse<PageInfo> manageOrderList(int pageNum ,int pageSize);

    ServerResponse<PageInfo> manageOrderSearch(Long orderNo ,int pageNum ,int pageSize);

    ServerResponse<OrderVo> manageOrderDetail(Long orderNo);

    ServerResponse<String> manageSendGood(Long orderNo);
}
