package com.example.lankesh.filetransferapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a file browser which allows the user to select a folder from the
 * Phone file system.
 */
public class FileuiLayoutActivity extends AppCompatActivity {

    ListView listView;

    List<String> path = new ArrayList<>();

    Button selelectFolderButton;

    Button backButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fileui_layout);

         selelectFolderButton =findViewById( R.id.selectFolderButton);
         backButton = findViewById(R.id.backButton);

         backButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 if(path.size() >1 ) {

                     path.remove(path.size() -1);
                 }

                 String directoryString ="";

                 for(String dir : path) {

                     directoryString += dir;
                     if(!dir.equals("/")){

                         directoryString +="/";
                     }
                 }


                 File directoryList = new File(directoryString);
                 setTitle(directoryString);

                 String[] fileList =  directoryList.list(new FilenameFilter() {
                     @Override
                     public boolean accept(File file, String s) {

                         File checkFile;
                         if(!file.getName().equals("/")) {
                             checkFile  = new File(file.getAbsolutePath() +File.separator +s);
                         } else {
                             checkFile = new File(file.getName() +s);

                         }
                         return  checkFile.isDirectory() && checkFile.canWrite();
                     }
                 });





                 if(fileList !=null) {


                     ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(FileuiLayoutActivity.this, R.layout.text_view_layout, R.id.listTextView, fileList);

                     listView.setAdapter(arrayAdapter);
                 }




             }
         });


         selelectFolderButton.setOnClickListener(new View.OnClickListener(){


             @Override
             public void onClick(View view) {



                 String directoryString ="";

                 for(String dir : path) {

                     directoryString += dir;
                     if(!dir.equals("/")){

                         directoryString +="/";
                     }
                 }



                 SelectedFolder.getInstance().setFolder(directoryString);

                 finish();

             }
         });

        listView = findViewById(R.id.fileList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                ConstraintLayout  textViewLyout = (ConstraintLayout) view;

                TextView textView = (TextView) textViewLyout.getChildAt(0);

                String fileName = (String) textView.getText();



                String directoryString ="";

                 for(String dir : path) {

                     directoryString += dir;
                      if(!dir.equals("/")){

                          directoryString +="/";
                      }
                 }


                 File directoryList = new File(directoryString +File.separator +fileName);
                 path.add(fileName);

                 String[] fileList =  directoryList.list(new FilenameFilter() {
                     @Override
                     public boolean accept(File file, String s) {

                         File checkFile;
                         if(!file.getName().equals("/")) {
                             checkFile  = new File(file.getAbsolutePath() +File.separator +s);
                         } else {
                             checkFile = new File(file.getName() +s);

                         }
                         return checkFile.isDirectory() && checkFile.canWrite();
                     }
                 });


                setTitle(directoryString+fileName);
                if(fileList !=null) {


                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(FileuiLayoutActivity.this, R.layout.text_view_layout, R.id.listTextView, fileList);

                    listView.setAdapter(arrayAdapter);
                }



            }
        });

        File fileList = new File("/");

        path.add("/");

        String[] fileListString = fileList.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {

                File checkFile;
                if(!file.getName().equals("/")) {
                   checkFile  = new File(file.getAbsolutePath() +File.separator +s);
                } else {
                    checkFile = new File(file.getName() +s);

                }
                return checkFile.isDirectory() && (checkFile.canWrite());
            }
        });

        ArrayAdapter<String>  arrayAdapter = new ArrayAdapter<String>(this, R.layout.text_view_layout, R.id.listTextView, fileListString);




       listView.setAdapter(arrayAdapter);





    }

}
