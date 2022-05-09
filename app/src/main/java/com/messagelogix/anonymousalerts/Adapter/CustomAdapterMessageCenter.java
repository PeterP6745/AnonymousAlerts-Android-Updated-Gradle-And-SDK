package com.messagelogix.anonymousalerts.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.model.Message;

import java.util.List;

/**
 * Created by Jeremy on 6/21/2017.
 */
public class CustomAdapterMessageCenter extends ArrayAdapter<Message.MessageItem> {
    Context context;
    //ArrayList<MessageSummaryItem> messages;
    List<Message.MessageItem> messages;

    public CustomAdapterMessageCenter(Context mContext,List<Message.MessageItem> data){
        super(mContext, R.layout.list_item_message_center, data);
        messages = data;
        context = mContext;

        Log.d("ADAPTER MESSAGES SIZE", "" + messages.size());
        Log.d("ADAPTER MESSAGES DATA", "" + messages.get(0).getMessage());
    }

    // View lookup cache
    private static class ViewHolder {
        TextView txtDate;
        TextView txtMessage;
        TextView txtCode;
     //   TextView txtStatus;
        ImageView iconIndicatorImageView;
    }

    @Override
    public int getCount() {
        if(messages.size()<1){
           return 0;
        }
        return messages.size();
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
       // MessageSummaryItem messageSummaryItem = messages.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_message_center, parent, false);
            viewHolder.txtDate = (TextView) convertView.findViewById(R.id.message_center_date);
            viewHolder.txtMessage = (TextView) convertView.findViewById(R.id.message_center_message);
            viewHolder.txtCode = (TextView) convertView.findViewById(R.id.message_center_confirmation_code);

            TextView incidentLabel = (TextView) convertView.findViewById(R.id.mc_incident_label);
            incidentLabel.setText(context.getString(R.string.incident_colon));

            TextView codeLabel = (TextView) convertView.findViewById(R.id.mc_code_label);
            codeLabel.setText(context.getString(R.string.code));

           // viewHolder.txt
            // = (TextView) convertView.findViewById(R.id.message_center_status);
            viewHolder.iconIndicatorImageView = (ImageView) convertView.findViewById(R.id.message_center_icon);
            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

//        Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//        result.startAnimation(animation);
        lastPosition = position;

        if (position % 2 == 0) {
            convertView.setBackgroundColor(Color.parseColor("#edebc9"));
        } else {
            convertView.setBackgroundColor(Color.parseColor("#eff5fa"));
        }

        viewHolder.txtMessage.setText(messages.get(position).getMessage());
      //  viewHolder.txtStatus.setText(messages.get(position).getStatus());
        viewHolder.txtCode.setText(messages.get(position).getCode());
        viewHolder.txtDate.setText(messages.get(position).getDate());
      //  viewHolder.iconIndicatorImageView.setOnClickListener(this);
        viewHolder.iconIndicatorImageView.setTag(position);
        String newMessageCount = messages.get(position).getNewMessageCount();
        boolean hasNewMessage = (!newMessageCount.equals("0"));
        if(hasNewMessage) {
            viewHolder.iconIndicatorImageView.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.iconIndicatorImageView.setVisibility(View.INVISIBLE);
        }
        // Return the completed view to render on screen
        return result;
    }


    @Override
    public long getItemId(int i) {

        return 0;
    }




}
