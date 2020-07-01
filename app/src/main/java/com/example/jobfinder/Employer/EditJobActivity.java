package com.example.jobfinder.Employer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EditJobActivity extends AppCompatActivity {
    private static final String LOGTAG = "UserRole";
    final static int PICK_PDF_CODE = 2342;

    private EditText mTitleField, mDescriptionField, mCountryField, mCityField, mContactField, mPhoneField, mJobWebsiteUrlField;
    private TextView mTextViewStatus, mTextViewPreviewDescription, mTextViewFileUploaded, mCategoryField, mTypeField;

    private Spinner mCategorySpinner, mTypeSpinner;

    private ArrayList<String> jobCategorySpinnerList, jobTypeSpinnerList;

    private Button mBack, mConfirm, mPreviewDescription, mSelectFile, mUploadFile;

    private ImageView mJobImage;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userId, title, category, type, description, country, city, contact, phone, jobWebsiteUrl, jobDescriptionUrl, jobImageUrl, userSex, jobId;
    private String selectedTypeSpinner, selectedCategorySpinner;

    private String resource_category_spinner_str;

    private Boolean initializedCategorySpinner = false;
    private Uri resultImageUri, resultFileUri;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);

        jobId = getIntent().getExtras().getString("jobId");
        Log.i(LOGTAG, jobId);

        mTitleField = (EditText) findViewById(R.id.jobTitle);
        mDescriptionField = (EditText) findViewById(R.id.jobDescription);
        mCountryField = (EditText) findViewById(R.id.country);
        mCityField = (EditText) findViewById(R.id.city);
        mContactField = (EditText) findViewById(R.id.contact);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mJobWebsiteUrlField = (EditText) findViewById(R.id.jobWebsiteUrl);

        mCategorySpinner = (Spinner) findViewById(R.id.category_spinner);
        mTypeSpinner = (Spinner) findViewById(R.id.type_spinner);

        mTextViewFileUploaded = (TextView) findViewById(R.id.fileUploadedTextView);
        mTextViewStatus = (TextView) findViewById(R.id.textViewStatus);
        mTextViewPreviewDescription = (TextView) findViewById(R.id.textViewPreviewDescription);

        mCategoryField = (TextView) findViewById(R.id.category_textview);
        mTypeField = (TextView) findViewById(R.id.type_textview);

        mJobImage = (ImageView) findViewById(R.id.jobImage);
        mUploadFile = (Button) findViewById(R.id.btn_upload_file);
        mSelectFile = (Button) findViewById(R.id.btn_select_file);
        mPreviewDescription = (Button) findViewById(R.id.btn_preview_description);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        resource_category_spinner_str = (String) this.getResources().getString(R.string.category_spinner_str);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(userId);
        Log.i(LOGTAG, userId);

        if (jobId != null && mUserDatabase!= null){
            getJobInfo();
        }

        mPreviewDescription.setEnabled(false);
        initializeJobCategorySpinner();
        initializeJobTypeSpinner();
        hideEditTextKeypadOnFocusChange();



        mSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPDF();
                //mTextViewStatus.setText("File Selected, Click Upload!");
            }
        });
        mUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(resultFileUri != null){
                    uploadFile(resultFileUri);
                    mPreviewDescription.setEnabled(true);
                }
            }
        });
        mPreviewDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jobDescriptionUrl != null){
                    Intent intent = new Intent(Intent.ACTION_QUICK_VIEW);
                    intent.setData(Uri.parse(jobDescriptionUrl));
                    startActivity(intent);
                }
            }
        });
        
        mJobImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveJobInformation(jobId);
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

    private void initializeJobCategorySpinner(){
        jobCategorySpinnerList = new ArrayList<>();
        JobObject.populateJobCategorySpinnerList(jobCategorySpinnerList);

        ArrayAdapter<String> categorySpinnerArrayAdapter = new ArrayAdapter<>(EditJobActivity.this,
                android.R.layout.simple_spinner_dropdown_item, jobCategorySpinnerList);

        mCategorySpinner.setAdapter(categorySpinnerArrayAdapter);

        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategorySpinner = parent.getItemAtPosition(position).toString();
                mCategoryField.setText(selectedCategorySpinner);
                Toast.makeText(EditJobActivity.this, selectedCategorySpinner, Toast.LENGTH_LONG).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initializeJobTypeSpinner(){
        jobTypeSpinnerList = new ArrayList<>();
        JobObject.populateJobTypeSpinnerList(jobTypeSpinnerList);

        mTypeSpinner.setAdapter(new ArrayAdapter<>(EditJobActivity.this,
                android.R.layout.simple_spinner_dropdown_item, jobTypeSpinnerList));


        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTypeSpinner = parent.getItemAtPosition(position).toString();
                mTypeField.setText(selectedTypeSpinner);
                Toast.makeText(EditJobActivity.this, selectedTypeSpinner, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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

                        //initializeJobCategorySpinner();

                    }
                    if(map.get("type")!=null){
                        type = map.get("type").toString();
                        mTypeField.setText(type);
                        //initializeJobTypeSpinner();
                    }
                    if(map.get("description")!=null){
                        description = map.get("description").toString();
                        mDescriptionField.setText(description);
                    }
                    if(map.get("country")!=null){
                        country = map.get("country").toString();
                        mCountryField.setText(country);
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
                        mJobWebsiteUrlField.setText(jobWebsiteUrl);
                    }
                    if(map.get("jobDescriptionUrl") != null){
                        mPreviewDescription.setEnabled(true);
                        jobDescriptionUrl = map.get("jobDescriptionUrl").toString();
                        mTextViewPreviewDescription.setText("Preview description here:");
                        mTextViewFileUploaded.setText("File Uploaded!");
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

    private void saveJobInformation(String key) {
        title = mTitleField.getText().toString();
        category = (selectedCategorySpinner!=null) ? selectedCategorySpinner : "";
        type = (selectedTypeSpinner!=null) ? selectedTypeSpinner : "";
        description = mDescriptionField.getText().toString();
        country = mCountryField.getText().toString();
        city = mCityField.getText().toString();
        contact = mContactField.getText().toString();
        phone = mPhoneField.getText().toString();
        jobWebsiteUrl = mJobWebsiteUrlField.getText().toString();
        Map userInfo = new HashMap();
        userInfo.put("title", title);
        userInfo.put("category", category);
        userInfo.put("type", type);
        userInfo.put("description", description);
        //userInfo.put("jobImageUrl", "default");
        userInfo.put("country", country);
        userInfo.put("city", city);
        userInfo.put("contact", contact);
        userInfo.put("phone", phone);
        userInfo.put("jobWebsiteUrl", jobWebsiteUrl);
        //userInfo.put("jobImageUrl", "default");
        mUserDatabase.child("jobs").child(key).updateChildren(userInfo);
        /*if(resultImageUri != null){
            final String jobId = key;
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("jobImages").child(jobId);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultImageUri);
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

                    Toast.makeText(EditJobActivity.this, "Upload Success, download URL " +
                            downloadUrl.toString(), Toast.LENGTH_LONG).show();

                    Map userInfo = new HashMap();
                    userInfo.put("jobImageUrl", downloadUrl.toString());
                    mUserDatabase.child("jobs").child(jobId).updateChildren(userInfo);

                    finish();
                    return;
                }
            });
        }else{
            finish();
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultImageUri = imageUri;
            mJobImage.setImageURI(resultImageUri);
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
                Toast.makeText(EditJobActivity.this, "No File Chosen", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void uploadImage(/*final String jobId*/) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();

        StorageReference filepath = FirebaseStorage.getInstance().getReference().child("jobImages").child(jobId);
        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultImageUri);
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

                Toast.makeText(EditJobActivity.this, "Image Uploaded Successfully", Toast.LENGTH_LONG).show();

                Map userInfo = new HashMap();
                userInfo.put("jobImageUrl", downloadUrl.toString());
                mUserDatabase.child("jobs").child(jobId).updateChildren(userInfo);
                jobImageUrl = downloadUrl.toString();
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
        //String fileName = userId + System.currentTimeMillis() + ".pdf";
        //StorageReference sRef = mStorageReference.child(Constants.STORAGE_PATH_UPLOADS + System.currentTimeMillis() + ".pdf");
        StorageReference pdffilepath = FirebaseStorage.getInstance().getReference().child("jobFiles/jobDetailedDescriptions").child(jobId);
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
                mTextViewPreviewDescription.setText("Preview description here:");
                mPreviewDescription.setText("Preview description");
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while(!uri.isComplete());
                Uri downloadUrl = uri.getResult();

                /*Toast.makeText(EditEmployeeProfileActivity.this, "Upload Success, download URL " +
                        downloadUrl.toString(), Toast.LENGTH_LONG).show();*/
                Map userInfo = new HashMap();
                userInfo.put("jobDescriptionUrl", downloadUrl.toString());
                mUserDatabase.child("jobs").child(jobId).updateChildren(userInfo);
                jobDescriptionUrl = downloadUrl.toString();
            }
        });

    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideEditTextKeypadOnFocusChange(){
        mTitleField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
        mJobWebsiteUrlField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }
}
