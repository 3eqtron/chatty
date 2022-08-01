package com.example.firstapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstapp.databinding.ItemContainerRecieverBinding;
import com.example.firstapp.databinding.ItemContainerSentMessageBinding;
import com.example.firstapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final List<ChatMessage> chatMessages;
    public final String senderId;
    private final Bitmap recieverprofileimage;
    public static final int VIEW_TYPE_SENT =1;
    public static final int VIEW_TYPE_RECIEVER =2;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType ==VIEW_TYPE_SENT){
            return new sendMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext())  ,parent,false )
         );
        }

       else {
           return new RecieverMessageViewHolder(
                   ItemContainerRecieverBinding.inflate(
                           LayoutInflater.from(parent.getContext()),
                           parent,false
                   )
           );
        }
        }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
if (getItemViewType(position) == VIEW_TYPE_SENT){
    ((sendMessageViewHolder)holder).setData(chatMessages.get(position));
}else {
    ((RecieverMessageViewHolder)holder).setData(chatMessages.get(position),recieverprofileimage);
}
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderid.equals(senderId)){
return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECIEVER;
        }
    }

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, Bitmap recieverprofileimage) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.recieverprofileimage = recieverprofileimage;
    }

    static class sendMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;
        sendMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding=itemContainerSentMessageBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.txtmessage.setText(chatMessage.message);
            binding.textdatetime.setText(chatMessage.datetime);

        }
    }
    static class RecieverMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerRecieverBinding binding;
        RecieverMessageViewHolder(ItemContainerRecieverBinding itemContainerRecieverBinding){
            super(itemContainerRecieverBinding.getRoot());
            binding=itemContainerRecieverBinding;
        }
        void setData(ChatMessage chatMessage ,Bitmap recieverprofileimage){
binding.textmessage.setText(chatMessage.message);
binding.textdatetime.setText(chatMessage.datetime);
binding.imageprofile.setImageBitmap(recieverprofileimage);
        }
    }
}
