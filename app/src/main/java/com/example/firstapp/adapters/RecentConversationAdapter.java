package com.example.firstapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstapp.databinding.ItemContainerRecentConversionBinding;
import com.example.firstapp.listeners.ConversionListener;
import com.example.firstapp.models.ChatMessage;
import com.example.firstapp.models.User;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversionViewholder>{
    private List<ChatMessage> chatMessages;
private final ConversionListener conversionListener;
    public RecentConversationAdapter(List<ChatMessage> chatMessages,ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener=conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewholder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewholder holder, int position) {
holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewholder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversionBinding binding;
        ConversionViewholder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding=itemContainerRecentConversionBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.imageprofile.setImageBitmap(getconversationimage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.TextRecentmessage.setText(chatMessage.message);
binding.getRoot().setOnClickListener(v -> {
    User user=new User();
    user.id= chatMessage.conversionId;
    user.name=chatMessage.conversionName;
    user.image=chatMessage.conversionImage;
    conversionListener.conversionclicked(user);
});
        }
    }

    private Bitmap getconversationimage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
