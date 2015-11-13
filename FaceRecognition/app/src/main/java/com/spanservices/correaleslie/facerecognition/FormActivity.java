package com.spanservices.correaleslie.facerecognition;

/*
*     This project uses Microsoft Project Oxford Face API for face detection and identification.
*     It requires Face API Key, Refer https://www.projectoxford.ai/doc/general/subscription-key-mgmt
*     Create Person Group, Refer https://dev.projectoxford.ai/docs/services/54d85c1d5eefd00dc474a0ef/operations/54f0387249c3f70a50e79b84
*
* */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.microsoft.projectoxford.face.contract.TrainingStatus;

import java.util.Locale;
import java.util.UUID;

public class FormActivity extends AppCompatActivity implements
        TextToSpeech.OnInitListener {
    private ProgressDialog detectionProgressDialog;
    TextToSpeech t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        Intent intent = getIntent();
        t1=new TextToSpeech(this, this);

        String stringData="Guest";
        String KEY="Id";
        if (null != intent) {
            stringData= intent.getStringExtra(KEY);


        }
        final UUID faceId=UUID.fromString(stringData);
        Log.e("id","id is = "+faceId);

        Button buttonsubmit=(Button) findViewById(R.id.buttonsubmit);
        Button buttoncancel=(Button) findViewById(R.id.buttoncancel);
        speakOut();


        buttonsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText name=(EditText) findViewById(R.id.name);
                EditText companyname=(EditText) findViewById(R.id.companyname);
                EditText designation=(EditText) findViewById(R.id.designation);
                final String Name=name.getText().toString();
                final String Companyname=companyname.getText().toString();
                final String Designation=designation.getText().toString();
                int l=Name.length();
                if(l !=0){
                    createperson(faceId, Name, Companyname, Designation);

                }
                else {
                    Toast.makeText(getApplicationContext(), "Enter your name",
                            Toast.LENGTH_LONG).show();

                }

            }

        });
        buttoncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FormActivity.this,MainActivity.class);

                startActivity(intent);

            }

        });
        detectionProgressDialog = new ProgressDialog(this);
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

        String text = "Sorry, We Dont have you in our records. Please register";
        t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
    @Override
    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }
    private FaceServiceClient faceServiceClient =
            new FaceServiceClient("API_Key");//Insert API Key

    public void createperson(UUID faceId,String Namee,String Companynamee,String Designation){
        Log.e("Inside createperson", "Inside createperson");
        Log.e("Namee","Namee "+Namee);
        final String groupId="Persongroupid";//Insert Persongroupid here
        final String Name=Namee;
        final String UserData=Designation+";"+Companynamee+";";

        final UUID[] Ids = new UUID[1];
        Ids[0] = faceId;

        AsyncTask<Void, String, CreatePersonResult> createpersonTask =
                new AsyncTask<Void, String, CreatePersonResult>() {
                    protected CreatePersonResult doInBackground(Void... Params) {
                        try{
                            publishProgress("CreatingPerson...");
                            CreatePersonResult result = faceServiceClient.createPerson(groupId, Ids, Name, UserData);
                            if (result == null)
                            {
                                publishProgress("CreatingPerson Finished. Nothing Found");
                                return null;
                            }
                            publishProgress(
                                    String.format("CreatingPerson Finished."
                                    ));
                            return result;

                        }catch (Exception e) {

                            publishProgress("createperson failed");
                            return null;

                        }

                    }
                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        detectionProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        detectionProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(CreatePersonResult result) {
                        Log.e("onPostExecute","onPostExecute"+result.personId);
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();
                        if (result == null) return;

                        displayPersonResult(result);
                    }

                };
        createpersonTask.execute();
    }
    public void displayPersonResult(CreatePersonResult res)
    {
        Log.e("displayPersonResult", "displayPersonResult" );
        UUID PersonId=res.personId;
        Log.e("PersonId is", "PersonId is " + PersonId);
        train();

    }
    @Override
    public void onBackPressed() {





        startActivity(new Intent(this, MainActivity.class));
    }
    public void train(){
        Log.e("Inside Traning", "Inside Traning");
        final String PersonGroupId="Persongroupid";//Insert Persongroupid here
        AsyncTask<Void, String, TrainingStatus> trainTask =
                new AsyncTask<Void, String, TrainingStatus>() {
                    protected TrainingStatus doInBackground(Void... Params) {
                        try {
                            publishProgress("Traning...");
                            TrainingStatus result = faceServiceClient.trainPersonGroup(PersonGroupId);
                            if (result == null)
                            {
                                publishProgress("Traning Finished. Nothing Found");
                                return null;
                            }
                            publishProgress(
                                    String.format("Traning Finished."
                                    ));
                            return result;

                        }catch (Exception e) {
                            publishProgress("Traning failed");
                            return null;
                        }

                    }
                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        detectionProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        detectionProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(TrainingStatus result) {
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();
                        if (result == null) return;

                        displayTrainResult(result);
                    }

                };
        trainTask.execute();
    }
    public void displayTrainResult(TrainingStatus res){
        Log.e("In displayTrainResult", "In displayTrainResult");
        if (res != null) {
            String Id=res.id;
            String Status=res.status;
            String StartTime=res.startTime;
            String EndTime=res.endTime;
            Log.e("displayTrainResult", "Is is "+Id+" Traning Status "+Status+" StartTime "+StartTime+" EndTime "+EndTime);
            Intent intent = new Intent(FormActivity.this,MainActivity.class);
            startActivity(intent);
        }

    }
}

