package com.spanservices.correaleslie.facerecognition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Locale;

public class Event extends ActionBarActivity implements
        TextToSpeech.OnInitListener {
    TextToSpeech t1;
    String sp="Guest";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Intent intent = getIntent();
        String stringData="Guest";
        String KEY="person";

        t1=new TextToSpeech(this, this);

        if (null != intent) {
            stringData= intent.getStringExtra(KEY);

        }
        if(stringData==null){
            stringData="Guest";
        }
        String toSpeak ="Welcome "+stringData;
        sp=stringData;
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        TextView text1=(TextView) findViewById(R.id.textview1);
        text1.setText(""+stringData);
        speakOut();


        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(8000);//Come to MainActivity after some time
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent intent = new Intent(Event.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        };
        timerThread.start();

    }
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = t1.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {

                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }
    private void speakOut() {

        String text = "Welcome "+sp+" Have a good Day";
        t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }
    @Override
    public void onBackPressed() {


        startActivity(new Intent(this, MainActivity.class));



    }


}

