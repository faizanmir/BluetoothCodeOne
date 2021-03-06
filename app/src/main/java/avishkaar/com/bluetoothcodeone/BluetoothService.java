package avishkaar.com.bluetoothcodeone;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothService extends Service {
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;
    private static final String TAG = "BluetoothService";
    BluetoothAdapter bluetoothAdapter;
    BluetoothGattCharacteristic bluetoothGattCharacteristic;
    public static UUID uuidService = UUID.fromString("00000021-0000-1000-8000-00805f9b34fb");//change this for EPSON
    public static UUID uuidCharacteristic = UUID.fromString("00000052-0000-1000-8000-00805f9b34fb");//THIS TOO
    private int mConnectionState = STATE_DISCONNECTED;
    Handler mHandler;
    ExecutorService executorService;
    int packetsToSend;


    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "avishkaar.com.BluetoothCodeOne.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "avishkaar.com.BluetoothCodeOne.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "avishkaar.com.BluetoothCodeOne.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "avishkaar.com.BluetoothCodeOne.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "avishkaar.com.BluetoothCodeOne.EXTRA_DATA";
//


    public BluetoothService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        executorService = Executors.newSingleThreadExecutor();


    }
    void mHandlerToService(Handler mHandler)
    {
        this.mHandler = mHandler;
    }



    @Override
    public IBinder onBind(Intent intent) {
       return mBinder;
    }
    public class LocalBinder extends Binder
    {
        BluetoothService getService(){
            return BluetoothService.this;
        }

    }

    public IBinder mBinder = new LocalBinder();

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//           bluetoothInterface.onCharacteristicWrite();
            String intentAction;
            if(newState ==  BluetoothProfile.STATE_CONNECTED)
            {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadCastUpdate(intentAction);
                Log.e(TAG, "onConnectionStateChange: " + "CONNECTED TO GATT SERVER" );
                Log.e(TAG, "onConnectionStateChange: " + "   "+"Attempting SERVICE discovery" );
                bluetoothGatt.discoverServices();

            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.e(TAG, "onConnectionStateChange: " + "DISCONNECTED FROM GATT" );
                broadCastUpdate(intentAction);
            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status==BluetoothGatt.GATT_SUCCESS)
            {
                broadCastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }else Log.e(TAG, "onServicesDiscovered: " + status );
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            broadCastUpdate(ACTION_DATA_AVAILABLE,characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            broadCastUpdate(ACTION_DATA_AVAILABLE,characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {


        }
    };

    public void broadCastUpdate(String action){
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    public  void broadCastUpdate(String action,BluetoothGattCharacteristic characteristic)
    {
        Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        Log.i(TAG, "data"+characteristic.getValue());
       intent.putExtra(EXTRA_DATA,String.format("%s", new String(data)));
        sendBroadcast(intent);

    }


    public  void connect (String mBluetoothName,String mBluetoothAddress){
        Log.e(TAG, "connect : "+ "Device Received " + mBluetoothName);
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(mBluetoothAddress);
        bluetoothGatt = bluetoothDevice.connectGatt(this,false,bluetoothGattCallback);


    }

    public void sendData(final String data) {

        Log.e(TAG, "sendData:data " + data);
        int chunksize = 20;
        int start = 0;
        byte[] source = data.getBytes();
        int delay =0;

        if(data.length()>=30) {
             packetsToSend = (int) Math.ceil(data.length() / chunksize+1);
             delay = 30;
        }else if(
                data.length()>20){
                packetsToSend =(int) Math.ceil(data.length()/chunksize+1);
        }else {

                packetsToSend = 1;
                chunksize = data.length();
                delay =20;
        }

        byte[][] packets = new byte[packetsToSend][chunksize];

        for (int i = 0; i < packets.length; i++) {
                packets[i] = Arrays.copyOfRange(source,start,start+chunksize);
            start += chunksize;
            delay = 10;

        }

        Log.e(TAG, "sendData: " + Arrays.deepToString(packets));


            if(!(bluetoothGattCharacteristic ==null)) {
            for (final byte[] dataArray : packets) {
                final String dataInString = new String(dataArray);
                Log.e(TAG, "sendData:dataInString " + dataInString );
                Log.e(TAG, "sendData: "+Arrays.toString(dataArray) );

                final int finalDelay = delay;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(finalDelay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                            bluetoothGattCharacteristic.setValue(dataInString);
                            bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                    }
                };
                executorService.execute(runnable);

            }

        }
    }

    public void getService(){
        List<BluetoothGattService>serviceList = bluetoothGatt.getServices();

        for (BluetoothGattService s:serviceList)
        {
            Log.e(TAG, "getService: " + s.getUuid() );
        }
        for(BluetoothGattService bluetoothGattService: serviceList)

        {
            for(BluetoothGattCharacteristic c :bluetoothGattService.getCharacteristics())
        {
            Log.e(TAG, "getService:char "+ c.getUuid()  + "Service : "+ bluetoothGattService.getUuid());
        }

            if(bluetoothGattService.getUuid().equals(uuidService))
            {
                List<BluetoothGattCharacteristic>characteristicList = bluetoothGattService.getCharacteristics();
                for(BluetoothGattCharacteristic characteristic:characteristicList)
                {
                    Log.e(TAG, "getService:Charact "+ characteristic.getUuid() );
                    if(characteristic.getUuid().equals(uuidCharacteristic)) {
                        bluetoothGattCharacteristic = characteristic;
                        Log.e(TAG, "getService: " +"Characteristics" +bluetoothGattCharacteristic.getUuid() );
                    }
                }
            }
        }


    }

    public void disconnect()
    {
        bluetoothGatt.disconnect();


    }

    public void testFunction()
    {
        Log.e(TAG, "testFunction: " + "Executed" );
    }


}
