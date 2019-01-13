package com.example.sai.imagelabelling;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sai.imagelabelling.Helper.InternetCheck;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    CameraView cameraView;
    Button btnDetect;
    AlertDialog waitalert;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.cameraview);
        btnDetect = findViewById(R.id.detectbtn);
        tv= findViewById(R.id.items);

        waitalert = new SpotsDialog.Builder().setMessage("Please wait...")
                .setContext(this)
                .setCancelable(false).build();

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitalert.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                cameraView.stop();
                runDetector(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.start();
                cameraView.captureImage();
                tv.setText(null);
            }
        });
    }

    private void runDetector(Bitmap bitmap) {
        final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(boolean internet) {

                if(internet){
                    FirebaseVisionCloudDetectorOptions options = new FirebaseVisionCloudDetectorOptions.Builder().setMaxResults(1).build();
                    FirebaseVisionCloudLabelDetector labelDetector = FirebaseVision.getInstance().getVisionCloudLabelDetector(options);
                    labelDetector.detectInImage(image)
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {

                                    processDataResultsCloud(firebaseVisionCloudLabels);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    if (waitalert.isShowing()){
                                        waitalert.dismiss();
                                    }
                                }
                            });
                }else{
                    FirebaseVisionLabelDetectorOptions options = new FirebaseVisionLabelDetectorOptions.Builder().setConfidenceThreshold(0.8f).build();
                    FirebaseVisionLabelDetector labelDetector = FirebaseVision.getInstance().getVisionLabelDetector(options);
                    labelDetector.detectInImage(image)
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                                    processDataResults(firebaseVisionLabels);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    if (waitalert.isShowing()){
                                        waitalert.dismiss();
                                    }
                                }
                            });

                }
            }
        });

    }

    private void processDataResultsCloud(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {
        for (FirebaseVisionCloudLabel label: firebaseVisionCloudLabels){
//            Toast.makeText(this, "Cloud results : "+label.getLabel(), Toast.LENGTH_SHORT).show();
            tv.setText("This is a : "+label.getLabel()+" with confidence : "+String.valueOf(label.getConfidence()));
        }
        if(waitalert.isShowing()){
            waitalert.dismiss();
        }
    }

    private void processDataResults(List<FirebaseVisionLabel> firebaseVisionLabels) {
        for (FirebaseVisionLabel label: firebaseVisionLabels){
//            Toast.makeText(this, "results : "+label.getLabel(), Toast.LENGTH_SHORT).show();
            tv.setText("This is a : "+label.getLabel()+" with confidence : "+String.valueOf(label.getConfidence()));

        }
        if(waitalert.isShowing()){
            waitalert.dismiss();
        }
    }

}



