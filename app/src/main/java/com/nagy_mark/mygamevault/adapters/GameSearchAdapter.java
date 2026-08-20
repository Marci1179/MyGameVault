package com.nagy_mark.mygamevault.adapters;

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
import com.nagy_mark.mygamevault.models.Game;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GameSearchAdapter extends RecyclerView.Adapter<GameSearchAdapter.GameViewHolder> {

    private List<Game> games = new ArrayList<>();

    public void setGames(List<Game> newGames) {
        this.games = newGames;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);

        holder.tvGameTitleSearch.setText(game.getName());

        if (game.getFirstReleaseDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(game.getFirstReleaseDate() * 1000);
            String yearString = String.valueOf(calendar.get(Calendar.YEAR));

            holder.tvGameYearSearch.setText(holder.itemView.getContext().getString(R.string.format_release_year, yearString));
        } else {
            holder.tvGameYearSearch.setText(holder.itemView.getContext().getString(R.string.unknown_year));
        }

        String publisherName = game.getPublisherName();
        if (publisherName != null) {
            holder.tvGamePublisherSearch.setText(holder.itemView.getContext().getString(R.string.format_publisher, publisherName));
        } else {
            holder.tvGamePublisherSearch.setText(holder.itemView.getContext().getString(R.string.unknown_publisher));
        }

        if (game.getCover() != null && game.getCover().getImageId() != null) {
            String imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/" + game.getCover().getImageId() + ".jpg";
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.ivGameCoverSearch);
        } else {
            holder.ivGameCoverSearch.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGameCoverSearch;
        TextView tvGameTitleSearch, tvGameYearSearch, tvGamePublisherSearch;
        ImageButton btnAddLibrarySearch, btnAddWishlistSearch;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGameCoverSearch = itemView.findViewById(R.id.ivGameCoverSearch);
            tvGameTitleSearch = itemView.findViewById(R.id.tvGameTitleSearch);
            tvGameYearSearch = itemView.findViewById(R.id.tvGameYearSearch);
            tvGamePublisherSearch = itemView.findViewById(R.id.tvGamePublisherSearch);
            btnAddLibrarySearch = itemView.findViewById(R.id.btnAddLibrarySearch);
            btnAddWishlistSearch = itemView.findViewById(R.id.btnAddWishlistSearch);
        }
    }
}
