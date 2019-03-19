/*
    Description: Handler for docker connection.
*/

package com.smart.web;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.*;

public class MyHandler extends AbstractWebSocketHandler {
    private Process process;
    private Runtime rt = Runtime.getRuntime(); //获得Runtime对象
    private String cmd = "/bin/bash";

    @Override
    //接收到信息后的处理器
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        System.out.println("message: " + message.getPayload());

        if(message.getPayload().equals("shell")){  //运行一个shell的docker
            closeDocker();
            cmd = "/bin/bash";
        }
        else if(message.getPayload().equals("gdb")){ cmd = "gdb"; }  //测试
        else if(message.getPayload().equals("compile")){  //编译代码
            Process pr = rt.exec("docker exec -i test2 g++ /home/test/code.cpp -o /home/test/a.out");

            //SequenceInputStream是一个串联流，能够把两个流结合起来，通过该对象就可以将
            //getInputStream方法和getErrorStream方法获取到的流一起进行查看了，当然也可以单独操作
            SequenceInputStream sis = new SequenceInputStream(pr.getInputStream(), pr.getErrorStream());
            InputStreamReader inst = new InputStreamReader(sis, "utf-8");//设置编码格式并转换为输入流
            BufferedReader br = new BufferedReader(inst);//输入流缓冲区

            String res = null;
            StringBuilder sb = new StringBuilder();
            while ((res = br.readLine()) != null) {//循环读取缓冲区中的数据
                sb.append(res+"\n");
            }
            br.close();

            pr.waitFor();
            pr.destroy();

            session.sendMessage(new TextMessage(sb));
            return;
        }
        else if(message.getPayload().equals("debug")){  //运行gdb的docker
            System.out.println(
                    rt.exec("docker exec -i test2 g++ -g /home/test/code.cpp -o /home/test/debug").waitFor()
            );
            closeDocker();
            cmd = "gdb /home/test/debug";
        }

        System.out.println("creating");
        int run;
        //容器正在stop的话要等待stop完成后再run
        while((run=rt.exec("docker run --security-opt seccomp=unconfined --name test2 -d -it -m 256m -v /home/tover/Programs/SCNU/docker/users/29:/home/test compiler:alpha "+cmd).waitFor()) != 0){
            Thread.sleep(2000);
        }
        System.out.println(run);
        System.out.println("created");
        session.sendMessage(new TextMessage("ok"));  //返回创建完成的信息
    }

    @Override
    //socket链接后的处理器
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        System.out.println("connected");
    }

    //关闭docker容器
    private void closeDocker() throws Exception{
        System.out.println("closing");
        System.out.println(
                rt.exec("docker stop test2").waitFor()
        );
        System.out.println(
                rt.exec("docker rm test2").waitFor()
        );
        System.out.println("closed");
    }

    @Override
    //关闭socket后的处理器
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
        closeDocker();
    }

}
