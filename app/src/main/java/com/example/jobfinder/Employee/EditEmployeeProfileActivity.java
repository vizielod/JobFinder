package com.example.jobfinder.Employee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.example.jobfinder.ChooseLoginRegistrationActivity;
import com.example.jobfinder.Employer.EditJobActivity;
import com.example.jobfinder.LoginActivity;
import com.example.jobfinder.MainActivity;
import com.example.jobfinder.Matches.EmployeeMatches.EmployeeMatchesActivity;
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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditEmployeeProfileActivity extends AppCompatActivity {
    final static int PICK_PDF_CODE = 2342;
    private static final String LOGTAG = "UserRole";
    private static final String UPLOAD_FILE = "UPLOAD FILE";
    private static final String DELETE_PROFILE = "DELETE PROFILE";

    private EditText mNameField, mDescriptionField, mPhoneField, mAgeField, mProfessionField, mCountryField, mCityField, mFacebookField, mLinkedInField, mWebsiteField, mSkypeField;
    private TextView mTextViewStatus, mTextViewPreviewCV, mTextViewFileUploaded;

    private Button mBack, mConfirm, mPreviewCV, mUploadCV, mSelectCV, mDeleteBtn;

    private ProgressBar mProgressBar;

    private ImageView mProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase, usersDb, chatDb;

    private String userId, userSex, userCVUrl;
    private String name, description, profileImageUrl, phone, age, profession, country, city, facebook, linkedIn, websiteURL, skype;
    private String alertDialogTitle, alertDialogMessage;

    private Uri resultUri, resultFileUri;
    private RadioGroup mGender_RadioGroup;

    private boolean deleteProfileAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_employee_profile);


        mTextViewFileUploaded = (TextView) findViewById(R.id.fileUploadedTextView);
        mTextViewStatus = (TextView) findViewById(R.id.textViewStatus);
        mTextViewPreviewCV = (TextView) findViewById(R.id.textViewPreviewCV);

        mNameField = (EditText) findViewById(R.id.name);
        mGender_RadioGroup = (RadioGroup) findViewById(R.id.gender_radioGroup);
        mAgeField = (EditText) findViewById(R.id.age);
        mProfessionField = (EditText) findViewById(R.id.profession);
        mDescriptionField = (EditText) findViewById(R.id.employeeDescription);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mCountryField = (EditText) findViewById(R.id.country);
        mCityField = (EditText) findViewById(R.id.city);
        mFacebookField = (EditText) findViewById(R.id.facebook);
        mLinkedInField = (EditText) findViewById(R.id.linkedIn);
        mWebsiteField = (EditText) findViewById(R.id.websiteURL);
        mSkypeField = (EditText) findViewById(R.id.skype);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        mUploadCV = (Button) findViewById(R.id.btn_upload_cv);
        mSelectCV = (Button) findViewById(R.id.btn_select_cv);
        mPreviewCV = (Button) findViewById(R.id.btn_preview_cv);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);


        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);
        mDeleteBtn = (Button) findViewById(R.id.btn_deleteUserProfile);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employee").child(userId);
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        //Log.i(LOGTAG, userId);

        getUserInfo();
        hideEditTextKeypadOnFocusChange();

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
                    alertDialogTitle = "Confirm File Upload";
                    alertDialogMessage = "Are you sure you want to upload this file as your CV?\n" +
                            "Note: If you click YES, this change will be confirmed. " +
                            "You can change your CV anytime.";
                    PopAlertDialogMessage(alertDialogTitle, alertDialogMessage, UPLOAD_FILE);
                    /*AlertDialog.Builder builder = new AlertDialog.Builder(EditEmployeeProfileActivity.this);

                    builder.setTitle("Confirm File Upload");
                    builder.setMessage("Are you sure you want to upload this file as your CV?\n" +
                            "Note: If you click YES, this change will be confirmed. " +
                            "You can change your CV anytime.");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            uploadFile(resultFileUri);
                            mPreviewCV.setEnabled(true);
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();*/
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
                /*if(resultUri != null){
                    //mProfileImage.setImageURI(resultUri);
                    //uploadImage();
                }*/
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
                //getUserInfo();
            }
        });
        /*mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogTitle = "Confirm Profile Delete";
                alertDialogMessage = "Are you sure you want to DELETE your profile?";
                PopAlertDialogMessage(alertDialogTitle, alertDialogMessage, DELETE_PROFILE);
            }
        });*/
        /*mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    if(map.get("age")!=null){
                        age = map.get("age").toString();
                        mAgeField.setText(age);
                    }
                    if(map.get("profession")!=null){
                        profession = map.get("profession").toString();
                        mProfessionField.setText(profession);
                    }
                    if(map.get("description")!=null){
                        description = map.get("description").toString();
                        mDescriptionField.setText(description);
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("country")!=null){
                        country = map.get("country").toString();
                        mCountryField.setText(country);
                    }
                    if(map.get("city")!=null){
                        city = map.get("city").toString();
                        mCityField.setText(city);
                    }
                    if(map.get("facebook")!=null){
                        facebook = map.get("facebook").toString();
                        mFacebookField.setText(facebook);
                    }
                    if(map.get("linkedIn")!=null){
                        linkedIn = map.get("linkedIn").toString();
                        mLinkedInField.setText(linkedIn);
                    }
                    if(map.get("websiteURL")!=null){
                        websiteURL = map.get("websiteURL").toString();
                        mWebsiteField.setText(websiteURL);
                    }
                    if(map.get("skype")!=null){
                        skype = map.get("skype").toString();
                        mSkypeField.setText(skype);
                    }
                    if(map.get("sex")!=null){
                        userSex = map.get("sex").toString();
                        switch(userSex){
                            case "Male":
                                mGender_RadioGroup.check(R.id.rb_male);
                                break;
                            case "Female":
                                mGender_RadioGroup.check(R.id.rb_female);
                                break;
                            default:
                                mGender_RadioGroup.clearCheck();
                                break;
                        }
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("userCVUrl") != null){
                        userCVUrl = map.get("userCVUrl").toString();
                        mTextViewPreviewCV.setText("Preview your CV/Resume here:");
                        mTextViewFileUploaded.setText("File Uploaded!");
                        mPreviewCV.setEnabled(true);
                        //mPreviewCV.setText("Preview CV/Resume");
                    }
                    else{
                        mPreviewCV.setEnabled(false);
                    }
                    Glide.clear(mProfileImage);

                    if(map.get("profileImageUrl")!=null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                        switch(profileImageUrl){
                            case "default":
                                Glide.with(getApplication()).load(R.mipmap.ic_launcher).into(mProfileImage);
                                break;
                            default:
                                Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                                break;
                        }
                    }
                    //Log.i(LOGTAG, profileImageUrl);
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
        Toast.makeText(EditEmployeeProfileActivity.this, "Changes Successfully Saved", Toast.LENGTH_LONG).show();
        /*if(resultUri != null){
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

                    Toast.makeText(EditEmployeeProfileActivity.this, "Upload Success, download URL " +
                            downloadUrl.toString(), Toast.LENGTH_LONG).show();

                    Map userInfo = new HashMap();
                    userInfo.put("profileImageUrl", downloadUrl.toString());
                    mUserDatabase.updateChildren(userInfo);
                }
            });
        }*/
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
                mTextViewStatus.setTextColor(Color.GREEN);
                mTextViewStatus.setText(Html.fromHtml("<u>"+returnCursor.getString(nameIndex)+"</u>"));
            }
            else{
                Toast.makeText(EditEmployeeProfileActivity.this, "No File Chosen", Toast.LENGTH_LONG).show();
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

                Toast.makeText(EditEmployeeProfileActivity.this, "Image Uploaded Successfully", Toast.LENGTH_LONG).show();

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

    public void onDeleteProfileButtonClick(View view) {
        //Itt is először megnézni, ha vannak kötődések akkor azokat kitörölni. Chat-et is, majd csak utána törölni a profilt.
        alertDialogTitle = "Confirm Profile Delete";
        alertDialogMessage = "Are you sure you want to DELETE your profile?";
        PopAlertDialogMessage(alertDialogTitle, alertDialogMessage, DELETE_PROFILE);
        /*deleteEmployeeProfileDataAndStorageFiles();
        Intent intent = new Intent(EditEmployeeProfileActivity.this, ChooseLoginRegistrationActivity.class);
        Toast.makeText(EditEmployeeProfileActivity.this, "Profile successfully deleted", Toast.LENGTH_LONG).show();
        startActivity(intent);
        //mUserDatabase.removeValue();
        mAuth.getCurrentUser().delete();
        finish();
        return;*/

    }

    public void deleteEmployeeProfileDataAndStorageFiles(){
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //Clear Data from Job Connections even if it only liked or disliked the job but didn't match
                    deleteUserIDFromJobConnections();
                    if(dataSnapshot.child("connections").child("matches").exists()){
                        Log.i(LOGTAG, "Torolni kell elemeket");
                        deleteChatAndEmployeeDataFromChatAndEmployerDb();
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null && !dataSnapshot.child("profileImageUrl").getValue().equals("default")){
                        StorageReference imagefilepath = FirebaseStorage.getInstance().getReference().child("profileImages").child("employeeProfileImages").child(userId);
                        imagefilepath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditEmployeeProfileActivity.this, "Image deleted successfully", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Image Deleted Successfully!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditEmployeeProfileActivity.this, "Image delete failed", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Error while deleting image!");
                            }
                        });
                    }
                    if(dataSnapshot.child("userCVUrl").getValue()!=null){
                        //delete file from storage
                        StorageReference pdffilepath = FirebaseStorage.getInstance().getReference().child("userFiles/userCV").child(userId);
                        pdffilepath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditEmployeeProfileActivity.this, "File deleted successfully", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "File Deleted Successfully!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditEmployeeProfileActivity.this, "File delete failed", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Error while deleting file!");
                            }
                        });
                    }
                    //Ez így ismét nem jó. Ha van chat cucc, akkor hamarabb kitörli már ezt mint ahogy odáig eljutna így onnan nem tud törölni.
                    //Rendesen megoldani, ha létrehozok egy Usert és nem csinálok vele semmit akkor és törlődjön rendesen az adatbázisból
                    if(mUserDatabase.getDatabase() != null){
                        mUserDatabase.removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteChatAndEmployeeDataFromChatAndEmployerDb() {
        final DatabaseReference userMatchesConnectionsDb = mUserDatabase.child("connections").child("matches");
        userMatchesConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot matchedEmployerProfileId : dataSnapshot.getChildren()){
                        Log.i(LOGTAG, matchedEmployerProfileId.getKey());
                        final String employerID = matchedEmployerProfileId.getKey();
                        userMatchesConnectionsDb.child(employerID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    for(DataSnapshot matchedJobId : dataSnapshot.getChildren()){
                                        Log.i(LOGTAG, matchedJobId.toString());
                                        Log.i(LOGTAG, matchedJobId.getKey());
                                        final String jobId = matchedJobId.getKey();
                                        //Megkeresni az Employer adatbázisba a getKey által visszaadott ID-val rendelkező felhasználót és annak a connections/matches ágából kitörölni
                                        usersDb.child("Employer").child(employerID).child("jobs").child(jobId).child("connections").child("matches").child(userId).removeValue();
                                        if(matchedJobId.child("chatId").exists()){
                                            final String chatID = matchedJobId.child("chatId").getValue().toString();
                                            Log.i(LOGTAG, chatID);
                                            //A Chat részben megkeresni ezekhez a chatID-khoz tartozó adatot és azokat is eltávolítani
                                            deleteChatDataOnEmployeeProfileDelete(chatID);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }
                else{
                    mUserDatabase.removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deleteChatDataOnEmployeeProfileDelete(final String chatID){
        chatDb = FirebaseDatabase.getInstance().getReference().child("Chat");
        chatDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    chatDb.child(chatID).removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mUserDatabase.removeValue();
    }

    public void deleteUserIDFromJobConnections(){
        final DatabaseReference employerDb =  usersDb.child("Employer");
        employerDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot employer : dataSnapshot.getChildren()){
                        final String employerId = employer.getKey();
                        final DatabaseReference jobsDb = employerDb.child(employerId).child("jobs");
                        jobsDb.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    for(DataSnapshot job : dataSnapshot.getChildren()){
                                        final String jobId = job.getKey();
                                        if(job.child("connections").child("liked").exists() && job.child("connections").child("liked").child(userId).exists()){
                                            usersDb.child("Employer").child(employerId).child("jobs").child(jobId).child("connections").child("liked").child(userId).removeValue();
                                        }
                                        else if(job.child("connections").child("disliked").exists() && job.child("connections").child("disliked").child(userId).exists()){
                                            usersDb.child("Employer").child(employerId).child("jobs").child(jobId).child("connections").child("disliked").child(userId).removeValue();
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void PopAlertDialogMessage(String title, String message, String callMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditEmployeeProfileActivity.this);

        builder.setTitle(title);
        builder.setMessage(message);

        if(callMessage.equals(UPLOAD_FILE)){
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    uploadFile(resultFileUri);
                    mPreviewCV.setEnabled(true);
                    dialog.dismiss();
                }
            });
        }
        else if(callMessage.equals(DELETE_PROFILE)){
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    deleteEmployeeProfileDataAndStorageFiles();
                    Intent intent = new Intent(EditEmployeeProfileActivity.this, ChooseLoginRegistrationActivity.class);
                    Toast.makeText(EditEmployeeProfileActivity.this, "Profile successfully deleted", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    //mUserDatabase.removeValue();
                    mAuth.getCurrentUser().delete();
                    finish();
                    dialog.dismiss();
                    return;
                }
            });
        }

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
