package com.encardio.er_ngrf.tool;

import android.os.Environment;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Check_LatestSystem {
    public static String[] get_latest_version_from_ftp() {
        String server = "122.15.209.75";
        int port = 50004;
        String user = "SANDEEP";
        String pass = "SANDEEP";
        boolean success = false;
        FTPClient ftpClient = new FTPClient();
        try {

            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            String remoteFile1 = "Version.txt";
            File downloadFile1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.COMPANY_FOLDER + Constant.ROOT_FOLDER + Constant.LATEST_VERSION_FILE);
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
              success = ftpClient.retrieveFile(remoteFile1, outputStream1);
            outputStream1.close();

            if (success) {
                System.out.println("File #1 has been downloaded successfully.");
            }



//            File downloadFile2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.COMPANY + Constant.ROOT + Constant.VERSION);
//            OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
//            InputStream inputStream = ftpClient.retrieveFileStream(Constant.VERSION);
//            byte[] bytesArray = new byte[4096];
//            int bytesRead = -1;
//            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
//                outputStream2.write(bytesArray, 0, bytesRead);
//            }
//
//            success = ftpClient.completePendingCommand();
//
//            outputStream2.close();
//            inputStream.close();


        } catch (Exception ex) {
//            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (success) {
            return Tool.read_file(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.COMPANY_FOLDER + Constant.ROOT_FOLDER + Constant.LATEST_VERSION_FILE);
        }
        return null;
    }
}
