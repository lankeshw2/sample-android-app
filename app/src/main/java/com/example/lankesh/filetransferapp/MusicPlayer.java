package com.example.lankesh.filetransferapp;

import android.media.MediaPlayer;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Lankesh on 4/14/2018.
 */

/**
 * Plays music on the phone
 */
public class MusicPlayer {


    MediaPlayer player;


    public synchronized void play(String path) {

        player = new MediaPlayer();

        try {
            player.setDataSource(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            player.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        player.start();


    }


    public synchronized void  stop() {

        if (player != null) {

            try {
                player.stop();
            }catch (IllegalStateException ex) {
                ex.printStackTrace();
            }


        }


    }


}
