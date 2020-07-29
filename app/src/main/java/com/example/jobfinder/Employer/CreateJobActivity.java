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

import com.example.jobfinder.Employee.EditEmployeeProfileActivity;
import com.example.jobfinder.R;
import com.example.jobfinder.SettingsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.OnCountryPickerListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CreateJobActivity extends AppCompatActivity implements OnCountryPickerListener {
    private static final String LOGTAG = "UserRole";
    final static int PICK_PDF_CODE = 2342;

    private EditText mTitleField, mDescriptionField, mCityField, mContactField, mPhoneField, mJobWebsiteUrlField;
    private TextView mTextViewStatus, mTextViewPreviewCV, mCategoryField, mTypeField, mCountryField;

    private Spinner mCategorySpinner, mTypeSpinner;

    private ArrayList<String> jobCategorySpinnerList, jobTypeSpinnerList;

    private Button mBack, mCreate, mPreviewCV, mSelectCV, mUploadCV;

    private ImageView mJobImage;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase, mLocationDatabase;

    private String userId, title, category, type, description, country, city, contact, phone, jobWebsiteUrl, jobDescriptionUrl, jobImageUrl, userSex;
    private String selectedTypeSpinner, selectedCategorySpinner;
    private Uri resultImageUri, resultFileUri;

    private Boolean imageUploadSuccess = false, fileUploadSuccess = false;

    private CountryPicker countryPicker;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_job);

        mTitleField = (EditText) findViewById(R.id.jobTitle);
        mDescriptionField = (EditText) findViewById(R.id.jobDescription);
        //mCountryField = (EditText) findViewById(R.id.country);
        mCityField = (EditText) findViewById(R.id.city);
        mContactField = (EditText) findViewById(R.id.contact);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mJobWebsiteUrlField = (EditText) findViewById(R.id.jobWebsiteUrl);

        mCategorySpinner = (Spinner) findViewById(R.id.category_spinner);
        mTypeSpinner = (Spinner) findViewById(R.id.type_spinner);

        mCategoryField = (TextView) findViewById(R.id.category_textview);
        mTypeField = (TextView) findViewById(R.id.type_textview);
        mCountryField = (TextView) findViewById(R.id.country);
        
        mTextViewStatus = (TextView) findViewById(R.id.textViewStatus);
        mTextViewPreviewCV = (TextView) findViewById(R.id.textViewPreviewCV);

        mJobImage = (ImageView) findViewById(R.id.jobImage);
        mSelectCV = (Button) findViewById(R.id.btn_select_description);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        mBack = (Button) findViewById(R.id.back);
        mCreate = (Button) findViewById(R.id.create);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(userId);
        Log.i(LOGTAG, userId);

        initializeJobCategorySpinner();
        initializeJobTypeSpinner();
        hideEditTextKeypadOnFocusChange();

        countryPicker = new CountryPicker.Builder().with(this)
                .listener(this)
                .build();

        mSelectCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPDF();
            }
        });

        mJobImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
                //uploadImage(key);
            }
        });

        mCountryField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countryPicker.showDialog(getSupportFragmentManager());
                return;
            }
        });

        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = mUserDatabase.child("jobs").push().getKey();
                Log.i(LOGTAG, key);
                //title = mTitleField.getText().toString();
                //mUserDatabase.child("jobs").child(key);/*.child("jobTitle").setValue(title);*/
                saveJobInformation(key);
                if(resultFileUri == null && resultImageUri == null){
                    Toast.makeText(CreateJobActivity.this, "Job Published", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                /*finish();
                return;*/
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

    @Override
    public void onSelectCountry(Country country) {
        mCountryField.setText(country.getName());
    }

    private void initializeJobCategorySpinner(){
        jobCategorySpinnerList = new ArrayList<>();
        JobObject.populateJobCategorySpinnerList(jobCategorySpinnerList);

        mCategorySpinner.setAdapter(new ArrayAdapter<>(CreateJobActivity.this,
                android.R.layout.simple_spinner_dropdown_item, jobCategorySpinnerList));

        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategorySpinner = parent.getItemAtPosition(position).toString();
                mCategoryField.setText(selectedCategorySpinner);
                //Toast.makeText(CreateJobActivity.this, selectedCategorySpinner, Toast.LENGTH_LONG).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initializeJobTypeSpinner(){
        jobTypeSpinnerList = new ArrayList<>();
        JobObject.populateJobTypeSpinnerList(jobTypeSpinnerList);

        mTypeSpinner.setAdapter(new ArrayAdapter<>(CreateJobActivity.this,
                android.R.layout.simple_spinner_dropdown_item, jobTypeSpinnerList));

        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTypeSpinner = parent.getItemAtPosition(position).toString();
                mTypeField.setText(selectedTypeSpinner);
                //Toast.makeText(CreateJobActivity.this, selectedTypeSpinner, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
        userInfo.put("jobImageUrl", "default");
        userInfo.put("country", country);
        userInfo.put("city", city);
        userInfo.put("contact", contact);
        userInfo.put("phone", phone);
        userInfo.put("jobWebsiteUrl", jobWebsiteUrl);
        mUserDatabase.child("jobs").child(key).updateChildren(userInfo);
        if(resultImageUri != null){
            uploadImage(key);
        }
        if(resultFileUri != null){
            uploadFile(resultFileUri, key);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultImageUri = imageUri;
            mJobImage.setImageURI(resultImageUri);
            //uploadImage();
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
                Toast.makeText(CreateJobActivity.this, "No File Chosen", Toast.LENGTH_LONG).show();
            }
        }
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

    private void uploadFile(Uri data, final String jobId) {
        mProgressBar.setVisibility(View.VISIBLE);
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
                //mTextViewPreviewCV.setText("Preview description here:");
                //mPreviewCV.setText("Preview description");
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while(!uri.isComplete());
                Uri downloadUrl = uri.getResult();

                /*Toast.makeText(EditEmployeeProfileActivity.this, "Upload Success, download URL " +
                        downloadUrl.toString(), Toast.LENGTH_LONG).show();*/
                Map userInfo = new HashMap();
                userInfo.put("jobDescriptionUrl", downloadUrl.toString());
                //userInfo.put("jobDescriptionFileName", );
                mUserDatabase.child("jobs").child(jobId).updateChildren(userInfo);
                jobDescriptionUrl = downloadUrl.toString();
                fileUploadSuccess = true;
                if(resultImageUri != null && imageUploadSuccess){
                    Toast.makeText(CreateJobActivity.this, "Job Published", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                else if(resultImageUri == null){
                    Toast.makeText(CreateJobActivity.this, "Job Published", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
        });

    }
    
    private void uploadImage(final String jobId) {
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

                Toast.makeText(CreateJobActivity.this, "Image Uploaded Successfully", Toast.LENGTH_LONG).show();

                Map userInfo = new HashMap();
                userInfo.put("jobImageUrl", downloadUrl.toString());
                mUserDatabase.child("jobs").child(jobId).updateChildren(userInfo);
                jobImageUrl = downloadUrl.toString();
                imageUploadSuccess = true;
                if(resultFileUri != null && fileUploadSuccess){
                    Toast.makeText(CreateJobActivity.this, "Job Published", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                else if(resultFileUri == null){
                    Toast.makeText(CreateJobActivity.this, "Job Published", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
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

    /*private void populateJobCategorySpinnerList(ArrayList<String> jobCategorySpinnerList){
        jobCategorySpinnerList.add("IT & Software");
        jobCategorySpinnerList.add("Sales and client care");
        jobCategorySpinnerList.add("Operations");
        jobCategorySpinnerList.add("Medicine & Health");
        jobCategorySpinnerList.add("Social Care");
        jobCategorySpinnerList.add("Accounting & Finance");
        jobCategorySpinnerList.add("Mechanical engineering");
        jobCategorySpinnerList.add("Marketing, Language & Communication");
        jobCategorySpinnerList.add("Electrical engineering");
        jobCategorySpinnerList.add("Business & Strategy");
        jobCategorySpinnerList.add("Supply Chain & Logistics");
        jobCategorySpinnerList.add("Administration");
        jobCategorySpinnerList.add("Education & training");
        jobCategorySpinnerList.add("Human Resources");
        jobCategorySpinnerList.add("Architecture & Construction");
        jobCategorySpinnerList.add("Climate, Environment & Sustainability");
        jobCategorySpinnerList.add("Legal");
        jobCategorySpinnerList.add("Biology, Biotech & Biochemistry");
        jobCategorySpinnerList.add("Quality assurance & risk");
        jobCategorySpinnerList.add("Creative & design");
        jobCategorySpinnerList.add("Hospitality & Tourism");
        jobCategorySpinnerList.add("Retail");
        jobCategorySpinnerList.add("Agriculture, forestry & marine");
        jobCategorySpinnerList.add("Chemistry & chemical engineering");
        jobCategorySpinnerList.add("Culture & Arts");
        jobCategorySpinnerList.add("Society & Politics");
        jobCategorySpinnerList.add("Consulting");
        jobCategorySpinnerList.add("Mathematics & Physics");
        jobCategorySpinnerList.add("Oil, Gas & Energy");
        jobCategorySpinnerList.add("Real Estate");
        jobCategorySpinnerList.add("Veterinary & Animal Sciences");

        Collections.sort(jobCategorySpinnerList);
        jobCategorySpinnerList.add("Other");
    }

    private void populateJobTypeSpinnerList(ArrayList<String> jobTypeSpinnerList){
        jobTypeSpinnerList.add("Full-time");
        jobTypeSpinnerList.add("Part-time");
        jobTypeSpinnerList.add("Student worker");
        jobTypeSpinnerList.add("Internship");
        jobTypeSpinnerList.add("Co-founder");
        jobTypeSpinnerList.add("Freelance");
        jobTypeSpinnerList.add("Project");
        jobTypeSpinnerList.add("Voluntary Work");
        jobTypeSpinnerList.add("Thesis");
        jobTypeSpinnerList.add("Phd / Research");
        jobTypeSpinnerList.add("Temporary");
        jobTypeSpinnerList.add("Graduate programme");

        Collections.sort(jobTypeSpinnerList);
        jobTypeSpinnerList.add("Other");
    }*/
}
