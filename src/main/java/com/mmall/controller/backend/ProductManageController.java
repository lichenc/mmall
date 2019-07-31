package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    //创建或者保存产品信息
    @Autowired
    IProductService iProductService;

    @Autowired
    IUserService iUserService;


    @RequestMapping(value="save.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product)
    {
        //判断用户是否已经登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        //判读用户是否是管理员权限
        if(user.getRole().equals(Const.Role.ROLE_ADMIN))
        {
            //调用方法
            return iProductService.saveOrUpdateProcduct(product);
        }
        return ServerResponse.createByErrorMessage("无操作权限");
    }
}
