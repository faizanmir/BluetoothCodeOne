package avishkaar.com.bluetoothcodeone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DeviceScan extends AppCompatActivity {
        BluetoothLeScanner bluetoothScanner;
        BluetoothAdapter bluetoothAdapter;
        Button scan,stop;
        boolean mScanInstance = true;
         ArrayList<BluetoothModelClass> devices;
         ArrayList<BluetoothDevice>bluetoothDevices;
        BleAdapter bleAdapter;
    private static final String TAG = "DeviceScan";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
        scan = findViewById(R.id.scan);
        stop =  findViewById(R.id.stop);
        final Handler handler = new Handler();
        bluetoothDevices = new ArrayList<>();
        devices = new ArrayList<>();

        RecyclerView BleRv = findViewById(R.id.listView);

        final ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                try {
                    Log.e(TAG, "onScanResult: "+ result.getDevice().getAddress() + "     " +result.getDevice().getName() );
                    bluetoothDevices.add(result.getDevice());
                    if (Objects.requireNonNull(Objects.requireNonNull(result.getScanRecord()).getDeviceName()).length()>0) {
                        BluetoothModelClass bme = new BluetoothModelClass(result.getDevice().getName(), result.getDevice().getAddress());
                        if(!devices.contains(bme))
                        devices.add(bme);
                        bleAdapter.notifyDataSetChanged();

                    }
                }catch (NullPointerException e){}
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
        bleAdapter = new BleAdapter(devices,DeviceScan.this,bluetoothScanner,scanCallback);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        BleRv.setLayoutManager(layoutManager);
        BleRv.setAdapter(bleAdapter);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devices.clear();
                bleAdapter.notifyDataSetChanged();
                if(mScanInstance)
                {
                    bluetoothScanner.startScan(scanCallback);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothScanner.stopScan(scanCallback);
                        }
                    },5000);


                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothScanner.stopScan(scanCallback);
            }
        });







    }

}
