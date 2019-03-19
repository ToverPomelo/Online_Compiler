/*
    Description: Used to deal with JSON.
                 Include information for a file.
*/

package com.smart.domain;

public class FileData {
    private String path;
    private String content;
    private String source;
    private String hash;

    public void setPath(String path) {
        this.path = path;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPath() {
        return path;
    }
    public String getContent() {
        return content;
    }
    public String getSource() {
        return source;
    }
    public String getHash() {
        return hash;
    }

    public FileData(){
        this.path = "";
        this.content = "";
        this.source = "";
        this.hash = "";
    }
}