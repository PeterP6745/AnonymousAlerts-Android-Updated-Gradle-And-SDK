package com.messagelogix.anonymousalerts.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.messagelogix.anonymousalerts.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Richard on 7/14/2017.
 */
public class EmergencyDialExpListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private HashMap<String, List<String>> phoneNumbersHashMap;
    private List<String> headerList;

    public EmergencyDialExpListAdapter(Context context, HashMap<String, List<String>> hashMap, List<String> list ){
        phoneNumbersHashMap = hashMap;
        this.context = context;
        this.phoneNumbersHashMap = hashMap;
        this.headerList = list;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        String groupTitle = (String)getGroup(groupPosition);
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_group_emergency_dial, parent, false);
        }
        TextView parentTextView = (TextView)convertView.findViewById(R.id.text_view_emergency_dial_group);
        parentTextView.setText(groupTitle);

        ExpandableListView eLV = (ExpandableListView) parent;
        eLV.expandGroup(groupPosition);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String childText = (String)getChild(groupPosition, childPosition);
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_child_emergency_dial, parent, false);
        }
        TextView childTextView = (TextView)convertView.findViewById(R.id.text_view_emergency_dial_child);
        childTextView.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        return true;
    }

    @Override
    public int getGroupCount() {

//        Log.d("HEADER COUNT", ""+ headerList.size());
//        Log.d("Example", "Getting 0 group for 0 child: "+ phoneNumbersHashMap.get(headerList.get(0)).get(0));
        return headerList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {

      //  Log.d("CHILDREN COUNT", ""+phoneNumbersHashMap.get(headerList.get(groupPosition)).size());
       // return phoneNumbersHashMap.get(headerList.get(groupPosition)).size();
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {

        return this.headerList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String headerKey = headerList.get(groupPosition);
//        Log.d("Header Key", headerKey);
//        Log.d("Child for Key", ""+phoneNumbersHashMap.get(headerKey).get(childPosition));
        return phoneNumbersHashMap.get(headerKey).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {

        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {

        return 0;
    }

    @Override
    public boolean hasStableIds() {

        return false;
    }



}
