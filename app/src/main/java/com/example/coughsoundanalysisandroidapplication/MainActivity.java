package com.example.coughsoundanalysisandroidapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

import okhttp3.OkHttpClient;

import android.os.AsyncTask;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MultipartBody;

public class MainActivity extends AppCompatActivity {

    // Declare variables
    Button btnStartRecord, btnStopRecord, btnStartPlay, btnStopPlay, btnAnalyze;
    String pathSave = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Handler handler;
    final int AUDIO_DURATION = 5000;
    final int REQUEST_PERMISSION_CODE = 1000;;
    final String API_url = "http://127.0.0.1:5000/classify/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request runtime permission
        if(!checkPermissionFromDevice())
            requestPermission();

        // Init view
        btnStartRecord = (Button) findViewById(R.id.btnStartRecord);
        // btnStopRecord = (Button) findViewById(R.id.btnStopRecord);
        btnStartPlay = (Button) findViewById(R.id.btnStartPlay);
        btnStopPlay = (Button) findViewById(R.id.btnStopPlay);
        btnAnalyze = (Button) findViewById(R.id.btnAnalyze);

        btnStartRecord.setEnabled(true);
        // btnStopRecord.setEnabled(false);
        btnStartPlay.setEnabled(false);
        btnStopPlay.setEnabled(false);
        btnAnalyze.setEnabled(false);

        btnStartRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                // for android M+
                if(checkPermissionFromDevice()){

                    pathSave = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ UUID.randomUUID()+"_audio_record.3gp";
                    setupMediaRecorder();
                    try{
                        mediaRecorder.prepare();
                        mediaRecorder.start();

                        handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                stopRecordingAudio();
                            }
                        }, AUDIO_DURATION);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    btnStartRecord.setEnabled(false);
                    // btnStopRecord.setEnabled(true);
                    btnStartPlay.setEnabled(false);
                    btnStopPlay.setEnabled(false);
                    btnAnalyze.setEnabled(false);

                    Toast.makeText(MainActivity.this, "Recording...", Toast.LENGTH_SHORT).show();

                } else {
                    requestPermission();
                }
            }
        });

//        btnStopRecord.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view){
//                stopRecordingAudio();
//            }
//        });

        btnStartPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                btnStartRecord.setEnabled(false);
                // btnStopRecord.setEnabled(false);
                btnStartPlay.setEnabled(false);
                btnStopPlay.setEnabled(true);
                btnAnalyze.setEnabled(false);

                mediaPlayer = new MediaPlayer();
                try{
                    mediaPlayer.setDataSource(pathSave);
                    mediaPlayer.prepare();

                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopPlayingAudio();
                        }
                    }, AUDIO_DURATION);
                } catch (IOException e){
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity.this, "Playing...", Toast.LENGTH_SHORT).show();
            }
        });

        btnStopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                stopPlayingAudio();
            }
        });

        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                 Toast.makeText(MainActivity.this, "API call", Toast.LENGTH_SHORT).show();
                // POST request to API

                File file = new File(pathSave);
//                uploadFile(API_url, file);

            }
        });
    }

//    public static Boolean uploadFile(String serverURL, File file) {
//        try {
//
//            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                    .addFormDataPart("file", file.getName(),
//                            RequestBody.create(MediaType.parse("text/csv"), file))
//                    .addFormDataPart("some-field", "some-value")
//                    .build();
//
//            Request request = new Request.Builder()
//                    .url(serverURL)
//                    .post(requestBody)
//                    .build();
//
//            client.newCall(request).enqueue(new Callback() {
//
//                @Override
//                public void onFailure(final Call call, final IOException e) {
//                    // Handle the error
//                }
//
//                @Override
//                public void onResponse(final Call call, final Response response) throws IOException {
//                    if (!response.isSuccessful()) {
//                        // Handle the error
//                    }
//                    // Upload successful
//                }
//            });
//
//            return true;
//        } catch (Exception ex) {
//            // Handle the error
//        }
//        return false;
//    }


//    public void sendPOSTRequest(String url, String authData, String attachmentFilePath, String outputFilePathName)
//    {
//        String charset = "UTF-8";
//        File binaryFile = new File(attachmentFilePath);
//        String boundary = "------------------------" + Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
//        String CRLF = "\r\n"; // Line separator required by multipart/form-data.
//        int    responseCode = 0;
//
//        try
//        {
//            //Set POST general headers along with the boundary string (the seperator string of each part)
//            URLConnection connection = new URL(url).openConnection();
//            connection.setDoOutput(true);
//            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//            connection.addRequestProperty("User-Agent", "CheckpaySrv/1.0.0");
//            connection.addRequestProperty("Accept", "*/*");
//            connection.addRequestProperty("Authentication", authData);
//
//
//            Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
//
//            OutputStream output = connection.getOutputStream();
//            PrintWriter writer  = new PrintWriter(new OutputStreamWriter(output, charset), true);
//
//
//            Toast.makeText(this, "2", Toast.LENGTH_SHORT).show();
//
//            // Send binary file - part
//            // Part header
//            writer.append("--" + boundary).append(CRLF);
//            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
//            writer.append("Content-Type: application/octet-stream").append(CRLF);// + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
//            writer.append(CRLF).flush();
//
//            // File data
//            Files.copy(binaryFile.toPath(), output);
//            output.flush();
//
//            // End of multipart/form-data.
//            writer.append(CRLF).append("--" + boundary + "--").flush();
//
//            responseCode = ((HttpURLConnection) connection).getResponseCode();
//
//            Toast.makeText(this, "val: "+responseCode, Toast.LENGTH_SHORT).show();
//
//            if(responseCode !=200) //We operate only on HTTP code 200
//                return;
//
//            InputStream Instream = ((HttpURLConnection) connection).getInputStream();
//
//            // Write PDF file
//            BufferedInputStream BISin = new BufferedInputStream(Instream);
//
//            int i;
//            while ((i = BISin.read()) != -1) {
//                Toast.makeText(this, "Value: "+i, Toast.LENGTH_SHORT).show();
//            }
//
//
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
//
//    }


    private void stopRecordingAudio(){
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        btnStartRecord.setEnabled(true);
        // btnStopRecord.setEnabled(false);
        btnStartPlay.setEnabled(true);
        btnStopPlay.setEnabled(false);
        btnAnalyze.setEnabled(true);

        handler.removeCallbacksAndMessages(null);
    }

    private void stopPlayingAudio(){
        btnStartRecord.setEnabled(true);
        // btnStopRecord.setEnabled(false);
        btnStartPlay.setEnabled(true);
        btnStopPlay.setEnabled(false);
        btnAnalyze.setEnabled(true);

        if(mediaPlayer != null) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
        }

        handler.removeCallbacksAndMessages(null);
    }

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSave);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_PERMISSION_CODE:
            {
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private boolean checkPermissionFromDevice(){
        int writeExternalStorageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int recordAudioResult = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return writeExternalStorageResult == PackageManager.PERMISSION_GRANTED && recordAudioResult == PackageManager.PERMISSION_GRANTED;
    }
}
