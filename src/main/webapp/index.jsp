<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2019/8/6
  Time: 7:36
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<body>
<h2>Hello World!</h2>
<form name="form1" action="/mmall_war/manage/product/upload.do" method="post"  enctype="multipart/form-data">
    <input type="file" name="upload_file">
    <input type="submit" value="上传文件"/>
</form>

<form name="form2" action="/mmall_war/manage/product/upload.do" method="post"  enctype="multipart/form-data">
    <input type="file" name="upload_file">
    <input type="submit" value="上传富文本"/>
</form>
</body>
</html>
