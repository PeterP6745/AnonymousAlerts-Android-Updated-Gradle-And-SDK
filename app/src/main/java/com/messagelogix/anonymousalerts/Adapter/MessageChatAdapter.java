package com.messagelogix.anonymousalerts.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.model.MessageChat;

import java.util.List;

/**
 * Created by Jeremy on 6/22/2017.
 */
public class MessageChatAdapter extends BaseAdapter {
    private Context context;

    private List<MessageChat> messageChatItems;

    public MessageChatAdapter(Context context, List<MessageChat> messageChatItems) {

        this.context = context;
        this.messageChatItems = messageChatItems;
    }

    @Override
    public int getCount() {

        return messageChatItems.size();
    }

    @Override
    public Object getItem(int position) {

        return messageChatItems.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /**
         * The following list not implemented reusable list items as list items
         * are showing incorrect data Add the solution if you have one
         * */
        MessageChat m = messageChatItems.get(position);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        // Identifying the message owner
        if (messageChatItems.get(position).isSelf()) {
            // message belongs to you, so load the right aligned layout
            convertView = mInflater.inflate(R.layout.list_item_message_left, null);
        } else {
            // message belongs to other person, load the left aligned layout
            convertView = mInflater.inflate(R.layout.list_item_message_right, null);
        }
        TextView lblFrom = (TextView) convertView.findViewById(R.id.lblMsgFrom);
        TextView txtMsg = (TextView) convertView.findViewById(R.id.txtMsg);
        txtMsg.setText(m.getMessage());
        lblFrom.setText(m.getFromName());
        return convertView;
    }
}
