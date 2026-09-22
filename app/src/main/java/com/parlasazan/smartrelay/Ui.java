package com.parlasazan.smartrelay;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class Ui {
    public static final int PRIMARY = Color.rgb(40, 159, 134);
    public static final int ACCENT = Color.rgb(90, 210, 178);
    public static final int MINT = Color.rgb(153, 238, 215);
    public static final int PALE = Color.rgb(188, 251, 238);
    public static final int BACKGROUND = Color.rgb(244, 250, 248);
    public static final int SURFACE = Color.WHITE;
    public static final int INK = Color.rgb(36, 36, 36);
    public static final int MUTED = Color.rgb(104, 116, 111);
    public static final int DANGER = Color.rgb(200, 79, 69);

    private Ui() {}

    public static void prepare(Activity activity) {
        activity.getWindow().setStatusBarColor(BACKGROUND);
        activity.getWindow().setNavigationBarColor(SURFACE);
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public static LinearLayout page(Context context) {
        LinearLayout root = column(context);
        root.setBackgroundColor(BACKGROUND);
        root.setPadding(dp(context, 20), dp(context, 16), dp(context, 20), dp(context, 24));
        return root;
    }

    public static ScrollView scroll(Context context, View content) {
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    public static LinearLayout column(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        layout.setTextDirection(View.TEXT_DIRECTION_RTL);
        return layout;
    }

    public static LinearLayout row(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return layout;
    }

    public static LinearLayout brandHeader(Context context, String eyebrow, String title) {
        LinearLayout header = row(context);
        header.setPadding(0, dp(context, 6), 0, dp(context, 18));
        ImageView logo = new ImageView(context);
        logo.setImageResource(com.parlasazan.smartrelay.R.drawable.parla_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        header.addView(logo, new LinearLayout.LayoutParams(dp(context, 82), dp(context, 52)));

        LinearLayout text = column(context);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        textParams.setMarginStart(dp(context, 12));
        header.addView(text, textParams);
        TextView small = text(context, eyebrow, 12, MUTED, false);
        TextView heading = text(context, title, 23, INK, true);
        text.addView(small);
        text.addView(heading);
        return header;
    }

    public static LinearLayout card(Context context) {
        LinearLayout card = column(context);
        card.setPadding(dp(context, 18), dp(context, 18), dp(context, 18), dp(context, 18));
        card.setBackground(roundRect(SURFACE, 22, 0, 0, context));
        card.setElevation(dp(context, 2));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(context, 14);
        card.setLayoutParams(params);
        return card;
    }

    public static TextView text(Context context, String value, float sp, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setGravity(Gravity.START);
        view.setTextDirection(View.TEXT_DIRECTION_RTL);
        view.setLineSpacing(0, 1.16f);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    public static TextView sectionTitle(Context context, String title, String detail) {
        LinearLayout wrap = column(context);
        TextView heading = text(context, title, 18, INK, true);
        TextView caption = text(context, detail, 13, MUTED, false);
        caption.setPadding(0, dp(context, 4), 0, dp(context, 14));
        wrap.addView(heading);
        wrap.addView(caption);
        return heading;
    }

    public static EditText field(Context context, String hint, int inputType) {
        EditText field = new EditText(context);
        field.setHint(hint);
        field.setTextSize(15);
        field.setTextColor(INK);
        field.setHintTextColor(MUTED);
        field.setSingleLine(true);
        field.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        field.setTextDirection(View.TEXT_DIRECTION_RTL);
        field.setPadding(dp(context, 14), 0, dp(context, 14), 0);
        field.setInputType(inputType);
        field.setBackground(roundRect(Color.WHITE, 14, 1, Color.rgb(218, 229, 225), context));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 54));
        params.bottomMargin = dp(context, 12);
        field.setLayoutParams(params);
        return field;
    }

    public static EditText pinField(Context context, String hint) {
        EditText field = field(context, hint,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        field.setTextDirection(View.TEXT_DIRECTION_LTR);
        field.setGravity(Gravity.CENTER);
        field.setLetterSpacing(0.28f);
        return field;
    }

    public static Button button(Context context, String title, int color, boolean outlined) {
        Button button = new Button(context);
        button.setText(title);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(outlined ? color : Color.WHITE);
        button.setGravity(Gravity.CENTER);
        button.setBackground(roundRect(outlined ? Color.TRANSPARENT : color, 15,
                outlined ? 1 : 0, color, context));
        button.setStateListAnimator(null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 54));
        params.bottomMargin = dp(context, 10);
        button.setLayoutParams(params);
        return button;
    }

    public static View divider(Context context) {
        View line = new View(context);
        line.setBackgroundColor(Color.rgb(229, 236, 233));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 1));
        params.topMargin = dp(context, 8);
        params.bottomMargin = dp(context, 12);
        line.setLayoutParams(params);
        return line;
    }

    public static GradientDrawable roundRect(int fill, int radiusDp, int strokeDp,
                                             int strokeColor, Context context) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(dp(context, radiusDp));
        if (strokeDp > 0) shape.setStroke(dp(context, strokeDp), strokeColor);
        return shape;
    }

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
