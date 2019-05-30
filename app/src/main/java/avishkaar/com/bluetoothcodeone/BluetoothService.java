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
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothService extends Service {
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;
    private static final String TAG = "BluetoothService";
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;
    BluetoothGattCharacteristic bluetoothGattCharacteristic;
    ArrayList<BluetoothGattService> bluetoothGattServices;
    public static UUID uuidService = UUID.fromString("00000021-0000-1000-8000-00805f9b34fb");
    public static UUID uuidCharacteristic = UUID.fromString("00000052-0000-1000-8000-00805f9b34fb");
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "avishkaar.com.bluetoothcodeone.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "avishkaar.com.bluetoothcodeone.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "avishkaar.com.bluetoothcodeone.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "avishkaar.com.bluetoothcodeone.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "avishkaar.com.bluetoothcodeone.EXTRA_DATA";



    public BluetoothService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

    }

    public  interface BluetoothInterface {
        void onConnectionChange();
        void onServiceDiscovered();
        void onCharacteristicWrite();
    }
    BluetoothInterface bluetoothInterface;

    void listerner(BluetoothInterface bluetoothInterface)
    {
        this.bluetoothInterface = bluetoothInterface;
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
            //bluetoothInterfaceReference.onServiceDetected();
            if(status==BluetoothGatt.GATT_SUCCESS)
            {
                broadCastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }else Log.e(TAG, "onServicesDiscovered: " + status );
//            bluetoothInterface.onServiceDiscovered();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //bluetoothInterfaceReference.onCharacteristicsRead();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //bluetoothInterfaceReference.onCharacteristicsWrite();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

        }
    };

    public void broadCastUpdate(String action){
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }






    public  void connect (String mBluetoothName,String mBluetoothAddress){
        Log.e(TAG, "connect : "+ "Device Received" + mBluetoothName);
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(mBluetoothAddress);
        bluetoothGatt = bluetoothDevice.connectGatt(this,true,bluetoothGattCallback);


    }

    public void sendData(String data)
    {
        Log.e(TAG, "sendData: " +data );
        if(!(bluetoothGattCharacteristic ==null)) {
            bluetoothGattCharacteristic.setValue(data);
            bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
        }

    }

    public void getService(){
        BluetoothGattCharacteristic blec = null;
        List<BluetoothGattService>serviceList = bluetoothGatt.getServices();

        for (BluetoothGattService s:serviceList)
        {
            Log.e(TAG, "getService: " + s.getUuid() );
        }
        for(BluetoothGattService bluetoothGattService: serviceList)
        {
            if(bluetoothGattService.getUuid().equals(uuidService))
            {
                List<BluetoothGattCharacteristic>characteristicList = bluetoothGattService.getCharacteristics();
                for(BluetoothGattCharacteristic characteristic:characteristicList)
                {
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









}
