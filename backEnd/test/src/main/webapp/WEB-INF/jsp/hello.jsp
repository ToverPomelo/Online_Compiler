<%--
  Created by IntelliJ IDEA.
  User: tover
  Date: 19-1-16
  Time: 上午11:03
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>hello</title>
</head>
<body>
    this is a shell!
    <input id="input"/>

    <script type="text/javascript" charset="UTF-8">
        var url = 'ws://' + window.location.host + '<%=request.getContextPath()%>/hello';
        var sock = new WebSocket(url);

        sock.onopen = function(){
            console.log("opened");
            //sayHello();
        }

        sock.onmessage = function(e){
            console.log("get message: " + e.data);
            //setTimeout(function () {sayHello();},2000);
        }

        sock.onclose = function(){
            console.log("closed");
        }
/*
        var sayHello = function(){
            console.log("say Hello!");
            sock.send("hello world!");
        }
*/

        var input = document.getElementById("input");
        input.onchange = function(){
            console.log(input.value);
            sock.send(input.value);
        }
    </script>
</body>
</html>
