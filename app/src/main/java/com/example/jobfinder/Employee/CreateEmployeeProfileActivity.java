package com.example.jobfinder.Employee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jobfinder.Employer.EmployerTabbedMainActivity;
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
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.OnCountryPickerListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateEmployeeProfileActivity extends AppCompatActivity implements OnCountryPickerListener {
    //this is the pic pdf code used in file chooser
    final static int PICK_PDF_CODE = 2342;
    private static final String LOGTAG = "UserRole";

    private EditText mNameField, mDescriptionField, mPhoneField, mAgeField, mProfessionField, mCityField, mFacebookField, mLinkedInField, mWebsiteField, mSkypeField;

    private TextView mTextViewStatus, mTextViewPreviewCV, mCountryField;

    private Button mBack, mCreate, mSkip, mPreviewCV, mSelectCV, mUploadCV;

    private ProgressBar mProgressBar;

    private ImageView mProfileImage, mBackIV;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userId, userSex, userCVUrl;
    private String name, description, profileImageUrl, phone, age, profession, country, city, facebook, linkedIn, websiteURL, skype;

    private Uri resultUri, resultFileUri;

    private RadioGroup mGender_RadioGroup;

    private CountryPicker countryPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_employee_profile);

        mTextViewStatus = (TextView) findViewById(R.id.textViewStatus);
        mTextViewPreviewCV = (TextView) findViewById(R.id.textViewPreviewCV);

        mNameField = (EditText) findViewById(R.id.name);
        mGender_RadioGroup = (RadioGroup) findViewById(R.id.gender_radioGroup);
        mAgeField = (EditText) findViewById(R.id.age);
        mProfessionField = (EditText) findViewById(R.id.profession);
        mDescriptionField = (EditText) findViewById(R.id.employeeDescription);
        mPhoneField = (EditText) findViewById(R.id.phone);
        //mCountryField = (EditText) findViewById(R.id.country);
        mCityField = (EditText) findViewById(R.id.city);
        mFacebookField = (EditText) findViewById(R.id.facebook);
        mLinkedInField = (EditText) findViewById(R.id.linkedIn);
        mWebsiteField = (EditText) findViewById(R.id.websiteURL);
        mSkypeField = (EditText) findViewById(R.id.skype);

        mCountryField = (TextView) findViewById(R.id.country);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        mUploadCV = (Button) findViewById(R.id.btn_upload_cv);
        mSelectCV = (Button) findViewById(R.id.btn_select_cv);
        mPreviewCV = (Button) findViewById(R.id.btn_preview_cv);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        mBackIV = (ImageView) findViewById(R.id.back_arrow);
        mCreate = (Button) findViewById(R.id.create);
        mSkip = (Button) findViewById(R.id.skip);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employee").child(userId);


        mPreviewCV.setEnabled(false);
        getUserInfo();
        hideEditTextKeypadOnFocusChange();

        countryPicker = new CountryPicker.Builder().with(this)
                .listener(this)
                .build();

        mCountryField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countryPicker.showDialog(getSupportFragmentManager());
                return;
            }
        });
        mSelectCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPDF();
                //mTextViewStatus.setText("File Selected, Click Upload!");
            }
        });
        mUploadCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(resultFileUri != null){
                    uploadFile(resultFileUri);
                    mPreviewCV.setEnabled(true);
                }
            }
        });
        mPreviewCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userCVUrl != null){
                    Intent intent = new Intent(Intent.ACTION_QUICK_VIEW);
                    intent.setData(Uri.parse(userCVUrl));
                    startActivity(intent);
                }
            }
        });
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
                Intent intent = new Intent(CreateEmployeeProfileActivity.this, EmployeeTabbedMainActivity.class);
                //intent.putExtra("userRole", userRole);
                startActivity(intent);
                finish();
                return;
            }
        });
        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateEmployeeProfileActivity.this, EmployeeTabbedMainActivity.class);
                //intent.putExtra("userRole", userRole);
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
        /*mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
                return;
            }
        });*/
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
                    Glide.clear(mProfileImage);

                    if(map.get("profileImageUrl")!=null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                        switch(profileImageUrl){
                            case "default":
                                Glide.with(getApplication()).load(R.drawable.placeholder_img).into(mProfileImage);
                                break;
                            default:
                                Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
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

    private void saveUserInformation() {
        int selectGenderID = mGender_RadioGroup.getCheckedRadioButtonId();
        final RadioButton gender_radioButton = (RadioButton) findViewById(selectGenderID);

        /* Egyelőre ezt nem rakom be, hogy tesztelésnél könnyen lehessen pörgetni és ne legyen még kötelező nemet választani
        De majd mindenképp kéne ilyen feltétel azokra a mezőkre amiket nem akarom, hogy üresen hagyhasson a felhasználó regisztráláskor
        if(gender_radioButton.getText() == null){
            return;
        }*/

        name = mNameField.getText().toString();
        age = mAgeField.getText().toString();
        profession = mProfessionField.getText().toString();
        description = mDescriptionField.getText().toString();
        phone = mPhoneField.getText().toString();
        country = mCountryField.getText().toString();
        city = mCityField.getText().toString();
        facebook = mFacebookField.getText().toString();
        linkedIn = mLinkedInField.getText().toString();
        websiteURL = mWebsiteField.getText().toString();
        skype = mSkypeField.getText().toString();
        if(selectGenderID > 0 && gender_radioButton.getText() != null){
            userSex = gender_radioButton.getText().toString();
        }
        else{
            userSex = "";
        }
        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("sex", userSex);
        userInfo.put("age", age);
        userInfo.put("profession", profession);
        userInfo.put("description", description);
        userInfo.put("phone", phone);
        userInfo.put("country", country);
        userInfo.put("city", city);
        userInfo.put("facebook", facebook);
        userInfo.put("linkedIn", linkedIn);
        userInfo.put("websiteURL", websiteURL);
        userInfo.put("skype", skype);
        mUserDatabase.updateChildren(userInfo);
        Toast.makeText(CreateEmployeeProfileActivity.this, "Changes Successfully Saved", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
            uploadImage();
        }
        else if(requestCode == PICK_PDF_CODE && resultCode == Activity.RESULT_OK && data != null){
            if(data.getData() != null){
                final Uri fileUri = data.getData();
                resultFileUri = fileUri;
                //Display Selected File name in the TextView
                Cursor returnCursor =
                        getContentResolver().query(fileUri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                mTextViewStatus.setText(returnCursor.getString(nameIndex));
                mTextViewStatus.setTextColor(Color.BLUE);
                mTextViewStatus.setText(Html.fromHtml("<u>"+returnCursor.getString(nameIndex)+"</u>"));
            }
            else{
                Toast.makeText(CreateEmployeeProfileActivity.this, "No File Chosen", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();

        StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages/employeeProfileImages").child(userId);
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
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while(!uri.isComplete());
                Uri downloadUrl = uri.getResult();

                Toast.makeText(CreateEmployeeProfileActivity.this, "Image Uploaded Successfully", Toast.LENGTH_LONG).show();

                Map userInfo = new HashMap();
                userInfo.put("profileImageUrl", downloadUrl.toString());
                mUserDatabase.updateChildren(userInfo);
                profileImageUrl = downloadUrl.toString();
            }
        });
    }

    private void getPDF() {
        //for greater than lolipop versions we need the permissions asked on runtime
        //so if the permission is not available user will go to the screen to allow storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        //creating an intent for file chooser
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF File"), PICK_PDF_CODE);
    }

    private void uploadFile(Uri data) {
        mProgressBar.setVisibility(View.VISIBLE);
        //StorageReference sRef = mStorageReference.child(Constants.STORAGE_PATH_UPLOADS + System.currentTimeMillis() + ".pdf");
        StorageReference pdffilepath = FirebaseStorage.getInstance().getReference().child("userFiles/userCV").child(userId);
        UploadTask uploadFileTask = pdffilepath.putFile(data);
        uploadFileTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
        uploadFileTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                mTextViewStatus.setText((int) progress + "% Uploading...");
            }
        });
        uploadFileTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                mProgressBar.setVisibility(View.GONE);
                mTextViewStatus.setText("File Uploaded Successfully");
                mTextViewPreviewCV.setText("Preview your CV/Resume here:");
                //mPreviewCV.setText("Preview CV/Resume");
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while(!uri.isComplete());
                Uri downloadUrl = uri.getResult();

                /*Toast.makeText(EditEmployeeProfileActivity.this, "Upload Success, download URL " +
                        downloadUrl.toString(), Toast.LENGTH_LONG).show();*/
                Map userInfo = new HashMap();
                userInfo.put("userCVUrl", downloadUrl.toString());
                mUserDatabase.updateChildren(userInfo);
                userCVUrl = downloadUrl.toString();
            }
        });

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
        mAgeField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mProfessionField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
        mPhoneField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mCountryField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mCityField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
        mSkypeField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

    @Override
    public void onSelectCountry(Country country) {
        mCountryField.setText(country.getName());
    }
}
