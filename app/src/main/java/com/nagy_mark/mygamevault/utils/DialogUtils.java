package com.nagy_mark.mygamevault.utils;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nagy_mark.mygamevault.R;

public class DialogUtils {
    public static void showWarningDialog(Context context, String title, String message, String pozitiveText, String negativeText, Runnable onPositiveClick) {
        new MaterialAlertDialogBuilder(context)
                .setIcon(R.drawable.ic_warning)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(pozitiveText, (dialog, which) -> {
                    if (onPositiveClick != null) {
                        onPositiveClick.run();
                    }
                })
                .setNegativeButton(negativeText, null)
                .show();
    }

    public static void showDelayedConfirmDialog(Context context, String title, String message, String positiveText, String negativeText, int delaySeconds, Runnable onPositiveClick) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setIcon(R.drawable.ic_warning)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, (d, which) -> {
                    if (onPositiveClick != null) {
                        onPositiveClick.run();
                    }
                })
                .setNegativeButton(negativeText, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);

            new CountDownTimer(delaySeconds * 1000L, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                    positiveButton.setText(positiveText + " (" + secondsLeft + ")");
                }

                @Override
                public void onFinish() {
                    positiveButton.setEnabled(true);
                    positiveButton.setText(positiveText);
                }
            }.start();
        });

        dialog.show();
    }
}
