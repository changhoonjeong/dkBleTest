package kr.onekey.app.bletest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import kr.onekey.app.bletest.Constant.BleConstant;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    TextView textStatus;
    Button btnSearch, bleAddressShow, bleShowLog;
    ListView listView;

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    private final static int REQUEST_ENABLE_BT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get permission
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);

        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // variables
        textStatus = (TextView) findViewById(R.id.text_status);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        bleAddressShow = (Button) findViewById(R.id.bluetoothShowLog);
        bleShowLog = (Button) findViewById(R.id.bleShowLog);
        listView = (ListView) findViewById(R.id.listview);

        // Show paired devices
        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        listView.setAdapter(btArrayAdapter);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscoverable(); // 내 핸드폰 Peripheral 등록
            }
        });

        bleAddressShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start, stop status 넣어 두고 일정 시간만 진행 하도록 수정
                btAdapter.startDiscovery(); // bluetooth 기기 검색 시작
            }
        });

        bleShowLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice(true); // ble 기기 검색 시작
            }
        });

        // 내 device name, 내 bluetooth address 가져오기 (02:00:00:00:00:00 나오는 것 테스트)
        Log.d(TAG,"mBluetoothAdapter.getName()... " + btAdapter.getName());
        Log.d(TAG,"mBluetoothAdapter.getAddress()... " + btAdapter.getAddress());

        initMain();
    }

    private void initMain(){
        initBle();
        initFilter();
    }

    private void initBle(){
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = BluetoothAdapter.getDefaultAdapter();   //블루투스 adapter 획득
        mBluetoothAdapter = bluetoothManager.getAdapter();
        scanLeDeviceHandler = new Handler();
        mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void initFilter(){
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //연결 확인
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //연결 끊김 확인
        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND);    //기기 검색됨
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);   //기기 검색 시작
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);  //기기 검색 종료
        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(receiver, stateFilter);
    }

    public void startDiscoverable(){
        Log.d("onClickButtonSearch","onClickButtonSearch...");
        Intent dIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        dIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,60 * 60);
        startActivity(dIntent);
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null){
                    String deviceName = device.getName();
//                    if (deviceName != null){
//                        Log.d(TAG,"type : " + device.getType());
//                    }
                    String deviceHardwareAddress = device.getAddress(); // MAC address
//                    String uuid = String.valueOf(device.getUuids()); // uuid
//                    Log.d("onReceive.. test.. : ","deviceName... : " + deviceName + "  deviceHardwareAddress... : " + deviceHardwareAddress);
                    Log.d("onReceive.. test.. : ","deviceName... : " + deviceName + "  uuid... : " + Arrays.toString(device.getUuids()));
                }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    public BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBleScanner;
    public Handler scanLeDeviceHandler;
    public boolean isScanning = false;

    public void scanLeDevice(final boolean enable) {
        Log.d(TAG, "scanLeDevice().. " + enable);

        if (enable) {
            scanLeDeviceHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanLeDevice(false);
                }
            }, BleConstant.LE_SCAN_PERIOD);

            isScanning = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "start scan!! if ");
                mBleScanner.startScan(mScanFilters(), mScanSettings(), mLeScanCallback);
//                mBleScanner.startScan(null, mScanSettings(), mLeScanCallback);
            } else {
                Log.d(TAG, "start scan!! else");
                mBleScanner.startScan(mLeScanCallback);
            }
//            sendMessageToActivity("startScan");

        } else {
            isScanning = false;
            mBleScanner.stopScan(mLeScanCallback);
//            sendMessageToActivity("stopScan");
        }
    }

    private List<ScanFilter> mScanFilters() {

        // uuid filter로 어떻게 거르는지 get 하면 다 null

        List<ScanFilter> scanFilters;

        scanFilters = new Vector<>();
        ScanFilter.Builder scanFilter = new ScanFilter.Builder();
//        scanFilter.setDeviceAddress("특정 기기의 MAC 주소"); //ex) 00:00:00:00:00:00
//        scanFilter.setServiceUuid(BleConstant.PARCEL_UUID);
        ScanFilter scan = scanFilter.build();
        scanFilters.add(scan);

        return scanFilters;
    }

    private ScanSettings mScanSettings() {
        ScanSettings scanSettings;

        ScanSettings.Builder mScanSettings;
        mScanSettings = new ScanSettings.Builder();
        scanSettings = mScanSettings.build();

        return  scanSettings;
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "onScanFailed :: errorCode : " + errorCode);
        }

        private void processResult(final ScanResult result) {
            BluetoothDevice device = result.getDevice();
            ScanRecord scanRecord = result.getScanRecord();

            if (device == null) {
                Log.e(TAG, "No result. device.");
                return;
            } else {
                String name = device.getName();
                String address = device.getAddress();
                String uuid = String.valueOf(device.getUuids());
                Log.d(TAG, "name... " + name + "  address... : " + address + "   uuid... : " + uuid);
            }
        }
    };

    public boolean isConnectableDevice(final BluetoothDevice device) {
        if (device == null) return false;

        String name = device.getName();
        String address = device.getAddress();

        if (name != null ) {
            Log.e(TAG, "isConnectableDevice().. " + name + " will be connected");
            return true;
        }
        return false;
    }


}