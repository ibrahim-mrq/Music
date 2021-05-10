package com.kennyc.bottomsheet.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kennyc.bottomsheet.R;

public class ViewHolder {
    public TextView title;

    public ImageView icon;
    public ImageView selectedMark;

    public ViewHolder(View view) {
        title = (TextView) view.findViewById(R.id.title);
        icon = (ImageView) view.findViewById(R.id.icon);
        selectedMark = (ImageView) view.findViewById(R.id.selectedMark);
        view.setTag(this);
    }
}
