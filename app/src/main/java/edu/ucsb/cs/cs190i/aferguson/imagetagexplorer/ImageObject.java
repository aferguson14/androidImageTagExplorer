package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

/**
 * Created by Ferg on 5/14/17.
 */

public class ImageObject {

    private int id;
    private String uri;

    public ImageObject(){
    }

    public ImageObject(int id, String uri){
        this.id = id;
        this.uri = uri;
    }

    // getting ID
    public int getID(){
        return this.id;
    }

    // setting id
    public void setID(int id){
        this.id = id;
    }

    // getting uri
    public String getUri(){
        return this.uri;
    }

    // setting uri
    public void setUri(String uri){
        this.uri = uri;
    }
}


