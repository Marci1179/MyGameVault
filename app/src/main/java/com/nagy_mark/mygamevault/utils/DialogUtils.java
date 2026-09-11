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

                boolean hasError = false;

                if (newPassword.isEmpty()) {
                    tilNewPasswordChangePassword.setError(context.getString(R.string.error_password_required));
                    hasError = true;
                } else if (!ValidationUtils.isPasswordStrong(newPassword)) {
                    tilNewPasswordChangePassword.setError(context.getString(R.string.error_weak_password));
                    hasError = true;
                }

                if (confirmPassword.isEmpty()) {
                    tilConfirmNewPasswordChangePassword.setError(context.getString(R.string.error_password_confirm_required));
                    hasError = true;
                } else if (!ValidationUtils.isPasswordMatch(newPassword, confirmPassword)) {
                    tilConfirmNewPasswordChangePassword.setError(context.getString(R.string.error_passwords_mismatch));
                    hasError = true;
                }

                if (hasError) {
                    return;
                }

                if (listener != null) {
                    listener.onSubmit(newPassword, dialog);
                }
            });
        });

        dialog.show();
    }

    public interface OnEmailSubmitListener {
        void onSubmit(String email, AlertDialog dialog);
    }

    public static void showForgotPasswordDialog(Context context, LayoutInflater inflater, OnEmailSubmitListener listener) {
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);

        TextInputLayout tilForgotEmailDialogForgotPassword = dialogView.findViewById(R.id.tilForgotEmailDialogForgotPassword);
        TextInputEditText etForgotEmailDialogForgotPassword = dialogView.findViewById(R.id.etForgotEmailDialogForgotPassword);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.forgot_password))
                .setView(dialogView)
                .setPositiveButton(context.getString(R.string.send), null)
                .setNegativeButton(context.getString(R.string.cancel), (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String email = etForgotEmailDialogForgotPassword.getText() != null ? etForgotEmailDialogForgotPassword.getText().toString().trim() : "";

                tilForgotEmailDialogForgotPassword.setError(null);

                boolean hasError = false;

                if (email.isEmpty()) {
                    tilForgotEmailDialogForgotPassword.setError(context.getString(R.string.error_email_required));
                    hasError = true;
                } else if (!ValidationUtils.isValidEmail(email)) {
                    tilForgotEmailDialogForgotPassword.setError(context.getString(R.string.error_invalid_email));
                    hasError = true;
                }

                if (hasError) {
                    return;
                }

                if (listener != null) {
                    listener.onSubmit(email, dialog);
                }
            });
        });

        dialog.show();
    }


    public interface OnOtpSubmitListener {
        void onSubmit(String otpCode, String newPassword, AlertDialog dialog);
    }

    public static void showResetPasswordOtpDialog(Context context, LayoutInflater inflater, OnOtpSubmitListener listener) {
        View dialogView = inflater.inflate(R.layout.dialog_reset_password_otp, null);

        TextInputLayout tilOtpCodeDialogResetPassword = dialogView.findViewById(R.id.tilOtpCodeDialogResetPassword);
        TextInputLayout tilResetNewPasswordDialogResetPassword = dialogView.findViewById(R.id.tilResetNewPasswordDialogResetPassword);
        TextInputLayout tilResetConfirmPasswordDialogResetPassword = dialogView.findViewById(R.id.tilResetConfirmPasswordDialogResetPassword);

        TextInputEditText etOtpCodeDialogResetPassword = dialogView.findViewById(R.id.etOtpCodeDialogResetPassword);
        TextInputEditText etResetNewPasswordDialogResetPassword = dialogView.findViewById(R.id.etResetNewPasswordDialogResetPassword);
        TextInputEditText etResetConfirmPasswordDialogResetPassword = dialogView.findViewById(R.id.etResetConfirmPasswordDialogResetPassword);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.reset_password_title))
                .setView(dialogView)
                .setPositiveButton(context.getString(R.string.save), null)
                .setNegativeButton(context.getString(R.string.cancel), (d, which) -> d.dismiss())
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String otp = etOtpCodeDialogResetPassword.getText() != null ? etOtpCodeDialogResetPassword.getText().toString().trim() : "";
                String newPass = etResetNewPasswordDialogResetPassword.getText() != null ? etResetNewPasswordDialogResetPassword.getText().toString() : "";
                String confirmPass = etResetConfirmPasswordDialogResetPassword.getText() != null ? etResetConfirmPasswordDialogResetPassword.getText().toString() : "";

                tilOtpCodeDialogResetPassword.setError(null);
                tilResetNewPasswordDialogResetPassword.setError(null);
                tilResetConfirmPasswordDialogResetPassword.setError(null);

                boolean hasError = false;

                if (otp.isEmpty()) {
                    tilOtpCodeDialogResetPassword.setError(context.getString(R.string.error_otp_required));
                    hasError = true;
                } else if (!ValidationUtils.isValidOtp(otp)) {
                    tilOtpCodeDialogResetPassword.setError(context.getString(R.string.error_otp_length));
                    hasError = true;
                }

                if (newPass.isEmpty()) {
                    tilResetNewPasswordDialogResetPassword.setError(context.getString(R.string.error_password_required));
                    hasError = true;
                } else if (!ValidationUtils.isPasswordStrong(newPass)) {
                    tilResetNewPasswordDialogResetPassword.setError(context.getString(R.string.error_weak_password));
                    hasError = true;
                }

                if (confirmPass.isEmpty()) {
                    tilResetConfirmPasswordDialogResetPassword.setError(context.getString(R.string.error_password_confirm_required));
                    hasError = true;
                } else if (!ValidationUtils.isPasswordMatch(newPass, confirmPass)) {
                    tilResetConfirmPasswordDialogResetPassword.setError(context.getString(R.string.error_passwords_mismatch));
                    hasError = true;
                }

                if (hasError) {
                    return;
                }

                if (!hasError && listener != null) {
                    listener.onSubmit(otp, newPass, dialog);
                }
            });
        });

        dialog.show();
    }
}
