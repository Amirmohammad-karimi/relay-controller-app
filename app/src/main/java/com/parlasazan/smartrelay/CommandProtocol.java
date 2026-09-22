package com.parlasazan.smartrelay;

/** Pure representation of the controller's documented SMS command protocol. */
public final class CommandProtocol {
    private CommandProtocol() {}

    public static String compose(String devicePin, String command) {
        if (devicePin == null || !devicePin.matches("\\d{4}")) {
            throw new IllegalArgumentException("Device PIN must contain four digits");
        }
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("Command is required");
        }
        return devicePin + command;
    }

    public static String relay(int relay, boolean on) {
        requireRelay(relay);
        return "R" + relay + (on ? "ON" : "OFF");
    }

    public static String relayName(int relay, String name) {
        requireRelay(relay);
        return "R" + relay + "N" + clean(name, 15);
    }

    public static String relayTime(int relay, int seconds) {
        requireRelay(relay);
        if (seconds < 0 || seconds > 999) {
            throw new IllegalArgumentException("Relay time must be between 0 and 999 seconds");
        }
        return "R" + relay + "T" + seconds;
    }

    public static String changePin(String newPin) {
        if (newPin == null || !newPin.matches("\\d{4}")) {
            throw new IllegalArgumentException("New PIN must contain four digits");
        }
        return "CP" + newPin;
    }

    public static String addPhone(String phone) { return "AD" + cleanPhone(phone); }
    public static String deletePhone(String phone) { return "DN" + cleanPhone(phone); }
    public static String enableRemote(int number) { return "ENR" + remote(number); }
    public static String disableRemote(int number) { return "DSR" + remote(number); }
    public static String deleteRemote(int number) { return "DR" + remote(number); }
    public static String setBalanceFormula(String formula) { return "CF" + clean(formula, 24); }
    public static String recharge(String code) { return "AC" + clean(code, 24); }
    public static String syncDateTime(String packed) { return "TS" + clean(packed, 24); }

    private static String cleanPhone(String value) {
        String result = PhoneNumber.normalize(value);
        if (!PhoneNumber.isValid(result)) {
            throw new IllegalArgumentException("Invalid phone number");
        }
        return result;
    }

    private static int remote(int number) {
        if (number < 1 || number > 500) {
            throw new IllegalArgumentException("Remote number must be between 1 and 500");
        }
        return number;
    }

    private static String clean(String value, int max) {
        String result = value == null ? "" : value.trim();
        if (result.length() > max || result.contains("\n") || result.contains("\r")) {
            throw new IllegalArgumentException("Invalid value");
        }
        return result;
    }

    private static void requireRelay(int relay) {
        if (relay != 1 && relay != 2) throw new IllegalArgumentException("Relay must be 1 or 2");
    }
}
