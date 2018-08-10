package com.example.park.dronecontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.park.dronecontroller.bluetooth.BluetoothAcceptor;
import com.example.park.dronecontroller.bluetooth.BluetoothManager;
import com.example.park.dronecontroller.status.EventStatus;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    /* 블루투스 활성화 값 */
    private static final int REQUEST_ENABLE_BT = 200;
    /* 블루투스 활성화 성공 값 */
    private static final int RESULT_OK = -1;
    /* 블루투스 활성화 취소 값 */
    private static final int RESULT_CANCELED = 0;

    public static final int BLUETOOTH_HANDLE_STATUS = 1;
    private Button button;

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothListViewAdapter bluetoothListViewAdapter;

    private BluetoothAcceptor bluetoothAcceptor;

    private BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.helloBt);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothManager != null) {
                    bluetoothManager.write("HELLO");
                    return;
                }

                Toast.makeText(getApplicationContext(), "Non BluetoothManager", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        /* 블루투스 작동 가능 기기인지 확인 */
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.e(TAG, "해당기기는 블루투스 기능을 지원하지 않습니다.");
            Toast.makeText(getApplicationContext(), "해당기기는 블루투스 기능을 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        /* bluetooth list view Adapter */
        bluetoothListViewAdapter = new BluetoothListViewAdapter(this);

        /* 사용가능한 블루투스 검색 브로드케스트 등록 */
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);

        registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /* 사용가능한 블루투스 검색 브로드케스트 제외 */
        unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "requestCode : " + requestCode + ", resultCode : " + resultCode);

        if (requestCode == REQUEST_ENABLE_BT) {
            switch (resultCode) {
                case RESULT_OK:
                    Toast.makeText(getApplicationContext(), "블루투스를 활성화 하였습니다.", Toast.LENGTH_SHORT).show();
                    break;
                case RESULT_CANCELED:
                    Toast.makeText(getApplicationContext(), "블루투스 기능이 필요합니다.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (bluetoothAdapter.isEnabled()) {
            menu.getItem(0).setTitle(getString(R.string.string_disable_bluetooth));
            menu.getItem(1).setVisible(true);
        } else {
            menu.getItem(0).setTitle(getString(R.string.string_enable_bluetooth));
            menu.getItem(1).setVisible(false);
        }


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_enable_bluetooth:
                if (StringUtils.equals(item.getTitle(), getString(R.string.string_enable_bluetooth))) {
                    bluetoothAdapter.enable();
                } else {
                    bluetoothAdapter.disable();
                }

                return true;
            case R.id.menu_show_bluetooth_list:
                doDiscovery();
                /* 블루투스 리스트 다이어로그 */
                showBluetoothList();

                return true;
            case R.id.menu_run_bluetooth_server:
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);

                if (bluetoothAcceptor != null) {
                    bluetoothAcceptor.cancel();
                }

                bluetoothAcceptor = new BluetoothAcceptor(this);
                bluetoothAcceptor.start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showBluetoothList() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.string_find_bluetooth));
        alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                bluetoothAdapter.cancelDiscovery();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.string_cancel, null);

        View view = this.getLayoutInflater().inflate(R.layout.bluetooth_listview, null);
        ListView listView = view.findViewById(R.id.bluetooth_listview);
        listView.setAdapter(bluetoothListViewAdapter);
        alertDialogBuilder.setView(view);

        // create and show the alert dialog
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                bluetoothListViewAdapter.addBluetooth(device);
                bluetoothListViewAdapter.notifyDataSetChanged();
            }
        }
    };

    private void doDiscovery() {
        bluetoothListViewAdapter.clear();

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();
    }

    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            /* 블루투스 찾기 */
            bluetoothAdapter.startDiscovery();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {

        }
    };

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == EventStatus.SHOW_TOAST.getStatus()) {
                String message = (String) msg.obj;
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public Handler getHandler() {
        return handler;
    }
}
