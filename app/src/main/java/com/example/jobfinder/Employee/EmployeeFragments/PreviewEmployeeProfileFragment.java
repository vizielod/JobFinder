package com.example.jobfinder.Employee.EmployeeFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.example.jobfinder.ChooseLoginRegistrationActivity;
import com.example.jobfinder.Employee.EditEmployeeProfileActivity;
import com.example.jobfinder.Employee.EmployeeTabbedMainActivity;
import com.example.jobfinder.Employer.EditJobActivity;
import com.example.jobfinder.Employer.EmployerTabbedMainActivity;
import com.example.jobfinder.Employer.JobTabbedMainActivity;
import com.example.jobfinder.R;
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

import java.util.Map;


public class PreviewEmployeeProfileFragment extends Fragment {
    final static int PICK_PDF_CODE = 2342;
    private static final String LOGTAG = "UserRole";
    private static final String UPLOAD_FILE = "UPLOAD FILE";
    private static final String DELETE_PROFILE = "DELETE PROFILE";

    private Context mContext;

    private EditText mDescriptionField, mWebsiteField, mContactField, mLinkedInField, mFacebookField, mPhoneField;
    private TextView mNameField, mAgeField, mProfessionField, mCountryField, mCityField;

    private Button mBack, mEditBtn, mPreviewCVBtn, mDeleteBtn, mLogout;

    private ImageView mProfileImage, mEmployerImage, mEditBtnIVBtn, mFacebookIcon, mLinkedinIcon, mWebsiteIcon;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase, chatDb, usersDb, jobsDb;

    private String userId, userRole, currentUId, jobId, employerId;
    private String name, age, profession, description, country, city, contact, phone, userCVUrl, websiteURL, profileImageUrl;
    private String facebook, linkedIn;
    private String alertDialogTitle, alertDialogMessage;

    private FragmentActivity mFragmentActivity;


    public PreviewEmployeeProfileFragment(){
        // Required empty public constructor
    }

    public static PreviewEmployeeProfileFragment newInstance() {
        PreviewEmployeeProfileFragment fragment = new PreviewEmployeeProfileFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_preview_employee_profile, container, false);

        mNameField = (TextView) view.findViewById(R.id.name_textview);
        mAgeField = (TextView) view.findViewById(R.id.age_textview);
        mProfessionField = (TextView) view.findViewById(R.id.profession_textview);
        mCountryField = (TextView) view.findViewById(R.id.country_textview);
        mCityField = (TextView) view.findViewById(R.id.city_textview);

        mDescriptionField = (EditText) view.findViewById(R.id.employeeDescription);
        mContactField = (EditText) view.findViewById(R.id.contact);
        mPhoneField = (EditText) view.findViewById(R.id.phone);

        mFacebookIcon = (ImageView) view.findViewById(R.id.facebook_icon_imageview);
        mLinkedinIcon = (ImageView) view.findViewById(R.id.linkedin_icon_imageview);
        mWebsiteIcon = (ImageView) view.findViewById(R.id.website_icon_imageview);

        mProfileImage = (ImageView) view.findViewById(R.id.profileImage);
        mEditBtnIVBtn = (ImageView) view.findViewById(R.id.editImageBtn);

        mEditBtn = (Button)  view.findViewById(R.id.edit);
        mPreviewCVBtn = (Button)  view.findViewById(R.id.btn_preview_cv);
        mDeleteBtn = (Button)  view.findViewById(R.id.btn_deleteUserProfile);
        mLogout = (Button)  view.findViewById(R.id.btn_logout);

