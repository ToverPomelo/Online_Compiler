<%--
  Created by IntelliJ IDEA.
  User: tover
  Date: 19-2-16
  Time: 下午5:08
  Description: Online compiler.
               Used docker remote API to provide user terminal.
               Used websocket(different from the docker's) to contral.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>test-ws</title>
    <link rel="stylesheet" href="./css/xterm.css" />
</head>
<style>
    #variableBox tr{
        line-height: 1.42857143;
    }

    #variableBox td{
        border: 1px solid #ddd;
        width: 50%;
    }
    #stackBox tr{
        line-height: 1.42857143;
    }

    #stackBox td{
        border: 1px solid #ddd;
        width: 33%;
    }
</style>
<body>
    <h2>Client test</h2>
    <div id="debugButtons" style="display:none">
        调试工具：&emsp;
        <button id="l_gdb">列出(list)</button>
        <button id="b_gdb">断点(b main)</button>
        <button id="r_gdb">运行(run)</button>
        <button id="n_gdb">下一步(next)</button>
        <button id="s_gdb">进入函数(step)</button>
        <button id="f_gdb">跳出函数(finish)</button>
        <button id="c_gdb">继续(continue)</button>
        <button id="q_gdb">退出(quit)</button>
    </div>
    <div id="output"></div>
    <input id="input" /><button id="submit">submit</button>
    <br/>
    <button id="close">close</button>
    <button id="clear">clear</button>
    <br/><br/>
    <button id="upload">上传</button>
    <button id="compile">编译</button>
    <button id="run">运行</button>
    <button id="debug">调试</button>
    <br/><br/>
    <textarea id="code"></textarea>

    <!-- debugger infomation -->
    <table id="variableBox" style="background: #f8f7a3""></table>
    <table id="stackBox" style="background: #a3f7f8"></table>

