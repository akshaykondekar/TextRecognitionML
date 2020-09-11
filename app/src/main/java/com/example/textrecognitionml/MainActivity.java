package com.example.textrecognitionml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView tvRecognizedText;
    public static final int REQUEST_CODE_CAMERA = 100;
    public static final int REQUEST_CODE_GALLERY = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        tvRecognizedText = findViewById(R.id.tvRecognizedText);
    }

    public void captureImage(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null ){
            startActivityForResult(intent,REQUEST_CODE_CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CAMERA) {
            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
            imageView.setImageBitmap(bitmap);
            tvRecognizedText.setText(R.string.default_text);

            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVision firebaseVision = FirebaseVision.getInstance();
            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();

            Task<FirebaseVisionText> textTask = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);

            textTask.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    tvRecognizedText.setText(firebaseVisionText.getText());
                }
            });

            textTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    tvRecognizedText.setText(R.string.task_fail_text);
                }
            });
        }
        else if (requestCode == REQUEST_CODE_GALLERY) {
            imageView.setImageURI(data.getData());
            tvRecognizedText.setText(R.string.loading_text);
            FirebaseVisionImage firebaseVisionImage = null;

            try {
                firebaseVisionImage = FirebaseVisionImage
                        .fromFilePath(getApplicationContext(), data.getData());
            } catch (Exception e) {
                tvRecognizedText.setText(R.string.error_text);
            }

            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer
                    = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

            firebaseVisionTextRecognizer.processImage(firebaseVisionImage)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            tvRecognizedText.setText(firebaseVisionText.getText());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            tvRecognizedText.setText(R.string.task_fail_text);
                        }
                    });
        }
    }

    public void galleryImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if(intent.resolveActivity(getPackageManager()) != null ){
            startActivityForResult(intent,REQUEST_CODE_GALLERY);
        }
    }
}