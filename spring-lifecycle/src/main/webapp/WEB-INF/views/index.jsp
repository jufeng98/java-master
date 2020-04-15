<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>hello world</title>
</head>
<body>
<h1>欢迎来到spring的世界</h1>
<!-- html注释:设置三个变量 -->
<%
    request.setAttribute("desc", "作者:梁煜东");
    request.setAttribute("code","02003");
    request.setAttribute("jsonStr","{\"name\":\"梁煜东\"}");
%>
<%-- jsp注释:三种方式打印变量 --%>
<p><c:out value="${desc}"/></p>
<p><%=request.getAttribute("desc")%></p>
<p>${requestScope.desc}</p>
</body>
<script type="text/javascript">
    console.log(${code}, "${code}", ${jsonStr}, ${jsonStr}.name);
</script>
</html>