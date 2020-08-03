package com.example.jobfinder.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jobfinder.Cards.JobCard;
import com.example.jobfinder.Employee.EmployeeFragments.PreviewEmployeeProfileFragment;
import com.example.jobfinder.Employee.PreviewEmployeeProfileActivity;
import com.example.jobfinder.Employer.EditJobActivity;
import com.example.jobfinder.Employer.JobFragments.PreviewJobProfileFragment;
import com.example.jobfinder.Employer.PreviewJobProfileActivity;
import com.example.jobfinder.Matches.EmployeeMatches.EmployeeMatchesActivity;
import com.example.jobfinder.Matches.EmployerJobMatches.MatchesEmployeeObject;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final String DELETE_JOB = "DELETE JOB";
    private static final String LOGTAG = "UserRole";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    private EditText mSendEditText;
    private TextView mMatchName;

    private Button mSendButton;
    private ImageView mSendButtonIV, mBackArrowBtnIV, mDeleteMatchBtnIV, mMatchImage;
    private String currentUserID, matchId, chatId, jobId, employerId, userRole, oppositeUserRole;

    DatabaseReference mDatabaseUser, mDatabaseChat, usersDb, chatDb;

    private String alertDialogTitle, alertDialogMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        matchId = getIntent().getExtras().getString("matchId");
        userRole = getIntent().getExtras().getString("userRole");

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        if(userRole.equals("Employee")){
            oppositeUserRole = "Employer";
            employerId = getIntent().getExtras().getString("employerId");
            Log.i(LOGTAG, "sendMessage" + " employerId   " + employerId);
            Log.i(LOGTAG, "sendMessage" + " matchId  " + matchId);
            mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(currentUserID).child("connections").child("matches").child(employerId).child(matchId).child("chatId");
            FetchJobInformation(employerId, matchId);
        }
        else if(userRole.equals("Employer")){
            oppositeUserRole = "Employee";
            employerId = getIntent().getExtras().getString("employerId");
            jobId = getIntent().getExtras().getString("jobId");
            /*Log.i(LOGTAG, "sendMessage" + " employerId   " + employerId);
            Log.i(LOGTAG, "sendMessage" + " jobId  " + jobId);
            Log.i(LOGTAG, "sendMessage" + " matchId  " + matchId);*/
            mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(currentUserID).child("jobs").child(jobId).child("connections").child("matches").child(matchId).child("chatId");
            FetchEmployerJobMatchInformation(matchId);
        }

        mDatabaseChat = FirebaseDatabase.getInstance().getReference().child("Chat");

        getChatId();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new ChatAdapter(getDataSetChat(), ChatActivity.this);
        mRecyclerView.setAdapter(mChatAdapter);

        mSendEditText = findViewById(R.id.message);
        mSendButtonIV = findViewById(R.id.sendMessageButton);

        mMatchName = (TextView) findViewById(R.id.matchName_textview);
        mMatchImage = (ImageView) findViewById(R.id.matchImage_ImageView);

        mBackArrowBtnIV = (ImageView) findViewById(R.id.backArrow_imageview);
        mDeleteMatchBtnIV = (ImageView) findViewById(R.id.deleteMatchBtn_imageview);
        //mSendButton = findViewById(R.id.send);

        //mMatchImage

        //Hide keyboard when EditText field loses focus
        //https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext/19828165
        hideEditTextKeypadOnFocusChange();

        mSendButtonIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        mBackArrowBtnIV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
        mMatchImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(userRole.equals("Employee")){
                    Intent intent = new Intent(ChatActivity.this, PreviewJobProfileActivity.class);
                    Bundle b = new Bundle();
                    b.putString("jobId", matchId);
                    b.putString("employerId", employerId);
                    intent.putExtras(b);
                    startActivity(intent);
                    return;
                }
                else if(userRole.equals("Employer")){
                    Intent intent = new Intent(ChatActivity.this, PreviewEmployeeProfileActivity.class);
                    intent.putExtra("employeeId", matchId);
                    startActivity(intent);
                    return;
                }else{
                    Toast.makeText(ChatActivity.this, "Cannot open this profile", Toast.LENGTH_LONG).show();
                }

            }
        });
        mDeleteMatchBtnIV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //TODO: Implement Match delete
                alertDialogTitle = "Confirm Match Delete";
                alertDialogMessage = "Are you sure you want to DELETE this user from the Matches list?";
                PopAlerDialogMessage(alertDialogTitle, alertDialogMessage, DELETE_JOB);
                //Toast.makeText(ChatActivity.this, "Delete employee from matches", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void FetchJobInformation(final String employerId, final String key) {
        Log.i(LOGTAG, "FetchJobInformation" + "   " + employerId + "   " + key);
        DatabaseReference jobDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId).child("jobs").child(key);
        jobDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String jobId = dataSnapshot.getKey();
                    String title = "";
                    String jobImageUrl = "default";
                    if(dataSnapshot.child("title").getValue()!=null){
                        title = dataSnapshot.child("title").getValue().toString();
                        mMatchName.setText(title);
                    }
                    Glide.clear(mMatchImage);

                    if(dataSnapshot.child("jobImageUrl").getValue()!=null){
                        jobImageUrl = dataSnapshot.child("jobImageUrl").getValue().toString();
                        Glide.with(getApplication()).load(jobImageUrl).into(mMatchImage);
                        switch(jobImageUrl){
                            case "default":
                                Glide.with(getApplication()).load(R.drawable.placeholder_img).into(mMatchImage);
                                break;
                            default:
                                Glide.with(getApplication()).load(jobImageUrl).into(mMatchImage);
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

    private void FetchEmployerJobMatchInformation(String key) {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employee").child(key);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String userId = dataSnapshot.getKey();
                    String name = "";
                    String profileImageUrl = "";
                    if(dataSnapshot.child("name").getValue()!=null){
                        name = dataSnapshot.child("name").getValue().toString();
                        mMatchName.setText(name);
                    }
                    Glide.clear(mMatchImage);

                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                        Glide.with(getApplication()).load(profileImageUrl).into(mMatchImage);
                        switch(profileImageUrl){
                            case "default":
                                Glide.with(getApplication()).load(R.drawable.placeholder_img).into(mMatchImage);
                                break;
                            default:
                                Glide.with(getApplication()).load(profileImageUrl).into(mMatchImage);
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

    private void sendMessage() {
        String sendMessageText = mSendEditText.getText().toString();

        if(!sendMessageText.isEmpty()){
            DatabaseReference newMessageDb = mDatabaseChat.push();

            Map newMessage = new HashMap();
            newMessage.put("createdByUser", currentUserID);
            newMessage.put("text", sendMessageText);
            newMessageDb.setValue(newMessage);

            String messaged = usersDb.child(userRole).child(currentUserID).child("connections").child("matches").child(employerId).child(matchId).child("messaged").getKey();
            Log.i(LOGTAG, "sendMessage" + "   " + messaged);

            if(userRole.equals("Employee")){

                usersDb.child(userRole).child(currentUserID).child("connections").child("matches").child(employerId).child(matchId).child("messaged").setValue("true");
                usersDb.child(oppositeUserRole).child(employerId).child("jobs").child(matchId).child("connections").child("matches").child(currentUserID).child("messaged").setValue("true");
            }
            else if(userRole.equals("Employer")/* && !usersDb.child(oppositeUserRole).child(matchId).child("connections").child("matches").child(employerId).child(jobId).child("messaged").getKey().equals("true")*/){
                usersDb.child(userRole).child(currentUserID).child("jobs").child(jobId).child("connections").child("matches").child(matchId).child("messaged").setValue("true");
                usersDb.child(oppositeUserRole).child(matchId).child("connections").child("matches").child(employerId).child(jobId).child("messaged").setValue("true");
            }
        }
        mSendEditText.setText(null);
    }

    private void getChatId(){
        mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    chatId = dataSnapshot.getValue().toString();
                    mDatabaseChat = mDatabaseChat.child(chatId);
                    getChatMessages();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getChatMessages() {
        mDatabaseChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    String message = null;
                    String createdByUser = null;

                    if(dataSnapshot.child("text").getValue()!=null){
                        message = dataSnapshot.child("text").getValue().toString();
                    }
                    if(dataSnapshot.child("createdByUser").getValue()!=null){
                        createdByUser = dataSnapshot.child("createdByUser").getValue().toString();
                    }

                    if(message!=null && createdByUser!=null){
                        Boolean currentUserBoolean = false;
                        if(createdByUser.equals(currentUserID)){
                            currentUserBoolean = true;
                        }
                        ChatObject newMessage = new ChatObject(message, currentUserBoolean);
                        resultsChat.add(newMessage);
                        mChatAdapter.notifyDataSetChanged();
                    }
                }

            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private ArrayList<ChatObject> resultsChat = new ArrayList<ChatObject>();
    private List<ChatObject> getDataSetChat() {
        return resultsChat;
    }

    /*public void removeAt(int position) {
        matchesList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }*/

    //Delete Match from Employer User Job matches
    public void deleteMatch(final String userID){
        //Itt implementció kérdése, hogy hogyan oldjuk ezt meg.
        //Ha azt szeretnénk, hogy egy match törlése után az Employee-nak ne dobja fel megint azt az állást, akkor nem töröljük az Employee-connections-liked adatbázisából a jobID-t
        //Ha azt szeretnénk, hogy törlés után a Job-hoz már ne dobja fel azt a felhasználót, akkor benne hagyjuk a liked-nál a userID-t
        //Most én annyit csinálok, hogy a matches listában már nem jelenítem meg, de többet nem dobja fel se az Employee felhasználónak se az adott Job-nak a másikat.
        final DatabaseReference jobEmployeeMatchDb = usersDb.child(userRole).child(currentUserID).child("jobs").child(jobId).child("connections").child("matches").child(userID);
        jobEmployeeMatchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    final String chatID = dataSnapshot.child("chatId").getValue().toString();
                    deleteChatDataOnJobDelete(chatID);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        jobEmployeeMatchDb.removeValue();
        usersDb.child("Employee").child(userID).child("connections").child("matches").child(employerId).child(jobId).removeValue();
    }

    //Delete Match from Employee user matches
    public void deleteMatch(final String employerId, final String jobId){
        //Itt implementció kérdése, hogy hogyan oldjuk ezt meg.
        //Ha azt szeretnénk, hogy egy match törlése után az Employee-nak ne dobja fel megint azt az állást, akkor nem töröljük az Employee-connections-liked adatbázisából a jobID-t
        //Ha azt szeretnénk, hogy törlés után a Job-hoz már ne dobja fel azt a felhasználót, akkor benne hagyjuk a liked-nál a userID-t
        //Most én annyit csinálok, hogy a matches listában már nem jelenítem meg, de többet nem dobja fel se az Employee felhasználónak se az adott Job-nak a másikat.
        final DatabaseReference employeeJobMatchDb = usersDb.child("Employee").child(currentUserID).child("connections").child("matches").child(employerId).child(jobId);
        employeeJobMatchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    final String chatID = dataSnapshot.child("chatId").getValue().toString();
                    deleteChatDataOnJobDelete(chatID);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        employeeJobMatchDb.removeValue();
        usersDb.child("Employer").child(employerId).child("jobs").child(jobId).child("connections").child("matches").child(currentUserID).removeValue();
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
    }

    public void PopAlerDialogMessage(String title, String message, String callMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);

        builder.setTitle(title);
        builder.setMessage(message);

        if(callMessage.equals(DELETE_JOB)){
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    //final String employerId = resultsEmployeeMatches.get(viewHolder.getAdapterPosition()).getEmployerId();
                    //final String jobId = resultsEmployeeMatches.get(viewHolder.getAdapterPosition()).getJobId();
                    if(userRole.equals("Employee")){
                        deleteMatch(employerId, matchId);
                    }
                    else if(userRole.equals("Employer")){
                        deleteMatch(matchId);
                    }
                    dialog.dismiss();
                    finish();
                    Toast.makeText(ChatActivity.this, "Deleted from matches", Toast.LENGTH_LONG).show();
                    return;
                }
            });
        }

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //RefreshRecyclerViewList();
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideEditTextKeypadOnFocusChange(){
        mSendEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }
}
