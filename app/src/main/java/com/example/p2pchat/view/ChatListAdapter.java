package com.example.p2pchat.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.p2pchat.Constants;
import com.example.p2pchat.R;
import com.example.p2pchat.model.ChatHistoryEntity;
import com.example.p2pchat.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatHolder> {

    private List<ChatHistoryEntity> chats;
    private OnChatClickListener listener;

    private MainViewModel model;
    private Context context;


    ChatListAdapter(Context context) {
        super();
        this.context = context;
        chats = new ArrayList<>();
        listener = new OnChatClickListener() {
            @Override
            public void onChatClick(ChatHistoryEntity chat, Context context) {
                SimpleDateFormat format = new SimpleDateFormat("MMM dd  |  hh:mm a");
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(Constants.ADDRESAT_NAME, chat.getName());
                intent.putExtra(Constants.IS_OFFLINE, true);
                intent.putExtra(Constants.DATE, format.format(chat.getStartDate()));
                context.startActivity(intent);
            }
        };
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_history_item_holder, viewGroup, false);
        model = ViewModelProviders.of((FragmentActivity) context).get(MainViewModel.class);
        return new ChatHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder chatHolder, int i) {
        ChatHistoryEntity chat = chats.get(i);
        SimpleDateFormat format = new SimpleDateFormat("MMM dd | hh:mm a");
        chatHolder.messageCount.setText(String.valueOf(chat.getMessageCount()));
        chatHolder.startDate.setText("Last seen "+format.format(chat.getStartDate()));
        chatHolder.name.setText(chat.getName());


    }


    @Override
    public int getItemCount() {
        return chats.size();
    }

    void updateData(List<ChatHistoryEntity> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    public class ChatHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView startDate;
        TextView messageCount;
        OnChatClickListener listener;

        ChatHolder(@NonNull View itemView, OnChatClickListener listener) {
            super(itemView);
            this.name = itemView.findViewById(R.id.chatName);
            this.startDate = itemView.findViewById(R.id.startDate);
            this.messageCount = itemView.findViewById(R.id.messageCount);
            this.listener = listener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final Snackbar snackbar = Snackbar.make(v,"Delete conversation?", Snackbar.LENGTH_LONG)
                            .setAction("DELETE", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    model.deleteChat(name.getText().toString());
                                }
                            });
                    snackbar.setActionTextColor(Color.RED);
                    snackbar.show();

                    return true;
                }
            });
        }

        @Override
        public void onClick(View view) {
            listener.onChatClick(chats.get(getAdapterPosition()), view.getContext());
        }
    }


    public interface OnChatClickListener {
        void onChatClick(ChatHistoryEntity chat, Context context);
    }
}
