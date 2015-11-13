package com.spanservices.correaleslie.facerecognition;



/*
*     This project uses Microsoft Project Oxford Face API for face detection and identification.
*     It requires Face API Key, Refer https://www.projectoxford.ai/doc/general/subscription-key-mgmt
*     Create Person Group, Refer https://dev.projectoxford.ai/docs/services/54d85c1d5eefd00dc474a0ef/operations/54f0387249c3f70a50e79b84
*
* */

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Candidate;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.Person;
import com.spanservices.correaleslie.facerecognition.FormActivity;
import com.spanservices.correaleslie.facerecognition.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends ActionBarActivity implements Camera.PictureCallback, SurfaceHolder.Callback {
    public static final String EXTRA_CAMERA_DATA = "camera_data";
    public boolean myRandomInstanceVariable = false;
    public boolean cam = true;
    private AudioManager audio;

    private static final String KEY_IS_CAPTURING = "is_capturing";
    private Camera mCamera;

    private SurfaceView mCameraPreview;

    private byte[] mCameraData;
    private boolean mIsCapturing;
    private ProgressDialog detectionProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

        mCameraPreview = (SurfaceView) findViewById(R.id.preview_view);
        mCameraPreview.setVisibility(View.VISIBLE);
        final SurfaceHolder surfaceHolder = mCameraPreview.getHolder();
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mCamera.setDisplayOrientation(90);
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        ImageButton button1=(ImageButton) findViewById(R.id.imagebutton1);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraPreview.setVisibility(View.INVISIBLE);

                captureImage();

            }

        });
        mIsCapturing = true;
        detectionProgressDialog = new ProgressDialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public void onBackPressed() {



        this.finishAffinity();
        System.exit(0);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void captureImage() {
        mCamera.takePicture(null, null, this);
    }
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(KEY_IS_CAPTURING, mIsCapturing);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mIsCapturing = savedInstanceState.getBoolean(KEY_IS_CAPTURING, mCameraData == null);
        if (mCameraData != null) {
            setupImageDisplay();
        } else {
            setupImageCapture();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera == null) {
            try {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(mCameraPreview.getHolder());
                if (mIsCapturing) {
                    mCamera.startPreview();
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Unable to open camera.", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mCameraData = data;
        setupImageDisplay();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
                if (mIsCapturing) {
                    mCamera.startPreview();
                }
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Unable to start camera preview.", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
    private void setupImageCapture() {

        mCameraPreview.setVisibility(View.VISIBLE);
        mCamera.startPreview();

        Bitmap bitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData.length);
    }
    private void setupImageDisplay() {

        Bitmap bitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap .getWidth(), bitmap .getHeight(), matrix, true);


        String path=saveToInternalSorage(rotatedBitmap);
        Log.e("Path","Path is= "+path);
        Log.e("Bitdata",""+mCameraData.toString());
        detectAndFrame(rotatedBitmap);
    }
    private String saveToInternalSorage(Bitmap bitmapImage){


        ContextWrapper cw = new ContextWrapper(getApplicationContext());

        final String path = Environment.DIRECTORY_DOWNLOADS;

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        String formattedDate = sdf.format(date);
        String result = formattedDate.replaceAll("[-+.^:/,]", "");
        String result2 = result.replaceAll(" ","");
        File mypath=new File(path,result2+"profile.jpg");
        Log.e("mypath","mypath= "+mypath);

        FileOutputStream fos = null;
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmapImage, result2+"profile.jpg" , "profile");

        try {

            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            //bitmapImage.
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), mypath.getAbsolutePath(), mypath.getName(), mypath.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mypath.getAbsolutePath();
    }
    private FaceServiceClient faceServiceClient =
            new FaceServiceClient("API_Key");//Insert API Key here
    private void detectAndFrame(final Bitmap imageBitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0], false, false, false, false);
                            Log.e("result","result "+result.length);
                            if (result == null)
                            {
                                publishProgress("Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(
                                    String.format("Detection Finished. %d face(s) detected",
                                            result.length));
                            return result;
                        } catch (Exception e) {
                            publishProgress("Detection failed");
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
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();
                        if (result == null){

                            return;
                        }

                        Detectface(result, imageBitmap);
                    }
                };
        detectTask.execute(inputStream);
    }
    private void Detectface(Face[] faces,Bitmap imageBitmap){
        Context context=getApplicationContext();
        Log.e("Here", "Detectface");
        int i=faces.length;
        Log.e("Here", "Detectface " + i);
        if (i != 0) {
            for (Face face : faces) {

                UUID mn = face.faceId;
                Log.e("faceId", "faceId " + mn);
                identify2(mn, imageBitmap,faces);
            }

        }else{
            Toast.makeText(getApplicationContext(), "No face Detected",
                    Toast.LENGTH_LONG).show();
            Thread timerThread = new Thread(){
                public void run(){
                    try{
                        sleep(400);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }finally{
                        Intent intent = new Intent(MainActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                }
            };
            timerThread.start();
            Log.e("Here","Detectface else");


        }

    }
    public void identify2(UUID facepresent, final Bitmap imageBitmap,final Face[] faces){
        Log.e("Inside identify2", "Inside identify2");



        final String Persongroupid="Persongroupid";//Insert Persongroupid here
        final int maxNumofCandidatesReturned=2;
        final UUID[] Ids = new UUID[faces.length];

        for(int i=0;i<faces.length;i++) {
            Face k = faces[i];
            Ids[i]=k.faceId;
        }


        AsyncTask<Void, String, IdentifyResult[]> identify2Task =
                new AsyncTask<Void, String, IdentifyResult[]>() {
                    protected IdentifyResult[] doInBackground(Void... Params) {
                        try{
                            publishProgress("Identifying...");

                            IdentifyResult[] result = faceServiceClient.identity(Persongroupid, Ids, maxNumofCandidatesReturned);
                            if (result == null)
                            {
                                publishProgress("Identifying Finished. Nothing Found");
                                return null;
                            }
                            publishProgress(
                                    String.format("Identifying Finished."
                                    ));
                            return result;



                        }catch (Exception e) {

                            publishProgress("Identify failed");
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
                    protected void onPostExecute(IdentifyResult[] result) {
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();
                        if (result == null) return;


                        getPerson(result, imageBitmap, faces);
                    }

                };
        identify2Task.execute();


    }
    public  void getPerson(IdentifyResult[] res,Bitmap originalBitmap, Face[] faces){
        if (faces != null) {
            Face k= faces[0];
            IdentifyResult res2=res[0];
            int count=res2.candidates.size();
            if(count!=0) {
                Log.e("if","if");
                Candidate value = res2.candidates.get(0);
                String personid = value.personId.toString();
                getname(personid);

            }else{

                Log.e("else","else");
                UUID id=k.faceId;
                String KEY="Id";
                String name=id.toString();

                Intent intent = new Intent(MainActivity.this,FormActivity.class);
                intent.putExtra(KEY, name);
                startActivity(intent);

            }

        }else{

            Log.e("else2","else2");
            Intent intent = new Intent(MainActivity.this,FormActivity.class);
            startActivity(intent);

        }


    }
    public  void getname(final String perid) {
        Log.e("getname","getname");
        final String groupId = "Persongroupid";//Insert Persongroupid here
        final UUID personId = UUID.fromString(perid);
        AsyncTask<Void, String, Person> getnameTask =
                new AsyncTask<Void, String, Person>() {
                    protected Person doInBackground(Void... Params) {
                        try {
                            publishProgress("Fetching names");

                            Person result = faceServiceClient.getPerson(groupId, personId);
                            if (result == null) {
                                publishProgress("Names not Found");
                                return null;
                            }
                            publishProgress(
                                    String.format("Fetching names Finished."
                                    ));
                            return result;


                        } catch (Exception e) {

                            publishProgress("Fetching names failed");
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
                    protected void onPostExecute(Person result) {
                        Log.e("onPostExecute","onPostExecute");
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();
                        if (result == null) return;
                        showpersonName(result);


                    }


                };
        getnameTask.execute();
    }
    public  void showpersonName(Person res){
        Log.e("showpersonName","showpersonName");
        String name=res.name;

        Intent intent = new Intent(MainActivity.this, Event.class);
        String KEY="person";
        intent.putExtra(KEY, name);
        startActivity(intent);
    }


}

