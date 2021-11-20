package com.sridatta.busyhunkproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity implements ChoosePhotoInputFragment.photoOptions {
    Toolbar toolbar;
    private Button logout, thats_it_button, edit_profile_button;
    TextView name,gender,location,email,phone_no, edit_gender;
    EditText edit_name, edit_email, edit_phonenum, otp;
    ImageButton name_tick, gender_tick, email_tick, phonenum_tick, otp_tick;
    NestedScrollView display_layout, edit_layout;
    CircleImageView profileImage, edit_profile_image;
    Button edit_profile_image_button;
    String GENDER;
    int REQUEST_CAMERA =10;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //intiating ui
        edit_profile_image_button = findViewById(R.id.edit_profile_disp);
        name=findViewById(R.id.textView34);
        gender=findViewById(R.id.textView41);
        location=findViewById(R.id.textView36);
        email=findViewById(R.id.email);
        phone_no=findViewById(R.id.textView43);
        profileImage = findViewById(R.id.circularImageView);
        logout=findViewById(R.id.logout);
        display_layout = findViewById(R.id.dsplay_layout);
        edit_layout = findViewById(R.id.edit_layout);
        edit_profile_button = findViewById(R.id.edit);
        thats_it_button = findViewById(R.id.thats_it);
        edit_name = findViewById(R.id.name_edittext);
        edit_gender = findViewById(R.id.edit_gender);
        edit_email = findViewById(R.id.edit_email);
        edit_phonenum = findViewById(R.id.phone_num_edit);
        otp = findViewById(R.id.otp);
        name_tick = findViewById(R.id.name_tick);
        gender_tick = findViewById(R.id.gender_tick);
        email_tick = findViewById(R.id.email_tick);
        phonenum_tick = findViewById(R.id.phone_num_tick);
        otp_tick = findViewById(R.id.otp_tick);
        edit_profile_image = findViewById(R.id.edit_profile_image);
        //toolbar
        toolbar=findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //logout onclick
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogOut();
            }
        });
        //profile image onclick
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ChoosePhotoInputFragment choosePhotoInputFragment = new ChoosePhotoInputFragment();
                choosePhotoInputFragment.show(ft, "fragment");
            }
        });
        //edit profileimage onclick
        edit_profile_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ChoosePhotoInputFragment choosePhotoInputFragment = new ChoosePhotoInputFragment();
                choosePhotoInputFragment.show(ft, "fragment");
            }
        });
        //edit profile onClick
        edit_profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
                display_layout.setVisibility(View.GONE);
                edit_layout.setVisibility(View.VISIBLE);
            }
        });
        //thats it onClick
        thats_it_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_layout.setVisibility(View.GONE);
                display_layout.setVisibility(View.VISIBLE);
            }
        });
        //get_n_set_details
        get_n_set_details();
    }

    private void LogOut(){
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt("IS_REGISTERED",0);
        editor.commit();
        Intent login_register_intent=new Intent(ProfileActivity.this,Login_Register.class);
        login_register_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(login_register_intent);
        finish();
    }

    void get_n_set_details(){
        //setting offline data
        setOfflineData();

        String UID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference profile_ref= FirebaseDatabase.getInstance().getReference().child("users/"+UID);
        profile_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    //name
                    name.setText(dataSnapshot.child("name").getValue().toString());
                    edit_name.setText(dataSnapshot.child("name").getValue().toString());
                    //gender
                    GENDER=dataSnapshot.child("gender").getValue().toString();
                    gender.setText(GENDER);
                    edit_gender.setText(GENDER);
                    Log.d("datta_text","gender is "+GENDER);
                    //location
                    location.setText(dataSnapshot.child("location").getValue().toString());
                    //phone num
                    phone_no.setText(dataSnapshot.child("phone_number").getValue().toString());
                    edit_phonenum.setText(dataSnapshot.child("phone_number").getValue().toString());
                    //image
                    String thumb_url=null;
                    try{
                        thumb_url=dataSnapshot.child("thumb_url").getValue().toString();
                    }
                    catch (Exception e){
                        Log.d("datta_text","profile_url error in profile_activity "+e.toString());
                        //setting image by gender
                        if(GENDER.equals(getResources().getString(R.string.male))){
                                profileImage.setImageDrawable(getResources().getDrawable(R.drawable.boy));
                                edit_profile_image.setImageDrawable(getResources().getDrawable(R.drawable.boy));
                        }
                    }
                    if(thumb_url!=null){
                        if(GENDER.equals(getResources().getString(R.string.male))){
                            Log.d("datta_text", thumb_url);
                            Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.boy).into(profileImage);
                            Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.boy).into(edit_profile_image);
                        }
                        else{
                            Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.girl).into(profileImage);
                            Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.girl).into(edit_profile_image);
                        }

                    }

                    //email
                    email.setText(dataSnapshot.child("email").getValue().toString());
                    edit_email.setText(dataSnapshot.child("email").getValue().toString());
                }
                catch (Exception e){
                    email.setText("NA");
                    Log.d("datta_text","error in retriving profile "+e.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode==REQUEST_CAMERA && resultCode==RESULT_OK){
            Uri imageUri=data.getData();
            //cropping image
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        //getting uri of cropped image and uploading file
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final Uri resultUri = result.getUri();
                File bitmapFile=new File(resultUri.getPath());
                try {
                    imageBitmap=new Compressor(this).
                            setMaxWidth(200).
                            setMaxHeight(200).
                            setQuality(90).
                            compressToBitmap(bitmapFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //uploading bitmap
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();
                if(isNetworkAvailable()) {
                    final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
                    final UploadTask uploadTask = storageReference.putBytes(thumb_byte);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                            Log.d("datta_text", "error in uploading image "+ exception.toString());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri thumb_url = uri;
                                    //uploading link to database
                                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("users/"+FirebaseAuth.getInstance().getCurrentUser().getUid().toString()+"/thumb_url");
                                    reference.setValue(thumb_url.toString());
                                    //changing offline url
                                    SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor editor=preferences.edit();
                                    editor.putString("image_url",thumb_url.toString());
                                    editor.commit();
                                    //picasso
                                    Picasso.with(ProfileActivity.this).load(thumb_url).into(profileImage);
                                }
                            });
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(),"Check Internet Connection",Toast.LENGTH_SHORT).show();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void setOfflineData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String gender = preferences.getString("gender", getResources().getString(R.string.male));
        String thumb_url = preferences.getString("image_url", null);
        //settig details
        name.setText(preferences.getString("name", getResources().getString(R.string.app_name)));
        location.setText(preferences.getString("location", "Kakinada"));
        if(thumb_url!=null) {
            if (gender.equals(getResources().getString(R.string.male))) {

                Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.boy).into(profileImage);
            } else {
                Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.girl).into(profileImage);
            }
        }
    }

    //internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void choosedOption(int num) {
        switch (num){
            case 0: Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, REQUEST_CAMERA);
                    break;
            case 1: Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(galleryIntent, null), REQUEST_CAMERA);
        }
    }
}
