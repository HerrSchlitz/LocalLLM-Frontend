package com.meshnet.llm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends BaseAdapter {

    private Context context;
    private List<ChatMessage> messages;
    private List<String> chatNames;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(int position, ChatMessage chat);
        void onLongClick(int position, ChatMessage chat);
    }

    public ChatAdapter(Context context, List<ChatMessage> messages, List<String> chatNames, OnChatClickListener listener) {
        this.context = context;
        this.messages = messages;
        this.chatNames = chatNames;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    android.R.layout.simple_list_item_multiple_choice, parent, false);
            holder = new ViewHolder();
            holder.messageText = convertView.findViewById(android.R.id.text1);
            holder.nameText = convertView.findViewById(android.R.id.text2);
            holder.button = convertView.findViewById(android.R.id.button1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ChatMessage message = messages.get(position);
        String name = chatNames.get(position);

        holder.messageText.setText(message.getMessage());
        holder.nameText.setText(name);
        holder.button.setVisibility(View.VISIBLE);

        return convertView;
    }

    private static class ViewHolder {
        TextView messageText;
        TextView nameText;
        Button button;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return messages.isEmpty();
    }
}
