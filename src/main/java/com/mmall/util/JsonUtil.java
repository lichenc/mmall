package com.mmall.util;

import com.mmall.pojo.TestPojo;
import com.mmall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
public class JsonUtil {
    //先初始化
    private static ObjectMapper objectMapper = new ObjectMapper();
    //启动工程的时候JVM直接加载
    static {
        //对象的所有字段全部列入 ,Inclusion 是包含的意思
        //
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);
        //取消默认转换timeStamp形式 1970-1-1 到现在的毫秒数
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,false);
        //忽略空bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);
        //所有的日期格式都统一为以下格式，yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.DATE_FORMAT));
        //忽略在json字符串中存在，但是在java对象中不存在对应属性的情况，防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,true);
    }

    //将Object转换成String
    public static <T> String obj2String(T obj ){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse object to string error," , e);
            return null;
        }
    }

    //将Object转换成String Pretty
    public static <T> String obj2StringPretty(T obj ){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse object to string error," , e);
            return null;
        }
    }
    //将String转换成Object
    public static <T> T string2Obj(String str ,Class<T> clazz){
        if(StringUtils.isEmpty(str) || clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class)? (T) str : objectMapper.readValue(str,clazz);
        } catch (Exception e) {
            log.warn("Parse String to Object error," , e);
            return null;
        }
    }
    //复杂类型的转换
    public static <T> T string2Obj(String str , TypeReference<T> typeReference){
        //判断参数是否为空
        if(StringUtils.isEmpty(str) || typeReference == null){
            return null;
        }
        try {
            return (T)(typeReference.getType().equals(String.class)? str : objectMapper.readValue(str,typeReference));
        } catch (Exception e) {
            log.warn("Parse String to Object error," , e);
            return null;
        }
    }
    // T 和 ？的区别
    // 为什么用<T>接收？
    public static <T> T string2Obj(String str , Class<?> collectionClass ,Class<?>... elementClasses){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClasses);
        try {
            return objectMapper.readValue(str,javaType);
        } catch (Exception e) {
            log.warn("Parse String to Object error," , e);
            return null;
        }
    }

    public static void main(String[] args) throws Exception{
      /*  String file = "C:/AM/demo.dat";
        //序列化
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
        User user = new User();
        user.setId(11111);
        user.setUsername("lichenc");
        outputStream.writeObject(user);
        outputStream.flush();
        outputStream.close();

        //反序列化
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
        User user1 = (User) objectInputStream.readObject();
        System.out.println(user1);
        objectInputStream.close();*/

       /* User user = new User();
        user.setId(11111);
        user.setUsername("lichenc");
        user.setCreateTime(new Date());
        String userListStr = JsonUtil.obj2StringPretty(user);
        log.info(userListStr);*/
        TestPojo testPojo = new TestPojo();
        testPojo.setId(1111);
        testPojo.setName("lichenc");
        //{"name":"lichenc","id":1111}

        String testPojoStr = "{\"name\":\"lichenc\",\"id\":1111,\"color\":\"blue\"}";
        TestPojo testPojo1 = JsonUtil.string2Obj(testPojoStr ,TestPojo.class);
        //log.info("testPojoStr:" + testPojoStr);
        log.info("end");

        //List<User> userLists = JsonUtil.string2Obj(userListStr ,new TypeReference<List<User>>(){});

        //List<User> userLists1 = JsonUtil.string2Obj(userListStr ,List.class ,User.class);

        //String str1 = new String();
        //String str2 = "";
        //String str3 = null;
        String str4;
       /* if(str3.isEmpty()){
            System.out.println("str1 is empty");
        }*/
        /*if(str4 == ""){
            System.out.println("str1 等于 \"\"");
        }
        if(str4 == null){
            System.out.println("str1 等于 null");
        }*/
    }
}
