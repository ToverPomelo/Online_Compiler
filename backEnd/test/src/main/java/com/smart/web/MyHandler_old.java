/*
package com.smart.web;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.*;

public class MyHandler_old extends AbstractWebSocketHandler {
    private Process process;
    private Runtime rt = Runtime.getRuntime(); //获得Runtime对象
    private BufferedWriter bout;
    private BufferedReader br;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        System.out.println("message: " + message.getPayload());

        Thread.sleep(1000);
        //System.out.println("send: Hello World!");

        //String arr[] = {"CLASSPATH=D://","Path=C:\\Program Files\\Java\\jdk1.8.0_131\\bin"};//执行exec时的环境变量

        //exec方法第一个参数是执行的命令，第二个参数是环境变量，第三个参数是工作目录
        //Process pr = rt.exec("cmd /c javac a.java && java a", arr, new File("D://"));
        ////process = rt.exec("docker exec -i test bash");
        //process = rt.exec("docker attach test");
        //process = rt.exec("/bin/bash");

        //获取输出流并转换成缓冲区
        bout = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        bout.write(message.getPayload());//输出数据
        bout.close();//关闭流
        //bout.flush();

        //SequenceInputStream是一个串联流，能够把两个流结合起来，通过该对象就可以将
        //getInputStream方法和getErrorStream方法获取到的流一起进行查看了，当然也可以单独操作
        SequenceInputStream sis = new SequenceInputStream(process.getInputStream(), process.getErrorStream());
        InputStreamReader inst = new InputStreamReader(sis, "utf-8");//设置编码格式并转换为输入流
        BufferedReader br = new BufferedReader(inst);//输入流缓冲区

        String res = null;
        StringBuilder sb = new StringBuilder();
        while ((res = br.readLine()) != null) {//循环读取缓冲区中的数据
            sb.append(res+"\n");
        }
        br.close();

        ////process.waitFor();
        ////process.destroy();

        System.out.print(sb);//输出获取的数据

        //session.sendMessage(new TextMessage("Hello World!"));
        session.sendMessage(new TextMessage(sb));
        //session.sendMessage(new TextMessage(result));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        process = rt.exec("docker exec -i test bash");
        //process.waitFor();
        System.out.println("connected!");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
        //if(process != null) process.destroy();
        process.waitFor();
        process.destroy();

        System.out.println("closed");
    }

}
*/