package com.kennyc.bottomsheet.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kennyc.bottomsheet.R;

import java.util.List;

/**
 * Created by kcampagna on 9/9/15.
 */
public class GridAdapter extends BaseAdapter {
    private final List<MenuItem> mItems;

    private final LayoutInflater mInflater;
    private int selectedItemId;
    private int selectedPosition;
    private String selectedItem = "";

    private boolean mIsGrid;

    @StyleRes
    private int mListStyle;

    @StyleRes
    private int mGridStyle;

    private int mTintColor;

    public GridAdapter(Context context, List<MenuItem> items, boolean isGrid, @StyleRes int listStyle, @StyleRes int gridStyle, int menuItemTintColor, int selectedPosition, int selectedItemId, String selectedItem) {
        mItems = items;
        mIsGrid = isGrid;
        mInflater = LayoutInflater.from(context);
        mListStyle = listStyle;
        mGridStyle = gridStyle;
        mTintColor = menuItemTintColor;
        this.selectedPosition = selectedPosition;
        this.selectedItemId = selectedItemId;
        this.selectedItem = selectedItem;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public MenuItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getItemId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuItem item = getItem(position);
        ViewHolder holder;
        Log.d("appAdapter", "griddd: isGrid" + position);
        Log.d("appAdapter", "selectedPosition" + selectedPosition);

        if (convertView == null) {
            convertView = mInflater.inflate(mIsGrid ? R.layout.bottom_sheet_grid_item : R.layout.bottom_sheet_list_item, parent, false);
            holder = new ViewHolder(convertView);
            int textAppearance = mIsGrid ? mGridStyle : mListStyle;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.title.setTextAppearance(textAppearance);
            } else {
                holder.title.setTextAppearance(convertView.getContext(), textAppearance);
            }

          /*  if (position == selectedPosition) {
                holder.selectedMark.setVisibility(View.VISIBLE);
                holder.title.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.selected_text_color));
            } else {
                holder.selectedMark.setVisibility(View.GONE);
                holder.title.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.mohandisi_text_color));
            }*/


        } else {
            holder = (ViewHolder) convertView.getTag();
        }

//        if (item.getTitle().toString().equalsIgnoreCase(selectedItem) || (item.getItemId() == selectedItemId)) {
        if (item.getTitle().toString().equalsIgnoreCase(selectedItem) || (item.getItemId() == selectedItemId) || position == selectedPosition) {
            holder.selectedMark.setVisibility(View.VISIBLE);
            holder.title.setTextColor(getAttributeColor(convertView.getContext(), R.attr.colorPrimary));
//            holder.title.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.selected_text_color));
        } else {
            holder.selectedMark.setVisibility(View.GONE);
            holder.title.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.text_color));
        }
        Drawable menuIcon = item.getIcon();
        if (mTintColor != Integer.MIN_VALUE && menuIcon != null) {
            // mutate it, so we do not tint the original menu icon
            menuIcon = menuIcon.mutate();
            menuIcon.setColorFilter(new LightingColorFilter(Color.BLACK, mTintColor));
        }

        holder.icon.setImageDrawable(menuIcon);
        holder.icon.setVisibility(menuIcon != null ? View.VISIBLE : View.GONE);
        holder.title.setText(item.getTitle());
        return convertView;
    }


    public static int getAttributeColor(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = context.getResources().getColor(colorRes);
        } catch (Resources.NotFoundException e) {
            Log.w("GridAdapter: ", "Not found color resource by id: " + colorRes);
        }
        return color;
    }

    @Nullable
    public static Drawable getAttributeDrawable(
            Context context,
            int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int drawableRes = typedValue.resourceId;
        Drawable drawable = null;
        try {
            drawable = context.getResources().getDrawable(drawableRes);
        } catch (Resources.NotFoundException e) {
            Log.w("GridAdapter: ", "Not found drawable resource by id: " + drawableRes);
        }
        return drawable;
    }
}
