package com.sp.infosafe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int GALLERY_REQUEST = 3;
    private static final String ORIGINAL_KEY = "originalKey";

    private EditText etName, etAge, etBloodgroup, etPhone, etEmail, etPassword;
    private RadioGroup rgGender;
    private Button bRegister;
    private ImageButton bProfilePic, bLogin;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private String profilePicURL = "https://firebasestorage.googleapis.com/v0/b/infosafe-2ef9b.appspot.com/o/profile_pics%2Fdefault-avatar.png?alt=media&token=6fc6e959-0f09-4f04-9612-dc782cb55c7c";
    private String gender = "male";
    private Uri profilePicURI = null;
    private StorageReference storageProfilePics;

    private char[] characters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            ' ', '@', '.', '+', '-',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;
    private int originalKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        findViews();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        rgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.rbFemale) {
                    gender = "female";
                } else {
                    gender = "male";
                }
            }
        });

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dataValid()) {
                    User user = new User(
                            etName.getText().toString().trim(),
                            etAge.getText().toString().trim(),
                            etBloodgroup.getText().toString().trim(),
                            gender,
                            etPhone.getText().toString(),
                            etEmail.getText().toString(),
                            profilePicURL
                    );
                    Log.i(TAG, "onClick: user.name = " + user.getName());
                    originalKey = generateKey(characters.length);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt(ORIGINAL_KEY, originalKey);
                    editor.apply();

                    registerUser(encryptedUser(user, originalKey),
                            etEmail.getText().toString(),
                            etPassword.getText().toString());
                }
            }
        });

        bProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickUserImage();
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
    }

    private User encryptedUser(User user, int key) {

        User encryptedUser = new User();
        encryptedUser.setName(encryptText(user.getName(), key));
        encryptedUser.setAge(encryptText(user.getAge(), key));
        encryptedUser.setBloodgroup(encryptText(user.getBloodgroup(), key));
        encryptedUser.setGender(encryptText(user.getGender(), key));
        encryptedUser.setPhone(encryptText(user.getPhone(), key));
        encryptedUser.setEmail(encryptText(user.getEmail(), key));
        encryptedUser.setProfile_pic(user.getProfile_pic());
        Log.i(TAG, "encryptedUser: encryptedUser.name = " + encryptedUser.getName());
        return encryptedUser;
    }

    private String encryptText(String text, int key) {
        Log.i(TAG, "encryptText: plaintext = " + text + " : key = " + key);
        StringBuilder cipherText = new StringBuilder();
        int p;
        int c;
        for (int i = 0; i < text.length(); i++) {
            p = getIndex(text.charAt(i));
            c = (p * key) % characters.length;
            cipherText.append(characters[c]);
        }
        Log.i(TAG, "encryptText: ciphertext = " + cipherText.toString());
        return cipherText.toString();
    }

    private int getIndex(char c) {
        int i;
        for (i = 0; i < characters.length; i++) {
            if (c == characters[i]) {
                break;
            }
        }
        return i;
    }

    private void pickUserImage() {
        Log.e(TAG, "pickUserImage: FUNCTION STARTED");
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, GALLERY_REQUEST);
    }

    private boolean dataValid() {
        if (TextUtils.isEmpty(etName.getText().toString().trim()) ||
                TextUtils.isEmpty(etAge.getText().toString().trim()) ||
                TextUtils.isEmpty(etBloodgroup.getText().toString().trim()) ||
                TextUtils.isEmpty(etPhone.getText().toString().trim()) ||
                TextUtils.isEmpty(etEmail.getText().toString().trim()) ||
                TextUtils.isEmpty(etPassword.getText().toString().trim())) {
            Snackbar.make(getCurrentFocus(),
                    Html.fromHtml("<font color=\"#ee0000\">Please enter the details correctly...</font>"),
                    Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void registerUser(final User user, String email, String password) {
        progressDialog.setTitle("Signing you in...");
        progressDialog.setMessage("Please give us a second while we include you to our family");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            final String currentUserId = currentUser.getUid();
                            HashMap<String, String> userInput = new HashMap<String, String>();
                            userInput.put("name", user.getName());
                            userInput.put("age", user.getAge());
                            userInput.put("bloodgroup", user.getBloodgroup());
                            userInput.put("gender", user.getGender());
                            userInput.put("phone", user.getPhone());
                            userInput.put("email", user.getEmail());
                            userInput.put("profile_pic", user.getProfile_pic());

                            DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
                            usersDatabase.child(currentUser.getUid()).setValue(userInput)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
//                                            uploadImage(currentUserId);
                                            progressDialog.dismiss();
                                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            finish();
                                        }
                                    });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void uploadImage(final String currentUserId) {
        Log.e(TAG, "uploadImage: FUNCTION STARTED");
        progressDialog.show();
        if (profilePicURI != null) {
            Log.e(TAG, "uploadImage: URI NOT NULL");
            storageProfilePics = FirebaseStorage.getInstance().getReference();
            StorageReference filePath = storageProfilePics.child("ProfilePics")
                    .child(currentUserId);
            filePath.putFile(profilePicURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadURL = taskSnapshot.getDownloadUrl();
                    DatabaseReference userProfile_pic = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(currentUserId).child("profile_pic");
                    userProfile_pic.setValue(downloadURL);
                    Log.i(TAG, "onSuccess: Image Added ... : " + downloadURL);
                    progressDialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to finish setup due to : " +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: FAILED TO FINISH SETUP");
                    progressDialog.dismiss();
                }
            });
        } else {
            Log.e(TAG, "uploadImage: URI NULL");
            progressDialog.dismiss();
        }
    }

    private void findViews() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etBloodgroup = findViewById(R.id.etBloodgroup);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        rgGender = findViewById(R.id.rgGender);
        bRegister = findViewById(R.id.bRegister);
        bProfilePic = findViewById(R.id.bProfilePic);
        bLogin = findViewById(R.id.bLogin);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            profilePicURI = data.getData();
            bProfilePic.setImageURI(profilePicURI);
        }
    }

    public static int generateKey(int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt(max);
        Log.i(TAG, "generateKey: " + randomNum);
        return randomNum;
    }
}
