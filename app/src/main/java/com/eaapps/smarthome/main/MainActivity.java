package com.eaapps.smarthome.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.eaapps.smarthome.ClickListener;
import com.eaapps.smarthome.R;
import com.eaapps.smarthome.databinding.ActivityMainBinding;
import com.eaapps.smarthome.model.ChatModel;
import com.eaapps.smarthome.model.Message;
import com.eaapps.smarthome.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ClickListener {

    private DatabaseReference messageRef;
    private DatabaseReference userRef;
    private ChatAdapter adapter;
    private FirebaseUser user;
    private User userModel;
    private String uid;
    private List<ChatModel> chatModelList;
    private FirebaseAuth mAuth;
    private ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainBinding.setClickListener(this);

        chatModelList = new ArrayList<>();

        adapter = new ChatAdapter(this);

        mainBinding.rc.setLayoutManager(new LinearLayoutManager(this));

        mainBinding.rc.setAdapter(adapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        messageRef = ref.child("Message");
        userRef = ref.child("User");
        getUser();

        getMessage();
    }

    public void getUser() {
        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userModel = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getMessage() {
        messageRef.addChildEventListener(new ChildEventListener() {
            Message message;

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.hasChildren()) {
                    message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        chatModelList.add(new ChatModel(message.getText(), "Code Executed"));
                        adapter.updateList(chatModelList);
                    }
                }
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void click(View v) {
        DatabaseReference refM = messageRef.push();
        String mText = mainBinding.message.getText().toString();
        if (!mText.isEmpty()) {
            Message message = new Message(refM.getKey(), user.getUid(), mText);
            refM.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mainBinding.message.setText("");
                    }
                }
            });
        }
    }
}
