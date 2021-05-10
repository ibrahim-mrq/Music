package com.kennyc.bottomsheet.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.R;

import java.util.List;
import java.util.Set;

/**
 * Adapter used when {@link BottomSheet#createShareBottomSheet(Context, Intent, String, boolean, Set, Set)} is invoked
 */
public class AppAdapter extends BaseAdapter {
    List<AppInfo> mApps;

    private LayoutInflater mInflater;

    private int mTextColor;
    Context context;
    private int mLayoutResource;
    private int selectedPosition;
    private int selectedID;

    public AppAdapter(Context context, List<AppInfo> apps, boolean isGrid, int selectedPosition) {
        Log.d("appAdapter", "AppAdapter: isGrid" + isGrid);

        mApps = apps;
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mTextColor = ContextCompat.getColor(context, R.color.black_85);
        mLayoutResource = isGrid ? R.layout.bottom_sheet_grid_item : R.layout.bottom_sheet_list_item;
        Log.d("appAdapter", "AppAdapter: selectedPosition" + selectedPosition);
        this.selectedPosition = selectedPosition;
//        this.selectedID = selectedID;
    }

    @Override
    public int getCount() {
        return mApps.size();
    }

    @Override
    public AppInfo getItem(int position) {
        return mApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppInfo appInfo = getItem(position);
        ViewHolder holder;
        Log.d("appAdapter", "AppAdapter: isGrid" + position);
        Log.d("appAdapter", "AppAdapter: --" + selectedPosition);

        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder(convertView);
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.text_color));
            if (position == selectedPosition) {
                holder.selectedMark.setVisibility(View.VISIBLE);
                holder.title.setText("fffff");
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.icon.setImageDrawable(appInfo.drawable);
        holder.title.setText(appInfo.title + "111");
        holder.title.setTextColor(ContextCompat.getColor(context, R.color.selected_text_color));
        return convertView;
    }

    public static class AppInfo {
        public String title;

        public String packageName;

        public String name;

        public Drawable drawable;

        public AppInfo(String title, String packageName, String name, Drawable drawable) {
            this.title = title;
            this.packageName = packageName;
            this.name = name;
            this.drawable = drawable;
        }
    }
}
