package com.example.jobfinder.Employer;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.jobfinder.Chat.ChatActivity;
import com.example.jobfinder.Employee.PreviewEmployeeProfileActivity;
import com.example.jobfinder.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PreviewJobProfileActivity extends AppCompatActivity {
    private static final String LOGTAG = "UserRole";
    final static int PICK_PDF_CODE = 2342;

    private EditText mDescriptionField, mWebsiteField, mContactField, mLinkedInField, mFacebookField, mPhoneField;
    private TextView mCompanyNameField, mTitleField, mCategoryField, mCountryField, mCityField;

    private Button mPreviewDetailsBtn;

    private ImageView mJobImage, mEmployerImage, mFacebookIcon, mLinkedinIcon, mWebsiteIcon, mBackArrowBtnIV;


    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userId, userRole, currentUId, jobId, employerId;
    private String title, category, description, country, city, contact, phone, jobWebsiteUrl, jobDescriptionUrl, jobImageUrl, profileImageUrl;
    private String name, facebook, linkedIn;

    private Uri resultImageUri, resultFileUri;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_job_profile);

        jobId = getIntent().getExtras().getString("jobId");
        employerId = getIntent().getExtras().getString("employerId");
        Log.i(LOGTAG, jobId);

        mTitleField = (TextView) findViewById(R.id.jobTitle_textview);
        mCategoryField = (TextView) findViewById(R.id.jobCategory_textview);
        mCountryField = (TextView) findViewById(R.id.country_textview);
        mCityField = (TextView) findViewById(R.id.city_textview);
        mCompanyNameField = (TextView) findViewById(R.id.companyname_textview);

        mDescriptionField = (EditText) findViewById(R.id.jobDescription);
        mContactField = (EditText) findViewById(R.id.contact);
        mPhoneField = (EditText) findViewById(R.id.phone);

        mFacebookIcon = (ImageView) findViewById(R.id.facebook_icon_imageview);
        mLinkedinIcon = (ImageView) findViewById(R.id.linkedin_icon_imageview);
        mWebsiteIcon = (ImageView) findViewById(R.id.website_icon_imageview);

        mJobImage = (ImageView) findViewById(R.id.jobImage);
        mEmployerImage = (ImageView) findViewById(R.id.employerImage);
        mBackArrowBtnIV = (ImageView) findViewById(R.id.backArrow_imageview);

        mPreviewDetailsBtn = (Button)  findViewById(R.id.details);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId);
        Log.i(LOGTAG, userId);

        if (jobId != null && mUserDatabase!= null){
            getJobInfo();
            getUserInfo();
        }

        mPreviewDetailsBtn.setEnabled(false);
        hideEditTextKeypadOnFocusChange();

        mPreviewDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jobDescriptionUrl != null){
                    Intent intent = new Intent(Intent.ACTION_QUICK_VIEW);
                    intent.setData(Uri.parse(jobDescriptionUrl));
                    startActivity(intent);
                }
            }
        });

        mFacebookIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(facebook != null){
                    if (!facebook.startsWith("http://") && !facebook.startsWith("https://"))
                        facebook = "http://" + facebook;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebook));
                    startActivity(browserIntent);
                    return;
                }else{
                    Toast.makeText(PreviewJobProfileActivity.this, "No Facebook Profile Linked!", Toast.LENGTH_LONG).show();
                }

            }
        });
        mLinkedinIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(linkedIn != null){
                    if (!linkedIn.startsWith("http://") && !linkedIn.startsWith("https://"))
                        linkedIn = "http://" + linkedIn;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedIn));
                    startActivity(browserIntent);
                    return;
                }else{
                    Toast.makeText(PreviewJobProfileActivity.this, "No LinkedIn Profile Linked!", Toast.LENGTH_LONG).show();
                }


            }
        });
        mWebsiteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(jobWebsiteUrl != null){
                    if (!jobWebsiteUrl.startsWith("http://") && !jobWebsiteUrl.startsWith("https://"))
                        jobWebsiteUrl = "http://" + jobWebsiteUrl;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(jobWebsiteUrl));
                    startActivity(browserIntent);
                    return;
                }else{
                    Toast.makeText(PreviewJobProfileActivity.this, "No Website Linked!", Toast.LENGTH_LONG).show();
                }

            }
        });

        mEmployerImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PreviewJobProfileActivity.this, PreviewEmployerProfileActivity.class);
                intent.putExtra("employerId", employerId);
                startActivity(intent);
                return;
            }
        });
        mBackArrowBtnIV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
        /*mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });*/
    }

    private void getJobInfo() {
        DatabaseReference jobDb = mUserDatabase.child("jobs").child(jobId);
        jobDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("title")!=null){
                        title = map.get("title").toString();
                        mTitleField.setText(title);
                    }
                    if(map.get("category")!=null){
                        category = map.get("category").toString();
                        mCategoryField.setText(category);
                    }
                    if(map.get("description")!=null){
                        description = map.get("description").toString();
                        mDescriptionField.setText(description);
                    }
                    if(map.get("country")!=null){
                        country = map.get("country").toString();
                        if(country.length()>1){
                            mCountryField.setText(country+", ");
                        }
                        else{
                            mCountryField.setText(null);
                        }

                    }
                    if(map.get("city")!=null){
                        city = map.get("city").toString();
                        mCityField.setText(city);
                    }
                    if(map.get("contact")!=null){
                        contact = map.get("contact").toString();
                        mContactField.setText(contact);
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("jobWebsiteUrl")!=null){
                        jobWebsiteUrl = map.get("jobWebsiteUrl").toString();
                        //mJobWebsiteUrlField.setText(jobWebsiteUrl);
                    }
                    if(map.get("jobDescriptionUrl") != null){
                        mPreviewDetailsBtn.setEnabled(true);
                        jobDescriptionUrl = map.get("jobDescriptionUrl").toString();
                    }
                    Glide.clear(mJobImage);

                    if(map.get("jobImageUrl")!=null){
                        jobImageUrl = map.get("jobImageUrl").toString();
                        Glide.with(getApplication()).load(jobImageUrl).into(mJobImage);
                        switch(jobImageUrl){
                            case "default":
                                Glide.with(getApplication()).load(R.mipmap.ic_launcher).into(mJobImage);
                                break;
                            default:
                                Glide.with(getApplication()).load(jobImageUrl).into(mJobImage);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        name = map.get("name").toString();
                        mCompanyNameField.setText(name);
                    }
                    if(map.get("facebook")!=null){
                        facebook = map.get("facebook").toString();
                        //mFacebookField.setText(facebook);
                    }
                    if(map.get("linkedIn")!=null){
                        linkedIn = map.get("linkedIn").toString();
                        //mLinkedInField.setText(linkedIn);
                    }

                    Glide.clear(mEmployerImage);

                    if(map.get("profileImageUrl")!=null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(profileImageUrl).into(mEmployerImage);
                        switch(profileImageUrl){
                            case "default":
                                Glide.with(getApplication()).load(R.mipmap.ic_launcher).into(mEmployerImage);
                                break;
                            default:
                                Glide.with(getApplication()).load(profileImageUrl).into(mEmployerImage);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideEditTextKeypadOnFocusChange(){
        mDescriptionField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mContactField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mPhoneField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }
}
