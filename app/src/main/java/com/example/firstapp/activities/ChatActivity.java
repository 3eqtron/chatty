package com.example.firstapp.activities;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.example.firstapp.adapters.ChatAdapter;
import com.example.firstapp.databinding.ActivityChatBinding;
import com.example.firstapp.models.ChatMessage;
import com.example.firstapp.models.User;
import com.example.firstapp.utulities.Constants;
import com.example.firstapp.utulities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Date;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends BaseActivity {
private ActivityChatBinding binding;
private User recieveruser;
private List<ChatMessage> chatMessages;
private ChatAdapter chatAdapter;
private PreferenceManager preferenceManager;
private FirebaseFirestore database;
private String conversationId=null;
private Boolean isrecievedavailable=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
LoadRecieverdetails();
setListeners();
init();
listMessages();
    }
    private void init(){
        preferenceManager =new PreferenceManager(getApplicationContext());
        chatMessages=new ArrayList<>();
        chatAdapter =new ChatAdapter(
                        chatMessages, preferenceManager.getstring(Constants.KEY_USERID),
                getBitmapfromencodedString(recieveruser.image)
                );
        binding.chatrecyclerview.setAdapter(chatAdapter);
        database =FirebaseFirestore.getInstance();
    }

    private void sendmessage(){
        HashMap<String ,Object>message =new HashMap<>();
        message.put(Constants.SENDER_ID,preferenceManager.getstring(Constants.KEY_USERID));
        message.put(Constants.RECIEVER_ID,recieveruser.id);
        message.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIME_STAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
    if (conversationId !=null){
        updateconversion(binding.inputMessage.getText().toString());
    }
    else {
        HashMap<String ,Object> conversion =new HashMap<>();
        conversion.put(Constants.SENDER_ID,preferenceManager.getstring(Constants.KEY_USERID));
        conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getstring(Constants.KEY_NAME));
        conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getstring(Constants.KEY_IMAGE));
        conversion.put(Constants.RECIEVER_ID,recieveruser.id);
        conversion.put(Constants.KEY_RECIEVER_NAME,recieveruser.name);
        conversion.put(Constants.KEY_RECIEVER_IMAGE,recieveruser.image);
        conversion.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
        conversion.put(Constants.KEY_TIME_STAMP,new Date());
        addconversion(conversion);
    }
        binding.inputMessage.setText(null);


    }
private void listenavailabiltyreciever(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                recieveruser.id
        ).addSnapshotListener(ChatActivity.this, (value ,error)->{
            if (error !=null){
                return;
            }
            if (value != null){
                if (value.getLong(Constants.KEY_Avalability)!= null){
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_Avalability)
                    ).intValue();
                    isrecievedavailable =availability ==1;
                }
                recieveruser.token =value.getString(Constants.KEY_FCM_TOKEN);
            }
            if (isrecievedavailable)
            {
                binding.textavalability.setVisibility(View.VISIBLE);
            }else {
                binding.textavalability.setVisibility(View.GONE);
            }

        });
}
    private void listMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.SENDER_ID,preferenceManager.getstring(Constants.KEY_USERID))
                .whereEqualTo(Constants.RECIEVER_ID,recieveruser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.SENDER_ID,recieveruser.id)
                .whereEqualTo(Constants.RECIEVER_ID,preferenceManager.getstring(Constants.KEY_USERID))
                .addSnapshotListener(eventListener);
    }


    private final EventListener <QuerySnapshot> eventListener=(value , error)-> {
        if (error != null){
            return ;
        }
        if (value !=null){
      int count =chatMessages.size();
      for (DocumentChange documentChange : value.getDocumentChanges()){

          if (documentChange.getType() == DocumentChange.Type.ADDED){
              ChatMessage chatMessage=new ChatMessage();
              chatMessage.senderid=documentChange.getDocument().getString(Constants.SENDER_ID);
              chatMessage.recieverid=documentChange.getDocument().getString(Constants.RECIEVER_ID);
              chatMessage.message=documentChange.getDocument().getString(Constants.KEY_MESSAGE);
              chatMessage.datetime=getReadableDate(documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP));
              chatMessage.dateObject=documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP);
              chatMessages.add(chatMessage);
          }
      }
      Collections.sort(chatMessages,(obj1 , obj2) -> obj1.dateObject.compareTo(obj2.dateObject));

      if (count ==0){
          chatAdapter.notifyDataSetChanged();
      }else {
          chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
          binding.chatrecyclerview.smoothScrollToPosition(chatMessages.size() - 1);

      }
      binding.chatrecyclerview.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE );
    if (conversationId == null){
        checkforconversion();
    }
    };


    private Bitmap getBitmapfromencodedString(String encodedImage){
byte[] bytes= Base64.decode(encodedImage ,Base64.DEFAULT);
return BitmapFactory.decodeByteArray(bytes , 0,bytes.length);
    }

    private void LoadRecieverdetails(){
        recieveruser =(User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(recieveruser.name);
    }
    private void setListeners(){
        binding.imageback.setOnClickListener(v -> onBackPressed());
        binding.layoutsend.setOnClickListener(v ->sendmessage());
    }
    private String getReadableDate(Date date){
        return new SimpleDateFormat("MMMM dd ,yyyy - hh:mm a", Locale.getDefault()).format(date);

    }
    private void addconversion(HashMap<String ,Object> conversion){
database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
        .add(conversion)
        .addOnSuccessListener(documentReference -> conversationId =documentReference.getId());
    }
    private void updateconversion(String message){
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE,message,
                Constants.KEY_TIME_STAMP,new Date()
        );
    }
    private void checkforconversion(){

        if (chatMessages.size() != 0){
            checkforconversationremotly(
                    preferenceManager.getstring(Constants.KEY_USERID),
                    recieveruser.id
            );
            checkforconversationremotly(
                    recieveruser.id ,
                    preferenceManager.getstring(Constants.KEY_USERID)
            );
        }
    }
    private void checkforconversationremotly(String senderId,String recieverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.SENDER_ID,senderId)
                .whereEqualTo(Constants.RECIEVER_ID,recieverId)
                .get()
                .addOnCompleteListener(conversioncompleteListener);

    }


    private final OnCompleteListener<QuerySnapshot> conversioncompleteListener =task -> {
        if (task.isSuccessful() && task.getResult() !=null && task.getResult().getDocuments().size() >0){
            DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
            conversationId =documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenavailabiltyreciever();
    }
}