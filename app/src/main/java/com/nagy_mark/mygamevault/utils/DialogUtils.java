package com.nagy_mark.mygamevault.utils;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

    public interface OnPasswordSubmitListener {
        void onSubmit(String newPassword, AlertDialog dialog);
    }

    public static void showChangePasswordDialog(Context context, LayoutInflater inflater, OnPasswordSubmitListener listener) {
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

        TextInputLayout tilNewPasswordChangePassword = dialogView.findViewById(R.id.tilNewPasswordChangePassword);
        TextInputLayout tilConfirmNewPasswordChangePassword = dialogView.findViewById(R.id.tilConfirmNewPasswordChangePassword);

        TextInputEditText etNewPasswordChangePassword = dialogView.findViewById(R.id.etNewPasswordChangePassword);
        TextInputEditText etConfirmNewPasswordChangePassword = dialogView.findViewById(R.id.etConfirmNewPasswordChangePassword);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.change_password))
                .setView(dialogView)
                .setPositiveButton(context.getText(R.string.save), null)
                .setNegativeButton(context.getText(R.string.cancel), (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String newPassword = etNewPasswordChangePassword.getText() != null ? etNewPasswordChangePassword.getText().toString() : "";
                String confirmPassword = etConfirmNewPasswordChangePassword.getText() != null ? etConfirmNewPasswordChangePassword.getText().toString() : "";

                tilNewPasswordChangePassword.setError(null);
                tilConfirmNewPasswordChangePassword.setError(null);

                if (!ValidationUtils.isPasswordStrong(newPassword)) {
                    tilNewPasswordChangePassword.setError(context.getString(R.string.error_weak_password));
                    return;
                }

                if (!ValidationUtils.isPasswordMatch(newPassword, confirmPassword)) {
                    tilConfirmNewPasswordChangePassword.setError(context.getString(R.string.error_passwords_mismatch));
                    return;
                }

                if (listener != null) {
                    listener.onSubmit(newPassword, dialog);
                }
            });
        });

        dialog.show();
    }
}
