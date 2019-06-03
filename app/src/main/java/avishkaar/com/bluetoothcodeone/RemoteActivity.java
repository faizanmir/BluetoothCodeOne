package avishkaar.com.bluetoothcodeone;

import android.annotation.SuppressLint;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class RemoteActivity extends AppCompatActivity {
    ServiceConnection serviceConnection;
    BluetoothService bluetoothService;
    BroadcastReceiver broadcastReceiver;
    TextView status;
    Button up,down,left,right;
    Boolean ack = false;
    Intent serviceIntent;
    private static final String TAG = "RemoteActivity";
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_service);
        up = findViewById(R.id.up);
        down = findViewById(R.id.down);
        right = findViewById(R.id.rightBut);
        left = findViewById(R.id.leftBut);
         serviceIntent = new Intent(RemoteActivity.this,BluetoothService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                 bluetoothService = ((BluetoothService.LocalBinder) service).getService();

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bluetoothService.disconnect();
                bluetoothService.unbindService(serviceConnection);
            }
        };

        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        //This service can be bound to and unbound to on demand




        status = findViewById(R.id.newStatus);
        broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String broadcast = intent.getAction();
                        Log.e(TAG, "onReceive: " + broadcast );
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
                        else if(BluetoothService.ACTION_DATA_AVAILABLE.equals(broadcast))
                        {    String data = intent.getStringExtra(BluetoothService.EXTRA_DATA);
                            Log.e(TAG, "onReceive: " + broadcast  + "    " + data );
                            ack = true;

                        }
                    }
                };
                registerReceiver(broadcastReceiver,makeGattUpdateIntentFilter());
                up.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bluetoothService.sendData(ControlStrings.stop);
                        Log.e(TAG, "onClick: " + "Button up" );
                    }
                });

                up.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() ==MotionEvent.ACTION_DOWN)
                        {
                            bluetoothService.sendData(ControlStrings.FORWARD);
                        }
                        else if(event.getAction()==MotionEvent.ACTION_UP)
                        {
                            up.performClick();
                        }
                        return true;
                    }
                });

                right.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() ==MotionEvent.ACTION_DOWN)
                        {
                            bluetoothService.sendData(ControlStrings.oneMotorStart);
                        }
                        else if(event.getAction()==MotionEvent.ACTION_UP)
                        {
                            bluetoothService.sendData(ControlStrings.oneMotorStop);
                        }
                        return true;
                    }
                });

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

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, makeGattUpdateIntentFilter());
        bindService(serviceIntent,serviceConnection,BIND_AUTO_CREATE);
        //This needs further testing

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        unbindService(serviceConnection);
        //This needs further testing
    }








//This is just a test function
    public void noIdea(View view) {
        bluetoothService.testFunction();
        bluetoothService.sendData("av,7,STALL,~");
    }

}
