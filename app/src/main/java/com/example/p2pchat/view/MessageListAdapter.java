package com.example.p2pchat.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.p2pchat.R;
import com.example.p2pchat.model.MessageEntity;
import com.example.p2pchat.viewmodel.ChatPageViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {
    private static final int SENT = 0;
    private static final int RECEIVED = 1;

    private ChatPageViewModel model;

    private Context context;


    private List<MessageEntity> messageEntities;

    MessageListAdapter(List<MessageEntity> messageEntities,Context context) {
        this.messageEntities = messageEntities;
        this.context = context;

    }


    @Override
    public int getItemViewType(int position) {
        if (messageEntities.get(position).isSentByMe())
            return SENT;
        return RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        int layoutId = (i == SENT) ? R.layout.sent_message_holder : R.layout.received_message_holder;

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);

        model = ViewModelProviders.of((FragmentActivity) context).get(ChatPageViewModel.class);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {
        final MessageEntity m = messageEntities.get(i);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("EEE, MMM dd  |  hh:mm a");


        messageViewHolder.text.setText(m.getMessage());
        messageViewHolder.date.setText(format.format(m.getDate()));
        messageViewHolder.text.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Snackbar snackbar = Snackbar.make(v,"Delete message?", Snackbar.LENGTH_LONG)
                        .setAction("DELETE", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                model.deleteMessage(m);
                            }
                        });
                snackbar.setActionTextColor(Color.RED);
                snackbar.show();
                return true;
            }
        });

        messageViewHolder.setDateVisibility(m.isDateVisible());
    }

    @Override
    public int getItemCount() {
        return messageEntities.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text;
        TextView date;

        MessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.messageText);
            date = itemView.findViewById(R.id.date);
            text.setOnClickListener(this);
        }

        void setDateVisibility(Boolean p) {
            if (p) {
                date.setVisibility(View.VISIBLE);
            } else {
                date.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            MessageEntity entity = messageEntities.get(getAdapterPosition());
            if (entity.isDateVisible()) {
                entity.setDateVisible(false);
                date.setVisibility(View.GONE);
                return;
            }

            entity.setDateVisible(true);
            date.setVisibility(View.VISIBLE);
        }
    }


    void updateData(List<MessageEntity> lst) {
        messageEntities = lst;
        notifyDataSetChanged();
    }
}
