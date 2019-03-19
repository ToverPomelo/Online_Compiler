/*
    Description: Take actions on files in backend when recived messages from frontend.
*/

package com.smart.web;

import com.google.gson.Gson;
import com.smart.domain.*;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileHandler  extends AbstractWebSocketHandler {
    private Gson gson = new Gson();
    private final String testRootURL = "/home/tover/Programs/SCNU/testSocket/data/test";
    private final String chmod = "chmod -R 664 ";

    private FileAlterationMonitor monitor = new FileAlterationMonitor(1000L);// 文件监控，每隔1000毫秒扫描一次

    private String getUserURL(){
        return testRootURL;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

//get files' list
    //paths and names
    private List<String> getFilesOnServer(File file){
        List<String> filesPath = new ArrayList<String>();
        if(file.isFile()) {
            filesPath.add(file.toPath().toString());
            return filesPath;
        }
        File[] fs = file.listFiles();
        for(File f:fs){
            if(f.isDirectory())	//若是目录，则递归打印该目录下的文件
                filesPath.addAll(getFilesOnServer(f));
            if(f.isFile())		//若是文件，直接打印
                filesPath.add(f.getPath());
        }
        return filesPath;
    }

    private String getFiles(FileData data){
        List<FileData> result = new ArrayList<FileData>();
        List<String> filesPath = getFilesOnServer(new File(getUserURL() + data.getPath()));  //获取用户目录全部文件绝对路径

        for(String fn : filesPath){
            try{
                FileData f = new FileData();
                f.setPath(fn.substring(getUserURL().length()));

                //文件内容
                StringBuffer buffer = new StringBuffer();
                InputStream is = new FileInputStream(fn);
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                line = reader.readLine();
                while (line != null) {
                    buffer.append(line);
                    buffer.append("\n");
                    line = reader.readLine();
                }
                reader.close();
                is.close();
                f.setContent(buffer.toString());

                result.add(f);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return gson.toJson(result);
    }

    private String getFilesHash(FileData data){
        List<FileData> result = new ArrayList<FileData>();
        List<String> filesPath = getFilesOnServer(new File(getUserURL() + data.getPath()));  //获取用户目录全部文件绝对路径

        for(String fn : filesPath){
            try{
                FileData f = new FileData();
                f.setPath(fn.substring(getUserURL().length()));

                //文件内容
                StringBuffer buffer = new StringBuffer();
                InputStream is = new FileInputStream(fn);
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                line = reader.readLine();
                while (line != null) {
                    buffer.append(line);
                    buffer.append("\n");
                    line = reader.readLine();
                }
                reader.close();
                is.close();

                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                byte[] b_contentSHA = sha.digest(buffer.toString().getBytes("UTF-8"));
                String contentSHA = bytesToHex(b_contentSHA);
                f.setHash(contentSHA);

                 result.add(f);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return gson.toJson(result);
    }

//file operations
    private String touchFile(FileData data){
        final String fileName = getUserURL() + data.getPath();
        try{
            File file = new File(fileName);

            if (!file.getParentFile().exists()){
                if(!file.getParentFile().mkdirs()) {
                    return "mkdir_failed";  //创建上层目录失败
                }
            }

            if(!file.exists()){
                if(!file.createNewFile()){
                    return "touch_failed";  //创建文件失败
                }
            }
            else{  //文件已存在
                return "existed";
                //比较hash
            }
            Runtime.getRuntime().exec(chmod+fileName,null).waitFor();
        }catch(Exception e){
            e.printStackTrace();
            return "failed";
        }

        return "success";
    }

    private String writeFile(FileData data){
        final String fileName = getUserURL() + data.getPath();

        try{
            File file = new File(fileName);

            if(!file.exists()){
                if(!touchFile(data).equals("success")){
                    return "touch_failed";
                }
            }
            //默认覆写？
            //else{  //文件已存在
            //    return "existed";
            //}
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(data.getContent());
            out.flush();
        }catch(Exception e){
            e.printStackTrace();
            return "failed";
        }

        return "success";
    }

    private String mkdirFile(FileData data){
        final String fileName = getUserURL() + data.getPath();
        try{
            File file = new File(fileName);

            if(!file.exists()){
                if(!file.mkdirs()) {
                    return "mkdir_failed";  //创建上层目录失败
                }
            }
            else{  //文件已存在
                return "existed";
                //比较hash
            }
            Runtime.getRuntime().exec(chmod+fileName,null).waitFor();
        }catch(Exception e){
            e.printStackTrace();
            return "failed";
        }

        return "success";
    }

    private String rmFile(FileData data){
        final String fileName = getUserURL() + data.getPath();
        try{
            File file = new File(fileName);

            if(file.exists()){
                file.delete();
            }
            else{  //文件不存在
                return "not_existed";
            }
            Runtime.getRuntime().exec(chmod+fileName,null).waitFor();
        }catch(Exception e){
            e.printStackTrace();
            return "failed";
        }

        return "success";
    }

    private String cpFile(FileData data){
        final String fileName = getUserURL() + data.getPath();
        final String fileSource = getUserURL() + data.getSource();
        try{
            File fileTo = new File(fileName);
            File fileFrom = new File(fileSource);

            if(fileFrom.exists()){
                if(fileTo.exists()){
                    return "dest_existed";
                }
                Files.copy(fileFrom.toPath(),fileTo.toPath());
            }
            else{  //文件不存在
                return "source_not_exist";
            }
            Runtime.getRuntime().exec(chmod+fileName,null).waitFor();
        }catch(Exception e){
            e.printStackTrace();
            return "failed";
        }

        return "success";
    }

    private String mvFile(FileData data){
        final String fileName = getUserURL() + data.getPath();
        final String fileSource = getUserURL() + data.getSource();
        try{
            File fileTo = new File(fileName);
            File fileFrom = new File(fileSource);

            if(fileFrom.exists()){
                if(fileTo.exists()){
                    return "dest_existed";
                }
                Files.move(fileFrom.toPath(),fileTo.toPath());
            }
            else{  //文件不存在
                return "source_not_exist";
            }
            Runtime.getRuntime().exec(chmod+fileName,null).waitFor();
        }catch(Exception e){
            e.printStackTrace();
            return "failed";
        }

        return "success";
    }

//watch files
    private void watchDir(File listenFile,WebSocketSession session){
        FileAlterationObserver observer = new FileAlterationObserver(listenFile);
        FileListerAdapter listener = new FileListerAdapter(getUserURL(),session);  //actions
        observer.addListener(listener);
        monitor.addObserver(observer);
    }

    private void initRootWatchDir(WebSocketSession session){
        File file = new File(getUserURL());
        LinkedList<File> fList = new LinkedList<File>();
        fList.addLast(file);

        try{
            watchDir(file,session);
            monitor.start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        System.out.println("connected");
        initRootWatchDir(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        //System.out.println("message: " + message.getPayload());
        FileAction wsa = gson.fromJson(message.getPayload(), FileAction.class);

        String result = "error";
        switch (wsa.getEvent()){
            case "get":
                    result = getFiles(wsa.getData());
                break;
            case "getHash":
                    result = getFilesHash(wsa.getData());
                break;
            case "touch":
                    result = touchFile(wsa.getData());
                break;
            case "write":
                    result = writeFile(wsa.getData());
                break;
            case "mkdir":
                    result = mkdirFile(wsa.getData());
                break;
            case "rm":
                    result = rmFile(wsa.getData());
                break;
            case "cp":
                    result = cpFile(wsa.getData());
                break;
            case "mv":
                    result = mvFile(wsa.getData());
                break;
            //case "test": monitor.stop();break;
            default:
                System.out.println("no match operation for : " + wsa.getEvent());
                break;
        }

        RespondAcction ra = new RespondAcction();
        ra.setEvent(wsa.getEvent() + "Respond");
        ra.setResult(result);
        session.sendMessage(new TextMessage(gson.toJson(ra)));

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
        System.out.println("closed");
        monitor.stop();
    }
}
