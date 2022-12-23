package com.encardio.er_ngrf.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.encardio.er_ngrf.tool.Constant;
import com.encardio.er_ngrf.tool.Tool;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Sandeep
 */
public class UploadDataActivity extends AppCompatActivity implements Runnable {

    private final Handler handler = new Handler();
    protected boolean isConnectionFailed;
    int uploadedFileCounter = 0;
    Thread thread;
    long startTimeToUpload;
    List<UploadFilesModel> list;
    UploadFilesAdapter adapter;
    Button buttonResetURL;
    Button buttonUpload;
    private File sdCard;
    private File csvFilesDir;
    private File[] csvFiles;
    private String absolutePath;
    private boolean isAnyFileSelected;
    private ProgressDialog dialog;
    private boolean wrongCredentials;
    private boolean isAllfilesUploadandArchive2;
    private RecyclerView rv_upload_files;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);

        try {
            SharedPreferences prefUrlSettings = UploadDataActivity.this.getSharedPreferences("URL_SETTINGS", 0);

            Constant.url = prefUrlSettings.getString("URL", Constant.url);
            Constant.port = prefUrlSettings.getInt("PORT", Constant.port);
            Constant.userName = prefUrlSettings.getString("USERNAME", Constant.userName);
            Constant.password = prefUrlSettings.getString("PASSWORD", Constant.password);

            initProgressDialog();

            sdCard = Environment.getExternalStorageDirectory();

            csvFilesDir = new File(sdCard.getAbsolutePath() + Constant.COMPANY_FOLDER + Constant.ROOT_FOLDER + Constant.CSV_FOLDER);
            absolutePath = sdCard.getAbsolutePath() + Constant.COMPANY_FOLDER + Constant.ROOT_FOLDER + Constant.CSV_FOLDER;

            TextView textViewUrl_Default = findViewById(R.id.textViewUrl_Default);
            rv_upload_files = findViewById(R.id.rv_upload_files);

            textViewUrl_Default.setText(Constant.url);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            rv_upload_files.setLayoutManager(layoutManager);

            list = new ArrayList<>();

            adapter = new UploadFilesAdapter(list);
            rv_upload_files.setAdapter(adapter);

            getFilesToUpload();

        } catch (Exception e) {
            e.printStackTrace();
        }

        buttonResetURL = findViewById(R.id.buttonResetURL);
        buttonResetURL.setOnClickListener(v -> {
            Tool.controlButtonDebouncing(buttonResetURL);
            Intent intent = new Intent(v.getContext(), URL_SettingsActivity.class);
            startActivity(intent);
        });
        buttonUpload = findViewById(R.id.buttonUpload_inUploadFiles);
        buttonUpload.setOnClickListener(v -> {
            Tool.controlButtonDebouncing(buttonUpload);
            uploadedFileCounter = 0;
            if (Constant.url.equals("0.0.0.0") || Constant.url.equals("xxx.xxx.xxx.xxx")) {
                Toast.makeText(getApplicationContext(), "Please reset the URL settings.", Toast.LENGTH_LONG).show();
            } else {

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isSelected()) {
                        isAnyFileSelected = true;
                        break;
                    }
                }

                if (isAnyFileSelected) {
                    initProgressDialog();
                    dialog.show();

                    thread = new Thread(UploadDataActivity.this);
                    thread.start();
                } else {
                    Toast.makeText(getApplicationContext(), "Please select a file to upload !!", Toast.LENGTH_LONG)
                            .show();
                }
                isAnyFileSelected = false;
            }
        });
        Button buttonCancel_inFileUpload = findViewById(R.id.buttonCancel_inFileUpload);
        buttonCancel_inFileUpload.setOnClickListener(v -> UploadDataActivity.this.finish());

    }

    protected boolean uploadandArchiveFiles() {

        uploadedFileCounter++;
        boolean isAllfilesUploadandArchive = false;
        try {
            FTPClient con = new FTPClient();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isSelected()) {
                    String fileName = list.get(i).getFileName();

                    boolean isFileuploaded2 = uploadFiles(fileName, con);
                    if (isFileuploaded2) {
                        try {
                            File archiveDir_CSV = new File(sdCard.getAbsolutePath() + Constant.COMPANY_FOLDER + Constant.ROOT_FOLDER + Constant.CSV_FOLDER + Constant.ARCHIVE_FOLDER);
                            archiveDir_CSV.mkdirs();
                            String FULL_PATH_OF_SDCARD_FILE = absolutePath + File.separator + fileName;
                            File inputFile = new File(FULL_PATH_OF_SDCARD_FILE);
                            File outputFile = new File(archiveDir_CSV.getAbsolutePath() + File.separator + fileName);
                            FileInputStream fis = new FileInputStream(inputFile);
                            FileOutputStream fos = new FileOutputStream(outputFile);
                            int c;
                            while ((c = fis.read()) != -1) {
                                fos.write(c);
                            }
                            fis.close();
                            fos.close();
                            inputFile.delete();
                            isAllfilesUploadandArchive = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            try {
                con.logout();
                con.disconnect();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            isAllfilesUploadandArchive = false;
        }
        return isAllfilesUploadandArchive;
    }


    private void initProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait !!");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setMessage("Uploading files");
    }

    private void getFilesToUpload() {

        if (csvFilesDir.listFiles() == null || Objects.requireNonNull(csvFilesDir.listFiles()).length == 0) {
            Toast.makeText(getApplicationContext(), "CSV File not available ", Toast.LENGTH_LONG).show();
        } else {
            absolutePath = csvFilesDir.getAbsolutePath();
            getCsvfiles();
        }
    }


    private void getCsvfiles() {
        list = new ArrayList<>();

        if (csvFilesDir.isDirectory())
            csvFiles = csvFilesDir.listFiles();

        assert csvFiles != null;
        for (int i = csvFiles.length - 1; i >= 0; i--) {
            if (csvFiles[i].isFile()) {
                String csvFileName = csvFiles[i].getName();
                if (csvFileName.endsWith(".csv") || csvFileName.endsWith(".CSV")) {
                    list.add(new UploadFilesModel(csvFileName, false));
                }
            }
        }
        adapter = new UploadFilesAdapter(list);
        rv_upload_files.setAdapter(adapter);
    }

    public void run() {
        try {
            startTimeToUpload = System.currentTimeMillis();
            isAllfilesUploadandArchive2 = uploadandArchiveFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }

        handler.post(() -> {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
            if (isConnectionFailed) {
                dialog.dismiss();
                Toast.makeText(UploadDataActivity.this.getApplicationContext(), "Unable to connect to server due to network error", Toast.LENGTH_SHORT).show();
                Toast.makeText(UploadDataActivity.this.getApplicationContext(), "Unable to connect to server due to network error", Toast.LENGTH_LONG).show();
            } else if (wrongCredentials) {
                dialog.dismiss();
                Toast.makeText(UploadDataActivity.this.getApplicationContext(), "Username or password is incorrect !!", Toast.LENGTH_LONG).show();
            } else if (isAllfilesUploadandArchive2) {
                getFilesToUpload();
                dialog.dismiss();
                if (uploadedFileCounter > 0) {
                    getCsvfiles();
                    Toast.makeText(getApplicationContext(), " file successfully uploaded.", Toast.LENGTH_SHORT).show();
                }
            } else {
                if ((System.currentTimeMillis() - startTimeToUpload) > 120000) {
                    getFilesToUpload();
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Unable to connect to server due connection timeout", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected boolean uploadFiles(String fileName, FTPClient con) {
        boolean isFileuploaded = false;
        isConnectionFailed = false;
        String FULL_PATH_OF_SDCARD_FILE = absolutePath + File.separator + fileName;

        try {
            con.setConnectTimeout(30000); // 30 sec
            con.connect(Constant.url, Constant.port);
            con.setDataTimeout(120000); // 2 minute
        } catch (Exception e1) {
            isConnectionFailed = true;
            return isFileuploaded;
        }
        int reply = con.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            try {
                con.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isFileuploaded;
        }
        try {
            if (con.login(Constant.userName, Constant.password)) {
                con.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                BufferedInputStream buffIn;
                buffIn = new BufferedInputStream(new FileInputStream(FULL_PATH_OF_SDCARD_FILE));
                con.enterLocalPassiveMode(); // important!
                con.storeFile(fileName, buffIn);
                isFileuploaded = true;
                buffIn.close();
            } else {
                wrongCredentials = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isFileuploaded;
    }
}
