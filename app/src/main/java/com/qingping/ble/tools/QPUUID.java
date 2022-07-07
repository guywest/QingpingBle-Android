package com.qingping.ble.tools;

import android.util.ArrayMap;

import java.util.Map;
import java.util.UUID;

/**
 * 定义青萍的UUID
 */
public class QPUUID {
    // 青萍协议相关的 Characteristic 都放在下面的服务里
    private static final UUID QP_SERVICE = UUID.fromString("22210000-554a-4546-5542-46534450464d");

    //基础的写特征，设备连接成功后的设置 token，验证 token、设置时区、时间等都是在向这个特征里写
    private static final UUID BASE_WRITE_CHARACTERISTIC = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
    //基础的通知特征，设备连接成功后 设置token、验证token、设置时区、时间后的设置结果会通过这个特征返回给客户端
    private static final UUID BASE_NOTIFY_CHARACTERISTIC = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");

    //每个设备对应的私有的特征，连接设成功，并且验证token通过后，与设备的通信主要靠私有特征来完成
    // 青萍商用温湿度气压计、青萍商用温湿度计、青萍温湿度气压计、青萍商用温湿度气压计 E
    private static final UUID PRIVATE_WRITE_CHARACTERISTIC_PH = UUID.fromString("00000009-0000-1000-8000-00805f9b34fb");
    private static final UUID PRIVATE_NOTIFY_CHARACTERISTIC_PH = UUID.fromString("0000000a-0000-1000-8000-00805f9b34fb");

    //青萍空气检测仪 Lite，型号 Model： CGDN1
    private static final UUID PRIVATE_WRITE_CHARACTERISTIC_DN = UUID.fromString("0000000d-0000-1000-8000-00805f9b34fb");
    private static final UUID PRIVATE_NOTIFY_CHARACTERISTIC_DN = UUID.fromString("0000000e-0000-1000-8000-00805f9b34fb");


    /**
     * 获取对应产品的 uuid
     *
     * @param productId 产品id 在 `QPProductID` 中查找
     * @return
     */
    public static Map<String, UUID> getUUIDByProductId(final String productId) {
        Map<String, UUID> uuids = new ArrayMap<>();
        uuids.put("service", QP_SERVICE);
        uuids.put("baseWrite", BASE_WRITE_CHARACTERISTIC);
        uuids.put("baseNotify", BASE_NOTIFY_CHARACTERISTIC);

        if (productId.equalsIgnoreCase(QPProductID.CGDN1)) {
            uuids.put("myWrite", PRIVATE_WRITE_CHARACTERISTIC_DN);
            uuids.put("myNotify", PRIVATE_NOTIFY_CHARACTERISTIC_DN);
        } else {
            uuids.put("myWrite", PRIVATE_WRITE_CHARACTERISTIC_PH);
            uuids.put("myNotify", PRIVATE_NOTIFY_CHARACTERISTIC_PH);
        }
        return uuids;
    }
}
