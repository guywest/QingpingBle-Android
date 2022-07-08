package com.qingping.ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.qingping.ble.tools.QPDevice;
import com.qingping.ble.tools.QPProductID;
import com.qingping.ble.tools.QPUUID;
import com.qingping.ble.tools.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //授权权限
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.WAKE_LOCK}, 1);

        findViewById(R.id.ble_scan_and_connect).setOnClickListener(view -> {
            this.bleScanConnect();
        });

    }


    private void bleScanConnect() {
        //扫描
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setReportDelay(2000)
                .setUseHardwareBatchingIfSupported(true)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        //设置过滤条件，只扫描青萍协议的广播
        filters.add(new ScanFilter.Builder().setServiceData(QPUUID.QP_UUID, null).build());

        //开始扫描
        try {
            scanner.startScan(filters, settings, scanCallback);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    //扫描回调
    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            parseScanResult(result);
        }

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            results.forEach(result -> parseScanResult(result));
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "onScanFailed: ");
        }
    };

    /**
     * 解析扫描结果
     *
     * @param result
     */
    @SuppressLint("MissingPermission")
    private void parseScanResult(ScanResult result) {
        if (result.getDevice().getName() != null) {
            ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord == null || scanRecord.getServiceData() == null) {
                return;
            }

            Set<ParcelUuid> uuidsSet = scanRecord.getServiceData().keySet();
            if (!uuidsSet.contains(QPUUID.QP_UUID)) {
                return;
            }
            String hexData = StringUtil.toHexString(result.getScanRecord().getServiceData(QPUUID.QP_UUID));
            QPDevice device = StringUtil.parseServiceData(hexData);
            if (device == null) {
                return;
            }

            if (device.getProductId().equalsIgnoreCase(QPProductID.CGF1L) && device.isBind()) {
                Log.e(TAG, "onBatchScanResults-hexData: " + hexData + ", productId:" + device.toString());
                //停止扫描
                stopScan();
                connectAndSettingTempOffset(result.getDevice());
            }

        }
    }


    /**
     * 连接、设置token、验证token、设置温度湿度offset
     * @param device
     */
    private void connectAndSettingTempOffset(BluetoothDevice device) {
        //这里传入了CGF1L（青萍商用温湿度计 E LoRa 版），开发时可以根据需要自定义
        QingpingBleManager qpBleManager = new QingpingBleManager(getApplicationContext(), QPUUID.getUUIDByProductId(QPProductID.CGF1L));

        qpBleManager.connect(device).retry(3).useAutoConnect(true).done(connectedDevice -> {
            Log.e(TAG, "onBatchScanResults: connect success");
            // token为 命令格式为1101+随机16字节（11为命令的长度，01是具体的命令，表示设置token），第一次绑定的时候 生成并保存，下次连接的时候直接用上次生成token去验证就成，不用
            // 再进行设置 token 了，也就是说下次连接的时候可以省略下面这一步
            qpBleManager.writeBaseCharacteristic("11013e4de816d0fe9b67c622fbe0d4dc4709")
                    .with((device1, data) -> {
                        Log.e(TAG, "设置 Token 结果: " + StringUtil.toHexString(data.getValue()));
                    }).done(device1 -> {
                        Log.e(TAG, "connectAndSettingTempOffset: 设置token完成");
                    }).enqueue();

            //验证token，命令格式为：1102 + 上一步设置的token
            qpBleManager.writeBaseCharacteristic("11023e4de816d0fe9b67c622fbe0d4dc4709")
                    .with((device1, data) -> {
                        Log.e(TAG, "验证 Token 结果: " + StringUtil.toHexString(data.getValue()));
                    }).done(device1 -> {
                        Log.e(TAG, "connectAndSettingTempOffset: 验证token完成");
                    }).enqueue();

            float tempOffset = 0f;
            float humOffset = 0;
            String offset = StringUtil.toHexString(tempOffset, humOffset);
            qpBleManager.writeCharacteristic("053a" + offset)
                    .with((device1, data) -> {
                        Log.e(TAG, "设置温湿度 offset 结果: " + StringUtil.toHexString(data.getValue()));
                    }).enqueue();
        }).enqueue();
    }


    /**
     * 停止扫描
     */
    private void stopScan() {
        BluetoothLeScannerCompat.getScanner().stopScan(scanCallback);
    }

}