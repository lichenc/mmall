package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePayRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPayResult;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;

import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderPruductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service("iOrderService")
@Slf4j
public class OrderServiceImpl implements IOrderService {

   // public static Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    PayInfoMapper payInfoMapper;

    @Autowired
    CartMapper cartMapper;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    ShippingMapper shippingMapper;

    public ServerResponse createOrder(Integer userId ,Integer shippingId){
        //根据用户标识查询所有被选中的购物车
        List<Cart> cartList = cartMapper.selectCartCheckedByUserId(userId);
        //判断查询到的购物车列表是否为空
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //如果购物车不为空，则计算所选商品总价
        ServerResponse serverResponse = this.getOrderItemList(userId ,cartList);
        if(!serverResponse.isSuccess()){
            return  serverResponse;
        }
        //如果成功，将返回值赋给订单详情列表,需要做强制转换
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        //计算商品总价
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);
        //生成订单
        Order order = this.assembleOrder(userId ,shippingId ,payment);
        if(order == null){
            return ServerResponse.createByErrorMessage("生成订单错误");
        }
        //判断订单明细列表是否为空，为空则返回购物车为空
        if(orderItemList == null){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //以上判断都完了，那么就要更新一下订单详情的订单号
        for(OrderItem orderItem : orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        //完了得把订单详情信息更新到表中，这里就需要批量写入
        orderItemMapper.batchInsertOrderItemList(orderItemList);

        //减少商品库存
        this.reduceProductCount(orderItemList);
        //清理购物车
        this.cleanCart(cartList);
        //下面就要返回数据了，要组装OrderVo类
        OrderVo orderVo = this.assemberOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    private Order assembleOrder(Integer userId ,Integer shippingId ,BigDecimal payment){
        Long orderNo =  this.generateOrderNo();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payment);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        //order.setSendTime();
        //order.setPaymentTime();
        int rowCount = orderMapper.insert(order);
        if(rowCount > 0){
            return order;
        }
        return null;
    }

    private OrderVo assemberOrderVo(Order order , List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setSendtime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        if(order.getShippingId() != null){
            Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingId(order.getShippingId());
            orderVo.setShippingVo(this.assemberShippingVo(shipping));
        }
        orderVo.setOrderItemVoList(this.assemberOrderItemList(orderItemList));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return orderVo;
    }

    private List<OrderItemVo> assemberOrderItemList(List<OrderItem> orderItemList){
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setOrderNo(orderItem.getOrderNo());
            orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
            orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
            orderItemVo.setTotalPrice(orderItem.getTotalPrice());
            orderItemVo.setProductId(orderItem.getProductId());
            orderItemVo.setProductName(orderItem.getProductName());
            orderItemVo.setProductImage(orderItem.getProductImage());
            orderItemVo.setQuantity(orderItem.getQuantity());
            orderItemVoList.add(orderItemVo);
        }
        return orderItemVoList;
    }
    private ShippingVo assemberShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setCreateTime(shipping.getCreateTime());
        return  shippingVo;
    }
    //清理购物车
    private void cleanCart(List<Cart> cartList){
        for (Cart cart : cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }
    //减少商品库存
    private void reduceProductCount(List<OrderItem> orderItemList){
        for(OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKey(product);
        }
    }
    //生成订单号
    private Long generateOrderNo(){
        Long currentTime = System.currentTimeMillis();
        //以下返回值如果是并发量大的时候就会出现订单号一样的情况
        //return currentTime + currentTime % 9;
        return currentTime + new Random().nextInt(100);
    }
    private BigDecimal getOrderTotalPrice( List<OrderItem> orderItemList ){
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    //获取购物车中商品详情
    private ServerResponse getOrderItemList(Integer userId ,List<Cart> cartList){
        List<OrderItem> orderItemList = Lists.newArrayList();
        //判断参数是否为空
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //遍历购物车列表
        for(Cart cartItem : cartList)
        {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            //判断商品是否是在售状态，如果不是在售则返回商品不是在线售卖状态
            if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode())
            {
                return ServerResponse.createByErrorMessage("商品已经下架");
            }
            // 校验库存，判断如果购物车中商品数量大于库存，则返回库存不足提示
            if(cartItem.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMessage("商品库存不足");
            }
            //组装订单详情
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(cartItem.getQuantity().doubleValue(),product.getPrice().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }
    //取消订单接口
    public  ServerResponse cancelOrder(Integer userId ,Long orderNo){
        //根据用户标识和订单号查询订单是否存在，如果不存在则返回订单不存在。
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo,userId);
        if(order == null){
            return ServerResponse.createByErrorMessage("用户订单不存在");
        }
        //判断订单状态是否为已经支付，如果已经支付则返回已经支付，无法取消
        if(order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()){
            return ServerResponse.createByErrorMessage("订单已经支付，无法取消");
        }
        //组装订单对象
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int rowCount = orderMapper.updateByPrimaryKey(updateOrder);
        if(rowCount > 0){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    //获取订单购物车商品
    public ServerResponse getOrderCartProduct(Integer userId){
        //通过用户标识获取已经选中的购物车信息
        List<Cart> cartList = cartMapper.selectCartCheckedByUserId(userId);

        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }

        //不为空则用通过用户标识和购物车列表作为参数查询
        ServerResponse serverResponse = this.getOrderItemList(userId,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        //
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();

        //实例化List<OrderItemVo>
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        //定义订单商品Vo
        OrderPruductVo orderPruductVo = new OrderPruductVo();
        //计算总价
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);
        //for循环将订单详情信息复制给订单Vo
        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = this.assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderPruductVo.setOrderItemVoList(orderItemVoList);
        orderPruductVo.setTotalPrice(payment);
        orderPruductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderPruductVo);
    }

    //获取订单详情
    public ServerResponse<OrderVo> getOrderDetail(Integer userId ,Long orderNo){
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo ,userId);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(orderNo,userId);
            OrderVo orderVo = assemberOrderVo(order ,orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return  ServerResponse.createByErrorMessage("订单不存在");
    }

    //获取订单列表
    public ServerResponse<PageInfo> getOrderList(Integer userId , int pageNum , int pageSize){
        PageHelper.startPage(pageNum ,pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList ,userId);
        //最终是要把orderVoList这个对象放到分页信息中
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    //组装List<OrderVo>
    private List<OrderVo> assembleOrderVoList(List<Order> orderList ,Integer userId){
        List<OrderVo> orderVoList =  Lists.newArrayList();
        for(Order order : orderList){
            OrderVo orderVo = new OrderVo();
            List<OrderItem> orderItemList = Lists.newArrayList();
            if(userId == null){
                //如果是管理员来查询，则不需要传用户标识
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.selectByOrderNoAndUserId(order.getOrderNo(),userId);
            }
            orderVo = this.assemberOrderVo(order ,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    //支付接口
    public ServerResponse pay(Integer userId ,Long orderNo ,String path){
        //判断订单是否存在
        Map resultmap = Maps.newHashMap();
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo ,userId);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        resultmap.put("orderNo",String.valueOf(order.getOrderNo()));

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = new StringBuilder().append("happymmall商城门店当面付订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

      // (可选，根据需要决定是否使用) 订单可打折金额，可以配合商家平台配置折扣活动，如果订单部分商品参与打折，可以将部分商品总价填写至此字段，默认全部商品可打折
        // 如果该值未传入,但传入了【订单总金额】,【不可打折金额】 则该值默认为【订单总金额】- 【不可打折金额】
        //        String discountableAmount = "1.00"; //

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0.0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品3件共20.00元"
        String body = new StringBuilder().append("购买商品").append(outTradeNo).append(order.getPayment()).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        String providerId = "2088100200300400500";
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId(providerId);

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        //循环获取订单明细
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(orderNo,userId);
        for(OrderItem orderItem : orderItemList)
        {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods1 = GoodsDetail.newInstance(
                    orderItem.getProductId().toString(),
                    orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double("100").doubleValue()).longValue(),
                    orderItem.getQuantity());

            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods1);
        }
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                //TODO 需要修改配置文件中的配置
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);
                //在服务器上创建文件夹
                File folder = new File(path);
                if(!folder.exists())
                {
                    folder.setWritable(true);//设置可写权限
                    folder.mkdirs();//创建目录
                }
                // 需要修改为运行机器上的路径
                String qrPath = String.format(path + "/qr-%s.png" ,response.getOutTradeNo());
                String qrFileName = String.format("/qr-%s.png" ,response.getOutTradeNo());
                //生成二维码
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                //目标文件，根据parent 为path ,child 为 qrFileName创建实例
                File targetFile = new File(path ,qrFileName);
                //使用FTP工具上传到FTP服务器
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("文件上传失败：",e);
                }
                log.info("qrPath:" + qrPath);
                //??TODO 去理解为什么要获取qrUrl,qrUrl的值为什么是这样的形式
                // targetFile.getName() 获取到的是文件名称及图片名称
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultmap.put("qrUrl",qrUrl);
                return ServerResponse.createBySuccess(resultmap);
            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }
    //回调方法
    public ServerResponse alipayCallBack(Map<String ,String> params){
        //获取订单号，
        //如果不清楚可以到支付宝的异步通知中查询
        //https://docs.open.alipay.com/194/103296/
        Long orderNo =Long.valueOf(params.get("out_trade_no"));
        //获取交易号
        String tradeNo = params.get("trade_no");
        //获取交易状态
        String tradeStatus = params.get("trade_status");
        //根据订单号查询订单是否存在
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null)
        {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        //查看订单支付状态
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode())
        {
            return ServerResponse.createByErrorMessage("订单已经支付，不能重复支付");
        }
        //查看交易状态是否成功
        if(tradeStatus.equals(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS))
        {
            //将订单的支付时间更新,交易付款时间字段是gmt_payment
            //字段名称请在 开发文档/当面付/异步通知 中查找
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            //将订单状态更新
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKey(order);
        }
        //往支付信息表中插入一条数据
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(orderNo);
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);
        //返回状态
        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId ,Long orderNo){
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo ,userId);
        if(order == null)
        {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }


    //backend funciton
    public ServerResponse<PageInfo> manageOrderList(int pageNum ,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAll();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList ,null);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    //根据订单号查询订单信息，这里做分页主要是为了以后扩展当查询条件是其他属性，比如手机、用户名
    public ServerResponse<PageInfo> manageOrderSearch(Long orderNo ,int pageNum ,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
            OrderVo orderVo = this.assemberOrderVo(order ,orderItemList);
            PageInfo pageResult = new PageInfo(Lists.newArrayList(order));
            pageResult.setList(Lists.newArrayList(orderVo));
            return ServerResponse.createBySuccess(pageResult);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    //根据订单号发货
    public ServerResponse<OrderVo> manageOrderDetail(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
            OrderVo orderVo = this.assemberOrderVo(order ,orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    //根据订单号发货
    public ServerResponse<String> manageSendGood(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                int rowCount = orderMapper.updateByPrimaryKey(order);
                if(rowCount > 0){
                    return ServerResponse.createBySuccess();
                }
                return ServerResponse.createByError();
            }
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }
}
