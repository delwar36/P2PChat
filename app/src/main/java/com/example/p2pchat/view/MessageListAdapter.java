package com.example.p2pchat.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.p2pchat.R;
import com.example.p2pchat.model.MessageEntity;

import java.text.SimpleDateFormat;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {
    private static final int SENT = 0;
    private static final int RECEIVED = 1;


    private List<MessageEntity> messageEntities;

    MessageListAdapter(List<MessageEntity> messageEntities) {
        this.messageEntities = messageEntities;

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
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {
        MessageEntity m = messageEntities.get(i);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        messageViewHolder.text.setText(m.getMessage());
        messageViewHolder.date.setText(format.format(m.getDate()));
        messageViewHolder.setDateVisibility(m.isDateVisible());
    }

    @Override
    public int getItemCount() {
        return messageEntities.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text;
        TextView date;

        MessageViewHolder(@NonNull View itemView) {
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
