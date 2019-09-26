package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {
    //查询订单列表接口

    @Autowired
    IOrderService iOrderService;

    @RequestMapping(value="list.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse list(HttpSession session , @RequestParam(value = "pageNum" ,defaultValue = "1") int pageNum ,
                                       @RequestParam(value = "pageSize" ,defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登陆");
        }
        //判断用户是否管理员
        if (user.getRole().equals(Const.Role.ROLE_ADMIN)){
            //调用方法添加节点
            return iOrderService.manageOrderList(pageNum ,pageSize);
        }
        return ServerResponse.createByErrorMessage("用户不是管理员，无法操作");
    }

    @RequestMapping(value="search.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse search(HttpSession session ,Long orderNo , @RequestParam(value = "pageNum" ,defaultValue = "1") int pageNum ,
                               @RequestParam(value = "pageSize" ,defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登陆");
        }
        //判断用户是否管理员
        if (user.getRole().equals(Const.Role.ROLE_ADMIN)){
            //调用方法添加节点
            return iOrderService.manageOrderSearch(orderNo ,pageNum ,pageSize);
        }
        return ServerResponse.createByErrorMessage("用户不是管理员，无法操作");
    }

    @RequestMapping(value="detail.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse orderDetail(HttpSession session ,Long orderNo ){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登陆");
        }
        //判断用户是否管理员
        if (user.getRole().equals(Const.Role.ROLE_ADMIN)){
            //调用方法添加节点
            return iOrderService.manageOrderDetail(orderNo);
        }
        return ServerResponse.createByErrorMessage("用户不是管理员，无法操作");
    }

    @RequestMapping(value="send_goods.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse sendGood(HttpSession session ,Long orderNo ){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登陆");
        }
        //判断用户是否管理员
        if (user.getRole().equals(Const.Role.ROLE_ADMIN)){
            //调用方法添加节点
            return iOrderService.manageSendGood(orderNo);
        }
        return ServerResponse.createByErrorMessage("用户不是管理员，无法操作");
    }
}
