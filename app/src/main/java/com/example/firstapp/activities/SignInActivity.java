package com.example.firstapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.firstapp.R;
import com.example.firstapp.databinding.ActivitySignInBinding;
import com.example.firstapp.utulities.Constants;
import com.example.firstapp.utulities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {
private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignInBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.is_SIGNED_IN)){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(binding.getRoot());
setListener();
    }
    private void setListener (){
        binding.text.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
binding.btnsignin.setOnClickListener(v ->{
    if (isvalidsignindetail()){
        Signin();
    }
});
    }
    private void Signin(){
loading(true);
FirebaseFirestore database =FirebaseFirestore.getInstance();
database.collection(Constants.KEY_COLLECTION_USERS)
        .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
        .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()&& task.getResult() != null && task.getResult().getDocuments().size()>0){
                DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                preferenceManager.putBoolean(Constants.is_SIGNED_IN,true);
                preferenceManager.putstring(Constants.KEY_USERID,documentSnapshot.getId());
                preferenceManager.putstring(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                preferenceManager.putstring(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
            else {
                loading(false);
                showToast("Unable to Sign In");
            }
        });
    }
    public void loading(Boolean isloading){
        if (isloading){
            binding.btnsignin.setVisibility(View.INVISIBLE);
            binding.progress.setVisibility(View.VISIBLE);
        }else {
            binding.btnsignin.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.INVISIBLE);
        }
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    public Boolean isvalidsignindetail() {

        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid Email");
            return false;
        } else {
            return true;
        }

    }
}