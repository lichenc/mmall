package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.service.IFileService;
import com.mmall.util.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    //创建或者保存产品信息
    @Autowired
    IProductService iProductService;

    @Autowired
    IUserService iUserService;

    @Autowired
    IFileService iFileService;

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

    @RequestMapping(value="set_sale_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setProductStatus(HttpSession session,@RequestParam(value="productId") Integer productId,@RequestParam(value="status")Integer status)
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
            return iProductService.setSaleStatus(productId ,status);
        }
        return ServerResponse.createByErrorMessage("无操作权限");
    }

    @RequestMapping(value="detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId)
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
           return iProductService.manageProductDetail(productId );
        }
        return ServerResponse.createByErrorMessage("无操作权限");
    }

    @RequestMapping(value="list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value="pageNum", defaultValue = "1") Integer pageNum,@RequestParam(value="pageSize",defaultValue = "10") Integer pageSize)
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
            return iProductService.getProductList(pageNum , pageSize);
        }
        return ServerResponse.createByErrorMessage("无操作权限");
    }

    @RequestMapping(value="search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse searchProduct(HttpSession session,@RequestParam(value="productName",required = false) String productName,@RequestParam(value="productId",required = false) Integer productId, @RequestParam(value="pageNum", defaultValue = "1") Integer pageNum,@RequestParam(value="pageSize",defaultValue = "10") Integer pageSize)
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
            return iProductService.searchProduct(productName,productId,pageNum , pageSize);
        }
        return ServerResponse.createByErrorMessage("无操作权限");
    }

    @RequestMapping(value="upload.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(HttpSession session ,@RequestParam(value="upload_file",required=false) MultipartFile file, HttpServletRequest request)
    {
        //判断用户是否已经登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        //判读用户是否是管理员权限
        if(user.getRole().equals(Const.Role.ROLE_ADMIN))
        {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);
        }
        return ServerResponse.createByErrorMessage("无操作权限");
    }

    @RequestMapping(value="richtext_img_upload.do",method = RequestMethod.POST)
    @ResponseBody
    public Map richtextImgUpload(HttpSession session , @RequestParam(value="upload_file",required=false) MultipartFile file, HttpServletRequest request, HttpServletResponse response)
    {
        //判断用户是否已经登录
        //返回值根据simditor的报文要求
        /*{
            "success": true/false,
                "msg": "error message", # optional
            "file_path": "[real file path]"
        }*/
        Map resultMap = Maps.newHashMap();
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            resultMap.put("success",false);
            resultMap.put("msg","用户未登录，无法操作");
            return resultMap;
        }
        //判读用户是否是管理员权限
        if(user.getRole().equals(Const.Role.ROLE_ADMIN))
        {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","富文本上传成功");
            resultMap.put("file_path",url);
            //与前端的约定，富文本的时候需要加上这个头
            response.setHeader("Access-Control-Allow-Headlers","X-File-Name");
            return resultMap;
        }
        resultMap.put("success",false);
        resultMap.put("msg","用户没有管理员权限，无法操作");
        return resultMap;
    }

}
