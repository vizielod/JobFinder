package com.example.jobfinder.Employee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jobfinder.Employer.CreateJobActivity;
import com.example.jobfinder.Employer.JobObject;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EmployeeSettingsActivity extends AppCompatActivity {
    final static int PICK_PDF_CODE = 2342;
    private static final String LOGTAG = "UserRole";

    private static FirebaseAuth mAuth;
    private static DatabaseReference mUserDatabase, usersDb;

    private static String userId, employeeId;

    private Button mCategoryBtn, mTypeBtn;
    private TextView mCategoryField, mTypeField;
    private Spinner mCategorySpinner, mTypeSpinner;
    private String selectedTypeSpinner, selectedCategorySpinner;

    private String alertDialogTitle, alertDialogMessage;

    private ArrayList<String> jobCategorySpinnerList, jobTypeSpinnerList, preferenceCategoryList, preferenceTypeList;
    private ArrayList categoryItemsSelected, typeItemsSelected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_settings);

        mAuth = EmployeeTabbedMainActivity.getFirebaseAuth();
        userId = EmployeeTabbedMainActivity.getCurrentUId();
        usersDb = EmployeeTabbedMainActivity.getUsersDb();
        mUserDatabase = EmployeeTabbedMainActivity.getUserDatabase();

        mCategoryBtn = (Button) findViewById(R.id.category_button);
        mTypeBtn = (Button) findViewById(R.id.type_button);

        mCategorySpinner = (Spinner) findViewById(R.id.category_spinner);
        mTypeSpinner = (Spinner) findViewById(R.id.type_spinner);

        mCategoryField = (TextView) findViewById(R.id.category_textview);
        mTypeField = (TextView) findViewById(R.id.type_textview);

        preferenceCategoryList = new ArrayList<>();
        preferenceTypeList = new ArrayList<>();
        getCategoryPreferencesFromDB();
        getTypePreferencesFromDB();

        mCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initializeJobCategoryDialog();
                return;
            }
        });

        mTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initializeJobTypeDialog();
                return;
            }
        });
    }


    private void initializeJobTypeDialog(){
        jobTypeSpinnerList = new ArrayList<>();
        JobObject.populateJobTypeSpinnerList(jobTypeSpinnerList);

        final String[] items = new String[jobTypeSpinnerList.size()-1];
        final boolean[] checked_items = new boolean[items.length];
        final ArrayList typeItemsSelected_temp = new ArrayList();
        int i = 0;

        for(String s : jobTypeSpinnerList){
            if(!s.equals("")){
                items[i] = s;
                checked_items[i] = false;
                i++;
            }
        }

        if(preferenceTypeList.size()>0){
            i = 0;
            for(String s1 : items){
                for(String s2 : preferenceTypeList){
                    if(s1.equals(s2)){
                        checked_items[i]=true;
                        typeItemsSelected_temp.add(i);
                        break;
                    }
                }
                i++;
            }
        }


        alertDialogTitle = "Filter Job Search Types";
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeSettingsActivity.this);
        builder.setTitle(alertDialogTitle);
        builder.setMultiChoiceItems(items, checked_items,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedItemId,
                                        boolean isSelected) {
                        if (isSelected) {
                            typeItemsSelected_temp.add(selectedItemId);
                        } else if (typeItemsSelected_temp.contains(selectedItemId)) {
                            typeItemsSelected_temp.remove(Integer.valueOf(selectedItemId));
                            preferenceTypeList.remove(jobTypeSpinnerList.get((Integer)selectedItemId+1));
                        }

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Your logic here when OK button is clicked
                        // get Values in categoryItemsSelected arraylist
                        for(Object i : typeItemsSelected_temp){
                            Log.i(LOGTAG, i.toString());
                        }
                        typeItemsSelected = typeItemsSelected_temp;
                        saveTypePreferencesInsideDB();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void saveTypePreferencesInsideDB() {
        mUserDatabase.child("preferences").child("types").removeValue();
        DatabaseReference currentUserTypePreferencesDb = mUserDatabase.child("preferences").child("types");

        for(Object i : typeItemsSelected){
            currentUserTypePreferencesDb.child(jobTypeSpinnerList.get((Integer)i+1)).setValue(true);
            preferenceTypeList.add(jobTypeSpinnerList.get((Integer)i+1));
        }
    }
    
    private void getTypePreferencesFromDB(){
        preferenceTypeList.clear();
        DatabaseReference currentUserTypePreferencesDb = mUserDatabase.child("preferences").child("types");
        currentUserTypePreferencesDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot typePreference : dataSnapshot.getChildren()){
                        preferenceTypeList.add(typePreference.getKey().toString());
                        Log.i(LOGTAG, typePreference.getKey().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeJobCategoryDialog(){
        jobCategorySpinnerList = new ArrayList<>();
        JobObject.populateJobCategorySpinnerList(jobCategorySpinnerList);

        final String[] items = new String[jobCategorySpinnerList.size()-1];
        final boolean[] checked_items = new boolean[items.length];
        final ArrayList categoryItemsSelected_temp = new ArrayList();
        int i = 0;

        for(String s : jobCategorySpinnerList){
            if(!s.equals("")){
                items[i] = s;
                checked_items[i] = false;
                i++;
            }
        }

        if(preferenceCategoryList.size()>0){
            i = 0;
            for(String s1 : items){
                for(String s2 : preferenceCategoryList){
                    if(s1.equals(s2)){
                        checked_items[i]=true;
                        categoryItemsSelected_temp.add(i);
                        break;
                    }
                }
                i++;
            }
        }


        alertDialogTitle = "Filter Job Search Category";
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeSettingsActivity.this);
        builder.setTitle(alertDialogTitle);
        builder.setMultiChoiceItems(items, checked_items,
        new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selectedItemId,
                    boolean isSelected) {
                          if (isSelected) {
                              categoryItemsSelected_temp.add(selectedItemId);
                          } else if (categoryItemsSelected_temp.contains(selectedItemId)) {
                              categoryItemsSelected_temp.remove(Integer.valueOf(selectedItemId));
                              preferenceCategoryList.remove(jobCategorySpinnerList.get((Integer)selectedItemId+1));
                          }

                    }
            })
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //Your logic here when OK button is clicked
                    // get Values in categoryItemsSelected arraylist
                    for(Object i : categoryItemsSelected_temp){
                        Log.i(LOGTAG, i.toString());
                    }
                    categoryItemsSelected = categoryItemsSelected_temp;
                    saveCategoryPreferencesInsideDB();
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                }
            });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void saveCategoryPreferencesInsideDB() {
        mUserDatabase.child("preferences").child("categories").removeValue();
        DatabaseReference currentUserCategoryPreferencesDb = mUserDatabase.child("preferences").child("categories");

        for(Object i : categoryItemsSelected){
            currentUserCategoryPreferencesDb.child(jobCategorySpinnerList.get((Integer)i+1)).setValue(true);
            preferenceCategoryList.add(jobCategorySpinnerList.get((Integer)i+1));
        }
    }

    private void getCategoryPreferencesFromDB(){
        preferenceCategoryList.clear();
        DatabaseReference currentUserCategoryPreferencesDb = mUserDatabase.child("preferences").child("categories");
        currentUserCategoryPreferencesDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot categoryPreference : dataSnapshot.getChildren()){
                        preferenceCategoryList.add(categoryPreference.getKey().toString());
                        Log.i(LOGTAG, categoryPreference.getKey().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
