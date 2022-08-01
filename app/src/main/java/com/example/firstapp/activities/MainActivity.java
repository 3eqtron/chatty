package com.example.firstapp.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.firstapp.adapters.RecentConversationAdapter;
import com.example.firstapp.databinding.ActivityMainBinding;
import com.example.firstapp.listeners.ConversionListener;
import com.example.firstapp.models.ChatMessage;
import com.example.firstapp.models.User;
import com.example.firstapp.utulities.Constants;
import com.example.firstapp.utulities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {
ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        loaduserdetails();
        getToken();
        init();
        setListener();
        listenConversation();
    }
    private void listenConversation(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.SENDER_ID,preferenceManager.getstring(Constants.KEY_USERID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.RECIEVER_ID,preferenceManager.getstring(Constants.KEY_USERID))
                .addSnapshotListener(eventListener);
    }
    private void init(){
        conversations =new ArrayList<>();
        conversationAdapter =new RecentConversationAdapter(conversations,this);
        binding.conversatioRecyclerview.setAdapter(conversationAdapter);
        database=FirebaseFirestore.getInstance();
    }
    private void setListener(){
        binding.imagesignout.setOnClickListener(v -> Signout());
        binding.fabnewchat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
    }
    private void loaduserdetails(){
        binding.textname.setText(preferenceManager.getstring(Constants.KEY_NAME));
        byte[] bytes= Base64.decode(preferenceManager.getstring(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.profileimage.setImageBitmap(bitmap);

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private final EventListener<QuerySnapshot> eventListener =((value, error) -> {
       if (error !=null){
           return;
       }
       if (value !=null){
           for (DocumentChange documentChange : value.getDocumentChanges()){
               if (documentChange.getType() == DocumentChange.Type.ADDED){
                   String senderId=documentChange.getDocument().getString(Constants.SENDER_ID);
                   String recieverId=documentChange.getDocument().getString(Constants.RECIEVER_ID);
                   ChatMessage chatMessage=new ChatMessage();
                   chatMessage.senderid=senderId;
                   chatMessage.recieverid=recieverId;
                   if (preferenceManager.getstring(Constants.KEY_USERID).equals(senderId)){
chatMessage.conversionImage=documentChange.getDocument().getString(Constants.KEY_RECIEVER_IMAGE);
chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_RECIEVER_NAME);
chatMessage.conversionId=documentChange.getDocument().getString(Constants.RECIEVER_ID);
                   }
                   else {
                       chatMessage.conversionImage=documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                       chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                       chatMessage.conversionId=documentChange.getDocument().getString(Constants.SENDER_ID);
                   }
                   chatMessage.message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                   chatMessage.dateObject=documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP);
                   conversations.add(chatMessage);
               }
               else if (documentChange.getType()==DocumentChange.Type.MODIFIED){
                   for (int i= 0 ;i < conversations.size();i++){
                       String senderId=documentChange.getDocument().getString(Constants.SENDER_ID);
                       String recieverId=documentChange.getDocument().getString(Constants.RECIEVER_ID);
                       if (conversations.get(i).senderid.equals(senderId)&& conversations.get(i).recieverid.equals(recieverId)){
                           conversations.get(i).message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                           conversations.get(i).dateObject=documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP);
                           break;
                       }

                   }
               }
           }
           Collections.sort(conversations,(obj1,obj2)->obj2.dateObject.compareTo(obj1.dateObject));
           conversationAdapter.notifyDataSetChanged();
           binding.conversatioRecyclerview.smoothScrollToPosition(0);
           binding.conversatioRecyclerview.setVisibility(View.VISIBLE);
           binding.progressBar.setVisibility(View.GONE);
       }
    });

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updatetoken);

    }
    private void updatetoken(String token){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(Constants.KEY_USERID).document(
                preferenceManager.getstring(Constants.KEY_USERID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to Update Token"));
    }
    private void Signout(){
        showToast("Signin out...");
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getstring(Constants.KEY_USERID)
        );
        HashMap<String , Object> updates =new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN , FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to Sign Out"));
    }

    @Override
    public void conversionclicked(User user) {
        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}