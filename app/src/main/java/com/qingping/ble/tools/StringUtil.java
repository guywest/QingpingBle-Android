package com.qingping.ble.tools;

public class StringUtil {
    public static String toHexString(byte[] bytes) {

        StringBuilder sb = new StringBuilder();

        if (bytes != null)
            for (byte b : bytes) {

                final String hexString = Integer.toHexString(b & 0xff);

                if (hexString.length() == 1)
                    sb.append('0');

                sb.append(hexString);//.append(' ');
            }

        return sb.toString();//.toUpperCase();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }

    public static QPDevice parseServiceData(String hex) {
        if (hex.length() < 16) {
            return null;
        }
        QPDevice device = new QPDevice();
        String frameControl = hex.substring(0, 2);
        device.setBind((((Character.digit(frameControl.charAt(0), 16) << 4) + Character.digit(frameControl.charAt(1), 16)) & 0x2) > 0);
        device.setProductId(hex.substring(2, 4));

        String mac = "";
        for (int i = 0; i < 6; i++) {
            mac = hex.substring(4 + (i * 2), 4 + (i * 2) + 2) + mac;
        }
        device.setMac(mac);

        return device;
    }
}
