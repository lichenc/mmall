package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManageContoller {

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping(value="add_gategory.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addGategory(HttpSession session , Integer parentId , String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登陆");
        }
        //判断用户是否管理员
        if (user.getRole().equals(Const.Role.ROLE_ADMIN)){
            //调用方法添加节点
            return iCategoryService.addGategory(parentId,categoryName);
        }
        return ServerResponse.createByErrorMessage("用户不是管理员，无法操作");
    }

    @RequestMapping(value="set_category_name.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session ,Integer categoryId, String categoryName)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登陆");
        }
        //判断用户是否管理员
        if (user.getRole().equals(Const.Role.ROLE_ADMIN)){
            //调用方法添加节点
            return iCategoryService.setGategoryName(categoryId,categoryName);
        }
        return ServerResponse.createByErrorMessage("用户不是管理员，无法操作");
    }

    @RequestMapping(value="get_category.do" ,method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value="categoryId" ,defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登陆");
        }
        //判断用户是否管理员
        if (user.getRole().equals(Const.Role.ROLE_ADMIN)){
            //调用方法查询平级的品类
            return iCategoryService.getChildrenParallelGategory(categoryId);
        }
        return ServerResponse.createByErrorMessage("用户不是管理员，无法操作");
    }

    @RequestMapping(value="get_deep_category.do" ,method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getCategoryIdAndChildrenCategoryId(HttpSession session,@RequestParam(value="categoryId" ,defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登陆");
        }
        //判断用户是否管理员
        if (user.getRole().equals(Const.Role.ROLE_ADMIN)){
            //查询单前节点ID和递归子节点ID
            return iCategoryService.getCategoryIdAndChildrenCategoryId(categoryId);
        }
        return ServerResponse.createByErrorMessage("用户不是管理员，无法操作");
    }



}
