package com.sp.infosafe;

import android.app.ProgressDialog;
import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int GALLERY_REQUEST = 3;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        findViews();

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
                    registerUser(etEmail.getText().toString().trim(), etPassword.getText().toString().trim());
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

    private void registerUser(String email, String password) {
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
                            userInput.put("name", etName.getText().toString().trim());
                            userInput.put("age", etAge.getText().toString().trim());
                            userInput.put("bloodgroup", etBloodgroup.getText().toString().trim());
                            userInput.put("gender", gender);
                            userInput.put("phone", etPhone.getText().toString());
                            userInput.put("email", etEmail.getText().toString());
                            userInput.put("profile_pic", profilePicURL);

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
}
