package com.example.lankesh.filetransferapp;

import android.content.res.AssetManager;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;

;

/**
 * Created by Lankesh on 4/9/2018.
 */

/**
 * This class implements a small special purpose webserver to handle application specific requiests
 * Such as file uplaods
 */
public class Server {


    public static final String INVALID_PORT = "INVALID_PORT";
    private static final String FILE_CONTENT_START = "------------fileContentStart-----\n";
    private static final String FILE_NAME_START = "----fileNameStart-----\n";
    private static final String FILE_NAME_END = "----fileNameEnd----\n";
    public static final String SERVER_DOWN = "SERVER_DOWN";
    public static final String SERVER_UP = "SERVER_UP";
    public static final String INVALID_UPLOAD_FOLDER = "INVALID_UPLOAD_FOLDER";

    private String uploadFolder = "";


    ServerSocket serverSocket;

    int port;

    public static final int SERVER_EVENT = 1;

    private boolean severStarted = false;

    private boolean startServerAccepetLoop = false;

    private ServerStatusListener listener;

    private AssetManager manager;

    private Handler handler;

    private boolean serverStopped = true;


    MusicPlayer musicPlayer;


    public boolean isServerStopped() {
        return serverStopped;
    }


    public void setListener(final ServerStatusListener listener) {
        this.listener = listener;
    }

