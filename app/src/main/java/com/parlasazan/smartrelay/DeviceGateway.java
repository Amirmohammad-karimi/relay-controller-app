package com.parlasazan.smartrelay;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;

public final class DeviceGateway {
    public static final int REQUEST_SMS = 501;
    public static final int REQUEST_CALL = 502;

    private final Activity activity;
    private final SecureStore store;

    public DeviceGateway(Activity activity) {
        this.activity = activity;
        this.store = new SecureStore(activity);
    }

    public void send(String actionLabel, String command) {
        send(actionLabel, command, () -> {});
    }

    public void send(String actionLabel, String command, Runnable onDispatched) {
        String phone = store.getSecret(SecureStore.DEVICE_PHONE, "");
        String pin = store.getSecret(SecureStore.DEVICE_PIN, "");
        if (phone.trim().isEmpty() || !pin.matches("\\d{4}")) {
            toast("ابتدا اطلاعات دستگاه را تکمیل کنید");
            return;
        }
        if (activity.checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS);
            toast("پس از تأیید دسترسی، فرمان را دوباره انتخاب کنید");
            return;
        }
        Runnable dispatch = () -> {
            try {
                String message = CommandProtocol.compose(pin, command);
                SmsManager.getDefault().sendTextMessage(phone, null, message, null, null);
                onDispatched.run();
                toast("فرمان «" + actionLabel + "» ارسال شد");
            } catch (Exception error) {
                toast("ارسال پیامک ناموفق بود");
            }
        };
        if (store.previewSms()) {
            new AlertDialog.Builder(activity)
                    .setTitle("تأیید ارسال فرمان")
                    .setMessage(actionLabel + "\n\nگیرنده: " + maskPhone(phone)
                            + "\nرمز دستگاه در پیش‌نمایش پنهان شده است.")
                    .setNegativeButton("انصراف", null)
                    .setPositiveButton("ارسال پیامک", (dialog, which) -> dispatch.run())
                    .show();
        } else {
            dispatch.run();
        }
    }

    public void callDevice() {
        String phone = store.getSecret(SecureStore.DEVICE_PHONE, "");
        if (phone.trim().isEmpty()) {
            toast("شماره سیم‌کارت دستگاه ثبت نشده است");
            return;
        }
        if (activity.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            toast("پس از تأیید دسترسی، تماس را دوباره انتخاب کنید");
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle("تماس با دستگاه")
                .setMessage("با شماره " + maskPhone(phone) + " تماس گرفته شود؟")
                .setNegativeButton("انصراف", null)
                .setPositiveButton("تماس", (dialog, which) -> activity.startActivity(
                        new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(phone)))))
                .show();
    }

    private String maskPhone(String phone) {
        if (phone.length() < 7) return phone;
        return phone.substring(0, 4) + "•••" + phone.substring(phone.length() - 3);
    }

    private void toast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }
}
