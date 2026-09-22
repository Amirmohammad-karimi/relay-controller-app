package com.parlasazan.smartrelay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public final class SetupActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.prepare(this);
        SecureStore store = new SecureStore(this);

        LinearLayout page = Ui.page(this);
        page.addView(Ui.brandHeader(this, "راه‌اندازی اولیه", "اتصال کنترلر پارلا"));

        LinearLayout intro = Ui.card(this);
        intro.setBackground(Ui.roundRect(Ui.PALE, 22, 0, 0, this));
        intro.addView(Ui.text(this, "یک‌بار تنظیم کنید، همیشه در دسترس", 18, Ui.INK, true));
        intro.addView(Ui.text(this,
                "اطلاعات فقط روی همین تلفن و با کلید امن دستگاه ذخیره می‌شوند.",
                13, Ui.MUTED, false));
        page.addView(intro);

        LinearLayout form = Ui.card(this);
        form.addView(Ui.text(this, "اطلاعات سیم‌کارت کنترلر", 18, Ui.INK, true));
        form.addView(Ui.text(this, "شماره سیم‌کارت داخل دستگاه را خودتان وارد کنید؛ این شماره ثابت نیست و بعداً هم قابل تغییر است.",
                13, Ui.MUTED, false));
        form.addView(space(12));

        EditText phone = Ui.field(this, "شماره سیم‌کارت داخل دستگاه", InputType.TYPE_CLASS_PHONE);
        phone.setTextDirection(android.view.View.TEXT_DIRECTION_LTR);
        form.addView(phone);
        EditText devicePin = Ui.pinField(this, "رمز چهاررقمی دستگاه");
        form.addView(devicePin);
        EditText balance = Ui.field(this, "فرمول استعلام شارژ، مثال: *141*1#",
                InputType.TYPE_CLASS_PHONE);
        balance.setTextDirection(android.view.View.TEXT_DIRECTION_LTR);
        form.addView(balance);
        page.addView(form);

        LinearLayout security = Ui.card(this);
        security.addView(Ui.text(this, "قفل برنامه", 18, Ui.INK, true));
        security.addView(Ui.text(this, "این رمز با رمز کنترلر متفاوت است و برای ورود به برنامه استفاده می‌شود.",
                13, Ui.MUTED, false));
        security.addView(space(12));
        EditText appPin = Ui.pinField(this, "رمز ورود چهاررقمی");
        EditText appPinConfirm = Ui.pinField(this, "تکرار رمز ورود");
        security.addView(appPin);
        security.addView(appPinConfirm);
        page.addView(security);

        Button save = Ui.button(this, "ذخیره و ورود به داشبورد", Ui.PRIMARY, false);
        save.setOnClickListener(view -> {
            String phoneValue = PhoneNumber.normalize(phone.getText().toString());
            String devicePinValue = devicePin.getText().toString();
            String appPinValue = appPin.getText().toString();
            if (!PhoneNumber.isValid(phoneValue)) {
                phone.setError("شماره تلفن معتبر وارد کنید");
                return;
            }
            if (!devicePinValue.matches("\\d{4}")) {
                devicePin.setError("رمز دستگاه باید چهاررقمی باشد");
                return;
            }
            if (!appPinValue.matches("\\d{4}")) {
                appPin.setError("رمز برنامه باید چهاررقمی باشد");
                return;
            }
            if (!appPinValue.equals(appPinConfirm.getText().toString())) {
                appPinConfirm.setError("تکرار رمز یکسان نیست");
                return;
            }
            store.putSecret(SecureStore.DEVICE_PHONE, phoneValue);
            store.putSecret(SecureStore.DEVICE_PIN, devicePinValue);
            store.putSecret(SecureStore.BALANCE_FORMULA, balance.getText().toString().trim());
            store.setAppPin(appPinValue);
            Toast.makeText(this, "کنترلر با موفقیت اضافه شد", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
        page.addView(save);
        setContentView(Ui.scroll(this, page));
    }

    private android.view.View space(int dp) {
        android.view.View view = new android.view.View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, dp)));
        return view;
    }
}
