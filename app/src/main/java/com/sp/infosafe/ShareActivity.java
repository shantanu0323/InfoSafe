package com.sp.infosafe;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Random;

public class ShareActivity extends AppCompatActivity {

    private static final String TAG = "ShareActivity";
    private static final String ORIGINAL_KEY = "originalKey";

    private ImageButton bClose;
    private Button bGenerateOTP;
    private ImageView ivQRCode;
    private int originalKey;
    private int encryptedKey;
    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        findViews();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        originalKey = sharedpreferences.getInt(ORIGINAL_KEY, -1);
        Log.i(TAG, "onCreate: ORIGINAL KEY = " + originalKey);
//        Toast.makeText(getApplicationContext(), "Original Key : " + originalKey, Toast.LENGTH_SHORT).show();
        int n = originalKey + generateRandom(30);
        Log.i(TAG, "onCreate:            n = " + n);
        final int indexOfKey = 1 + generateRandom(4);
        Log.i(TAG, "onCreate:   indexOfKey = " + indexOfKey);
        final int keyToEncrypt = generateKeyToEncrypt(n);
        Log.i(TAG, "onCreate: keyToEncrypt = " + keyToEncrypt);
        encryptedKey = (originalKey * keyToEncrypt) % n;
        Log.i(TAG, "onCreate: encryptedKey = " + encryptedKey);

        bClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });

        bGenerateOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder OTP = new StringBuilder();
                int k = 0;
                while (k < 6) {
                    if (k == indexOfKey) {
                        OTP.append(keyToEncrypt / 10);
                        k++;
                        OTP.append(keyToEncrypt % 10);
                        k++;
                    } else {
                        OTP.append(generateRandom(10));
                        k++;
                    }
                }
                String OTPvalue = OTP.toString();
//                Toast.makeText(getApplicationContext(),OTPvalue,Toast.LENGTH_LONG).show();
                Log.i(TAG, "onClick: OTP = " + OTPvalue);
                publishOTP(OTPvalue);
            }
        });

        String content = "{'user_id' : '" + getIntent().getStringExtra("currentUserId") + "'," +
                "'encrypted_key': '" + encryptedKey + "'," +
                "'n': '" + n + "" + indexOfKey + "'}";

        generateQRCode(content);
    }

    private void publishOTP(String OTPvalue) {
        bGenerateOTP.setVisibility(View.INVISIBLE);
        ((LinearLayout) findViewById(R.id.OTPcontainer)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.otp0)).setText("" + OTPvalue.charAt(0));
        ((TextView) findViewById(R.id.otp1)).setText("" + OTPvalue.charAt(1));
        ((TextView) findViewById(R.id.otp2)).setText("" + OTPvalue.charAt(2));
        ((TextView) findViewById(R.id.otp3)).setText("" + OTPvalue.charAt(3));
        ((TextView) findViewById(R.id.otp4)).setText("" + OTPvalue.charAt(4));
        ((TextView) findViewById(R.id.otp5)).setText("" + OTPvalue.charAt(5));
    }

    private int generateKeyToEncrypt(int n) {
        int i;
        for (i = n/2; i <n; i++) {
            if ((n % i) != 0) {
                break;
            }
        }
        return i;
    }

    private void generateQRCode(String content) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(content,
                    BarcodeFormat.QR_CODE, 400, 400);
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
        bGenerateOTP = findViewById(R.id.bGenerateOTP);
    }

    public static int generateRandom(int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt(max);
        return randomNum;
    }

}
