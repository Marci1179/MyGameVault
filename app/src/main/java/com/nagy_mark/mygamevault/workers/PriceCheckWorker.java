package com.nagy_mark.mygamevault.workers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nagy_mark.mygamevault.MainActivity;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.database.AppDatabase;
import com.nagy_mark.mygamevault.database.WishlistPriceDao;
import com.nagy_mark.mygamevault.database.WishlistPriceEntity;
import com.nagy_mark.mygamevault.models.CheapSharkDealInfo;
import com.nagy_mark.mygamevault.models.CheapSharkGameDetailResponse;
import com.nagy_mark.mygamevault.models.CheapSharkGameSearchResult;
import com.nagy_mark.mygamevault.models.SavedGameModel;
import com.nagy_mark.mygamevault.network.CheapSharkApi;
import com.nagy_mark.mygamevault.network.CheapSharkApiClient;
import com.nagy_mark.mygamevault.network.SupabaseApi;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;

import java.util.List;

import retrofit2.Response;

public class PriceCheckWorker extends Worker {
    public PriceCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();

            WishlistPriceDao priceDao = AppDatabase.getDatabase(context).wishlistPriceDao();
            CheapSharkApi cheapSharkApi = CheapSharkApiClient.getClient().create(CheapSharkApi.class);

            SharedPreferences prefs = context.getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
            String currentUserId = prefs.getString("USER_ID", null);

            if (currentUserId == null) {
                return Result.failure();
            }

            SupabaseApi supabaseApi = SupabaseApiClient.getClient(context).create(SupabaseApi.class);

            Response<List<SavedGameModel>> supabaseResponse = supabaseApi.getGamesByStatus("eq." + currentUserId, "eq.4").execute();

            if (!supabaseResponse.isSuccessful() || supabaseResponse.body() == null) {
                return Result.retry();
            }

            List<SavedGameModel> gamesToFetch = supabaseResponse.body();

            for (SavedGameModel game : gamesToFetch) {
                if (game.getGameName() == null || game.getGameName().isEmpty()) continue;

                Response<List<CheapSharkGameSearchResult>> searchResponse = cheapSharkApi.searchGame(game.getGameName(), 1).execute();

                if (searchResponse.isSuccessful() && searchResponse.body() != null && !searchResponse.body().isEmpty()) {
                    String gameId = searchResponse.body().get(0).getGameId();

                    Response<CheapSharkGameDetailResponse> detailResponse = cheapSharkApi.getGameDetails(gameId).execute();

                    if (detailResponse.isSuccessful() && detailResponse.body() != null && detailResponse.body().getDeals() != null) {
                        double lowestPrice = Double.MAX_VALUE;
                        String bestStoreName = "";

                        for (CheapSharkDealInfo deal : detailResponse.body().getDeals()) {
                            String storeName = getStoreName(deal.getStoreId());
                            if (storeName != null) {
                                try {
                                    double currentPrice = Double.parseDouble(deal.getPrice());
                                    if (currentPrice < lowestPrice) {
                                        lowestPrice = currentPrice;
                                        bestStoreName = storeName;
                                    }
                                } catch (NumberFormatException e) {

                                }
                            }
                        }

                        if (lowestPrice != Double.MAX_VALUE) {
                            WishlistPriceEntity savedPrice = priceDao.getPriceForGame(game.getId());

                            if (savedPrice != null) {
                                if (lowestPrice < savedPrice.getLastKnownPrice()) {
                                    String priceText = lowestPrice + "$ (" + bestStoreName + ")";
                                    sendNotification(game.getGameName(), priceText);
                                }
                            }

                            priceDao.insertOrUpdatePrice(new WishlistPriceEntity(game.getId(), lowestPrice, bestStoreName));
                        }
                    }
                }
            }

            return Result.success();

        } catch (Exception e) {
            Log.e("PriceCheckWorker", "Hiba a háttérfolyamatban: " + e.getMessage());
            return Result.retry();
        }
    }

    private void sendNotification(String gameTitle, String priceText) {
        Context context = getApplicationContext();

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        intent.putExtra("NAVIGATE_TO", "WISHLIST");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                gameTitle.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String expandedText = context.getString(R.string.notif_price_drop_expanded, gameTitle, priceText);
        String collapsedText = context.getString(R.string.notif_price_drop_collapsed, gameTitle, priceText);
        String titleText = context.getString(R.string.notif_price_drop_title);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "wishlist_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(titleText)
                .setContentText(collapsedText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(expandedText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(gameTitle.hashCode(), builder.build());
    }

    private String getStoreName(String storeId) {
        switch (storeId) {
            case "1": return "Steam";
            case "3": return "GreenManGaming";
            case "7": return "GOG";
            case "8": return "EA/Origin";
            case "11": return "Humble Store";
            case "13": return "Ubisoft";
            case "15": return "Fanatical";
            case "25": return "Epic Games";
            default: return null;
        }
    }
}
