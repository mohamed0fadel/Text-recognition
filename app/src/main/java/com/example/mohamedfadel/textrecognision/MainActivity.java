package com.example.mohamedfadel.textrecognision;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private ImageView mImageView;
    private Button mSnap, mDetect;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap thumbnail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.textView);
        mImageView = findViewById(R.id.imageView);
        mSnap = findViewById(R.id.button);
        mDetect = findViewById(R.id.button2);

        mSnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeImage();
            }
        });

        mDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (thumbnail != null)
                detectText();
            }
        });

    }


    private void takeImage(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE){
            if (resultCode == RESULT_OK){
                thumbnail = data.getParcelableExtra("data");
                mImageView.setImageBitmap(thumbnail);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void detectText(){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(thumbnail);
        FirebaseVisionTextRecognizer detctor = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detctor.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processText(firebaseVisionText);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d("wmfadel", e.getMessage());
            }
        });
    }

    private void processText(FirebaseVisionText firebaseVisionText){
        mTextView.setText("");
        List<FirebaseVisionText.TextBlock> blocs = firebaseVisionText.getTextBlocks();
        if (blocs.size() == 0){
            mTextView.setText("No text found");
            return;
        }
        for(FirebaseVisionText.TextBlock bloc : blocs){
            List<FirebaseVisionText.Line> lines = bloc.getLines();
            for (FirebaseVisionText.Line line : lines){
                List<FirebaseVisionText.Element> elements = line.getElements();
                for(FirebaseVisionText.Element element : elements){
                    mTextView.append(element.getText() + " ");
                }
            }
        }
    }
}
