package com.parlasazan.smartrelay;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

/** Modern, task-oriented dashboard for the Parla Smart Relay controller. */
public final class DashboardActivity extends Activity {
    private static final int NAV_HOME = 0;
    private static final int NAV_RELAYS = 1;
    private static final int NAV_ACCESS = 2;
    private static final int NAV_MORE = 3;

    private SecureStore store;
    private DeviceGateway gateway;
    private FrameLayout content;
    private LinearLayout navigation;
    private final LinearLayout[] navItems = new LinearLayout[4];
    private Runnable detailBack;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.prepare(this);
        store = new SecureStore(this);
        gateway = new DeviceGateway(this);

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        shell.setBackgroundColor(Ui.BACKGROUND);

        content = new FrameLayout(this);
        shell.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        navigation = buildNavigation();
        shell.addView(navigation, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 76)));
        setContentView(shell);
        selectNav(NAV_HOME);
    }

    private LinearLayout buildNavigation() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(Ui.dp(this, 8), Ui.dp(this, 7), Ui.dp(this, 8), Ui.dp(this, 7));
        nav.setBackground(Ui.roundRect(Color.WHITE, 0, 1, Color.rgb(231, 238, 235), this));
        navItems[NAV_HOME] = navItem("خانه", R.drawable.ic_home, NAV_HOME);
        navItems[NAV_RELAYS] = navItem("رله‌ها", R.drawable.ic_power, NAV_RELAYS);
        navItems[NAV_ACCESS] = navItem("دسترسی", R.drawable.ic_users, NAV_ACCESS);
        navItems[NAV_MORE] = navItem("بیشتر", R.drawable.ic_grid, NAV_MORE);
        for (LinearLayout item : navItems) {
            nav.addView(item, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1));
        }
        return nav;
    }

    private LinearLayout navItem(String label, int iconRes, int index) {
        LinearLayout item = Ui.column(this);
        item.setGravity(Gravity.CENTER);
        item.setPadding(Ui.dp(this, 4), Ui.dp(this, 5), Ui.dp(this, 4), Ui.dp(this, 4));
        item.setTag(index);
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Ui.MUTED);
        item.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 24), Ui.dp(this, 24)));
        TextView title = Ui.text(this, label, 11, Ui.MUTED, false);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = Ui.dp(this, 3);
        item.addView(title, titleParams);
        item.setOnClickListener(v -> selectNav(index));
        return item;
    }

    private void selectNav(int selected) {
        detailBack = null;
        navigation.setVisibility(View.VISIBLE);
        for (int i = 0; i < navItems.length; i++) {
            boolean active = i == selected;
            ImageView icon = (ImageView) navItems[i].getChildAt(0);
            TextView text = (TextView) navItems[i].getChildAt(1);
            icon.setColorFilter(active ? Ui.PRIMARY : Ui.MUTED);
            text.setTextColor(active ? Ui.PRIMARY : Ui.MUTED);
            text.setTypeface(Typeface.DEFAULT, active ? Typeface.BOLD : Typeface.NORMAL);
            navItems[i].setBackground(active
                    ? Ui.roundRect(Color.rgb(232, 248, 243), 16, 0, 0, this)
                    : null);
        }
        if (selected == NAV_HOME) setPage(homePage());
        if (selected == NAV_RELAYS) setPage(relaysPage());
        if (selected == NAV_ACCESS) setPage(accessPage());
        if (selected == NAV_MORE) setPage(morePage());
    }

    private LinearLayout basePage() {
        LinearLayout page = Ui.page(this);
        page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return page;
    }

    private void setPage(View page) {
        content.removeAllViews();
        content.addView(Ui.scroll(this, page), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private LinearLayout homePage() {
        LinearLayout page = basePage();
        page.addView(homeHeader());
        page.addView(connectionHero());
        page.addView(section("دسترسی سریع", "کارهای روزمره، بدون جست‌وجو در منوها"));
        page.addView(quickActions());
        page.addView(section("رله‌ها", "کنترل فوری خروجی‌های دستگاه"));
        page.addView(relaySummary(1));
        page.addView(relaySummary(2));
        page.addView(infoBanner("فرمان‌ها با پیامک امن به کنترلر ارسال می‌شوند. هزینه پیامک مطابق تعرفه اپراتور است."));
        return page;
    }

    private View homeHeader() {
        LinearLayout header = Ui.row(this);
        header.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 16));
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.parla_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        header.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 76), Ui.dp(this, 48)));
        LinearLayout text = Ui.column(this);
        header.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        text.addView(Ui.text(this, "کنترل هوشمند پارلا", 12, Ui.MUTED, false));
        text.addView(Ui.text(this, "سلام، آماده کنترل است", 22, Ui.INK, true));
        return header;
    }

    private View connectionHero() {
        LinearLayout hero = Ui.column(this);
        hero.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(25, 119, 103), Ui.PRIMARY, Color.rgb(91, 205, 174)});
        background.setCornerRadius(Ui.dp(this, 26));
        hero.setBackground(background);
        hero.setElevation(Ui.dp(this, 3));

        LinearLayout state = Ui.row(this);
        TextView badge = Ui.text(this, "●  آماده ارسال فرمان", 12, Color.WHITE, true);
        badge.setPadding(Ui.dp(this, 12), Ui.dp(this, 7), Ui.dp(this, 12), Ui.dp(this, 7));
        badge.setBackground(Ui.roundRect(Color.argb(45, 255, 255, 255), 14, 0, 0, this));
        state.addView(badge);
        hero.addView(state);

        TextView title = Ui.text(this, "کنترلر رله سیم‌کارتی", 21, Color.WHITE, true);
        title.setPadding(0, Ui.dp(this, 18), 0, Ui.dp(this, 5));
        hero.addView(title);
        String phone = store.getSecret(SecureStore.DEVICE_PHONE, "");
        hero.addView(Ui.text(this, phone.isEmpty() ? "شماره دستگاه ثبت نشده" : maskPhone(phone),
                14, Color.rgb(224, 255, 247), false));
        TextView hint = Ui.text(this, "برای دریافت آخرین وضعیت، دکمه بررسی را بزنید.", 12,
                Color.rgb(217, 250, 241), false);
        hint.setPadding(0, Ui.dp(this, 13), 0, 0);
        hero.addView(hint);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = Ui.dp(this, 22);
        hero.setLayoutParams(params);
        return hero;
    }

    private View quickActions() {
        LinearLayout wrap = Ui.column(this);
        LinearLayout row1 = Ui.row(this);
        LinearLayout row2 = Ui.row(this);
        row1.addView(quickTile("وضعیت دستگاه", "بررسی آنلاین", "◉", () -> gateway.send("بررسی وضعیت", "CHECK")), tileParams(true));
        row1.addView(quickTile("اعتبار سیم‌کارت", "استعلام موجودی", "◒", () -> gateway.send("استعلام اعتبار", "CC")), tileParams(false));
        row2.addView(quickTile("تماس با دستگاه", "تماس مستقیم", "☎", gateway::callDevice), tileParams(true));
        row2.addView(quickTile("شارژ مستقیم", "ارسال کد شارژ", "+", () -> showDetail("شارژ سیم‌کارت", () -> selectNav(NAV_HOME), rechargePage())), tileParams(false));
        wrap.addView(row1);
        wrap.addView(row2);
        return wrap;
    }

    private LinearLayout.LayoutParams tileParams(boolean first) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        if (first) params.setMarginEnd(Ui.dp(this, 7));
        else params.setMarginStart(Ui.dp(this, 7));
        params.bottomMargin = Ui.dp(this, 14);
        return params;
    }

    private View quickTile(String title, String subtitle, String glyph, Runnable action) {
        LinearLayout tile = modernCard(16);
        tile.setMinimumHeight(Ui.dp(this, 126));
        TextView icon = Ui.text(this, glyph, 20, Ui.PRIMARY, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(Ui.roundRect(Color.rgb(231, 248, 243), 15, 0, 0, this));
        tile.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 42), Ui.dp(this, 42)));
        TextView heading = Ui.text(this, title, 15, Ui.INK, true);
        heading.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 2));
        tile.addView(heading);
        tile.addView(Ui.text(this, subtitle, 12, Ui.MUTED, false));
        tile.setOnClickListener(v -> action.run());
        return tile;
    }

    private View relaySummary(int relay) {
        String fallback = relay == 1 ? "رله اول" : "رله دوم";
        String saved = store.getSecret(relay == 1 ? SecureStore.RELAY_1_NAME : SecureStore.RELAY_2_NAME, "");
        final String name = saved.trim().isEmpty() ? fallback : saved;
        LinearLayout card = modernCard(16);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView roundIcon = Ui.text(this, "⏻", 22, Ui.PRIMARY, true);
        roundIcon.setGravity(Gravity.CENTER);
        roundIcon.setBackground(Ui.roundRect(Color.rgb(230, 248, 243), 18, 0, 0, this));
        card.addView(roundIcon, new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 48)));
        LinearLayout labels = Ui.column(this);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        labelParams.setMarginStart(Ui.dp(this, 13));
        card.addView(labels, labelParams);
        labels.addView(Ui.text(this, name, 16, Ui.INK, true));
        String seconds = store.getSecret(relay == 1 ? SecureStore.RELAY_1_TIME : SecureStore.RELAY_2_TIME, "0");
        labels.addView(Ui.text(this, "زمان‌بندی: " + seconds + " ثانیه", 12, Ui.MUTED, false));
        Button on = compactButton("روشن", Ui.PRIMARY, false);
        Button off = compactButton("خاموش", Ui.DANGER, true);
        on.setOnClickListener(v -> gateway.send("روشن کردن " + name, CommandProtocol.relay(relay, true)));
        off.setOnClickListener(v -> gateway.send("خاموش کردن " + name, CommandProtocol.relay(relay, false)));
        card.addView(on);
        card.addView(off);
        return card;
    }

    private LinearLayout relaysPage() {
        LinearLayout page = basePage();
        page.addView(pageHeading("رله‌ها", "کنترل، نام‌گذاری و زمان‌بندی خروجی‌ها"));
        page.addView(relayControlCard(1));
        page.addView(relayControlCard(2));
        page.addView(infoBanner("زمان ۰ یعنی حالت دائم. مقدار ۱ تا ۹۹۹، رله را به‌صورت پالسی فعال می‌کند."));
        return page;
    }

    private View relayControlCard(int relay) {
        String fallback = relay == 1 ? "رله اول" : "رله دوم";
        String saved = store.getSecret(relay == 1 ? SecureStore.RELAY_1_NAME : SecureStore.RELAY_2_NAME, "");
        final String name = saved.trim().isEmpty() ? fallback : saved;
        String seconds = store.getSecret(relay == 1 ? SecureStore.RELAY_1_TIME : SecureStore.RELAY_2_TIME, "0");
        LinearLayout card = modernCard(20);
        LinearLayout top = Ui.row(this);
        TextView number = Ui.text(this, String.valueOf(relay), 18, Color.WHITE, true);
        number.setGravity(Gravity.CENTER);
        number.setBackground(Ui.roundRect(relay == 1 ? Ui.PRIMARY : Color.rgb(52, 112, 164), 16, 0, 0, this));
        top.addView(number, new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46)));
        LinearLayout labels = Ui.column(this);
        LinearLayout.LayoutParams labelsParams = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        labelsParams.setMarginStart(Ui.dp(this, 13));
        top.addView(labels, labelsParams);
        labels.addView(Ui.text(this, name, 19, Ui.INK, true));
        labels.addView(Ui.text(this, "خروجی " + relay + "  •  " + seconds + " ثانیه", 12, Ui.MUTED, false));
        TextView edit = Ui.text(this, "تنظیمات  ‹", 13, Ui.PRIMARY, true);
        edit.setGravity(Gravity.CENTER);
        edit.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
        edit.setOnClickListener(v -> showDetail("تنظیمات " + name, () -> selectNav(NAV_RELAYS), relaySettingsPage(relay)));
        top.addView(edit);
        card.addView(top);
        card.addView(thinDivider());
        LinearLayout buttons = Ui.row(this);
        Button on = actionButton("روشن کردن", Ui.PRIMARY, false);
        Button off = actionButton("خاموش کردن", Ui.DANGER, true);
        buttons.addView(on, weightedButton(true));
        buttons.addView(off, weightedButton(false));
        on.setOnClickListener(v -> gateway.send("روشن کردن " + name, CommandProtocol.relay(relay, true)));
        off.setOnClickListener(v -> gateway.send("خاموش کردن " + name, CommandProtocol.relay(relay, false)));
        card.addView(buttons);
        return card;
    }

    private LinearLayout accessPage() {
        LinearLayout page = basePage();
        page.addView(pageHeading("مدیریت دسترسی", "افراد و ریموت‌های مجاز را مدیریت کنید"));
        page.addView(categoryMenu("شماره‌های مجاز", "افزودن، حذف و مشاهده کاربران تلفنی", "☎", () ->
                showDetail("شماره‌های مجاز", () -> selectNav(NAV_ACCESS), phoneAccessPage())));
        page.addView(categoryMenu("ریموت‌ها", "تعریف و مدیریت تا ۵۰۰ ریموت", "⌁", () ->
                showDetail("مدیریت ریموت‌ها", () -> selectNav(NAV_ACCESS), remoteAccessPage())));
        page.addView(infoBanner("تغییرات دسترسی مستقیماً برای کنترلر ارسال می‌شوند؛ فقط افراد مورد اعتماد را اضافه کنید."));
        return page;
    }

    private LinearLayout phoneAccessPage() {
        LinearLayout page = detailPage("شماره‌های مجاز", "مدیریت کاربران تلفنی کنترلر");
        LinearLayout addCard = modernCard(18);
        addCard.addView(Ui.text(this, "افزودن شماره جدید", 17, Ui.INK, true));
        addCard.addView(spacer(10));
        EditText phone = Ui.field(this, "مثلاً 09121234567", InputType.TYPE_CLASS_PHONE);
        addCard.addView(phone);
        Button add = Ui.button(this, "افزودن به فهرست مجاز", Ui.PRIMARY, false);
        add.setOnClickListener(v -> runValidated(() -> gateway.send("افزودن شماره", CommandProtocol.addPhone(phone.getText().toString()))));
        addCard.addView(add);
        page.addView(addCard);

        LinearLayout manage = modernCard(18);
        manage.addView(Ui.text(this, "مدیریت فهرست", 17, Ui.INK, true));
        manage.addView(Ui.text(this, "برای حذف، شماره را دقیق وارد کنید.", 12, Ui.MUTED, false));
        manage.addView(spacer(12));
        EditText target = Ui.field(this, "شماره مورد نظر", InputType.TYPE_CLASS_PHONE);
        manage.addView(target);
        Button delete = Ui.button(this, "حذف شماره", Ui.DANGER, true);
        delete.setOnClickListener(v -> runValidated(() -> gateway.send("حذف شماره", CommandProtocol.deletePhone(target.getText().toString()))));
        manage.addView(delete);
        Button list = Ui.button(this, "دریافت فهرست شماره‌ها", Ui.PRIMARY, true);
        list.setOnClickListener(v -> gateway.send("فهرست شماره‌ها", "SL"));
        manage.addView(list);
        Button clear = Ui.button(this, "حذف همه شماره‌ها", Ui.DANGER, true);
        clear.setOnClickListener(v -> confirm("حذف همه شماره‌ها", "همه کاربران تلفنی حذف شوند؟", () -> gateway.send("حذف همه شماره‌ها", "DAN")));
        manage.addView(clear);
        page.addView(manage);
        return page;
    }

    private LinearLayout remoteAccessPage() {
        LinearLayout page = detailPage("مدیریت ریموت‌ها", "شماره ریموت باید بین ۱ تا ۵۰۰ باشد");
        LinearLayout card = modernCard(18);
        EditText number = Ui.field(this, "شماره ریموت (۱ تا ۵۰۰)", InputType.TYPE_CLASS_NUMBER);
        card.addView(number);
        Button enable = Ui.button(this, "فعال کردن ریموت", Ui.PRIMARY, false);
        enable.setOnClickListener(v -> withRemote(number, n -> gateway.send("فعال کردن ریموت", CommandProtocol.enableRemote(n))));
        card.addView(enable);
        Button disable = Ui.button(this, "غیرفعال کردن ریموت", Color.rgb(184, 124, 32), true);
        disable.setOnClickListener(v -> withRemote(number, n -> gateway.send("غیرفعال کردن ریموت", CommandProtocol.disableRemote(n))));
        card.addView(disable);
        Button delete = Ui.button(this, "حذف ریموت", Ui.DANGER, true);
        delete.setOnClickListener(v -> withRemote(number, n -> gateway.send("حذف ریموت", CommandProtocol.deleteRemote(n))));
        card.addView(delete);
        card.addView(thinDivider());
        Button all = Ui.button(this, "حذف همه ریموت‌ها", Ui.DANGER, true);
        all.setOnClickListener(v -> confirm("حذف همه ریموت‌ها",
                "تمام شناسه‌های ریموت از کنترلر پاک شوند؟",
                () -> gateway.send("حذف همه ریموت‌ها", "DAR")));
        card.addView(all);
        page.addView(card);
        return page;
    }

    private LinearLayout morePage() {
        LinearLayout page = basePage();
        page.addView(pageHeading("بیشتر", "تنظیمات و امکانات تکمیلی"));
        page.addView(identityCard());
        page.addView(section("تنظیمات دستگاه", "همه گزینه‌ها بر اساس موضوع دسته‌بندی شده‌اند"));
        page.addView(categoryMenu("سیم‌کارت و شارژ", "تغییر شماره دستگاه، اعتبار و فرمول استعلام", "SIM", () ->
                showDetail("سیم‌کارت و شارژ", () -> selectNav(NAV_MORE), simPage())));
        page.addView(categoryMenu("تنظیمات کنترلر", "رمز، تاریخ، زبان و اطلاعات دستگاه", "⚙", () ->
                showDetail("تنظیمات کنترلر", () -> selectNav(NAV_MORE), deviceSettingsPage())));
        page.addView(categoryMenu("گزارش‌ها و اعلان‌ها", "فعال‌سازی مستقل گزارش رویدادها", "☷", () ->
                showDetail("گزارش‌ها", () -> selectNav(NAV_MORE), reportsPage())));
        page.addView(categoryMenu("امنیت برنامه", "قفل برنامه و تأیید پیش از ارسال", "◆", () ->
                showDetail("امنیت برنامه", () -> selectNav(NAV_MORE), securityPage())));
        return page;
    }

    private View identityCard() {
        LinearLayout card = modernCard(18);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView icon = Ui.text(this, "P", 21, Color.WHITE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(Ui.roundRect(Ui.PRIMARY, 20, 0, 0, this));
        card.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 54), Ui.dp(this, 54)));
        LinearLayout labels = Ui.column(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMarginStart(Ui.dp(this, 14));
        card.addView(labels, lp);
        labels.addView(Ui.text(this, "کنترلر پارلا", 17, Ui.INK, true));
        String phone = store.getSecret(SecureStore.DEVICE_PHONE, "");
        labels.addView(Ui.text(this, phone.isEmpty() ? "شماره ثبت نشده" : maskPhone(phone), 13, Ui.MUTED, false));
        TextView version = Ui.text(this, "نسخه ۱.۱", 12, Ui.PRIMARY, true);
        card.addView(version);
        return card;
    }

    private LinearLayout simPage() {
        LinearLayout page = detailPage("سیم‌کارت و شارژ", "مدیریت شماره و اعتبار سیم‌کارت دستگاه");
        LinearLayout numberCard = modernCard(18);
        numberCard.addView(Ui.text(this, "شماره سیم‌کارت دستگاه", 17, Ui.INK, true));
        numberCard.addView(Ui.text(this, "شماره ثابت نیست؛ شماره سیم‌کارتی را وارد کنید که اکنون داخل کنترلر قرار دارد.", 12, Ui.MUTED, false));
        numberCard.addView(spacer(10));
        EditText phone = Ui.field(this, "شماره سیم‌کارت داخل دستگاه", InputType.TYPE_CLASS_PHONE);
        phone.setTextDirection(View.TEXT_DIRECTION_LTR);
        phone.setText(store.getSecret(SecureStore.DEVICE_PHONE, ""));
        numberCard.addView(phone);
        Button savePhone = Ui.button(this, "ذخیره شماره جدید دستگاه", Ui.PRIMARY, false);
        savePhone.setOnClickListener(v -> {
            String value = PhoneNumber.normalize(phone.getText().toString());
            if (!PhoneNumber.isValid(value)) { toast("شماره تلفن معتبر نیست"); return; }
            store.putSecret(SecureStore.DEVICE_PHONE, value);
            phone.setText(value);
            toast("شماره جدید دستگاه ذخیره شد و فرمان‌های بعدی به همین شماره ارسال می‌شوند");
        });
        numberCard.addView(savePhone);
        page.addView(numberCard);

        LinearLayout balance = modernCard(18);
        balance.addView(Ui.text(this, "اعتبار سیم‌کارت", 17, Ui.INK, true));
        balance.addView(Ui.text(this, "فرمول استعلام اپراتور را وارد کنید؛ مثال: *141*1#", 12, Ui.MUTED, false));
        balance.addView(spacer(12));
        EditText formula = Ui.field(this, "فرمول استعلام اعتبار", InputType.TYPE_CLASS_TEXT);
        formula.setText(store.getSecret(SecureStore.BALANCE_FORMULA, ""));
        balance.addView(formula);
        Button saveFormula = Ui.button(this, "ذخیره و ارسال فرمول", Ui.PRIMARY, false);
        saveFormula.setOnClickListener(v -> runValidated(() -> gateway.send("ثبت فرمول اعتبار",
                CommandProtocol.setBalanceFormula(formula.getText().toString()), () ->
                        store.putSecret(SecureStore.BALANCE_FORMULA, formula.getText().toString().trim()))));
        balance.addView(saveFormula);
        Button check = Ui.button(this, "استعلام اعتبار", Ui.PRIMARY, true);
        check.setOnClickListener(v -> gateway.send("استعلام اعتبار", "CC"));
        balance.addView(check);
        page.addView(balance);
        page.addView(rechargeCard());
        return page;
    }

    private LinearLayout rechargePage() {
        LinearLayout page = detailPage("شارژ سیم‌کارت", "کد شارژ را مستقیماً برای دستگاه بفرستید");
        page.addView(rechargeCard());
        return page;
    }

    private View rechargeCard() {
        LinearLayout card = modernCard(18);
        card.addView(Ui.text(this, "شارژ مستقیم", 17, Ui.INK, true));
        card.addView(Ui.text(this, "کد کامل شارژ اپراتور را وارد کنید.", 12, Ui.MUTED, false));
        card.addView(spacer(12));
        EditText code = Ui.field(this, "کد شارژ", InputType.TYPE_CLASS_TEXT);
        card.addView(code);
        Button send = Ui.button(this, "ارسال کد شارژ", Ui.PRIMARY, false);
        send.setOnClickListener(v -> runValidated(() -> gateway.send("شارژ سیم‌کارت", CommandProtocol.recharge(code.getText().toString()))));
        card.addView(send);
        return card;
    }

    private LinearLayout deviceSettingsPage() {
        LinearLayout page = detailPage("تنظیمات کنترلر", "پیکربندی اصلی دستگاه");
        page.addView(categoryMenu("تغییر رمز دستگاه", "رمز چهاررقمی فرمان‌های پیامکی", "••••", () ->
                showNested(devicePinPage())));
        page.addView(categoryMenu("تاریخ و ساعت", "همگام‌سازی خودکار با تلفن", "◷", () -> syncDateTime()));
        page.addView(categoryMenu("نوع تقویم", "تغییر تقویم دستگاه", "۱۲", () -> calendarDialog()));
        page.addView(categoryMenu("زبان پاسخ‌ها", "فارسی یا انگلیسی", "FA", () -> languageDialog()));
        page.addView(categoryMenu("نسخه دستگاه", "دریافت اطلاعات نرم‌افزاری", "i", () -> gateway.send("نسخه دستگاه", "SV")));
        page.addView(categoryMenu("وضعیت کامل", "دریافت وضعیت رله‌ها و تنظیمات", "✓", () -> gateway.send("وضعیت کامل", "CHECK")));
        return page;
    }

    private LinearLayout devicePinPage() {
        LinearLayout page = detailPage("تغییر رمز دستگاه", "رمز جدید باید دقیقاً چهار رقم باشد");
        LinearLayout card = modernCard(18);
        EditText pin = Ui.pinField(this, "رمز جدید چهاررقمی");
        card.addView(pin);
        Button save = Ui.button(this, "تغییر رمز دستگاه", Ui.PRIMARY, false);
        save.setOnClickListener(v -> {
            String value = pin.getText().toString();
            if (!value.matches("\\d{4}")) { toast("رمز باید چهار رقم باشد"); return; }
            gateway.send("تغییر رمز دستگاه", CommandProtocol.changePin(value), () -> store.putSecret(SecureStore.DEVICE_PIN, value));
        });
        card.addView(save);
        page.addView(card);
        return page;
    }

    private LinearLayout relaySettingsPage(int relay) {
        String keyName = relay == 1 ? SecureStore.RELAY_1_NAME : SecureStore.RELAY_2_NAME;
        String keyTime = relay == 1 ? SecureStore.RELAY_1_TIME : SecureStore.RELAY_2_TIME;
        LinearLayout page = detailPage("تنظیمات رله " + relay, "نام و رفتار خروجی را شخصی‌سازی کنید");
        LinearLayout nameCard = modernCard(18);
        nameCard.addView(Ui.text(this, "نام رله", 17, Ui.INK, true));
        nameCard.addView(spacer(10));
        EditText name = Ui.field(this, "حداکثر ۱۵ نویسه", InputType.TYPE_CLASS_TEXT);
        name.setText(store.getSecret(keyName, ""));
        nameCard.addView(name);
        Button saveName = Ui.button(this, "ذخیره نام", Ui.PRIMARY, false);
        saveName.setOnClickListener(v -> runValidated(() -> gateway.send("تغییر نام رله",
                CommandProtocol.relayName(relay, name.getText().toString()), () ->
                        store.putSecret(keyName, name.getText().toString().trim()))));
        nameCard.addView(saveName);
        Button clearName = Ui.button(this, "حذف نام سفارشی", Ui.DANGER, true);
        clearName.setOnClickListener(v -> confirm("حذف نام سفارشی", "نام ذخیره‌شده رله پاک شود؟",
                () -> gateway.send("حذف نام رله", CommandProtocol.relayName(relay, ""), () -> {
                    store.putSecret(keyName, "");
                    toast("نام سفارشی حذف شد");
                    showDetail("تنظیمات رله " + relay, () -> selectNav(NAV_RELAYS), relaySettingsPage(relay));
                })));
        nameCard.addView(clearName);
        page.addView(nameCard);

        LinearLayout timeCard = modernCard(18);
        timeCard.addView(Ui.text(this, "زمان عملکرد", 17, Ui.INK, true));
        timeCard.addView(Ui.text(this, "بین ۰ تا ۹۹۹ ثانیه؛ صفر یعنی روشن/خاموش دائم", 12, Ui.MUTED, false));
        timeCard.addView(spacer(12));
        EditText seconds = Ui.field(this, "زمان به ثانیه", InputType.TYPE_CLASS_NUMBER);
        seconds.setText(store.getSecret(keyTime, "0"));
        timeCard.addView(seconds);
        Button saveTime = Ui.button(this, "ذخیره زمان", Ui.PRIMARY, false);
        saveTime.setOnClickListener(v -> runValidated(() -> {
            int value = Integer.parseInt(seconds.getText().toString());
            gateway.send("تنظیم زمان رله", CommandProtocol.relayTime(relay, value), () ->
                    store.putSecret(keyTime, String.valueOf(value)));
        }));
        timeCard.addView(saveTime);
        page.addView(timeCard);
        return page;
    }

    private LinearLayout reportsPage() {
        LinearLayout page = detailPage("گزارش‌ها و اعلان‌ها", "هر نوع گزارش را مستقل مدیریت کنید");
        LinearLayout card = modernCard(8);
        card.addView(reportRow("گزارش ریموت به مدیر", "اعلان استفاده از ریموت برای مدیر", "RPR"));
        card.addView(thinDivider());
        card.addView(reportRow("گزارش تماس‌گیرنده به مدیر", "اعلان تماس ورودی برای مدیر", "RPC"));
        card.addView(thinDivider());
        card.addView(reportRow("کنترل پیامکی به مدیر", "گزارش اجرای فرمان پیامکی برای مدیر", "RPS"));
        card.addView(thinDivider());
        card.addView(reportRow("کنترل پیامکی به کاربر", "گزارش اجرای فرمان پیامکی برای کاربر", "RPU"));
        page.addView(card);
        page.addView(infoBanner("کلید هر گزینه، فرمان فعال یا غیرفعال‌سازی را همان لحظه برای دستگاه ارسال می‌کند."));
        return page;
    }

    private View reportRow(String title, String subtitle, String command) {
        LinearLayout row = Ui.row(this);
        row.setPadding(Ui.dp(this, 8), Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 10));
        LinearLayout labels = Ui.column(this);
        row.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        labels.addView(Ui.text(this, title, 15, Ui.INK, true));
        labels.addView(Ui.text(this, subtitle, 12, Ui.MUTED, false));
        Switch toggle = new Switch(this);
        toggle.setText("");
        toggle.setOnCheckedChangeListener((button, checked) -> gateway.send(
                (checked ? "فعال کردن " : "غیرفعال کردن ") + title,
                command + (checked ? "ON" : "OFF")));
        row.addView(toggle);
        return row;
    }

    private LinearLayout securityPage() {
        LinearLayout page = detailPage("امنیت برنامه", "کنترل دسترسی و تأیید فرمان‌ها");
        LinearLayout card = modernCard(8);
        Switch preview = settingsSwitch("پیش‌نمایش پیش از ارسال", "نمایش مقصد و نوع فرمان برای تأیید", store.previewSms());
        preview.setOnCheckedChangeListener((button, checked) -> store.setPreviewSms(checked));
        card.addView(switchRow(preview, "پیش‌نمایش پیش از ارسال", "نمایش مقصد و نوع فرمان برای تأیید"));
        card.addView(thinDivider());
        Switch pinLock = settingsSwitch("قفل برنامه", "درخواست رمز هنگام ورود", store.isPinEnabled());
        pinLock.setOnCheckedChangeListener((button, checked) -> store.setPinEnabled(checked));
        card.addView(switchRow(pinLock, "قفل برنامه", "درخواست رمز هنگام ورود"));
        page.addView(card);

        LinearLayout pinCard = modernCard(18);
        pinCard.addView(Ui.text(this, "تغییر رمز ورود برنامه", 17, Ui.INK, true));
        pinCard.addView(spacer(10));
        EditText pin = Ui.pinField(this, "رمز چهاررقمی جدید");
        pinCard.addView(pin);
        Button save = Ui.button(this, "ذخیره رمز جدید", Ui.PRIMARY, false);
        save.setOnClickListener(v -> {
            String value = pin.getText().toString();
            if (!value.matches("\\d{4}")) { toast("رمز باید چهار رقم باشد"); return; }
            store.setAppPin(value);
            toast("رمز ورود تغییر کرد");
        });
        pinCard.addView(save);
        Button lock = Ui.button(this, "قفل کردن برنامه", Ui.DANGER, true);
        lock.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        pinCard.addView(lock);
        page.addView(pinCard);
        page.addView(infoBanner("شماره دستگاه، رمز کنترلر و رمز برنامه به‌صورت رمزنگاری‌شده در همین تلفن نگهداری می‌شوند."));
        return page;
    }

    private View switchRow(Switch toggle, String title, String subtitle) {
        LinearLayout row = Ui.row(this);
        row.setPadding(Ui.dp(this, 8), Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 10));
        LinearLayout labels = Ui.column(this);
        row.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        labels.addView(Ui.text(this, title, 15, Ui.INK, true));
        labels.addView(Ui.text(this, subtitle, 12, Ui.MUTED, false));
        row.addView(toggle);
        return row;
    }

    private Switch settingsSwitch(String title, String subtitle, boolean checked) {
        Switch toggle = new Switch(this);
        toggle.setChecked(checked);
        toggle.setText("");
        return toggle;
    }

    private View categoryMenu(String title, String subtitle, String glyph, Runnable action) {
        LinearLayout card = modernCard(15);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView icon = Ui.text(this, glyph, glyph.length() > 2 ? 11 : 20, Ui.PRIMARY, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(Ui.roundRect(Color.rgb(231, 248, 243), 17, 0, 0, this));
        card.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 50), Ui.dp(this, 50)));
        LinearLayout labels = Ui.column(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMarginStart(Ui.dp(this, 14));
        card.addView(labels, lp);
        labels.addView(Ui.text(this, title, 16, Ui.INK, true));
        labels.addView(Ui.text(this, subtitle, 12, Ui.MUTED, false));
        TextView arrow = Ui.text(this, "‹", 26, Ui.MUTED, false);
        arrow.setGravity(Gravity.CENTER);
        card.addView(arrow, new LinearLayout.LayoutParams(Ui.dp(this, 28), Ui.dp(this, 40)));
        card.setOnClickListener(v -> action.run());
        return card;
    }

    private void showDetail(String title, Runnable back, View page) {
        detailBack = back;
        navigation.setVisibility(View.GONE);
        setPage(page);
    }

    private void showNested(View page) {
        detailBack = () -> showDetail("تنظیمات کنترلر", () -> selectNav(NAV_MORE), deviceSettingsPage());
        navigation.setVisibility(View.GONE);
        setPage(page);
    }

    private LinearLayout detailPage(String title, String subtitle) {
        LinearLayout page = basePage();
        LinearLayout header = Ui.row(this);
        header.setPadding(0, Ui.dp(this, 2), 0, Ui.dp(this, 22));
        TextView back = Ui.text(this, "‹", 30, Ui.INK, false);
        back.setGravity(Gravity.CENTER);
        back.setBackground(Ui.roundRect(Color.WHITE, 16, 1, Color.rgb(226, 235, 232), this));
        back.setOnClickListener(v -> goBackFromDetail());
        header.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46)));
        LinearLayout labels = Ui.column(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMarginStart(Ui.dp(this, 14));
        header.addView(labels, lp);
        labels.addView(Ui.text(this, title, 22, Ui.INK, true));
        labels.addView(Ui.text(this, subtitle, 12, Ui.MUTED, false));
        page.addView(header);
        return page;
    }

    private View pageHeading(String title, String subtitle) {
        LinearLayout block = Ui.column(this);
        block.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 22));
        block.addView(Ui.text(this, title, 26, Ui.INK, true));
        TextView detail = Ui.text(this, subtitle, 13, Ui.MUTED, false);
        detail.setPadding(0, Ui.dp(this, 4), 0, 0);
        block.addView(detail);
        return block;
    }

    private View section(String title, String subtitle) {
        LinearLayout block = Ui.column(this);
        block.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 12));
        block.addView(Ui.text(this, title, 18, Ui.INK, true));
        block.addView(Ui.text(this, subtitle, 12, Ui.MUTED, false));
        return block;
    }

    private LinearLayout modernCard(int padding) {
        LinearLayout card = Ui.column(this);
        card.setPadding(Ui.dp(this, padding), Ui.dp(this, padding), Ui.dp(this, padding), Ui.dp(this, padding));
        card.setBackground(Ui.roundRect(Color.WHITE, 22, 1, Color.rgb(232, 239, 236), this));
        card.setElevation(Ui.dp(this, 1));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = Ui.dp(this, 14);
        card.setLayoutParams(params);
        return card;
    }

    private View infoBanner(String message) {
        LinearLayout banner = Ui.row(this);
        banner.setPadding(Ui.dp(this, 15), Ui.dp(this, 14), Ui.dp(this, 15), Ui.dp(this, 14));
        banner.setBackground(Ui.roundRect(Color.rgb(230, 247, 243), 18, 0, 0, this));
        TextView icon = Ui.text(this, "i", 14, Color.WHITE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(Ui.roundRect(Ui.PRIMARY, 14, 0, 0, this));
        banner.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 28), Ui.dp(this, 28)));
        TextView text = Ui.text(this, message, 12, Color.rgb(44, 105, 91), false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMarginStart(Ui.dp(this, 10));
        banner.addView(text, lp);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = Ui.dp(this, 2);
        params.bottomMargin = Ui.dp(this, 16);
        banner.setLayoutParams(params);
        return banner;
    }

    private View thinDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(233, 239, 237));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 1));
        lp.topMargin = Ui.dp(this, 15);
        lp.bottomMargin = Ui.dp(this, 15);
        divider.setLayoutParams(lp);
        return divider;
    }

    private View spacer(int dp) {
        return new View(this) {{ setLayoutParams(new LinearLayout.LayoutParams(1, Ui.dp(DashboardActivity.this, dp))); }};
    }

    private Button compactButton(String title, int color, boolean outlined) {
        Button button = actionButton(title, color, outlined);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(this, 70), Ui.dp(this, 42));
        lp.setMarginStart(Ui.dp(this, 7));
        button.setLayoutParams(lp);
        button.setTextSize(12);
        return button;
    }

    private Button actionButton(String title, int color, boolean outlined) {
        Button button = new Button(this);
        button.setText(title);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(outlined ? color : Color.WHITE);
        button.setBackground(Ui.roundRect(outlined ? Color.TRANSPARENT : color, 14, 1, color, this));
        button.setStateListAnimator(null);
        return button;
    }

    private LinearLayout.LayoutParams weightedButton(boolean first) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 50), 1);
        if (first) lp.setMarginEnd(Ui.dp(this, 6));
        else lp.setMarginStart(Ui.dp(this, 6));
        return lp;
    }

    private void syncDateTime() {
        Calendar now = Calendar.getInstance();
        int[] jalali = PersianDate.fromGregorian(now.get(Calendar.YEAR),
                now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
        String packed = String.format(Locale.US, "%04d%02d%02d%02d%02d",
                jalali[0], jalali[1], jalali[2], now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
        String readable = String.format(Locale.US, "%04d/%02d/%02d - %02d:%02d",
                jalali[0], jalali[1], jalali[2], now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
        confirm("همگام‌سازی تاریخ و ساعت", "زمان دستگاه روی «" + readable + "» تنظیم شود؟",
                () -> gateway.send("تنظیم تاریخ و ساعت", CommandProtocol.syncDateTime(packed)));
    }

    private void calendarDialog() {
        confirm("تغییر تقویم دستگاه", "فرمان تغییر تقویم برای کنترلر ارسال شود؟",
                () -> gateway.send("تغییر تقویم دستگاه", "CD"));
    }

    private void languageDialog() {
        confirm("تغییر زبان دستگاه", "فرمان تغییر زبان پاسخ‌ها برای کنترلر ارسال شود؟",
                () -> gateway.send("تغییر زبان دستگاه", "CL"));
    }

    private void confirm(String title, String message, Runnable action) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setNegativeButton("انصراف", null)
                .setPositiveButton("تأیید", (dialog, which) -> action.run()).show();
    }

    private void runValidated(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException error) { toast("مقدار واردشده معتبر نیست"); }
    }

    private interface RemoteAction { void run(int number); }

    private void withRemote(EditText input, RemoteAction action) {
        runValidated(() -> action.run(Integer.parseInt(input.getText().toString())));
    }

    private String maskPhone(String phone) {
        if (phone.length() < 7) return phone;
        return phone.substring(0, 4) + " ••• " + phone.substring(phone.length() - 3);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void goBackFromDetail() {
        if (detailBack != null) {
            Runnable back = detailBack;
            detailBack = null;
            back.run();
        } else {
            selectNav(NAV_MORE);
        }
    }

    @Override
    public void onBackPressed() {
        if (detailBack != null) goBackFromDetail();
        else super.onBackPressed();
    }
}
