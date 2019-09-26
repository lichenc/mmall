package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order/")
public class OrderController {

    private static Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    IOrderService iOrderService;

    @RequestMapping(value = "create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session,Integer shippingId){
        //判断用户是否登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //调用service层
        return iOrderService.createOrder(user.getId() ,shippingId);
    }

    //取消订单接口
    @RequestMapping(value = "cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session,Long orderNo){
        //判断用户是否登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //调用service层
        return iOrderService.cancelOrder(user.getId() ,orderNo);
    }

    //获取订单购物商品信息接口
    @RequestMapping(value = "get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session){
        //判断用户是否登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //调用service层
        return iOrderService.getOrderCartProduct(user.getId());
    }

    //获取订单详情接口
    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServerResponse detail(HttpSession session ,Long orderNo){
        //判断用户是否登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //调用service层
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    //获取订单列表接口
    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session , @RequestParam(value = "pageNum",defaultValue = "1") int pageNum ,@RequestParam(value = "pageSize",defaultValue = "10")  int pageSize){
        //判断用户是否登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //调用service层
        return iOrderService.getOrderList(user.getId(),pageNum ,pageSize);
    }

    //支付接口
    @RequestMapping(value = "pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session,Long orderNo, HttpServletRequest request){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createBySuccess(0);
        }
        //TODO path的值是什么形式？？是url？
        String path = request.getSession().getServletContext().getRealPath("upload");
        return  iOrderService.pay(user.getId() ,orderNo ,path);
    }

    @RequestMapping(value = "alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
       //接收request里面的值
        Map<String,String> params = Maps.newHashMap();
        //回调函数的值都放在request的Map里面,需要从request中获取
        Map requstParams = request.getParameterMap();
        //通过迭代器从requstParams中将key和value取出
        //通过迭代器，可以将所有的key和value都取出来放到另外一个Map
        for(Iterator iter = requstParams.keySet().iterator();iter.hasNext();)
        {
            //?? 为什么iter.next()就可以获取到名称
            String name = (String)iter.next();
            //通过key获取到值
            String[] values = (String[]) requstParams.get(name);
            String valueStr = "";
            //通过循环获取到值中的数组
            for(int i = 0;i < values.length;i++){
                //下面的组装是为了将数组中的值用逗号隔开的方式连接起来，
                // 如果i == values.length -1 的时候说明是最后一个值，则不需要连接逗号
                valueStr = (i == values.length -1)?valueStr + values[i]:valueStr + values[i] + ",";
            }
            params.put(name,valueStr);
        }
        logger.info("支付宝回调，sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());

        //接下来该干嘛呢？
        //在支付宝异步通知的文档中写到，需要对传过来的参数延签
        //在通知返回参数列表中，除去sign、sign_type 两个参数外，凡是通知返回回来的参数皆是待验签的参数。
        //将剩下参数进行 url_decode, 然后进行字典排序，组成字符串，得到待签名字符串
        //这里为什么只remove掉 sign_type 这个key呢？进入resaCheckV2方法里面getSignCheckContentV2方法可以看到，sign 这个key在方法中已经remove
        params.remove("sign_type");
        try {
            boolean signVerified = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"UTF-8","RSA2");
            if(!signVerified)
            {
                return ServerResponse.createByErrorMessage("非支付宝请求，再恶意请求将报警！！！");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝延签失败，",e);
        }

        //验完签之后该如何进行下一步呢？
        //需要更新订单状态和支付信息
        ServerResponse serverResponse = iOrderService.alipayCallBack(params);
        if(serverResponse.isSuccess())
        {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    @RequestMapping(value = "query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session ,Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //轮询订单状态
        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId() ,orderNo);
        if(serverResponse.isSuccess())
        {
            return ServerResponse.createBySuccess(true);
        }
        return  ServerResponse.createBySuccess(false);
    }
}
