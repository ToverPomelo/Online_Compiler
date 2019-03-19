<%--
  Created by IntelliJ IDEA.
  User: tover
  Date: 19-3-6
  Time: 下午8:38
  Description: To test the file system.
               Send JSON message(event:get|getHash|touch|mkdir|cp|mv...) to chang the files in backend.
               Get JSON message when files in backend changed(from frontend or not from frontend).
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>File</title>
</head>
<body>
    <div>event:<br/><input id="event"/></div>
    <div>path:<br/><input id="path"/></div>
    <div>source:<br/><input id="source"/></div>
    <div>content:<br/><textarea id="content"></textarea></div>
    <div><button onclick="takeAction();">submit</button></div>

    <script charset="utf-8" src="/js/core.js"></script>
    <script charset="utf-8" src="/js/sha256-min.js"></script>
    <script>
        var test = function(data){
            if(JSON.parse(data).event != "getRespond") return;
            var content = JSON.parse(data).result;
            console.log(content);
            var content = JSON.parse(content)[0].content;
            console.log(content);
            var sHA256 = CryptoJS.SHA256(content).toString(CryptoJS.enc.Hex);
            console.log("sHA256 = %s", sHA256);
        }

        var takeAction = function(){
            var event = document.getElementById("event").value;
            var path = document.getElementById("path").value;
            var source = document.getElementById("source").value;
            var content = document.getElementById("content").value;

            var action = {
                event: event,
                data: {
                    path: path,
                    content: content,
                    source: source
                }
            };
            sock.send(JSON.stringify(action));
        }

        /*  socket (Spring)  */
        var url = 'ws://' + window.location.host + '<%=request.getContextPath()%>/file';
        console.log(url)
        var sock = new WebSocket(url);

        sock.onopen = function(){
            console.log("opened");
        }

        sock.onmessage = function(e){
            console.log("get message: " + e.data);
            //test(e.data);
        }

        sock.onclose = function(){
            console.log("closed");
        }
    </script>
</body>
</html>
