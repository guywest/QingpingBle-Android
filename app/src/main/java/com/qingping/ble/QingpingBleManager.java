package com.qingping.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.qingping.ble.tools.StringUtil;

import java.util.Map;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.WaitForValueChangedRequest;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

public class QingpingBleManager extends BleManager {
    private static final String TAG = "QingpingBleManager";
    private BluetoothGattCharacteristic baseWriteCharacteristic;
    private BluetoothGattCharacteristic baseReadCharacteristic;
    private BluetoothGattCharacteristic myWriteCharacteristic;
    private BluetoothGattCharacteristic myReadCharacteristic;

    private Map<String, UUID> uuidsMap;

    /**
     * BleManger 对应的构造
     *
     * @param context  上下文
     * @param uuidsMap 设备对应的UUIDMap，需要从 QPUUID 的 getUUIDByProductId 方法中传入对应的产品类型获取，产品类型可查看产品背部的Model
     */
    public QingpingBleManager(@NonNull Context context, @NonNull Map<String, UUID> uuidsMap) {
        super(context);
        this.uuidsMap = uuidsMap;
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new QingpingGattCallBackImpl();
    }

    @Override
    public int getMinLogPriority() {
        return Log.VERBOSE;
    }

    @Override
    public void log(int priority, @NonNull String message) {
        Log.println(priority, TAG, message);
    }

    /**
     * 向设备写入数据，并等待设备回复，设置 token、验证token、同步时间、同步时区用次方法
     * @param hexString
     * @return
     */
    public WaitForValueChangedRequest writeBaseCharacteristic(final String hexString) {
        return waitForNotification(baseReadCharacteristic)
                .trigger(writeCharacteristic(baseWriteCharacteristic,
                        new Data(StringUtil.hexStringToByteArray(hexString)), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE));
    }

    /**
     * 向设备写入数据，并等待设备回复，除 设置 token、验证token、同步时间、同步时区 以外的其他操作用此方法
     * @param hexString
     * @return
     */
    public WaitForValueChangedRequest writeCharacteristic(final String hexString) {
        return waitForNotification(myReadCharacteristic)
                .trigger(writeCharacteristic(myWriteCharacteristic,
                        new Data(StringUtil.hexStringToByteArray(hexString)), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE));

    }

    private class QingpingGattCallBackImpl extends BleManagerGattCallback {

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            BluetoothGattService service = gatt.getService(uuidsMap.get("service"));
            if (service != null) {
                baseWriteCharacteristic = service.getCharacteristic(uuidsMap.get("baseWrite"));
                baseReadCharacteristic = service.getCharacteristic(uuidsMap.get("baseNotify"));
                myWriteCharacteristic = service.getCharacteristic(uuidsMap.get("myWrite"));
                myReadCharacteristic = service.getCharacteristic(uuidsMap.get("myNotify"));
            }
            return baseReadCharacteristic != null && baseWriteCharacteristic != null
                    && myReadCharacteristic != null && myWriteCharacteristic != null;
        }

        @Override
        protected boolean isOptionalServiceSupported(@NonNull BluetoothGatt gatt) {
            return false;
        }

        @Override
        protected void onServicesInvalidated() {
            Log.e(TAG, "onServicesInvalidated: ");
            baseWriteCharacteristic = null;
            baseReadCharacteristic = null;
            myWriteCharacteristic = null;
            myReadCharacteristic = null;
        }

        @Override
        protected void initialize() {
            beginAtomicRequestQueue()
                    .add(requestMtu(247))
                    .add(enableNotifications(baseReadCharacteristic))
                    .add(enableNotifications(myReadCharacteristic))
                    .done(device -> Log.e(TAG, "initialize: target initialized"))
                    .enqueue();
            setNotificationCallback(baseReadCharacteristic).with(dataReceivedCallback);
            setNotificationCallback(myReadCharacteristic).with(dataReceivedCallback);
        }
    }

    private final DataReceivedCallback dataReceivedCallback =
            (device, data) -> Log.e(TAG, "onDataReceived: " + StringUtil.tempAndHumiToHexString(data.getValue()));


}

