package com.sp.infosafe;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ShareActivity extends AppCompatActivity {

    private static final String TAG = "ShareActivity";

    private ImageButton bClose;
    private ImageView ivQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        findViews();
        bClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });

        String content = "What is Lorem Ipsum? Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the ..";
        generateQRCode(content);
    }

    private void generateQRCode(String content) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(content,
                    BarcodeFormat.QR_CODE,400,400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ivQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e(TAG, "generateQRCode: ", e);
        }
    }

    private void findViews() {
        bClose = findViewById(R.id.bClose);
        ivQRCode = findViewById(R.id.ivQRCode);
    }
}
