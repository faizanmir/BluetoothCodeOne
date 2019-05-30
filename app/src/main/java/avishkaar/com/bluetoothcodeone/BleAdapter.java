package avishkaar.com.bluetoothcodeone;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class BleAdapter extends RecyclerView.Adapter<BleAdapter.viewHolder> {
ArrayList<BluetoothModelClass>devices;
Context mContext;


    public BleAdapter(ArrayList<BluetoothModelClass> devices, Context mContext) {
        this.devices = devices;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new viewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ble,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder viewHolder, final int i) {
        viewHolder.name.setText(devices.get(i).deviceName);
        viewHolder.address.setText(devices.get(i).deviceAddress);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,MainActivity.class);
                intent.putExtra("DEVICE-NAME",devices.get(i).deviceName);
                intent.putExtra("DEVICE-ADDRESS",devices.get(i).deviceAddress);
                mContext.startActivity(intent);

            }
        });



    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView address;
        public viewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.rvname);
                address = itemView.findViewById(R.id.rvad);
            }
        }
}
