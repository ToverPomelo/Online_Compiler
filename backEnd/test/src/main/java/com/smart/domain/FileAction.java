/*
    Description: Used to deal with JSON.
                 Messages recived from frontend.
                 Messages sending to frontend when user change files directly in backend.
*/

package com.smart.domain;

public class FileAction {
    private String event;
    //private List<W2SData> data;
    private FileData data;

    public void setEvent(String event) {
        this.event = event;
    }
    public String getEvent() {
        return event;
    }
/*
    public void setData(List<W2SData> data) {
        this.data = data;
    }
    public List<W2SData> getData() {
        return data;
    }
*/

    public FileData getData() {
        return data;
    }

    public void setData(FileData data) {
        this.data = data;
    }

    public FileAction(){
        this.event = "";
        //this.data = new ArrayList<W2SData>();
        this.data = new FileData();
    }

    @Override
    public String toString() {
        String result = "event: "+this.event+"\ndata: [\n";
        /*for(int i=0;i<this.data.size();i++){
            result += "\t{" + "\n\t\tpath: " + this.data.get(i).getPath() + ",\n\t\tcontent: " + this.data.get(i).getContent() + ",\n\t\tsource: " + this.data.get(i).getSource() + "\n\t},\n";
        }*/
        result += "\t{" + "\n\t\tpath: " + this.data.getPath() + ",\n\t\tcontent: " + this.data.getContent() + ",\n\t\tsource: " + this.data.getSource() + "\n\t},\n";
        result += "]";
        return result;
    }
}