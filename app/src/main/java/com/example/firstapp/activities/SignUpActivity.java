package com.example.firstapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.firstapp.R;
import com.example.firstapp.databinding.ActivitySignInBinding;
import com.example.firstapp.databinding.ActivitySignUpBinding;
import com.example.firstapp.utulities.Constants;
import com.example.firstapp.utulities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
private ActivitySignUpBinding binding;
private PreferenceManager preferenceManager;
private String encodedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
    }
    private void setListener(){
        binding.textsignin.setOnClickListener(v ->
                onBackPressed());
        binding.btnsignup.setOnClickListener(v ->{
            if (isvalidsignupdetail()){
                Signup();
            }
                }
        );
        binding.layoutImage.setOnClickListener(v ->{


                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
        });

    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    public void Signup(){
loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String,Object>user=new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
loading(false);
preferenceManager.putBoolean(Constants.is_SIGNED_IN,true);
preferenceManager.putstring(Constants.KEY_USERID,documentReference.getId());
preferenceManager.putstring(Constants.KEY_NAME,binding.inputName.getText().toString());
preferenceManager.putstring(Constants.KEY_IMAGE,encodedImage);
Intent intent=new Intent(getApplicationContext(),MainActivity.class);
intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(intent);
                })
                .addOnFailureListener(Exception ->{
loading(false);
showToast(Exception.getMessage());
                });

    }
    public Boolean isvalidsignupdetail(){
if (encodedImage == null){
showToast("Select Profile Image");
return false;

}
else if (binding.inputName.getText().toString().trim().isEmpty()){
showToast("Enter Name");
return false;
}
else if (binding.inputEmail.getText().toString().trim().isEmpty()){
    showToast("Enter Email");
    return false;
}
else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
    showToast("Enter valid Email");
    return false;
}
else if (binding.inputPassword.getText().toString().trim().isEmpty()){
    showToast("Enter Password");
    return false;
}
else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
    showToast("Confirm your Password");
    return false;
}
else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())){
    showToast("Password & Confirm Password must Matches!...");
    return false;
}
else {
    return true;
}
    }
    public void loading(Boolean isloading){
if (isloading){
    binding.btnsignup.setVisibility(View.INVISIBLE);
    binding.progress.setVisibility(View.VISIBLE);
}else {
    binding.btnsignup.setVisibility(View.VISIBLE);
    binding.progress.setVisibility(View.INVISIBLE);
}
    }
    private String EncodedImage(Bitmap bitmap){
        int previewwidth=150;
        int previewHeight= bitmap.getHeight()*previewwidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewwidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50, byteArrayOutputStream );
        byte[]bytes=byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);

    }
    private final ActivityResultLauncher<Intent> pickImage =registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->
            {
            if (result.getResultCode()==RESULT_OK){
                if (result.getData() != null){
                    Uri imageUri =result.getData().getData();
                    try {
                        InputStream inputStream=getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                        binding.profileimage.setImageBitmap(bitmap);
                        binding.textaddimage.setVisibility(View.GONE);
                        encodedImage=EncodedImage(bitmap);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
            }
            }

    );
}