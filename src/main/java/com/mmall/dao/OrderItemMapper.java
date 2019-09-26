package com.mmall.dao;

import com.mmall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> selectByOrderNoAndUserId(@Param(value = "orderNo") Long orderNo ,@Param(value="userId") Integer userId);

    void batchInsertOrderItemList(@Param(value="orderItemList") List<OrderItem> orderItemList);

    List<OrderItem> selectByOrderNo(@Param(value = "orderNo") Long orderNo);
}