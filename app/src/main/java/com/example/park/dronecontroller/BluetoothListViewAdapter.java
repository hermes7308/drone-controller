package com.example.park.dronecontroller;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.park.dronecontroller.bluetooth.BluetoothConnector;

import java.util.ArrayList;
import java.util.List;

public class BluetoothListViewAdapter extends BaseAdapter {
    private List<BluetoothDevice> bluetoothList = new ArrayList<>();
    private MainActivity activity;

    private BluetoothConnector bluetoothConnector;

    public BluetoothListViewAdapter(MainActivity activity) {
        this.activity = (MainActivity) activity;
    }

    public void addBluetooth(BluetoothDevice bluetoothDevice) {
        bluetoothList.add(bluetoothDevice);
    }

    public void clear() {
        bluetoothList.clear();
    }

    @Override
    public int getCount() {
        return bluetoothList.size();
    }

    @Override
    public Object getItem(int position) {
        return bluetoothList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final int pos = position;
        TextView deviceName;
        TextView deviceAddress;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.bluetooth_listview_row, parent, false);
            deviceName = view.findViewById(R.id.bluetooth_device_name);
            deviceAddress = view.findViewById(R.id.bluetooth_device_address);

            BluetoothListViewHolder holder = new BluetoothListViewHolder(deviceName, deviceAddress);

            view.setTag(holder);
            deviceName.setVisibility(View.VISIBLE);
            deviceAddress.setVisibility(View.VISIBLE);
        } else {
            BluetoothListViewHolder holder = (BluetoothListViewHolder) view.getTag();
            deviceName = holder.getBluetoothDeviceName();
            deviceAddress = holder.getBluetoothDeviceAddress();
        }

        BluetoothDevice device = bluetoothList.get(pos);
        deviceName.setText(device.getName());
        deviceAddress.setText(device.getAddress());

        /* 리스트 아이템을 터치 했을 때 이벤트 발생 */
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothDevice device = bluetoothList.get(pos);
                Toast.makeText(activity.getApplicationContext(), device.getName() + "\n" + device.getAddress(), Toast.LENGTH_SHORT).show();

                connect(device);
            }
        });

        return view;
    }

    private void connect(BluetoothDevice device) {
        if (bluetoothConnector != null) {
            bluetoothConnector.cancel();
        }

        bluetoothConnector = new BluetoothConnector(activity.getHandler(), device);
        bluetoothConnector.start();
    }

    private class BluetoothListViewHolder {
        private TextView bluetoothDeviceName;
        private TextView bluetoothDeviceAddress;

        BluetoothListViewHolder(TextView bluetoothDeviceName, TextView bluetoothDeviceAddress) {
            this.bluetoothDeviceName = bluetoothDeviceName;
            this.bluetoothDeviceAddress = bluetoothDeviceAddress;
        }

        void setBluetoothDeviceName(TextView bluetoothDeviceName) {
            this.bluetoothDeviceName = bluetoothDeviceName;
        }

        void setBluetoothDeviceAddress(TextView bluetoothDeviceAddress) {
            this.bluetoothDeviceAddress = bluetoothDeviceAddress;
        }

        TextView getBluetoothDeviceName() {
            return this.bluetoothDeviceName;
        }

        TextView getBluetoothDeviceAddress() {
            return bluetoothDeviceAddress;
        }
    }
}
