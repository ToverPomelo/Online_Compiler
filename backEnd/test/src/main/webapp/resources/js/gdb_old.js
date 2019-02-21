var gdb_init = function(html_elements){
    var logArea = html_elements.logs || undefined;
    /********************************* var&&bt ********************************************/

    function addMessage(text){
        term.write(text);
    }

    function get_variable_details(data){
        var ret_var_info = {};
        //get variable name = part of info
        //console.log(data);
        try {
            var var_name = /^[_A-Za-z][_A-Za-z0-9]* = /.exec(data)[0];
            //console.log(data.split(/^[_A-Za-z][_A-Za-z0-9]* = /));
            ret_var_info.name = var_name.split("=")[0].trim();
            ret_var_info.val = data.split(/^[_A-Za-z][_A-Za-z0-9]* = /)[1];//'value';
        }
        catch (e) {
            ret_var_info = null;
        }
        return ret_var_info;
    }

    var encodeHtmlEntity = function(str) {  //处理放置html的
        var buf = [];
        for (var i=str.length-1;i>=0;i--) {
            buf.unshift(['&#', str[i].charCodeAt(), ';'].join(''));
        }
        return buf.join('');
    };

    function get_frame_details(frame){
        var ret_frame = {};
        try{
            var tokens = frame.split(" ");

            var token, tn=0;
            //Frame Number: Regex = '#[0-9]'
            token = tokens[tn++];
            if(token.substr(0,1)!="#"){
                return null;
            }else {
                ret_frame.frame_no = token.replace("#","");
            }
            //Function Address: Regex = '0x[0-9]*' (optional Token)
            token = tokens[tn++];
            if(token=="") token = tokens[tn++];
            if(token.substr(0,2)=='0x'){
                ret_frame.fun_addr = token;
                tn++; // Skip next token as well, which will be 'in'
                token = tokens[tn++];
            }

            //Function Name: Regex = '[A-Za-z_]'
            ret_frame.fun_name = token;

            //Function argumets:
            ret_frame.fun_args = "";
            do {
                token = tokens[tn++];
                ret_frame.fun_args += token;
            } while(!token.endsWith(")"));

            tn++; // skip next token, which is 'at'
            if(tn >= tokens.length) {
                //report_error("unexpected token");
                ret_frame.file_name = "";
                ret_frame.line = "";
            }else {
                token = tokens[tn++];
                //Filename:lineno
                var file_line = token.split(":");
                ret_frame.file_name = file_line[0];
                ret_frame.line = file_line[1];
            }
            //console.log(ret_frame);
            return ret_frame;
        } catch(e) {
            //report_error(e);
        }
        return null;
    }

    function gui_call_stack_add_frame(frame){  //处理放置html的?
        if(!frame) return;

        stackBox.innerHTML +='<tr data-file="'+encodeHtmlEntity(frame.file_name)+'" data-line="'+frame.line+
        '" data-frame-num="'+frame.frame_no+'" onclick="ide.set_frame('+frame.frame_no+')"><td>'
        +frame.frame_no+'</td><td>'+encodeHtmlEntity(frame.fun_name)+'</td><td>'+encodeHtmlEntity(frame.file_name)+':'
        +frame.line+'</td></tr>';
    }

    var content_list = [];
    var content_main=[],content_var=[],content_sta=[];
    var mode = 0; //0->main ; 1->var ; 2->sta ;

    var var_handle = function(){
        //content_var = content_var.replace(/\r\n/g,'</br>');
        //variableBox.innerHTML = content_var;
        let lines = content_var;
        var html_code = "";
        variableBox.innerHTML = "";
        for(var i=0;i<lines.length; i++){
            //if(lines[i]=="(gdb) " && i==lines.length-1) continue;
            if(lines[i]=="---Type  to continue, or q  to quit---") continue;
            if(lines[i]=="No frame selected.") continue;
            if(lines[i]=="No locals.") continue;

            var var_info_line = "";
            var var_info = null;
            if(/^[_A-Za-z][_A-Za-z0-9]* = /.test(lines[i])){
                var_info_line = lines[i];
                while(i<lines.length-2 && /^\s+/.test(lines[i+1])){
                    var append_line = lines[++i];
                    append_line = append_line.replace(/^\s+/,' ');
                    var_info_line += append_line;
                }
                var_info_line = var_info_line.replace(/\r?\n|\r/g,'');
            }

            var_info = get_variable_details(var_info_line);
            if(var_info){
                variableBox.innerHTML += '<tr><td>'+var_info.name+'</td><td>'+encodeHtmlEntity(var_info.val)+'</td></tr>';
            }
        }
        //variableBox.parentNode.scrollTop = variableBox.scrollHeight;
        content_var = [];
    }

    var sta_handle = function(){
        var lines = content_sta;

        var html_code = "";
        stackBox.innerHTML = "";
        var no_of_frames=0;
        for(var i=0;i<lines.length; i++){
            //if(lines[i]==data.cmd) continue;
            //if(lines[i]=="(gdb) " && i==lines.length-1) continue;
            if(lines[i]=="No stack.") continue;

            frame_line = "";
            var frame = null;
            if(/^\#[0-9]+/.test(lines[i])){
                frame_line = lines[i];
                while(i<lines.length-2 &&  !/^\#[0-9]+/.test(lines[i+1])){
                    var append_line = lines[++i];
                    if(frame_line.endsWith('('))
                    append_line = append_line.trim();
                    append_line = append_line.replace(/^\s+at/,' at');
                    frame_line += append_line;
                }
                frame_line = frame_line.replace(/\r?\n|\r/g,'');
            }

            frame = get_frame_details(frame_line);
            no_of_frames++;
            gui_call_stack_add_frame(frame);
        }
        //gui_call_stack_post_processing(no_of_frames);
        //stackBox.parentNode.scrollTop = stackBox.scrollHeight;
        content_sta = [];
    }

    var bt_sj = 0;
    var msg_handle = function(content){
        if(/@sl@/.test(content)){
            mode = 1;
            //console.log('mode = '+mode);
            return;
        }else if(/@el_sbt@/.test(content)){
            mode = 2;
            //console.log('mode = '+mode);
            var_handle();
            let s_bt = bt_sj;
            setTimeout(function(){
                if(s_bt == bt_sj){
                    ws.send(JSON.stringify({type:'kill',data:'Huge stack!'}));
                    //stackBox.innerHTML = '运行栈过大，请检测是否存在递归的死循环！<br\> 程序即将退出！';
                    //if(logArea) logArea.innerHTML = '运行栈过大，请检测是否存在递归的死循环！程序即将退出！';
                    //else
                    alert('运行栈过大，请检测是否存在递归的死循环！程序即将退出！');
                    //term.clear();
                }
            },5000);
            return;
        }else if(/@ebt_sib@/.test(content)){
            bt_sj++;
            mode = 0;
            //console.log('mode = '+mode);
            sta_handle();
            return;
        }else if(/\(gdb\)/.test(content)){
            //console.log('gdb');
            //content_main.push(content);
            addMessage('(gdb)');
            mode = 0;
            content_main = []
            content_var = [];
            content_sta = [];
            //msg_finish();
        }else{
            switch(mode){
                case 0:
                //content_main.push(content);
                addMessage(content);
                break;
                case 1:
                content_var.push(content);
                break;
                case 2:
                content_sta.push(content);
                break;
            }
        }
    }

    /********************************* websocket ********************************************/
    //var variableBox = document.getElementById('variableBox');
    //var stackBox = document.getElementById('stackBox');
    //var input = document.getElementById('contentBox');
    //var editor = document.getElementById("editor");
    //var send = document.getElementById("gdb_compiler");
    if(html_elements.variableBox) var variableBox = html_elements.variableBox;
    if(html_elements.stackBox) var stackBox = html_elements.stackBox;
    if(html_elements.send) var send = html_elements.send;
    var start_gdb = function(){
        var i = 0;
        var gdb_on = false;
        ws = new WebSocket('ws://localhost:40510');
        //var ws = new WebSocket('ws://10.242.55.8:40510');

        ws.onmessage = function(msg){
            //console.log(msg);
            switch(JSON.parse(msg.data).type){
                case 'gdb_main':
                    //console.log("main??");
                    if(!gdb_on) break;
                    let data = JSON.parse(msg.data).data.split(/(\n)/g);
                    for(i=0;i<data.length;i++){
                        let content = data[i];

                        if(/Connection to localhost closed./.test(content)) content = 'Closed.\r\n';
                        if(/Error while running hook_stop:/.test(content)) return;
                        if(/No frame selected./.test(content)) return;

                        ///if(/000/.test(content)) console.log(content);
                        content_list.push(content);
                        if(gdb_on) msg_handle(content);  //step:1 -> entry
                    }
                    break;
            }
        };
}
