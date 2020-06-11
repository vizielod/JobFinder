package com.example.jobfinder.Employer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jobfinder.ChooseLoginRegistrationActivity;
import com.example.jobfinder.Employee.EditEmployeeProfileActivity;
import com.example.jobfinder.R;
import com.example.jobfinder.SettingsActivity;
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

public class EditEmployerProfileActivity extends AppCompatActivity {
    private static final String DELETE_PROFILE = "DELETE PROFILE";

    private static final String LOGTAG = "UserRole";

    private EditText mNameField, mDescriptionField, mIndustryField, mWebsiteField, mContactField, mAddressField, mLinkedInField, mFacebookField;

    private Button mBack, mConfirm;

    private ImageView mEmployerImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase, chatDb, usersDb, jobsDb;

    private String userId, userRole, employeeID;
    private String name, description, profileImageUrl, industry, websiteUrl, facebook, linkedIn, address, contact;
    private String alertDialogTitle, alertDialogMessage;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_employer_profile);

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
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(userId);
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        Log.i(LOGTAG, userId);

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
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
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
                    if(map.get("description")!=null){
                        description = map.get("description").toString();
                        mDescriptionField.setText(description);
                    }
                    if(map.get("industry")!=null){
                        industry = map.get("industry").toString();
                        mIndustryField.setText(industry);
                    }
                    if(map.get("websiteUrl")!=null){
                        websiteUrl = map.get("websiteUrl").toString();
                        mWebsiteField.setText(websiteUrl);
                    }
                    if(map.get("facebook")!=null){
                        facebook = map.get("facebook").toString();
                        mFacebookField.setText(facebook);
                    }
                    if(map.get("linkedIn")!=null){
                        linkedIn = map.get("linkedIn").toString();
                        mLinkedInField.setText(linkedIn);
                    }
                    if(map.get("address")!=null){
                        address = map.get("address").toString();
                        mAddressField.setText(address);
                    }
                    if(map.get("contactEmail")!=null){
                        contact = map.get("contactEmail").toString();
                        mContactField.setText(contact);
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

    private void saveUserInformation() {
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

                    Toast.makeText(EditEmployerProfileActivity.this, "Upload Success, download URL " +
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

    public void onDeleteProfileButtonClick(View view) {
        alertDialogTitle = "Confirm Profile Delete";
        alertDialogMessage = "Are you sure you want to DELETE your profile?";
        PopAlertDialogMessage(alertDialogTitle, alertDialogMessage, DELETE_PROFILE);
        /*deleteEmployerProfileDataAndStorageFiles();
        Intent intent = new Intent(EditEmployerProfileActivity.this, ChooseLoginRegistrationActivity.class);
        Toast.makeText(EditEmployerProfileActivity.this, "Profile successfully deleted", Toast.LENGTH_LONG).show();
        startActivity(intent);
        mAuth.getCurrentUser().delete();
        finish();
        return;*/
    }

    public void PopAlertDialogMessage(String title, String message, String callMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditEmployerProfileActivity.this);

        builder.setTitle(title);
        builder.setMessage(message);

        if(callMessage.equals(DELETE_PROFILE)){
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    deleteEmployerProfileDataAndStorageFiles();
                    Intent intent = new Intent(EditEmployerProfileActivity.this, ChooseLoginRegistrationActivity.class);
                    Toast.makeText(EditEmployerProfileActivity.this, "Profile successfully deleted", Toast.LENGTH_LONG).show();
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


    public void deleteEmployerProfileDataAndStorageFiles(){
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null && !dataSnapshot.child("profileImageUrl").getValue().equals("default")){
                        StorageReference imagefilepath = FirebaseStorage.getInstance().getReference().child("profileImages").child("employerProfileImages").child(userId);
                        imagefilepath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditEmployerProfileActivity.this, "Image deleted successfully", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Image Deleted Successfully!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditEmployerProfileActivity.this, "Image delete failed", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Error while deleting image!");
                            }
                        });
                    }
                    //Clear Data from Job Connections even if it only liked or disliked the job but didn't match
                    deleteEmployerUserIdFromEmployeeConnections();
                    if(dataSnapshot.child("jobs").exists()){
                        Log.i(LOGTAG, "Torolni kell elemeket");
                        deleteChatAndEmployerDataFromChatAndEmployeeDb();
                    }
                    else{
                        mUserDatabase.removeValue();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void deleteChatAndEmployerDataFromChatAndEmployeeDb(){
        DatabaseReference jobsDb = mUserDatabase.child("jobs");
        jobsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot job : dataSnapshot.getChildren()){
                        final String jobId = job.getKey();
                        deleteJob(jobId);
                    }
                    //usersDb.child("Employee").child(employeeID).child("connections").child("matches").child(userId).removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void deleteJob(final String jobId){
        final DatabaseReference jobDb = usersDb.child("Employer").child(userId).child("jobs").child(jobId);
        jobDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if(dataSnapshot.child("connections").child("matches").exists()){
                        //Job Data must be removed from EmployeeDB and Chat Db
                        //In case if the job is removed but it has connections with Employeee users and if they had
                        //a conversation, all should be removed from the DB. Otherwise it remains there as a "trash"
                        //Same happens in Tinder, also if somebody Disconnects from their pair of Deletes his/her own profile.
                        //The chat and the profile of the User who disconnected is no longer available from the user's profile who was disconnected
                        deleteChatAndJobDataFromChatAndEmployeeDb(jobId);
                    }
                    if(dataSnapshot.child("jobImageUrl").getValue()!=null && !dataSnapshot.child("jobImageUrl").getValue().equals("default")){
                        //delete image from storage
                        StorageReference imagefilepath = FirebaseStorage.getInstance().getReference().child("jobImages").child(jobId);
                        imagefilepath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditEmployerProfileActivity.this, "Image deleted successfully", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Image Deleted Successfully!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditEmployerProfileActivity.this, "Image delete failed", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Error while deleting image!");
                            }
                        });
                    }
                    if(dataSnapshot.child("jobDescriptionUrl").getValue()!=null){
                        //delete file from storage
                        StorageReference pdffilepath = FirebaseStorage.getInstance().getReference().child("jobFiles/jobDetailedDescriptions").child(jobId);
                        pdffilepath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditEmployerProfileActivity.this, "File deleted successfully", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "File Deleted Successfully!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditEmployerProfileActivity.this, "File delete failed", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Error while deleting file!");
                            }
                        });
                    }
                    //delete connections
                    jobDb.removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void deleteChatAndJobDataFromChatAndEmployeeDb(final String jobId) {
        final DatabaseReference jobMatchesConnectionsDb = usersDb.child("Employer").child(userId).child("jobs").child(jobId).child("connections").child("matches");
        jobMatchesConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot matchedUser : dataSnapshot.getChildren()){
                        employeeID = matchedUser.getKey();
                        //Megkeresni az Employee adatbázisba a getKey által visszaadott ID-val rendelkező felhasználót és annak a connections/matches ágából kitörölni a jobID-t
                        usersDb.child("Employee").child(employeeID).child("connections").child("matches").child(userId).child(jobId).removeValue();
                        //Log.i(LOGTAG, matchedUser.toString());
                        //Log.i(LOGTAG, matchedUser.getKey());
                        if(matchedUser.child("chatId").exists()){
                            final String chatID = matchedUser.child("chatId").getValue().toString();
                            //Log.i(LOGTAG, matchedUser.child("chatId").getValue().toString());
                            //A Chat részben megkeresni ezekhez a chatID-khoz tartozó adatot és azokat is eltávolítani
                            deleteChatDataOnJobDelete(chatID);
                        }
                    }
                    //notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deleteChatDataOnJobDelete(final String chatID){
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

    public void deleteEmployerUserIdFromEmployeeConnections(){
        final DatabaseReference employeeDb =  usersDb.child("Employee");
        employeeDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot employee : dataSnapshot.getChildren()){
                        final String employeeId = employee.getKey();
                        //final DatabaseReference jobsDb = employerDb.child(employerId).child("jobs");
                        if(employee.child("connections").child("liked").exists() && employee.child("connections").child("liked").child(userId).exists()){
                            usersDb.child("Employee").child(employeeId).child("connections").child("liked").child(userId).removeValue();
                        }
                        else if(employee.child("connections").child("disliked").exists() && employee.child("connections").child("disliked").child(userId).exists()){
                            usersDb.child("Employee").child(employeeId).child("connections").child("disliked").child(userId).removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
