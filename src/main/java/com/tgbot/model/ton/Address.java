package com.tgbot.model.ton;

import org.apache.commons.lang3.StringUtils;

import java.util.Base64;

import static com.tgbot.utils.TonUtils.bytesToHex;
import static com.tgbot.utils.TonUtils.hexToBytes;

public class Address {
    private static final int TEST_FLAG = 0x80;
    private static final int BOUNCEABLE_TAG = 0x11;
    private static final int NON_BOUNCEABLE_TAG = 0x51;

    private Integer wc;
    private int[] hashPart;
    private boolean urlSafe;
    private boolean testOnly;
    private boolean bounceable;
    private boolean userFriendly;

    public boolean isValid(String address) {
        try {
            new Address(address);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Address(String address) {
        if (StringUtils.isEmpty(address)) {
            throw new RuntimeException("Invalid address");
        }

        if (address.contains("-") || address.contains("_")) {
            this.urlSafe = true;
            address = address.replaceAll("-", "+").replaceAll("_", "/");
        } else {
            this.urlSafe = false;
        }

        if (address.indexOf(':') > -1) {
            String[] arr = address.split(":");
            if (arr.length != 2) {
                throw new Error("Invalid address " + address);
            }

            int wc = Integer.parseInt(arr[0]);
            if (wc != 0 && wc != -1) {
                throw new Error("Invalid address wc " + address);
            }

            String hex = arr[1];
            if (hex.length() != 64) {
                throw new Error("Invalid address hex " + address);
            }

            this.userFriendly = false;
            this.wc = wc;
            this.hashPart = hexToBytes(hex);
            this.testOnly = false;
            this.bounceable = false;
        } else {
            this.userFriendly = true;
            parseFriendlyAddress(address);
        }
    }

    @Override
    public String toString() {
        return toString(true, true, true, false);
    }

    public String toString(boolean userFriendly,
                           boolean urlSafe,
                           boolean bounceable,
                           boolean testOnly) {

        if (!userFriendly) {
            return this.wc + ":" + bytesToHex(this.hashPart);
        } else {
            int tag = bounceable ? BOUNCEABLE_TAG : NON_BOUNCEABLE_TAG;
            if (testOnly) {
                tag |= TEST_FLAG;
            }

            int[] addr = new int[34];
            addr[0] = tag;
            addr[1] = this.wc;
            System.arraycopy(this.hashPart, 0, addr, 2, 32);

            byte[] addressWithChecksum = new byte[36];
            for (int i = 0; i < addr.length; i++) {
                addressWithChecksum[i] = (byte) addr[i];
            }

            int[] ints = crc16(addr);
            for (int i = 0; i < ints.length; i++) {
                addressWithChecksum[i + 34] = (byte) ints[i];
            }

            String addressBase64 = Base64.getEncoder().encodeToString(addressWithChecksum);

            if (urlSafe) {
                addressBase64 = addressBase64.replaceAll("\\+", "-").replaceAll("/", "_");
            }

            return addressBase64;
        }
    }

    private void parseFriendlyAddress(String addressString) {
        if (addressString.length() != 48) {
            throw new Error("User-friendly address should contain strictly 48 characters");
        }

        byte[] address = Base64.getDecoder().decode(addressString);
        if (address.length != 36) { // 1byte tag + 1byte workchain + 32 bytes hash + 2 byte crc
            throw new RuntimeException("Unknown address type: byte length is not equal to 36");
        }

        int[] addr = new int[35];
        System.arraycopy(address, 0, addr, 0, 35);

        byte[] crc = new byte[3];
        System.arraycopy(address, 34, crc, 0, 2);

        int[] calcedCrc = crc16(addr);
        if (!(calcedCrc[0] == crc[0] && calcedCrc[1] == crc[1])) {
            throw new RuntimeException("Wrong crc16 hashsum");
        }

        int tag = addr[0];
        boolean isTestOnly = false;
        boolean isBounceable = false;
        if ((tag & TEST_FLAG) != 0) {
            isTestOnly = true;
            tag = tag ^ TEST_FLAG;
        }

        if ((tag != BOUNCEABLE_TAG) && (tag != NON_BOUNCEABLE_TAG)) {
            throw new RuntimeException("Unknown address tag");
        }

        isBounceable = tag == BOUNCEABLE_TAG;

        int workchain;
        if (addr[1] == 0xff) { // TODO we should read signed integer here
            workchain = -1;
        } else {
            workchain = addr[1];
        }

        if (workchain != 0 && workchain != -1) {
            throw new Error("Invalid address wc " + workchain);
        }

        int[] hashPart = new int[32];
        System.arraycopy(addr, 2, hashPart, 0, 32);

        this.testOnly = isTestOnly;
        this.bounceable = isBounceable;
        this.wc = workchain;
        this.hashPart = hashPart;
    }

    private int[] crc16(int[] data) {
        int poly = 0x1021;
        int reg = 0;
        int[] message = new int[data.length + 2];
        System.arraycopy(data, 0, message, 0, data.length);

        for (int b : message) {
            int mask = 0x80;
            while (mask > 0) {
                reg <<= 1;
                if ((b & mask) != 0) {
                    reg += 1;
                }
                mask >>= 1;
                if (reg > 0xffff) {
                    reg &= 0xffff;
                    reg ^= poly;
                }
            }
        }

        return new int[]{Double.valueOf(Math.floor((double) reg / 256)).intValue(), reg % 256};
    }
}
