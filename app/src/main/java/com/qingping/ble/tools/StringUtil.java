package com.qingping.ble.tools;

public class StringUtil {
    public static String hexToAscii(String hexString) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexString.length(); i += 2) {
            String str = hexString.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    /**
     * 字节数组转换为hexString
     *
     * @param bytes
     * @return
     */
    public static String toHexString(byte[] bytes) {

        StringBuilder sb = new StringBuilder();

        if (bytes != null)
            for (byte b : bytes) {

                final String hexString = Integer.toHexString(b & 0xff);

                if (hexString.length() == 1)
                    sb.append('0');

                sb.append(hexString);
            }

        return sb.toString();
    }

    /**
     * hexString 转换为字节数组
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i + 1), 16));
        }
        return b;
    }

    /**
     * 广播报解析
     *
     * @param hexServiceData
     * @return
     */
    public static QPDevice parseServiceData(String hexServiceData) {
        if (hexServiceData.length() < 16) {
            return null;
        }
        QPDevice device = new QPDevice();
        String frameControl = hexServiceData.substring(0, 2);
        device.setBind((((Character.digit(frameControl.charAt(0), 16) << 4) + Character.digit(frameControl.charAt(1), 16)) & 0x2) > 0);
        device.setProductId(hexServiceData.substring(2, 4));

        String mac = "";
        for (int i = 0; i < 6; i++) {
            mac = hexServiceData.substring(4 + (i * 2), 4 + (i * 2) + 2) + mac;
        }
        device.setMac(mac);

        return device;
    }


    /**
     * Integer 型温湿度值转换为 hexString类型
     *
     * @param temperature
     * @param humidity
     * @return
     */

    public static String tempAndHumiToHexString(final float temperature, final float humidity) {
        int temp = Math.round(temperature * 10);
        int hum = Math.round(humidity * 10);
        return formatAndReverseHexString(Integer.toHexString(temp)) + formatAndReverseHexString(Integer.toHexString(hum));
    }


    /**
     * 格式化并反转hex字符串
     *
     * @param target
     * @return
     */
    private static final String formatAndReverseHexString(final String target) {
        String result = "";
        String newTarget =
                target.length() < 4 ? String.format("%4s", target).replace(" ", "0") : target;

        int len = newTarget.length();
        result += newTarget.substring(len - 2);
        result += newTarget.substring(len - 4, len - 2);
        return result;
    }


}
