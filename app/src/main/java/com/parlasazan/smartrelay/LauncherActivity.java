package com.parlasazan.smartrelay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class LauncherActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.prepare(this);

        LinearLayout root = Ui.column(this);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Ui.BACKGROUND);
        root.setPadding(Ui.dp(this, 40), Ui.dp(this, 40), Ui.dp(this, 40), Ui.dp(this, 40));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.parla_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        root.addView(logo, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 126)));

        TextView title = Ui.text(this, "پارلا کنترل", 25, Ui.INK, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, Ui.dp(this, 26), 0, 0);
        root.addView(title);
        TextView subtitle = Ui.text(this, "کنترل امن و ساده تجهیزات ورودی", 14, Ui.MUTED, false);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);
        setContentView(root);

        root.postDelayed(this::route, 650);
    }

    private void route() {
        SecureStore store = new SecureStore(this);
        Class<?> destination;
        if (!store.isConfigured()) destination = SetupActivity.class;
        else if (store.isPinEnabled()) destination = LoginActivity.class;
        else destination = DashboardActivity.class;
        startActivity(new Intent(this, destination));
        finish();
    }
}
