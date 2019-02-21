/*    */
var gdb_init = function(gdbHandler){
    var gdb = {
        content_main: "",  //过滤后的gdb文本
        content_var: [],   //变量信息
        content_sta: [],   //运行栈信息

        mode: 0, //中间变量，当前状态 0->main ; 1->var ; 2->sta ;
        bt_sj: 0, //中间变量

        var_result: [],  //存放变量结果
        sta_result: [],  //存放运行栈结果

        breakHandler: gdbHandler,  //gdb命令运行一次后执行的操作

        addMessage: function(text){
            this.content_main += text; //+ "\n";
        },

        //获取变量信息
        getVar: function(){
            return this.var_result;
        },

        //获取运行栈信息
        getStack: function(){
            return this.sta_result;
        },

        //提取变量信息
        get_variable_details: function(data){
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
        },

        //提取运行栈信息
        get_frame_details: function(frame){
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
        },

        //把与变量信息有关的文本以行位单位分开处理，再合并
        var_handle: function(){
            //content_var = content_var.replace(/\r\n/g,'</br>');
            //variableBox.innerHTML = content_var;
            var lines = this.content_var;
            //var html_code = "";
            //variableBox.innerHTML = "";  //处理变量的html信息的历史遗留问题
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

                var_info = this.get_variable_details(var_info_line);
                if(var_info){
                    //处理变量的html信息的坑
                    //console.log("variable: ");
                    //console.log(var_info);
                    this.var_result.push(var_info);  //存放处理后的结果
                    //variableBox.innerHTML += '<tr><td>'+var_info.name+'</td><td>'+encodeHtmlEntity(var_info.val)+'</td></tr>';
                }
            }
            //variableBox.parentNode.scrollTop = variableBox.scrollHeight;
            this.content_var = [];
        },

        //把与运行栈信息有关的文本以行位单位分开处理，再合并
        sta_handle: function(){
            var lines = this.content_sta;

            //var html_code = "";
            //stackBox.innerHTML = "";  //处理html的历史遗留
            var no_of_frames=0;
            for(var i=0;i<lines.length; i++){
                //if(lines[i]==data.cmd) continue;
                //if(lines[i]=="(gdb) " && i==lines.length-1) continue;
                if(lines[i]=="No stack.") continue;

                var frame_line = "";
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

                frame = this.get_frame_details(frame_line);
                no_of_frames++;
                //console.log("stack(frame):");
                //console.log(frame);
                if(frame){ this.sta_result.push(frame); }
                //this.gui_call_stack_add_frame(frame);  //处理放置stack的html信息
            }
            //gui_call_stack_post_processing(no_of_frames);
            //stackBox.parentNode.scrollTop = stackBox.scrollHeight;
            this.content_sta = [];
        },

        //主处理器，把编码好的文本根据信息类型分配处理
        msg_handle: function(content){
            if(/@sl@/.test(content)){
                this.mode = 1;
                //console.log('mode = '+this.mode);
                return;
            }else if(/@el_sbt@/.test(content)){
                this.mode = 2;
                //console.log('mode = '+this.mode);
                this.var_handle();
                var s_bt = this.bt_sj;
                setTimeout(function(){
                    if(s_bt == this.bt_sj){
                        sock.send("shell");  //强行退出
                        alert('运行栈过大，请检测是否存在递归的死循环！程序即将退出！');
                    }
                },5000);  //妥协的处理方法？
                return;
            }else if(/@ebt_sib@/.test(content)){
                this.bt_sj++;
                this.mode = 0;
                //console.log('mode = '+this.mode);
                this.sta_handle();
                return;
            }else if(/\(gdb\)/.test(content)){
                //console.log('gdb');
                this.addMessage('(gdb)');
                this.breakHandler();
                this.mode = 0;
                this.content_var = [];
                this.content_sta = [];
                this.var_result = [];
                this.sta_result = [];
            }else{
                switch(this.mode){
                    case 0:
                        this.addMessage(content);
                        break;
                    case 1:
                        this.content_var.push(content);
                        break;
                    case 2:
                        this.content_sta.push(content);
                        break;
                }
            }
        },

        //把编码好的gdb文本输入，过滤筛选出变量信息和运行栈信息
        gdbFilter: function(msg){  //测试
            //console.log("msg: " + msg);
            this.content_main = "";

            var data = msg.split(/(\n)/g);
            for(var i=0;i<data.length;i++){
                var content = data[i];

                if(/Connection to localhost closed./.test(content)) content = 'Closed.\r\n';
                if(/Error while running hook_stop:/.test(content)) return;
                if(/No frame selected./.test(content)) return;

                this.msg_handle(content);
            }

            //this.content_main = msg; // test
            //console.log("c_main: " + this.content_main);
            return this.content_main;
        }
    };
    return gdb;
}