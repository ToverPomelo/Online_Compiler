<%--
  Created by IntelliJ IDEA.
  User: tover
  Date: 19-2-24
  Time: 下午4:48
  Description: Test files upload.(Abandoned)
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>upload_test</title>
</head>
<body>
    <script>
        var files = {
            event:"test",
            data: [
                {
                    path: "/doc/file1.txt",
                    content: "this is file one!",
                    source: ""
                }
            ]
        };

        var filesJSON = JSON.stringify(files);
        console.log(filesJSON);

        var upload = function(){
            var xmlhttp;
            if (window.XMLHttpRequest)
            {// code for IE7+, Firefox, Chrome, Opera, Safari
                xmlhttp=new XMLHttpRequest();
            }
            else
            {// code for IE6, IE5
                xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
            }
            xmlhttp.onreadystatechange=function()
            {
                if (xmlhttp.readyState==4 && xmlhttp.status==200)
                {
                    alert('xml-http请求失败-0101');
                }
            }

            //xmlhttp.open("POST","http://localhost:8080/SCNU/uploadCode",true);
            xmlhttp.open("POST","uploadFiles",true);
            xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
            xmlhttp.send("files="+filesJSON);

            xmlhttp.onreadystatechange = function(){
                if(xmlhttp.readyState == 4){
                    if(xmlhttp.status == 200){
                        console.log(xmlhttp.responseText);
                        alert(xmlhttp.responseText+"-testing");
                    } else {
                        console.log(xmlhttp.status);
                    }
                }
            }
        }

        //upload();

        var getFiles = function(){
            var xmlhttp;
            if (window.XMLHttpRequest)
            {// code for IE7+, Firefox, Chrome, Opera, Safari
                xmlhttp=new XMLHttpRequest();
            }
            else
            {// code for IE6, IE5
                xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
            }
            xmlhttp.onreadystatechange=function()
            {
                if (xmlhttp.readyState==4 && xmlhttp.status==200)
                {
                    alert('xml-http请求失败-0101');
                }
            }

            //xmlhttp.open("POST","http://localhost:8080/SCNU/uploadCode",true);
            xmlhttp.open("POST","getFiles",true);
            xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
            xmlhttp.send("");

            xmlhttp.onreadystatechange = function(){
                if(xmlhttp.readyState == 4){
                    if(xmlhttp.status == 200){
                        console.log(xmlhttp.responseText);
                        alert(xmlhttp.responseText+"-testing-2");
                    } else {
                        console.log(xmlhttp.status);
                    }
                }
            }
        }

        getFiles();
    </script>
</body>
</html>
