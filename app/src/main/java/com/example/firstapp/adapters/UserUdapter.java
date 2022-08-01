package com.example.firstapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstapp.databinding.ItemContainerUserBinding;
import com.example.firstapp.listeners.UserListener;
import com.example.firstapp.models.User;

import java.util.List;

public class UserUdapter extends RecyclerView.Adapter<UserUdapter.UserViewHolder> {
    private final List<User> users;
    private  final UserListener userListener;

    public UserUdapter(List<User> users,UserListener userListener) {
        this.users = users;
        this.userListener=userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding=ItemContainerUserBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

ItemContainerUserBinding binding;
UserViewHolder(ItemContainerUserBinding itemContainerUserBinding){
    super(itemContainerUserBinding.getRoot());
    binding =itemContainerUserBinding;
}
void setUserData(User user){
    binding.textName.setText(user.name);
    binding.TextEmail.setText(user.email);
    binding.imageprofile.setImageBitmap(getUserImage(user.image));
    binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));

}
        }
        private Bitmap getUserImage (String encodedImage){
        byte[] bytes= Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes ,0 ,bytes.length);
    }
}
