package avishkaar.com.bluetoothcodeone;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity  {
    ServiceConnection serviceConnection ;
    BluetoothService bluetoothService;
    String deviceName,deviceAddress;

    private static final String TAG = "MainActivity";
    Button send,disconnect,reconnect;
    TextView status,name,address;
    BroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Intent intent = getIntent();
        deviceName = intent.getStringExtra("DEVICE-NAME");
        deviceAddress = intent.getStringExtra("DEVICE-ADDRESS");
        name.setText(deviceName);
        address.setText(deviceAddress);
        BluetoothService.BluetoothInterface bluetoothInterface = new BluetoothService.BluetoothInterface() {
            @Override
            public void onConnectionChange() {
                Log.e(TAG, "onConnectionChange: "  );
            }

            @Override
            public void onServiceDiscovered() {
                Log.e(TAG, "onServiceDiscovered: "  );
            }

            @Override
            public void onCharacteristicWrite() {

            }
        };

        Log.e(TAG, "onCreate: " + "NAME:"+"  " + deviceName + "ADDRESS:"+"   " + deviceAddress);
        Intent serviceIntent = new Intent(MainActivity.this,BluetoothService.class);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bluetoothService = ((BluetoothService.LocalBinder) service).getService();
                bluetoothService.connect(deviceName,deviceAddress);

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bluetoothService.disconnect();
                bluetoothService.unbindService(serviceConnection);
            }
        };

        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothService.sendData("av,22,MC,1,DM1,720,0,~");
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothService.disconnect();
                //bluetoothService.stopSelf();
            }
        });
        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {bluetoothService.connect(deviceName,deviceAddress);
            }
        });












//    bluetoothService.listener(bluetoothInterface);

         broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String broadcast = intent.getAction();
                if(BluetoothService.ACTION_GATT_CONNECTED.equals(broadcast))
                {
                    connectionUpdate("Connected");
                }
                else if(BluetoothService.ACTION_GATT_DISCONNECTED.equals(broadcast))
                {
                    connectionUpdate("Disconnected");
                }
                else if (BluetoothService.ACTION_GATT_SERVICES_DISCOVERED.equals(broadcast))
                {
                    bluetoothService.getService();
                }
            }
        };
         registerReceiver(broadcastReceiver,makeGattUpdateIntentFilter());




    }



    void init()
    {
        status = findViewById(R.id.status);
        name = findViewById(R.id.deviceName);
        address = findViewById(R.id.deviceAddress);
        disconnect = findViewById(R.id.disconnect);
        send = findViewById(R.id.sendData);
        reconnect = findViewById(R.id.connect);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    void connectionUpdate(final String update){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText(update);
            }
        });

    }
}