        getUserInfo();
        hideEditTextKeypadOnFocusChange();

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
                    Toast.makeText(mFragmentActivity, "No Facebook Profile Linked!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(mFragmentActivity, "No LinkedIn Profile Linked!", Toast.LENGTH_LONG).show();
                }


            }
        });
        mWebsiteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(websiteURL != null){
                    if (!websiteURL.startsWith("http://") && !websiteURL.startsWith("https://"))
                        websiteURL = "http://" + websiteURL;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteURL));
                    startActivity(browserIntent);
                    return;
                }else{
                    Toast.makeText(mFragmentActivity, "No Website Linked!", Toast.LENGTH_LONG).show();
                }

            }
        });
        mEditBtnIVBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mFragmentActivity, EditEmployeeProfileActivity.class);
                intent.putExtra("jobId", jobId);
                startActivity(intent);
                return;
            }
        });
        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mFragmentActivity, EditEmployeeProfileActivity.class);
                intent.putExtra("jobId", jobId);
                startActivity(intent);
                return;
            }
        });
        mPreviewCVBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userCVUrl != null){
                    Intent intent = new Intent(Intent.ACTION_QUICK_VIEW);
                    intent.setData(Uri.parse(userCVUrl));
                    startActivity(intent);
                }
            }
        });
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogTitle = "Confirm Profile Delete";
                alertDialogMessage = "Are you sure you want to DELETE your profile?";
                PopAlertDialogMessage(alertDialogTitle, alertDialogMessage, DELETE_PROFILE);
            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(mFragmentActivity, ChooseLoginRegistrationActivity.class);
                startActivity(intent);
                return;
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
        mFragmentActivity = getActivity();
        usersDb = (DatabaseReference) EmployeeTabbedMainActivity.getUsersDb();
        mUserDatabase = (DatabaseReference) EmployeeTabbedMainActivity.getUserDatabase();
        userId = EmployeeTabbedMainActivity.getCurrentUId();
        userRole = (String) EmployeeTabbedMainActivity.getUserRole();
        mAuth = EmployeeTabbedMainActivity.getFirebaseAuth();

    }

    @Override
    public void onResume() {
        super.onResume();
        getUserInfo();
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
                        mCountryField.setText(country + ", ");
                    }
                    if(map.get("city")!=null){
                        city = map.get("city").toString();
                        mCityField.setText(city);
                    }
                    if(map.get("facebook")!=null){
                        facebook = map.get("facebook").toString();
                    }
                    if(map.get("linkedIn")!=null){
                        linkedIn = map.get("linkedIn").toString();
                    }
                    if(map.get("websiteURL")!=null){
                        websiteURL = map.get("websiteURL").toString();
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("userCVUrl") != null){
                        mPreviewCVBtn.setEnabled(true);
                        userCVUrl = map.get("userCVUrl").toString();
                    }
                    Glide.clear(mProfileImage);

                    if(map.get("profileImageUrl")!=null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(mFragmentActivity.getApplication()).load(profileImageUrl).into(mProfileImage);
                        switch(profileImageUrl){
                            case "default":
                                Glide.with(mFragmentActivity.getApplication()).load(R.mipmap.ic_launcher).into(mProfileImage);
                                break;
                            default:
                                Glide.with(mFragmentActivity.getApplication()).load(profileImageUrl).into(mProfileImage);
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

    

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)mFragmentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
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
                                Toast.makeText(mFragmentActivity, "Image deleted successfully", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Image Deleted Successfully!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(mFragmentActivity, "Image delete failed", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(mFragmentActivity, "File deleted successfully", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "File Deleted Successfully!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(mFragmentActivity, "File delete failed", Toast.LENGTH_LONG).show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(mFragmentActivity);

        builder.setTitle(title);
        builder.setMessage(message);

        if(callMessage.equals(DELETE_PROFILE)){
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    deleteEmployeeProfileDataAndStorageFiles();
                    Intent intent = new Intent(mFragmentActivity, ChooseLoginRegistrationActivity.class);
                    Toast.makeText(mFragmentActivity, "Profile successfully deleted", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    //mUserDatabase.removeValue();
                    mAuth.getCurrentUser().delete();
                    mFragmentActivity.finish();
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
