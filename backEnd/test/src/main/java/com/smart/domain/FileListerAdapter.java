/*
    Description: Actions when files in backend changed.
    Note: session is the websocket session used to communicate with frontend.
*/

package com.smart.domain;

import com.google.gson.Gson;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.File;

public class FileListerAdapter extends FileAlterationListenerAdaptor {
    private Gson gson = new Gson();
    private String userURL;
    private WebSocketSession session;

    public FileListerAdapter(String userURL,WebSocketSession session){
        this.userURL = userURL;
        this.session = session;
    }

    private void sendMessage(String event,String result){
        RespondAcction ra = new RespondAcction();
        ra.setEvent(event);
        ra.setResult(result);
        try {
            session.sendMessage(new TextMessage(gson.toJson(ra)));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onFileChange(File file) {
        if (!file.exists() || !file.canRead()) {
            System.out.println("The file "+ file.toPath().toString().substring(userURL.length()) +" is not exists or is not readable!");
            return;
        }
        //System.out.println("----The file "+ file.toPath().toString().substring(userURL.length()) +" is change.");
        sendMessage("write",file.toPath().toString().substring(userURL.length()));
        //TODO 读取操作
        super.onFileChange(file);
    }

    @Override
    public void onFileCreate(File file) {
        if (!file.exists()) {
            System.out.println("The file "+ file.toPath().toString().substring(userURL.length()) +" is not exists!");
            return;
        }
        if (!file.canRead()) {
            System.out.println("The file "+ file.toPath().toString().substring(userURL.length()) +" is not readable!");
            return;
        }
        //System.out.println("----The file "+ file.toPath().toString().substring(userURL.length()) +" is created.");
        sendMessage("touch",file.toPath().toString().substring(userURL.length()));
        //TODO 读取操作
        super.onFileCreate(file);
    }

    @Override
    public void onFileDelete(File file) {
        //System.out.println("----The file "+ file.toPath().toString().substring(userURL.length()) +" is deleted.");
        sendMessage("rm",file.toPath().toString().substring(userURL.length()));
        super.onFileDelete(file);
    }

    @Override
    public void onDirectoryChange(File directory) {
        if (!directory.exists()) {
            System.out.println("The directory "+ directory.toPath().toString().substring(userURL.length()) +" is not exists!");
            return;
        }
        //System.out.println("----The directory "+ directory.toPath().toString().substring(userURL.length()) +" has changed.");
        super.onDirectoryChange(directory);
    }

    @Override
    public void onDirectoryCreate(File directory) {
        if (!directory.exists()) {
            System.out.println("The directory "+ directory.toPath().toString().substring(userURL.length()) +" is not exists!");
            return;
        }
        //System.out.println("----The directory "+ directory.toPath().toString().substring(userURL.length()) +" is created.");
        sendMessage("mkdir",directory.toPath().toString().substring(userURL.length()));
        super.onDirectoryCreate(directory);
    }

    @Override
    public void onDirectoryDelete(File directory) {
        //System.out.println("----The directory "+ directory.toPath().toString().substring(userURL.length()) +" is deleted.");
        sendMessage("rm-rf",directory.toPath().toString().substring(userURL.length()));
        super.onDirectoryDelete(directory);
    }
}