    public void setUploadFolder(String uploadFolder) {
        this.uploadFolder = uploadFolder;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setManager(AssetManager manager) {
        this.manager = manager;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void pingServer(int port) {

        while (true) {
            try {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Socket clientSocket = new Socket("localhost", port);

                handler.obtainMessage(SERVER_EVENT, Server.SERVER_UP).sendToTarget();
                clientSocket.close();
            } catch (IOException e) {

                handler.obtainMessage(SERVER_EVENT, SERVER_DOWN).sendToTarget();
            }
        }


    }


    public void startServer() {

        musicPlayer = new MusicPlayer();

        serverStopped = false;

        if (uploadFolder.isEmpty()) {

            handler.obtainMessage(SERVER_EVENT, INVALID_UPLOAD_FOLDER).sendToTarget();
            return;


        }

        try {

            if (port < 1 || port > 65535) {

                handler.obtainMessage(SERVER_EVENT, INVALID_PORT).sendToTarget();
                return;

            }
            serverSocket = new ServerSocket(port);

            handler.obtainMessage(SERVER_EVENT, SERVER_UP).sendToTarget();
        } catch (Exception ex) {

            handler.obtainMessage(SERVER_EVENT, SERVER_DOWN).sendToTarget();
            return;
        }

        startServerAccepetLoop = true;


        while (startServerAccepetLoop) {


            System.out.println("Listening...........");


            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                handler.obtainMessage(SERVER_EVENT, "SERVER_DOWN").sendToTarget();
                return;
            }


            Thread serverThread = new Thread(new ServerThread(clientSocket));

            serverThread.start();


        }

    }


    public void stopServer() {

        serverStopped = true;
        startServerAccepetLoop = false;

        if (musicPlayer != null) {

            musicPlayer.stop();
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void processRequest(Socket clientSocket) throws IOException {
        FileOutputStream writer;

        String method = null;

        String path = null;

        PrintStream response = new PrintStream(clientSocket.getOutputStream());

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


        StringBuilder builder = new StringBuilder();


        String line;

        //Read th first line of the request;
        try {
            line = inputStream.readLine();

            if (line != null) {

                String[] methodAndPath = line.split("\\s+");

                method = methodAndPath[0].trim();

                path = methodAndPath[1].trim();


            }


            System.out.println("Method " + method);
            System.out.println("Path " + path);

            if ((method != null && !method.isEmpty()) && (path != null && !path.isEmpty())) {
                if (method.equalsIgnoreCase("POST") && path.matches("/fileuploads")) {


                    while (line != null && !(line = inputStream.readLine()).matches("------------fileContentEnd-----")) {


                        builder.append(line + "\n");

                    }


                    String buffer = new String(builder.toString());
                    System.out.println(buffer);

                    System.out.println("Char length" + buffer.length());


                    //Parse the content
                    int fileConteBoundary = buffer.indexOf(FILE_CONTENT_START);


                    if (fileConteBoundary >= 0) {

                        String base64FileContent = buffer.substring(fileConteBoundary + FILE_CONTENT_START.length(), buffer.length());

                        System.out.println("Base 64 content " + base64FileContent);

                        int fileNameStart = buffer.indexOf(FILE_NAME_START);
                        int fileNameEnd = buffer.indexOf(FILE_NAME_END);

                        String base64fileName = buffer.substring(fileNameStart + FILE_NAME_START.length(), fileNameEnd);


                        writer = new FileOutputStream(new File(uploadFolder + File.separator + base64fileName.trim()));


                        writer.write(android.util.Base64.decode(base64FileContent.trim(), android.util.Base64.DEFAULT));
                        writer.flush();
                        writer.close();
                    }


                    System.out.println("Send Response ......");


                    response.println("HTTP/2.0 200 OK");
                    response.println("Access-Control-Allow-Origin: *");
                    response.println("content-type: text/plain");
                    response.println("content-length: " + "Uploaded Successfully!".getBytes().length);
                    response.println();
                    response.write("Uploaded Successfully!".getBytes());

                    response.flush();
                    response.close();

                } else if (method.equalsIgnoreCase("GET") && path.matches("/filelist")) {


                    File files = new File(uploadFolder);

                    File[] list = files.listFiles();

                    StringBuilder fileListBuilder = new StringBuilder();

                    for (File file : list) {

                        fileListBuilder.append("<a href=\"" + "/download/" + URLEncoder.encode(file.getName()) + "\">" + file.getName() + "</a>\n");

                    }


                    System.out.println("Send Response ......");

                    String payLoad = fileListBuilder.toString();


                    response.println("HTTP/2.0 200 OK");
                    response.println("Access-Control-Allow-Origin: *");
                    response.println("content-type: text/plain");
                    response.println("content-length: " + payLoad.getBytes().length);
                    response.println();
                    response.write(payLoad.getBytes());

                    response.flush();
                    response.close();


                } else if (method.equalsIgnoreCase("GET") && path.matches("/download/.*")) {


                    String fileName = path.split("/download/")[1];
                    File files = new File(uploadFolder + File.separator + URLDecoder.decode(fileName.replace("/", File.separator), "utf-8"));


                    FileInputStream stream = new FileInputStream(files);

                    byte[] fileByteArray = new byte[(int) files.length()];

                    stream.read(fileByteArray, 0, fileByteArray.length);


                    System.out.println("Send Response ......");


                    response.println("HTTP/2.0 200 OK");
                    response.println("Access-Control-Allow-Origin: *");

                    int extentionSeparator = fileName.lastIndexOf(".");

                    String fileExt = fileName.substring(extentionSeparator + 1);
                    if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {

                        response.println("content-type: text/html");
                    } else if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".PNG") || fileName.endsWith(".gif")) {

                        response.println("content-type: image/" + fileExt);
                    } else {
                        response.println("content-type: application/octet-stream");
                    }
                    response.println("content-length: " + fileByteArray.length);
                    response.println();

                    response.write(fileByteArray);

                    response.flush();
                    response.close();


                } else if ((method.equalsIgnoreCase("GET") && path.trim().equals("/"))) {


                    InputStream homePageInputStream = manager.open("FileUpload.html");


                    StringBuilder webPage = new StringBuilder();

                    int character;
                    while ((character = homePageInputStream.read()) != -1) {

                        webPage.append((char) character);
                    }


                    System.out.println("Send Response ......");


                    response.println("HTTP/2.0 200 OK");
                    response.println("Access-Control-Allow-Origin: *");

                    response.println("content-type: text/html");

                    response.println("content-length: " + webPage.toString().length());
                    response.println();

                    response.write(webPage.toString().getBytes());

                    response.flush();
                    response.close();


                }else if ((method.equalsIgnoreCase("GET") && path.trim().equals("/audio"))) {


                    InputStream homePageInputStream = manager.open("PlayMusic.html");


                    StringBuilder webPage = new StringBuilder();

                    int character;
                    while ((character = homePageInputStream.read()) != -1) {

                        webPage.append((char) character);
                    }


                    System.out.println("Send Response ......");


                    response.println("HTTP/2.0 200 OK");
                    response.println("Access-Control-Allow-Origin: *");

                    response.println("content-type: text/html");

                    response.println("content-length: " + webPage.toString().length());
                    response.println();

                    response.write(webPage.toString().getBytes());

                    response.flush();
                    response.close();


                }else if (method.equalsIgnoreCase("GET") && path.matches("/audio/play/.*")) {


                    String fileName = path.split("/audio/play/")[1];
                    String musicFilePath = uploadFolder + File.separator + URLDecoder.decode(fileName.replace("/", File.separator), "utf-8");

                    musicPlayer.stop();

                    musicPlayer.play(musicFilePath);

                    System.out.println("Send Response ......");


                    String message = "Playing file";
                    response.println("HTTP/2.0 200 OK");
                    response.println("Access-Control-Allow-Origin: *");

                    response.println("content-type: text/plain");

                    response.println("content-length: " + message.getBytes().length);
                    response.println();

                    response.write(message.getBytes());

                    response.flush();
                    response.close();


                }

                else if (method.equalsIgnoreCase("GET") && path.matches("/audio/stop")) {


                    musicPlayer.stop();

                    System.out.println("Send Response ......");


                    String message = "Player stopped";
                    response.println("HTTP/2.0 200 OK");
                    response.println("Access-Control-Allow-Origin: *");

                    response.println("content-type: text/plain");

                    response.println("content-length: " + message.getBytes().length);
                    response.println();

                    response.write(message.getBytes());

                    response.flush();
                    response.close();


                } else if (method.equalsIgnoreCase("GET") && path.matches("/audio/list")) {


                    File files = new File(uploadFolder);

                    String[] list = files.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String s) {


                            return s.endsWith(".mp3") ;
                        }
                    });


                    StringBuilder fileListBuilder = new StringBuilder();

                    fileListBuilder.append("{");
                    fileListBuilder.append("\"audioFileList\" : [");
                    int  fileListLength = list.length;

                    int fileIndex = 0;
                    for (String file : list) {

                        fileListBuilder.append("\"");
                        fileListBuilder.append(file);
                        fileListBuilder.append("\"");
                        if(fileIndex < fileListLength -1) {
                         fileListBuilder.append(",");
                        }

                        fileIndex ++;

                    }

                    fileListBuilder.append("] }");

                    String fileListJson = fileListBuilder.toString();


                    System.out.println("Send Response ......");


                    response.println("HTTP/2.0 200 OK");
                    response.println("Access-Control-Allow-Origin: *");
                    response.println("content-type: application/json");
                    response.println("content-length: " + fileListJson.length());
                    response.println();
                    response.write(fileListJson.getBytes());

                    response.flush();
                    response.close();


                }

            }
        } catch (FileNotFoundException ex) {

            if (response != null) {

                String message = "The page cannot be found";
                response.println("HTTP/2.0 404 Not Found");
                response.println("Access-Control-Allow-Origin: *");
                response.println("content-length: " + message.getBytes().length);
                response.println();
                response.write(message.getBytes());
                response.flush();
                response.close();

            }
        } catch (Exception ex) {

            String message = "A error has occured while processing request.";
            response.println("HTTP/2.0 500 Internal Server Error");
            response.println("Access-Control-Allow-Origin: *");
            response.println("content-length: " + message.getBytes().length);
            response.println();
            response.write(message.getBytes());
            response.flush();
            response.close();

        } finally {

            if (response != null) {

                response.close();
            }

        }


    }

    class ServerThread implements Runnable {

        private Socket client;

        public ServerThread(Socket client) {

            this.client = client;


        }

        public void run() {

            try {
                processRequest(client);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public boolean isStartServerAccepetLoop() {
        return startServerAccepetLoop;
    }


    public void setStartServerAccepetLoop(boolean startServerAccepetLoop) {
        this.startServerAccepetLoop = startServerAccepetLoop;
    }

    public void setSeverStarted(final boolean severStarted) {
        this.severStarted = severStarted;
    }

    public boolean isSeverStarted() {
        return severStarted;
    }
}
