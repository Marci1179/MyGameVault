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

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private List<SavedGameModel> gameList = new ArrayList<>();
    private final Context context;
    private final OnWishlistItemClickListener listener;
    private java.util.Map<Integer, String> gamePrices = new java.util.HashMap<>();

    public interface OnWishlistItemClickListener {
        void onDeleteClick(SavedGameModel game);
    }

    public WishlistAdapter(Context context, OnWishlistItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setGames(List<SavedGameModel> games) {
        this.gameList = games;
        notifyDataSetChanged();
    }

    public void setGamePrice(int gameId, String priceText) {
        this.gamePrices.put(gameId, priceText);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        SavedGameModel game = gameList.get(position);

        String unknownGame = context.getString(R.string.unknown_game);
        holder.tvGameTitleWishlist.setText(game.getGameName() != null ? game.getGameName() : unknownGame);

        String rawDate = game.getReleaseYear();
        String yearOnly = "-";
        if (rawDate != null && rawDate.length() >= 4) {
            yearOnly = rawDate.substring(0, 4);
        }

        String publisher = game.getPublisher() != null ? game.getPublisher() : "-";

        holder.tvGameYearWishlist.setText(context.getString(R.string.format_release_year, yearOnly));
        holder.tvGamePublisherWishlist.setText(context.getString(R.string.format_publisher, publisher));

        if (game.getCover() != null && !game.getCover().isEmpty()) {
            String imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/" + game.getCover() + ".jpg";
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.ivGameCoverWishlist);
        } else {
            holder.ivGameCoverWishlist.setImageDrawable(null);
        }

        String priceInfo = gamePrices.get(game.getId());
        if (priceInfo != null) {
            holder.tvGamePriceWishlist.setVisibility(View.VISIBLE);
            holder.tvGamePriceWishlist.setText(priceInfo);
        } else {
            holder.tvGamePriceWishlist.setVisibility(View.GONE);
        }

        holder.btnDeleteWishlist.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(game);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGameCoverWishlist;
        TextView tvGameTitleWishlist, tvGameYearWishlist, tvGamePublisherWishlist, tvGamePriceWishlist;
        ImageButton btnDeleteWishlist;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGameCoverWishlist = itemView.findViewById(R.id.ivGameCoverWishlist);
            tvGameTitleWishlist = itemView.findViewById(R.id.tvGameTitleWishlist);
            tvGameYearWishlist = itemView.findViewById(R.id.tvGameYearWishlist);
            tvGamePublisherWishlist = itemView.findViewById(R.id.tvGamePublisherWishlist);
            tvGamePriceWishlist = itemView.findViewById(R.id.tvGamePriceWishlist);
            btnDeleteWishlist = itemView.findViewById(R.id.btnDeleteWishlist);
        }
    }
}
