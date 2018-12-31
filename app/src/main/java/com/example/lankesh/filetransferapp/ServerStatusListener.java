package com.example.lankesh.filetransferapp;

/**
 * Created by Lankesh on 4/9/2018.
 */

public interface ServerStatusListener {

    void onStart();

    void onFailed();

    void onServerDown();



}
