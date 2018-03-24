package com.sp.infosafe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private TextView tvName;
    private CircleImageView ivProfilePic;
    private ImageButton bLogout;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabase;
    private String currentUserId = null;
    private String profilePicURL = "https://firebasestorage.googleapis.com/v0/b/infosafe-2ef9b.appspot.com/o/profile_pics%2Fdefault-avatar.png?alt=media&token=6fc6e959-0f09-4f04-9612-dc782cb55c7c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        findViews();

        progressDialog.setTitle("Loading Profile...");
        progressDialog.setMessage("Please wait while we set up your profile");
        progressDialog.show();
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentUserId)) {
                    User userDetails = dataSnapshot.child(currentUserId).getValue(User.class);
                    tvName.setText(userDetails.getName());
                    if (!profilePicURL.equals(userDetails.getProfile_pic())) {
                        profilePicURL = userDetails.getProfile_pic();
                        Picasso.with(getApplicationContext()).load(profilePicURL).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(getResources().getDrawable(R.drawable.ic_profile_pic)).into(ivProfilePic,
                                new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        progressDialog.dismiss();
                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getApplicationContext()).load(profilePicURL).into(ivProfilePic);
                                        progressDialog.dismiss();
                                    }
                                });
                    } else {
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });

        bLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

    }

    private void findViews() {
        tvName = findViewById(R.id.tvName);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        bLogout = findViewById(R.id.bLogout);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        } else {
            currentUserId = mAuth.getCurrentUser().getUid();
        }
//        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
    }
}
