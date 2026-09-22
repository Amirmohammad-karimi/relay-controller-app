package com.parlasazan.smartrelay;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;

/** Stores controller secrets encrypted by a non-exportable Android Keystore key. */
public final class SecureStore {
    private static final String PREFS = "parla_secure_preferences";
    private static final String KEY_ALIAS = "parla_controller_key_v1";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int PIN_ITERATIONS = 120_000;

    public static final String DEVICE_PHONE = "device_phone";
    public static final String DEVICE_PIN = "device_pin";
    public static final String BALANCE_FORMULA = "balance_formula";
    public static final String RELAY_1_NAME = "relay_1_name";
    public static final String RELAY_2_NAME = "relay_2_name";
    public static final String RELAY_1_TIME = "relay_1_time";
    public static final String RELAY_2_TIME = "relay_2_time";

    private final SharedPreferences prefs;

    public SecureStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isConfigured() {
        return !getSecret(DEVICE_PHONE, "").trim().isEmpty()
                && getSecret(DEVICE_PIN, "").matches("\\d{4}");
    }

    public void putSecret(String key, String value) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key());
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            String packed = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP) + "."
                    + Base64.encodeToString(encrypted, Base64.NO_WRAP);
            prefs.edit().putString(key, packed).apply();
        } catch (Exception error) {
            throw new IllegalStateException("Unable to encrypt app settings", error);
        }
    }

    public String getSecret(String key, String fallback) {
        String packed = prefs.getString(key, null);
        if (packed == null) return fallback;
        try {
            String[] pieces = packed.split("\\.", 2);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key(),
                    new GCMParameterSpec(128, Base64.decode(pieces[0], Base64.NO_WRAP)));
            return new String(cipher.doFinal(Base64.decode(pieces[1], Base64.NO_WRAP)), StandardCharsets.UTF_8);
        } catch (Exception error) {
            return fallback;
        }
    }

    public void setAppPin(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("App PIN must contain four digits");
        }
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] hash = pinHash(pin, salt);
            prefs.edit()
                    .putString("app_pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                    .putString("app_pin_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
                    .putBoolean("pin_enabled", true)
                    .apply();
        } catch (Exception error) {
            throw new IllegalStateException("Unable to protect app PIN", error);
        }
    }

    public boolean verifyAppPin(String pin) {
        try {
            byte[] salt = Base64.decode(prefs.getString("app_pin_salt", ""), Base64.NO_WRAP);
            byte[] expected = Base64.decode(prefs.getString("app_pin_hash", ""), Base64.NO_WRAP);
            return expected.length > 0 && MessageDigest.isEqual(expected, pinHash(pin, salt));
        } catch (Exception error) {
            return false;
        }
    }

    public boolean isPinEnabled() { return prefs.getBoolean("pin_enabled", true); }
    public void setPinEnabled(boolean enabled) { prefs.edit().putBoolean("pin_enabled", enabled).apply(); }
    public boolean previewSms() { return prefs.getBoolean("preview_sms", true); }
    public void setPreviewSms(boolean enabled) { prefs.edit().putBoolean("preview_sms", enabled).apply(); }

    private byte[] pinHash(String pin, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, PIN_ITERATIONS, 256);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    }

    private SecretKey key() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());
            generator.generateKey();
        }
        return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
    }
}
