package com.mmall.util;

import java.math.BigDecimal;

public class BigDecimalUtil {

    private BigDecimalUtil(){

    }

    //加
    public static BigDecimal add(Double v1 ,Double v2)
    {
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.add(b2);
    }
    //减
    public static BigDecimal sub(Double v1 ,Double v2)
    {
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.subtract(b2);
    }

    //乘
    public static BigDecimal mul(Double v1 ,Double v2)
    {
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.multiply(b2);
    }

    //除,需要保留两位小数点，并且四舍五入
    public static BigDecimal div(Double v1 ,Double v2)
    {
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);
    }
}
