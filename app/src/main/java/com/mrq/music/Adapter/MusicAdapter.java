package com.mrq.music.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.widget.Filter;
import android.widget.Filterable;

import com.mrq.music.Interface.MusicInterface;
import com.mrq.music.Model.Music;
import com.mrq.music.R;
import com.squareup.picasso.Picasso;

import pl.droidsonroids.gif.GifImageView;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicAdapterViewHolder> implements Filterable {

    private static Context mContext;
    private ArrayList<Music> list;
    private List<Music> exampleListFull;
    private MusicInterface anInterface;
    public int selectedPosition = -1;
    public int selectedPosition2 = -1;

    public MusicAdapter(ArrayList<Music> list, Context mContext) {
        this.list = list;
        MusicAdapter.mContext = mContext;
        exampleListFull = new ArrayList<>(list);
    }

    public MusicAdapter(ArrayList<Music> list, Context mContext, MusicInterface anInterface) {
        this.list = list;
        MusicAdapter.mContext = mContext;
        this.anInterface = anInterface;
        exampleListFull = new ArrayList<>(list);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @NonNull
    @Override
    public MusicAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_music_design, parent, false);
        return new MusicAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MusicAdapterViewHolder holder, final int position) {
        final Music model = list.get(position);
        holder.bind(model);

        if (position == list.size() - 1)
            holder.view.setVisibility(View.GONE);

        holder.gifImageView.setVisibility(View.GONE);
        if (selectedPosition == position) {
            holder.itemView.setSelected(true);
            holder.img.setImageResource(R.drawable.list_pause_btn);
//            holder.gifImageView.setVisibility(View.VISIBLE);
        } else {
            holder.itemView.setSelected(false);
//            holder.img.setImageResource(R.drawable.list_play_btn);
            Picasso.get().load(model.getImage()).placeholder(R.drawable.list_play_btn).into(holder.img);
//            holder.gifImageView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition >= 0)
                    notifyItemChanged(selectedPosition);
                selectedPosition = holder.getAdapterPosition();
                selectedPosition2 = holder.getAdapterPosition();
                notifyItemChanged(selectedPosition);
                anInterface.onClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (list != null ? list.size() : 0);
    }

    static class MusicAdapterViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_title, tv_artist, tv_duration;
        private ImageView img;
        private GifImageView gifImageView;
        private View view;

        private MusicAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.customMusic_img);
            gifImageView = itemView.findViewById(R.id.customMusic_gifImageView);
            tv_title = itemView.findViewById(R.id.customMusic_tv_title);
            tv_artist = itemView.findViewById(R.id.customMusic_tv_artist);
            tv_duration = itemView.findViewById(R.id.customMusic_tv_duration);
            view = itemView.findViewById(R.id.customMusic_view);

        }

        private void bind(Music model) {
            tv_title.setText(model.getSongTitle());
            tv_artist.setText(model.getSongArtist());
            tv_duration.setText(model.getSongDuration());
            Picasso.get().load(model.getImage()).into(img);

        }
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Music> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(exampleListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Music item : exampleListFull) {
                    if (item.getSongTitle().toLowerCase().contains(filterPattern) ||
                            item.getSongArtist().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}