package com.nagy_mark.mygamevault.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.models.SavedGameModel;

import java.util.ArrayList;
import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder> {

    private List<SavedGameModel> gameList = new ArrayList<>();
    private final Context context;

    private  final OnLibraryItemClickListener listener;

    public interface OnLibraryItemClickListener {
        void onDeleteClick(SavedGameModel game);
        void onItemClick(SavedGameModel game);
    }

    public LibraryAdapter(Context context, OnLibraryItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setGames(List<SavedGameModel> games) {
        this.gameList = games;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_library, parent, false);
        return new LibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
        SavedGameModel game = gameList.get(position);

        holder.tvGameTitleLibrary.setText(game.getGameName() != null ? game.getGameName() : context.getString(R.string.unknown_game));
        holder.tvGameYearLibrary.setText(game.getReleaseYear() != null ? game.getReleaseYear() : context.getString(R.string.unknown_year));

        String rawDate = game.getReleaseYear();
        String yearOnly = "-";
        if (rawDate != null && rawDate.length() >= 4) {
            yearOnly = rawDate.substring(0, 4);
        }

        String publisher = game.getPublisher() != null ? game.getPublisher() : context.getString(R.string.unknown_publisher);

        holder.tvGameYearLibrary.setText(context.getString(R.string.format_release_year, yearOnly));
        holder.tvGamePublisherLibrary.setText(context.getString(R.string.format_publisher, publisher));

        String statusText;
        switch (game.getStatusId()) {
            case 1:
                statusText = context.getString(R.string.status_owned);
                break;
            case 2:
                statusText = context.getString(R.string.status_in_progress);
                break;
            case 3:
                statusText = context.getString(R.string.status_finished);
                break;
            default:
                statusText = "-";
                break;
        }

        holder.tvGameStatusLibrary.setText(context.getString(R.string.status_label, statusText));

        if (game.getCover() != null && !game.getCover().isEmpty()) {
            String imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/" + game.getCover() + ".jpg";

            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.ivGameCoverLibrary);
        } else {
            holder.ivGameCoverLibrary.setImageDrawable(null);
        }

        holder.btnDeleteLibrary.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(game);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(game);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public static class LibraryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGameCoverLibrary;
        TextView tvGameTitleLibrary, tvGameYearLibrary, tvGamePublisherLibrary, tvGameStatusLibrary;
        ImageButton btnDeleteLibrary;

        public LibraryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGameCoverLibrary = itemView.findViewById(R.id.ivGameCoverLibrary);
            tvGameTitleLibrary = itemView.findViewById(R.id.tvGameTitleLibrary);
            tvGameYearLibrary = itemView.findViewById(R.id.tvGameYearLibrary);
            tvGamePublisherLibrary = itemView.findViewById(R.id.tvGamePublisherLibrary);
            tvGameStatusLibrary = itemView.findViewById(R.id.tvGameStatusLibrary);
            btnDeleteLibrary = itemView.findViewById(R.id.btnDeleteLibrary);
        }
    }
}
