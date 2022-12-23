package com.encardio.er_ngrf.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.encardio.er_ngrf.tool.Constant;
import com.encardio.er_ngrf.tool.Tool;

import org.apache.commons.net.ftp.FTPClient;

public class URL_SettingsActivity extends AppCompatActivity implements Runnable {

    private final Handler handler = new Handler();
    protected boolean isConnectionFailed;
    Thread thread;
    Button buttonSaveOnUrlSettings;
    private ProgressDialog dialog;
    private boolean wrongCredentials;
    private EditText editUrl;
    private EditText editPort;
    private EditText editUser;
    private EditText editPassword_of_Url;
    private String editTextUrl;
    private String editTextPort;
    private String editTextUserName;
    private String editTextPassword;
    private String editcnfirmPasswrd;
    private EditText edittextcnfirmPas;

    @SuppressLint("SetTextI18n")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_settings);
        SharedPreferences prefUrlSettings = URL_SettingsActivity.this
                .getSharedPreferences("URL_SETTINGS", 0);
        Constant.url = prefUrlSettings.getString("URL", Constant.url);
        Constant.port = prefUrlSettings.getInt("PORT", Constant.port);
        Constant.userName = prefUrlSettings.getString("USERNAME",
                Constant.userName);
        Constant.password = prefUrlSettings.getString("PASSWORD",
                Constant.password);
        editUrl = findViewById(R.id.editUrl);
        editPort = findViewById(R.id.editPort);
        editUser = findViewById(R.id.editUser);
        editPassword_of_Url = findViewById(R.id.editPassword_of_Url);
        edittextcnfirmPas = findViewById(R.id.editTextConfirm);
        editUrl.setText(Constant.url);
        editPort.setText("" + Constant.port);
        editUser.setText(Constant.userName);

        buttonSaveOnUrlSettings = findViewById(R.id.buttonSaveOnUrlSettings);


        buttonSaveOnUrlSettings.setOnClickListener(
                v -> {
                    Tool.controlButtonDebouncing(buttonSaveOnUrlSettings);
                    try {
                        getUrlSettingsParameters();
                        if (editcnfirmPasswrd.equals(editTextPassword)) {


                            if (Tool.validateFTP_Password(editUser.getText().toString())) {
                                if (Tool.validateFTP_Password(edittextcnfirmPas.getText().toString())) {
                                    if (Tool.validateFTP_URL(editUrl.getText().toString())) {
                                        initProgressDialog();
                                        dialog.show();

                                        thread = new Thread(URL_SettingsActivity.this);
                                        thread.start();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "The FTP URL should contain only Number(0-9), and dot(.) characters", Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "The password should contain only Upper(A-Z), lower(a-z) characters and numbers(0-9)", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "The FTP User should contain only Upper(A-Z), lower(a-z) characters and numbers(0-9)", Toast.LENGTH_LONG).show();
                            }


                        } else
                            Toast.makeText(getApplicationContext(),
                                    "Password should be same..", Toast.LENGTH_LONG)
                                    .show();

                    } catch (Exception ignored) {
                    }
                });
        findViewById(R.id.buttonCancelOnUrlSettings).setOnClickListener(
                v -> {
                    Intent intent = new Intent(v.getContext(),
                            UploadDataActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                });

    }

    protected void getUrlSettingsParameters() {
        editTextUrl = (editUrl).getText().toString();
        editTextPort = (editPort).getText().toString();
        editTextUserName = (editUser).getText().toString();
        editTextPassword = (editPassword_of_Url).getText().toString();
        editcnfirmPasswrd = (edittextcnfirmPas).getText().toString();
    }

    private void initProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait !!");
        dialog.setCancelable(false);
        dialog.setMessage("Checking Credentials");
    }

    /**
     * this method must be overridden, as we are implementing the Runnable
     * interface.
     */
    @Override
    public void run() {
        // upload the files
        try {
            checkCredentials();
        } catch (Exception ignored) {
        }
        // update the canvas from this thread using the Handler object
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (thread != null) {
                    thread.interrupt();
                    thread = null;
                }
                if (isConnectionFailed) {
                    dialog.dismiss();
                    Toast.makeText(
                            URL_SettingsActivity.this.getApplicationContext(),
                            "Unable to connect to server !!", Toast.LENGTH_LONG)
                            .show();
                    Toast.makeText(
                            URL_SettingsActivity.this.getApplicationContext(),
                            "Please check Network, URL and Port.",
                            Toast.LENGTH_LONG).show();

                } else if (wrongCredentials) {
                    dialog.dismiss();
                    Toast.makeText(
                            URL_SettingsActivity.this.getApplicationContext(),
                            "Username or password is incorrect !!",
                            Toast.LENGTH_LONG).show();
                } else {
                    dialog.dismiss();
                    Toast.makeText(
                            URL_SettingsActivity.this.getApplicationContext(),
                            "URL settings updated successfully ",
                            Toast.LENGTH_LONG).show();
                    saveUrlSettings();
                }
            }
        });
    }

    /**
     * to check the userId and password of FTP server.
     */
    protected void checkCredentials() {
        isConnectionFailed = false;
        String url = editTextUrl;
        int port = Integer.parseInt(editTextPort);
        String userName = editTextUserName;
        String password = editTextPassword;
        try {
            FTPClient con = new FTPClient();
            try {
                con.setConnectTimeout(30000); // 1 minute
                con.connect(url, port);

            } catch (Exception e1) {
                isConnectionFailed = true;
                return;
            }
            try {
                wrongCredentials = !con.login(userName, password);
                con.logout();
                con.disconnect();
            } catch (Exception ignored) {
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * updates the URl settings.
     */
    protected void saveUrlSettings() {
        SharedPreferences.Editor prefUrlSettings = URL_SettingsActivity.this
                .getSharedPreferences("URL_SETTINGS", 0).edit();
        prefUrlSettings.putString("URL", editTextUrl);
        prefUrlSettings.putInt("PORT", Integer.parseInt(editTextPort));
        prefUrlSettings.putString("USERNAME", editTextUserName);
        prefUrlSettings.putString("PASSWORD", editTextPassword);
        prefUrlSettings.apply();
        Intent intent = new Intent(getApplicationContext(),
                UploadDataActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


}