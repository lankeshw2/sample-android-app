package com.example.lankesh.filetransferapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

/**
 * Main UI of the application.
 */
public class MainActivity extends AppCompatActivity {


    public static final String SELECTED_PORT = "SELECTED_PORT";
    public static final String ERROR_MESSAGE = "Error: Please check the given server port or the server upload path is valid";
    EditText serverPortText;
    TextView fileIUploadFolderPathText;
    Button selectfolderButton;
    Button startServerButton;
    Server fileServer;
    Handler uiHandler;
    Handler activeNetInterfaceHandler;

    TextView networkInterfaceText;

    WifiManager wifiManager;

    TextView errorText;




    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        fileServer = new Server();


        errorText = findViewById(R.id.errorText);


        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        networkInterfaceText = findViewById(R.id.networkInterfaceText);

        activeNetInterfaceHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                String netInterfaceText = "";

                NetworkInfo networkInterface = (NetworkInfo) msg.obj;


                if (networkInterface.getType() == ConnectivityManager.TYPE_WIFI) {

                    netInterfaceText += "Current WIFI Access Point : " + networkInterface.getExtraInfo() + "\n";
                    netInterfaceText += "IP Address: " + IntergerToStringIPAddress.intToStringIp(wifiManager.getConnectionInfo().getIpAddress());


                    networkInterfaceText.setText(netInterfaceText);

                } else {


                    networkInterfaceText.setText("Connect to a WI-FI network.");
                }

                super.handleMessage(msg);
            }
        };

        ActiveNetworkInterfaceSearchThread networkInterfaceSearchThread = new ActiveNetworkInterfaceSearchThread(activeNetInterfaceHandler, this);


        networkInterfaceSearchThread.start();

        uiHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {

                String serverState = (String) msg.obj;
                if (serverState.equals(Server.SERVER_UP)) {


                    startServerButton.setText("Stop Server");
                    errorText.setText("");
                    fileServer.setSeverStarted(true);

                } else if ( serverState.equals(Server.INVALID_PORT) || serverState.equals(Server.INVALID_UPLOAD_FOLDER)) {

                    errorText.setText(ERROR_MESSAGE);
                    startServerButton.setText("Restart Server");
                    fileServer.setSeverStarted(false);
                } else if (serverState.equals(Server.SERVER_DOWN)) {

                    fileServer.setSeverStarted(false);
                    startServerButton.setText("Restart Server");

                    if(fileServer.isServerStopped()) {
                        errorText.setText("");

                    } else {

                        errorText.setText(ERROR_MESSAGE);

                    }

                }


                super.handleMessage(msg);
            }
        };

        serverPortText = findViewById(R.id.fileServerPort);


        fileIUploadFolderPathText = findViewById(R.id.serverUploadFolderPath);


        startServerButton = findViewById(R.id.startServerButton);

        startServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileServer != null && fileServer.isSeverStarted()) {

                    errorText.setText("");
                    Thread stopThread = new Thread() {
                        @Override
                        public void run() {
                            fileServer.stopServer();
                        }
                    };

                    stopThread.start();

                } else {
                    errorText.setText("");
                    final int port = (!serverPortText.getText().toString().isEmpty()) ? Integer.valueOf(serverPortText.getText().toString()) : 0;

                    fileServer.setPort(port);
                    fileServer.setUploadFolder(fileIUploadFolderPathText.getText().toString());
                    fileServer.setManager(getAssets());
                    fileServer.setHandler(uiHandler);


                    final Thread serverStartThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            fileServer.startServer();

                        }
                    });

                    serverStartThread.start();





                }

            }
        });


        selectfolderButton = findViewById(R.id.selectFolderButton);

        //Show the file browser screen
        selectfolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, FileuiLayoutActivity.class);

                startActivity(intent);


            }
        });


        //Load the Advert.
        MobileAds.initialize(this, "ca-app-pub-4020974464328781~8643270102");
        AdView adview = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);



    }


    @Override
    protected void onResume() {

        fileIUploadFolderPathText.setText(SelectedFolder.getInstance().getFolder());
        super.onResume();
    }

    @Override
    protected void onDestroy() {
       if(fileServer != null){

           fileServer.stopServer();

       }
       super.onDestroy();
    }
}




