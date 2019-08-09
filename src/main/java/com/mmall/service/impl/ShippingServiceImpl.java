package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    ShippingMapper shippingMapper;

    public ServerResponse<Map> add(Integer userId , Shipping shipping){
        if(shipping == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        shipping.setUserId(userId);
        int resultCode = shippingMapper.insert(shipping);
        if(resultCode > 0){
            //gava
            Map result = Maps.newHashMap();
            //想不通，为什么这里的ID是存在的？？
            //需要在mybatis里面添加返回值   <insert id="insert" parameterType="com.mmall.pojo.Shipping" useGeneratedKeys="true" keyColumn="id" >
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    public ServerResponse<String> del(Integer userId ,Integer shippingId){
        //为防止横向越权，需要在sql查询的时候加上userId
        int resultCode = shippingMapper.deleteByPrimaryKeyAndUserId(userId ,shippingId);
        if(resultCode > 0){
            //gava
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    public ServerResponse<String> update(Integer userId ,Shipping shipping){
        //为防止横向越权，需要在sql查询的时候加上userId
        shipping.setUserId(userId);//防止横向越权,第一次写的时候没有考虑到
        int resultCode = shippingMapper.updateByShipping(shipping);
        if(resultCode > 0){
            //gava
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse<Shipping> select(Integer userId , Integer shippingId){

        Shipping shipping= shippingMapper.selectByPrimaryKeyAndUserId(userId ,shippingId);
        if(shipping != null){
            //gava
            return ServerResponse.createBySuccess("查询地址详情成功",shipping);
        }
        return ServerResponse.createByErrorMessage("查询地址详情失败");
    }

    public ServerResponse<PageInfo> list(Integer userId ,Integer pageNum ,Integer pageSize){
        PageHelper.startPage(pageNum ,pageSize);
        List<Shipping> shippingList= shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }

}
