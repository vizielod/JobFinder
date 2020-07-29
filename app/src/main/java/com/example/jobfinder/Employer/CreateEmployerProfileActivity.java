package com.example.jobfinder.Employer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jobfinder.Employee.EmployeeTabbedMainActivity;
import com.example.jobfinder.R;
import com.example.jobfinder.RegistrationActivity;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateEmployerProfileActivity extends AppCompatActivity {
    private static final String LOGTAG = "UserRole";

    private EditText mNameField, mDescriptionField, mIndustryField, mWebsiteField, mContactField, mAddressField, mLinkedInField, mFacebookField;

    private Button mBack, mCreate, mSkip;

    private ImageView mEmployerImage, mBackIV;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userId, userRole;
    private String name, description, profileImageUrl, industry, websiteUrl, facebook, linkedIn, address, contact;

    private Uri resultUri;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_employer_profile);

        mNameField = (EditText) findViewById(R.id.employerName);
        mDescriptionField = (EditText) findViewById(R.id.employerDescription);
        mIndustryField = (EditText) findViewById(R.id.industry);
        mWebsiteField = (EditText) findViewById(R.id.websiteURL);
        mFacebookField = (EditText) findViewById(R.id.facebook);
        mLinkedInField = (EditText) findViewById(R.id.linkedIn);
        mAddressField = (EditText) findViewById(R.id.headquarter);
        mContactField = (EditText) findViewById(R.id.contact);

        mEmployerImage = (ImageView) findViewById(R.id.employerImage);

        //mBack = (Button) findViewById(R.id.back);
        mBackIV = (ImageView) findViewById(R.id.back_arrow);
        mCreate = (Button) findViewById(R.id.create);
        mSkip = (Button) findViewById(R.id.skip);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(userId);
        Log.i(LOGTAG, userId);

        userRole = getIntent().getStringExtra("userRole");
        Log.i(LOGTAG, userRole);

        getUserInfo();
        hideEditTextKeypadOnFocusChange();

        mEmployerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEmployerInformation();
                Intent intent = new Intent(CreateEmployerProfileActivity.this, EmployerTabbedMainActivity.class);
                intent.putExtra("userRole", userRole);
                //Log.i(LOGTAG, userRole);
                startActivity(intent);
                finish();
                return;
            }
        });
        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateEmployerProfileActivity.this, EmployerTabbedMainActivity.class);
                intent.putExtra("userRole", userRole);
                //Log.i(LOGTAG, userRole);
                startActivity(intent);
                finish();
                return;
            }
        });
        mBackIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
                return;
            }
        });
    }

    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }
                    Glide.clear(mEmployerImage);

                    if(map.get("profileImageUrl")!=null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(profileImageUrl).into(mEmployerImage);
                        switch(profileImageUrl){
                            case "default":
                                Glide.with(getApplication()).load(R.drawable.placeholder_img).into(mEmployerImage);
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

    private void saveEmployerInformation() {
        name = mNameField.getText().toString();
        description = mDescriptionField.getText().toString();
        industry = mIndustryField.getText().toString();
        websiteUrl = mWebsiteField.getText().toString();
        facebook = mFacebookField.getText().toString();
        linkedIn = mLinkedInField.getText().toString();
        address = mAddressField.getText().toString();
        contact = mContactField.getText().toString();
        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("description", description);
        userInfo.put("industry", industry);
        userInfo.put("websiteURL", websiteUrl);
        userInfo.put("facebook", facebook);
        userInfo.put("linkedIn", linkedIn);
        userInfo.put("address", address);
        userInfo.put("contactEmail", contact);
        mUserDatabase.updateChildren(userInfo);
        if(resultUri != null){
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages/employerProfileImages").child(userId);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete());
                    Uri downloadUrl = uri.getResult();

                    Toast.makeText(CreateEmployerProfileActivity.this, "Upload Success, download URL " +
                            downloadUrl.toString(), Toast.LENGTH_LONG).show();

                    Map userInfo = new HashMap();
                    userInfo.put("profileImageUrl", downloadUrl.toString());
                    mUserDatabase.updateChildren(userInfo);

                    finish();
                    return;
                }
            });
        }else{
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mEmployerImage.setImageURI(resultUri);
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideEditTextKeypadOnFocusChange(){
        mNameField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mDescriptionField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mIndustryField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mWebsiteField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mFacebookField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mLinkedInField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mAddressField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
    }
}
