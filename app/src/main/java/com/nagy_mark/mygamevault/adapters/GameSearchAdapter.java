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
import com.nagy_mark.mygamevault.models.Game;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class GameSearchAdapter extends RecyclerView.Adapter<GameSearchAdapter.GameViewHolder> {
    public interface OnGameAddListener {
        void onAddClick(Game game, int statusId);
    }

    private List<Game> games = new ArrayList<>();
    private OnGameAddListener listener;

    private final Map<String, Integer> savedGamesMap;

    public GameSearchAdapter(OnGameAddListener listener, Map<String, Integer> savedGamesMap) {
        this.listener = listener;
        this.savedGamesMap = savedGamesMap;
    }

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
        Context context = holder.itemView.getContext();

        holder.tvGameTitleSearch.setText(game.getName());

        if (game.getFirstReleaseDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(game.getFirstReleaseDate() * 1000);
            String yearString = String.valueOf(calendar.get(Calendar.YEAR));

            holder.tvGameYearSearch.setText(context.getString(R.string.format_release_year, yearString));
        } else {
            holder.tvGameYearSearch.setText(context.getString(R.string.unknown_year));
        }

        String publisherName = game.getPublisherName();
        if (publisherName != null) {
            holder.tvGamePublisherSearch.setText(context.getString(R.string.format_publisher, publisherName));
        } else {
            holder.tvGamePublisherSearch.setText(context.getString(R.string.unknown_publisher));
        }

        if (game.getCover() != null && game.getCover().getImageId() != null) {
            String imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/" + game.getCover().getImageId() + ".jpg";
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.ivGameCoverSearch);
        } else {
            holder.ivGameCoverSearch.setImageDrawable(null);
        }

        Integer savedStatusId = (savedGamesMap != null) ? savedGamesMap.get(game.getName()) : null;

        if (savedStatusId != null && savedStatusId <= 3) {
            holder.btnAddLibrarySearch.setImageResource(R.drawable.ic_check);
            holder.btnAddLibrarySearch.setEnabled(false);
            holder.btnAddLibrarySearch.setAlpha(0.5f);

            holder.btnAddWishlistSearch.setImageResource(R.drawable.ic_wishlist);
            holder.btnAddWishlistSearch.setEnabled(false);
            holder.btnAddWishlistSearch.setAlpha(0.5f);
        }
        else if (savedStatusId != null && savedStatusId == 4) {
            holder.btnAddLibrarySearch.setImageResource(R.drawable.ic_add);
            holder.btnAddLibrarySearch.setEnabled(false);
            holder.btnAddLibrarySearch.setAlpha(0.5f);

            holder.btnAddWishlistSearch.setImageResource(R.drawable.ic_check);
            holder.btnAddWishlistSearch.setEnabled(false);
            holder.btnAddWishlistSearch.setAlpha(0.5f);
        }
        else {
            holder.btnAddLibrarySearch.setImageResource(R.drawable.ic_add);
            holder.btnAddLibrarySearch.setEnabled(true);
            holder.btnAddLibrarySearch.setAlpha(1.0f);

            holder.btnAddWishlistSearch.setImageResource(R.drawable.ic_wishlist);
            holder.btnAddWishlistSearch.setEnabled(true);
            holder.btnAddWishlistSearch.setAlpha(1.0f);
        }

        holder.btnAddLibrarySearch.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddClick(game, 1); // 1 = Könyvtár (status_id)
            }
        });

        holder.btnAddWishlistSearch.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddClick(game, 4); // 4 = Kívánságlista (status_id)
            }
        });
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
