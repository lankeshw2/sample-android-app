package com.example.lankesh.filetransferapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Lankesh on 4/10/2018.
 */

public class ActiveNetworkInterfaceSearchThread extends Thread {


    Enumeration<NetworkInterface> networkInterfaceEnumeration;

    Handler handler;

    Context context;


    public ActiveNetworkInterfaceSearchThread(Handler handler, Context context){

        this.handler =  handler;
        this.context = context;

    }

    @Override
    public void run() {

        while(true) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ConnectivityManager connectivityService =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);




            NetworkInfo currentNetwork = connectivityService.getActiveNetworkInfo();

           handler.obtainMessage(Server.SERVER_EVENT, currentNetwork).sendToTarget();



        }

    }
}
