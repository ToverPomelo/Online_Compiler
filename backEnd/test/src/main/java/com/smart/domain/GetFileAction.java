/*
    Description: Used to deal with JSON.
                 Give the frontend the files list when recived the 'get' event.
*/

package com.smart.domain;

import java.util.ArrayList;
import java.util.List;

public class GetFileAction {
    private String event;
    private List<FileData> data;

    public void setEvent(String event) {
        this.event = event;
    }
    public String getEvent() {
        return event;
    }

    public void setData(List<FileData> data) {
        this.data = data;
    }
    public List<FileData> getData() {
        return data;
    }


    public GetFileAction(){
        this.event = "";
        this.data = new ArrayList<FileData>();
    }

    @Override
    public String toString() {
        String result = "event: "+this.event+"\ndata: [\n";
        for(int i=0;i<this.data.size();i++){
            result += "\t{" + "\n\t\tpath: " + this.data.get(i).getPath() + ",\n\t\tcontent: " + this.data.get(i).getContent() + ",\n\t\tsource: " + this.data.get(i).getSource() + "\n\t},\n";
        }
        result += "]";
        return result;
    }
}