</body>
<script src="./js/xterm.js"></script>
<script src="./js/gdb.js"></script>
<!--<script src="./js/initSocket.js"></script>-->
<script>
    /*  ws  */
    //on tover's the result is blob
    function blobToString(b) {
        var u, x;
        u = URL.createObjectURL(b);
        x = new XMLHttpRequest();
        x.open('GET', u, false); // although sync, you're not fetching over internet
        x.send();
        URL.revokeObjectURL(u);
        return x.responseText;
    }

    //initialize websocket connection and event handlers
    var input,output,submit,close,clear;
    var ws,term;

    output = document.getElementById("output");  //输出框（xterm）
    input = document.getElementById("input");  //输入框（打命令的）
    submit = document.getElementById("submit");  //执行命令
    close = document.getElementById("close");  //强行关闭（断开链接）
    clear = document.getElementById("clear");  //清除输出框内容

    var upload,compile,run,debug,code;

    upload = document.getElementById("upload");  //上传代码
    compile = document.getElementById("compile");  //编译
    run = document.getElementById("run");  //运行
    debug = document.getElementById("debug");  //调试
    code = document.getElementById("code");  //上传代码时写代码的地方

    var debugButtons = document.getElementById("debugButtons");  //调试时的按钮（代替命令）

    var termStatus = "shell"; //表明当前状态是在运行shell还是gdb的变量  shell || gdb || toGdb

    //xterm
    var term = new Terminal({
        cursorBlink: false,
        cols: 100,
        rows: 20
    });

    input.onchange = function(){
        sendMessage(input.value+"\n");
        input.value = "";
    };

    submit.onclick = function(){
        sendMessage(input.value+"\n");
        input.value = "";
    };

    //初始化并启动docker ws
    function setup() {
        //ws=new WebSocket("ws://120.79.95.86:2376/containers/test/attach/ws?stream=1&stdin=1&stdout=1");
        ws = new WebSocket("ws://localhost:2376/containers/test2/attach/ws?stream=1&stdin=1&stdout=1");

        //Listen for the connection open event then call the sendMessage function
        ws.onopen=function (e) {
            //log("connected-1 hello bi*ch!");
            //sendMessage("date \n");
            sendMessage("\n");
        }

        //Listen for the close connection event
        ws.onclose=function (e) {
            term.write("Disconnected");
            //sock.close();  //挖个坑
            console.log("status: " + termStatus);
            if(termStatus == "toGdb"){  //跳转到gdb
                termStatus = "gdb";
                debugButtons.style.display = "block";
            }
            else if(termStatus == "gdb"){  //关闭gdb后自动打开shell
                sock.send("shell");
                termStatus = "shell";
                debugButtons.style.display = "none";
            }
            //log("Disconnected:"+e.reason);
        }

        //Listen for connection errors
        ws.onerror=function (e) {
            console.log("error： "+e);
        }

        //Listen for new messages arriving at the client
        ws.onmessage=function (e) {
            //console.log(e);
            //log("Message received: "+e.data+"->>Listen for new messages arriving at the client");
            //log("Message received: "+blobToString(e.data)+"->>Listen for new messages arriving at the client");
            //某些版本的docker返回的是blob则需要用blobToString，某些是string则不需要
            console.log(blobToString(e.data));
            if(termStatus == "gdb"){term.write(gdb.gdbFilter(blobToString(e.data)));}  //gdb打开时用gdb过滤器对信息进行过滤
            else{ term.write(blobToString(e.data)); }  //否则直通
            //Close the socket once one message has arrived.
            //ws.close();
        }

        close.onclick = function(){
            ws.close();
        };

        clear.onclick = function(){
            //console.log(term);
            term.clear();
        }

        term.destroy();  //因为要重复打开所以要destroy
        term = new Terminal({
            cursorBlink: false,
            cols: 100,
            rows: 20
        });
        term.open(output);
        term.focus();
    }

    //send message on the websocket
    function sendMessage(msg) {
        ws.send(msg);
        //log("Message sent-3");
    }

    //start running the ws-test
    //setup();


    /*  socket (Spring)  */
    //spring的socket主要起监控和控制的作用
    var url = 'ws://' + window.location.host + '<%=request.getContextPath()%>/hello';
    var sock = new WebSocket(url);

    sock.onopen = function(){
        console.log("opened");
        //sock.send("test");
        sock.send("shell");  //发送shell后会打开一个shell
    }

    sock.onmessage = function(e){
        //console.log("get message: " + e.data);
        //setTimeout(function () {sayHello();},2000);
        if(compiling == true){  //编译错误等信息
            comErrMsg = e.data;
            //console.log(comErrMsg == "");
            if(comErrMsg == ""){ compileErr = 0; }
            else if(comErrMsg.match("error")){ compileErr = 1; alert("compile error: "+comErrMsg); }
            else if(comErrMsg.match("warning")){ compileErr = 2; alert("compile warning: "+comErrMsg); }
            compiling = false;
            compiled = true;
            return;
        }

        if(e.data.match("ok")){  //等后端容器打开后再进行ws链接（setup）
            console.log("ok")
            setTimeout(function(){setup();},10)
        }
    }

    sock.onclose = function(){
        console.log("closed");
    }

    /* functional button */
    //上传代码
    upload.onclick = function(){
        if(termStatus == "gdb"){
            alert("请先退出调试！");  //其实上传代码是不用的。。。
            return;
        }

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

        xmlhttp.open("POST","uploadCode",true);
        xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
        xmlhttp.send("code="+code.value);

        xmlhttp.onreadystatechange = function(){
            if(xmlhttp.readyState == 4){
                if(xmlhttp.status == 200){
                    console.log(xmlhttp.responseText);
                    //document.getElementById("output").value=xmlhttp.responseText;
                    //alert(xmlhttp.responseText+"-upload-Code.js");
                } else {
                    console.log(xmlhttp.status);
                }
            }
        }
    }

    //编译
    var compiled = false;
    var compileErr = -1; //挖个坑，记录是否编译错误，没错才运行？
    var compiling = false;
    var comErrMsg = "";
    compile.onclick = function(){
        if(termStatus == "gdb"){
            alert("请先退出调试！");
            return;
        }

        console.log("compile")
        sock.send("compile");
        compiling = true;
    }

    //运行
    run.onclick = function(){
        console.log("run");
        if(termStatus == "gdb"){
            alert("请先退出调试！");
            return;
        }
        if(compiled == false){alert("compile first!");}
        else{
            if(compileErr == 1){ alert("compiled error: "+comErrMsg); }
            else{ sendMessage("/home/test/a.out\n"); }
        }
    }

    //调试
    debug.onclick = function(){
        console.log("debug")
        if(termStatus == "gdb"){
            alert("正在调试中！");
            return;
        }

        sock.send("debug");
        termStatus = "toGdb";
        //setTimeout(function(){termStatus="gdb";},1000);
    }

    /* debug button */
    var l_gdb,b_gdb,r_gdb,n_gdb,s_gdb,f_gdb,c_gdb,q_gdb;
    l_gdb = document.getElementById("l_gdb");
    b_gdb = document.getElementById("b_gdb");
    r_gdb = document.getElementById("r_gdb");
    n_gdb = document.getElementById("n_gdb");
    s_gdb = document.getElementById("s_gdb");
    f_gdb = document.getElementById("f_gdb");
    c_gdb = document.getElementById("c_gdb");
    q_gdb = document.getElementById("q_gdb");

    l_gdb.onclick = function(){ if(termStatus == "gdb"){sendMessage("l\n");}}
    b_gdb.onclick = function(){ if(termStatus == "gdb"){sendMessage("b main\n");}}
    r_gdb.onclick = function(){ if(termStatus == "gdb"){sendMessage("r\n");}}
    n_gdb.onclick = function(){ if(termStatus == "gdb"){sendMessage("n\n");}}
    s_gdb.onclick = function(){ if(termStatus == "gdb"){sendMessage("s\n");}}
    f_gdb.onclick = function(){ if(termStatus == "gdb"){sendMessage("f\n");}}
    c_gdb.onclick = function(){ if(termStatus == "gdb"){sendMessage("c\n");}}
    q_gdb.onclick = function(){ if(termStatus == "gdb"){sendMessage("q\n");}}

    /*  gdb  */
    var variableBox,stackBox;
    variableBox = document.getElementById("variableBox");  //存放变量信息的表格
    stackBox = document.getElementById("stackBox");  //存放运行栈信息的表格

    //编码成html格式
    var encodeHtmlEntity = function(str) {
        var buf = [];
        for (var i=str.length-1;i>=0;i--) {
            buf.unshift(['&#', str[i].charCodeAt(), ';'].join(''));
        }
        return buf.join('');
    };

    //把变量信息转化为html并显示
    var showVariables = function(variables){
        variableBox.innerHTML = "";
        for(var i=0;i<variables.length;i++){
            var var_info = variables[i];
            //console.log(var_info);
            variableBox.innerHTML += '<tr><td>'+var_info.name+'</td><td>'+encodeHtmlEntity(var_info.val)+'</td></tr>';
        }
    }

    //把运行栈信息转化为html并显示
    var showStack = function(frames){
        stackBox.innerHTML = "";
        for(var i=0;i<frames.length;i++){
            var frame = frames[i];
            //console.log(frame);
            stackBox.innerHTML +='<tr data-file="'+encodeHtmlEntity(frame.file_name)+'" data-line="'+frame.line+
                '" data-frame-num="'+frame.frame_no+'"><td>'
                +frame.frame_no+'</td><td>'+encodeHtmlEntity(frame.fun_name)+'</td><td>'+encodeHtmlEntity(frame.file_name)+':'
                +frame.line+'</td></tr>';
        }
    }

    //gdb类
    var gdb = gdb_init(function(){
        console.log("variable: ");
        console.log(gdb.getVar());
        console.log("stack(frame):");
        console.log(gdb.getStack());
        showVariables(gdb.getVar());
        showStack(gdb.getStack());
    });


</script>
</html>
