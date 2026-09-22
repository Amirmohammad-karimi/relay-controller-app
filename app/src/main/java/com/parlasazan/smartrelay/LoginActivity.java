package com.parlasazan.smartrelay;

import android.app.Activity;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;

public final class LoginActivity extends Activity {
    private int failures;
    private long lockedUntil;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.prepare(this);
        SecureStore store = new SecureStore(this);

        LinearLayout page = Ui.page(this);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.parla_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 110));
        logoParams.topMargin = Ui.dp(this, 32);
        logoParams.bottomMargin = Ui.dp(this, 18);
        page.addView(logo, logoParams);

        TextView title = Ui.text(this, "خوش آمدید", 26, Ui.INK, true);
        title.setGravity(Gravity.CENTER);
        page.addView(title);
        TextView detail = Ui.text(this, "برای ورود، رمز برنامه را وارد کنید", 14, Ui.MUTED, false);
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 24));
        page.addView(detail);

        LinearLayout card = Ui.card(this);
        EditText pin = Ui.pinField(this, "••••");
        pin.setMaxEms(4);
        card.addView(pin);
        Button login = Ui.button(this, "ورود امن", Ui.PRIMARY, false);
        login.setOnClickListener(view -> {
            if (SystemClock.elapsedRealtime() < lockedUntil) {
                long seconds = (lockedUntil - SystemClock.elapsedRealtime()) / 1000 + 1;
                Toast.makeText(this, "لطفاً " + seconds + " ثانیه دیگر تلاش کنید", Toast.LENGTH_SHORT).show();
                return;
            }
            if (store.verifyAppPin(pin.getText().toString())) {
                openDashboard();
            } else {
                failures++;
                pin.setError("رمز صحیح نیست");
                pin.setText("");
                if (failures >= 5) {
                    failures = 0;
                    lockedUntil = SystemClock.elapsedRealtime() + 30_000;
                    Toast.makeText(this, "ورود برای ۳۰ ثانیه محدود شد", Toast.LENGTH_LONG).show();
                }
            }
        });
        card.addView(login);

        Button biometric = Ui.button(this, "ورود با اثر انگشت", Ui.PRIMARY, true);
        biometric.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? View.VISIBLE : View.GONE);
        biometric.setOnClickListener(view -> authenticateBiometric());
        card.addView(biometric);
        page.addView(card);
        setContentView(Ui.scroll(this, page));
    }

    private void authenticateBiometric() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return;
        Executor executor = getMainExecutor();
        BiometricPrompt prompt = new BiometricPrompt.Builder(this)
                .setTitle("ورود به پارلا کنترل")
                .setSubtitle("هویت خود را تأیید کنید")
                .setNegativeButton("انصراف", executor, (dialog, which) -> {})
                .build();
        prompt.authenticate(new CancellationSignal(), executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        openDashboard();
                    }

                    @Override public void onAuthenticationError(int code, CharSequence message) {
                        if (code != BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED) {
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void openDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}
