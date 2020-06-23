package com.example.coughsoundanalysisandroidapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.UUID;
import java.io.IOException;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    // Declare variables
    Button btnStartRecord, btnStopRecord, btnStartPlay, btnStopPlay, btnAnalyze;
    String pathSave = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Handler handler;
    final int AUDIO_DURATION = 5000;
    final int REQUEST_PERMISSION_CODE = 1000;;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request runtime permission
        if(!checkPermissionFromDevice())
            requestPermission();

        resultText = (TextView)findViewById(R.id.resultText);

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

                File file = new File(pathSave);
                resultText.setText("Processing...");

                // #######################################

                // create upload service client
                FileUploadService service =
                        ServiceGenerator.createService(FileUploadService.class);

                // create RequestBody instance from file
                RequestBody requestFile =
                        RequestBody.create(
                                MediaType.parse("media/type"),
                                file
                        );

                // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                // finally, execute the request
                Call<ResponseBody> call = service.upload(body);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {
                        Log.v("Upload", "success");
                        Log.v("Response", response.toString());
                        if(response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject(response.body().string());
                                String prediction = json.getJSONObject("data").getString("prediction");
                                String message = json.getString("message");
                                if(prediction == "null")
                                    resultText.setText("Cough not detected");
                                else
                                    resultText.setText(prediction);
                                Log.v("StatusCode", json.getString("status"));
                                Log.e("Error", message);
                                Log.v("Prediction", prediction);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("Upload error:", t.getMessage());
                    }
                });

                // #######################################

            }
        });
    }

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
                Manifest.permission.READ_EXTERNAL_STORAGE,
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
        int readExternalStorageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int recordAudioResult = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return writeExternalStorageResult == PackageManager.PERMISSION_GRANTED && readExternalStorageResult == PackageManager.PERMISSION_GRANTED && recordAudioResult == PackageManager.PERMISSION_GRANTED;
    }
}
