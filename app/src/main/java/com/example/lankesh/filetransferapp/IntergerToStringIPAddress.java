package com.example.lankesh.filetransferapp;

/**
 * Created by Lankesh on 4/10/2018.
 */

public class IntergerToStringIPAddress {


    static String intToStringIp(int ip) {

        int result;
        int firstOctal;
        int secondOctal;
        int thirdOctal;
        int lastOctal;

        firstOctal =  (result = ip) & 255;
        secondOctal = (result  >>>= 8) & 255;
        thirdOctal = (result >>>=8) & 255;
        lastOctal = (result >>>=8) & 255;

        return firstOctal +"."+secondOctal +"."+thirdOctal+"."+lastOctal;


    }


}
