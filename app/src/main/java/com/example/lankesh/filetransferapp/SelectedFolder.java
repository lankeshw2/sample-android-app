package com.example.lankesh.filetransferapp;

/**
 * Created by Lankesh on 4/11/2018.
 */

public class SelectedFolder {

    String folder;

    public static SelectedFolder selectedFolder;


    private SelectedFolder(){

    }


    public void setFolder(final String folder) {
        this.folder = folder;
    }

    public static SelectedFolder getInstance(){

        if(selectedFolder != null) {

            return selectedFolder;
        }else {

            selectedFolder = new SelectedFolder();
            return selectedFolder;
        }



    }


    public String getFolder() {
        return folder;
    }
}
