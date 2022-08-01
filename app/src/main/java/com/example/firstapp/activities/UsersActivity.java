package com.example.firstapp.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.firstapp.adapters.UserUdapter;
import com.example.firstapp.databinding.ActivityUsersBinding;
import com.example.firstapp.listeners.UserListener;
import com.example.firstapp.models.User;
import com.example.firstapp.utulities.Constants;
import com.example.firstapp.utulities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
public class UsersActivity extends BaseActivity implements UserListener {
private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListenr();
        getUser();
    }
    private void setListenr(){
        binding.back.setOnClickListener(v ->onBackPressed());
    }
    private void getUser(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String Current_user_id=preferenceManager.getstring(Constants.KEY_USERID);
                    if (task.isSuccessful()&& task.getResult() !=null){
                        List<User> users=new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot :task.getResult()){
                            if (Current_user_id.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user=new User();
                            user.name=queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email=queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image=queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token=queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id=queryDocumentSnapshot.getId();
                            users.add(user);

                        }
                        if (users.size()>0){
                            UserUdapter userUdapter=new UserUdapter(users, this);
                            binding.usersrecycler.setAdapter(userUdapter);
                            binding.usersrecycler.setVisibility(View.VISIBLE);
                        }else {
                            showerrormessage();
                        }
                    }else {
                        showerrormessage();
                    }
                });
    }
    public void showerrormessage(){
        binding.texterrormessaage.setText(String.format("%s","No user is available..."));
        binding.texterrormessaage.setVisibility(View.VISIBLE);

    }
    public void loading(Boolean isloading){
        if (isloading){

            binding.progressBar.setVisibility(View.VISIBLE);
        }else {

            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent= new Intent(getApplicationContext(),ChatActivity.class);
intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}