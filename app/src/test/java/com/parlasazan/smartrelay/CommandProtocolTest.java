package com.parlasazan.smartrelay;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public final class CommandProtocolTest {
    @Test public void composesAuthenticatedRelayCommand() {
        assertEquals("1234R1ON", CommandProtocol.compose("1234", CommandProtocol.relay(1, true)));
        assertEquals("1234R2OFF", CommandProtocol.compose("1234", CommandProtocol.relay(2, false)));
    }

    @Test public void validatesRemoteRange() {
        assertEquals("ENR500", CommandProtocol.enableRemote(500));
        assertEquals("DSR12", CommandProtocol.disableRemote(12));
        assertEquals("DR12", CommandProtocol.deleteRemote(12));
        assertThrows(IllegalArgumentException.class, () -> CommandProtocol.enableRemote(501));
    }

    @Test public void validatesDevicePin() {
        assertThrows(IllegalArgumentException.class, () -> CommandProtocol.compose("12", "CHECK"));
    }

    @Test public void acceptsUserEnteredDevicePhoneFormats() {
        assertEquals("09121234567", PhoneNumber.normalize("۰۹۱۲ ۱۲۳ ۴۵۶۷"));
        assertEquals("+989121234567", PhoneNumber.normalize("+98 (912) 123-4567"));
        assertEquals("AD09121234567", CommandProtocol.addPhone("۰۹۱۲-۱۲۳-۴۵۶۷"));
    }

    @Test public void convertsGregorianDateToSolarHijri() {
        assertArrayEquals(new int[]{1403, 1, 1}, PersianDate.fromGregorian(2024, 3, 20));
        assertArrayEquals(new int[]{1404, 1, 1}, PersianDate.fromGregorian(2025, 3, 21));
    }
}
