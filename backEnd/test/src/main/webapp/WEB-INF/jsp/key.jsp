<%--
  Created by IntelliJ IDEA.
  User: tover
  Date: 19-1-20
  Time: 下午10:33
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>key</title>
</head>
<body>
<div id="mainBox" class="page-center">
    <div id="header">身份认证</div>
    <form class="shadow-20" method="post" id="form" onsubmit="return showResult()">
        <div id="inputs">
            <div style="padding: 0.5vw">用户名</div>
            <div><input class="shadow-2" type="text" id="name"/></div>
            <div style="padding: 0.5vw">密码</div>
            <div><input class="shadow-2" type="password" id="pass"/></div>
        </div>
        <button id="submit" type="submit">登录</button>
    </form>
</div>

<script charset="utf-8" src="/js/core.js"></script>
<script charset="utf-8" src="/js/enc-base64.js"></script>
<script charset="utf-8" src="/js/jsencrypt.js"></script>
<script>
    var showResult = function()
    {
        var name = document.getElementById("name").value;
        var pass = document.getElementById("pass").value;
        if (name=="")
        {
            document.getElementById("txtHint").innerHTML="";
            return false;
        }
        if (window.XMLHttpRequest)
        {
            // IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码
            xmlhttp=new XMLHttpRequest();
        }
        else
        {
            // IE6, IE5 浏览器执行代码
            xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
        }
        xmlhttp.onreadystatechange=function()
        {
            if (xmlhttp.readyState==4 && xmlhttp.status==200)
            {
                //document.getElementById("txtHint").innerHTML=xmlhttp.responseText;
                var res = xmlhttp.responseText;
                console.log(res);
                //if(/-----BEGIN PUBLIC KEY-----/g.test(res) && /-----END PUBLIC KEY-----/g.test(res)) {
                if(res.length == 588){
                    var encrypt = new JSEncrypt();
                    encrypt.setPublicKey(res);
                    var encrypted = encrypt.encrypt(pass);
                    console.log(encrypted);
                    //var wordArray = CryptoJS.enc.Utf8.parse(encrypted);
                    //var base64 = CryptoJS.enc.Base64.stringify(wordArray);
                    //console.log(base64);

                    xmlhttp.open("POST","dealEnc",true);
                    xmlhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                    xmlhttp.send("p="+encrypted);

                }
                /*if(/no_user/g.test(res))
                    alert("用户不存在！");
                //document.getElementById("txtHint").innerHTML="用户不存在！";
                else if(/wrong_password/g.test(res))
                    alert("密码错误！");
                //document.getElementById("txtHint").innerHTML="密码错误！";
                else if(/correct/g.test(res)){
                    res = res.replace(/correct/g,"");
                    var parsedWordArray = CryptoJS.enc.Base64.parse(res);
                    var parsedStr = parsedWordArray.toString(CryptoJS.enc.Utf8);
                    //document.getElementsByTagName("body")[0].innerHTML = '';
                    document.write(parsedStr);
                }*/
            }
        }
        //xmlhttp.open("GET","login.php?method=ask&n="+name+"&p="+pass,true);
        xmlhttp.open("POST","getKey",true);
        xmlhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        xmlhttp.send("method=ask");
        return false;
    };
</script>
</body>
</html>
