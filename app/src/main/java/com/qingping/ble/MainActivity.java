package com.qingping.ble;

import android.Manifest;
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
    private static final ParcelUuid QP_UUID = ParcelUuid.fromString("0000fdcd-0000-1000-8000-00805f9b34fb");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //授权权限
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.WAKE_LOCK}, 1);

        findViewById(R.id.ble_start_scan).setOnClickListener(view -> {
            this.bleConnect();
        });

    }

    private void bleConnect() {
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setReportDelay(2000)
                .setUseHardwareBatchingIfSupported(true)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceData(ParcelUuid.fromString("0000fdcd-0000-1000-8000-00805f9b34fb"), null).build());
        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                Log.d(TAG, "onScanResult: " + callbackType + result.toString());
            }

            @Override
            public void onBatchScanResults(@NonNull List<ScanResult> results) {
                results.forEach(result -> {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                        Log.e(TAG, "onBatchScanResults: " );
//                        return;
                    }
                    if (result.getDevice().getName() != null) {
                        ScanRecord scanRecord = result.getScanRecord();
                        if (scanRecord == null) {
//                            Log.e(TAG, "onBatchScanResults: scanRecord is null");
                            return;
                        }
                        if (scanRecord.getServiceData() == null) {
//                            Log.e(TAG, "onBatchScanResults: ServiceData is null");
                            return;
                        }
                        Set<ParcelUuid> uuidsSet = scanRecord.getServiceData().keySet();
                        if (!uuidsSet.contains(QP_UUID)) {
//                            Log.e(TAG, "onBatchScanResults: 不是青萍的设备");
                            return;
                        }
                        String hexData = StringUtil.toHexString(result.getScanRecord().getServiceData(QP_UUID));
                        QPDevice device = StringUtil.parseServiceData(hexData);
                        if(device == null) {
                            return;
                        }
                        QingpingBleManager qingpingBleManager = new QingpingBleManager(getApplicationContext(), QPUUID.getUUIDByProductId(QPProductID.CGF1L));
                        if (device.getProductId().equalsIgnoreCase(QPProductID.CGF1L) && device.isBind()) {
                            Log.e(TAG, "onBatchScanResults-hexData: " + hexData + ", productId:" + device.toString());
                            BluetoothLeScannerCompat.getScanner().stopScan(this);
                            qingpingBleManager.connect(result.getDevice()).retry(3).useAutoConnect(true).done(device1 -> {
                                Log.e(TAG, "onBatchScanResults: connect success" + device1.getName());
                                qingpingBleManager.verify();
                            }).enqueue();
                        }

//                        Log.d(TAG, "onBatchScanResults: " + result.getDevice().getName() + ", " + StringUtil.toHexString(result.getScanRecord().getServiceData(QP_UUID)));
                    }
                });
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG, "onScanFailed: ");
            }
        };
        scanner.startScan(filters, settings, callback);
    }


}