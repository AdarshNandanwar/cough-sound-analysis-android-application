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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Declare variables
    Button btnStartRecord, btnStopRecord, btnStartPlay, btnStopPlay;
    String pathSave = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Handler handler;
    final int AUDIO_DURATION = 5000;
    final int REQUEST_PERMISSION_CODE = 1000;

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

        btnStartRecord.setEnabled(true);
        // btnStopRecord.setEnabled(false);
        btnStartPlay.setEnabled(false);
        btnStopPlay.setEnabled(false);

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

    }

    private void stopRecordingAudio(){
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        btnStartRecord.setEnabled(true);
        // btnStopRecord.setEnabled(false);
        btnStartPlay.setEnabled(true);
        btnStopPlay.setEnabled(false);

        handler.removeCallbacksAndMessages(null);
    }

    private void stopPlayingAudio(){
        btnStartRecord.setEnabled(true);
        // btnStopRecord.setEnabled(false);
        btnStartPlay.setEnabled(true);
        btnStopPlay.setEnabled(false);

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
