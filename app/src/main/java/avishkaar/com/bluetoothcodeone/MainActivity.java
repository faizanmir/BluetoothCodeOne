package avishkaar.com.bluetoothcodeone;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {
    ServiceConnection serviceConnection ;
     BluetoothService bluetoothService;
    String deviceName,deviceAddress;
    static  BluetoothService bts;
    private static final String TAG = "MainActivity";
    Button send,disconnect,reconnect;
    TextView status,name,address;
    BroadcastReceiver broadcastReceiver;
    ArrayList<Message>messageArrayList;
    Handler handler;
    Intent serviceIntent;

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
        messageArrayList = new ArrayList<>();
        handler = new Handler();


        Log.e(TAG, "onCreate: " + "NAME:"+"  " + deviceName + "ADDRESS:"+"   " + deviceAddress);
        serviceIntent = new Intent(MainActivity.this,BluetoothService.class);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bts =  bluetoothService = ((BluetoothService.LocalBinder) service).getService();
                bluetoothService.connect(deviceName,deviceAddress);
                bluetoothService.mHandlerToService(handler);


            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bluetoothService.disconnect();
                bluetoothService.unbindService(serviceConnection);
            }
        };

        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        //startService(serviceIntent);

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
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        unbindService(serviceConnection);
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

    public void newActivity(View view) {
        Intent intent = new Intent(MainActivity.this, RemoteActivity.class);
        startActivity(intent);
    }


}
