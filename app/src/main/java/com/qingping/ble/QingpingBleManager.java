package com.qingping.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;

import com.qingping.ble.tools.StringUtil;

import java.util.Map;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

public class QingpingBleManager extends BleManager {
    private static final String TAG = "QingpingBleManager";
    private BluetoothGattCharacteristic baseWriteCharacteristic;
    private BluetoothGattCharacteristic baseReadCharacteristic;
    private BluetoothGattCharacteristic myWriteCharacteristic;
    private BluetoothGattCharacteristic myReadCharacteristic;
    private static final ParcelUuid QP_UUID = ParcelUuid.fromString("0000fdcd-0000-1000-8000-00805f9b34fb");

    private Map<String, UUID> uuidsMap;

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


    public void connectAndVerify() {

    }

    public void connectAndBind() {

    }


    public void startScan() {

    }


   synchronized public void verify() {
         waitForNotification(baseReadCharacteristic)
                .trigger(writeCharacteristic(baseWriteCharacteristic, new Data(StringUtil.hexStringToByteArray("11013e4de816d0fe9b67c622fbe0d4dc4709")), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE))
                .with((device, data) -> {
                    Log.e(TAG, "verify: " + StringUtil.toHexString(data.getValue()));

                }).done(device -> {
                    Log.e(TAG, "onWriteDone");
                }).enqueue();

        waitForNotification(baseReadCharacteristic)
                .trigger(writeCharacteristic(baseWriteCharacteristic, new Data(StringUtil.hexStringToByteArray("11023e4de816d0fe9b67c622fbe0d4dc4709")), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE))
                .with((device, data) -> {
                    Log.e(TAG, "verify1102: " + StringUtil.toHexString(data.getValue()));

                }).done(device -> {
                    Log.e(TAG, "onWriteDone2");
                }).enqueue();

        float tempOffset = 0f;
        float humOffset = 0;
        String offset = StringUtil.toHexString(tempOffset, humOffset);

        waitForNotification(myReadCharacteristic)
                .trigger(writeCharacteristic(myWriteCharacteristic, new Data(StringUtil.hexStringToByteArray("053a" + offset) ), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE))
                .with((device, data) -> {
                    Log.e(TAG, "offset: " + StringUtil.toHexString(data.getValue()));
                }).done(device -> {
                    Log.e(TAG, "onWriteDone3");
                }).enqueue();

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
            (device, data) -> Log.e(TAG, "onDataReceived: " + StringUtil.toHexString(data.getValue()));


}

