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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.park.dronecontroller.bluetooth.BluetoothAcceptor;
import com.example.park.dronecontroller.bluetooth.BluetoothManager;
import com.example.park.dronecontroller.handler.EventHandler;
import com.example.park.dronecontroller.handler.event.MainActivityEvent;
import com.example.park.dronecontroller.model.JoystickItem;
import com.example.park.dronecontroller.util.GsonFactory;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.zerokol.views.JoystickView;

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

    private Handler handler;

    private JoystickView leftJoyStick;
    private JoystickView rightJoyStick;
    private ListView logListView;
    private ScrollView scrollView;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothListViewAdapter bluetoothListViewAdapter;
    private BluetoothAcceptor bluetoothAcceptor;
    private BluetoothManager bluetoothManager;

    private ArrayAdapter<String> logArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* 핸들러 생성 */
        handler = new EventHandler(this, getApplicationContext());

        /* 로그 스크롤 뷰 설정 */
        scrollView = findViewById(R.id.log_scrollview);

        logArrayAdapter = new ArrayAdapter<>(this, R.layout.log_listview_row);
        logListView = findViewById(R.id.log_listview);
        logListView.setAdapter(logArrayAdapter);
        logListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        /* 조이스틱 설정 */
        leftJoyStick = findViewById(R.id.left_joystick);
        leftJoyStick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                send("LEFT", angle, power, direction);
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

        rightJoyStick = findViewById(R.id.right_joystick);
        rightJoyStick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                send("RIGHT", angle, power, direction);
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

    }

    public void send(String from, int angle, int power, int direction) {
        JoystickItem item = new JoystickItem(from, angle, power, direction);
        String message = GsonFactory.get().toJson(item);

        handler.obtainMessage(MainActivityEvent.SEND.getStatus(), message)
                .sendToTarget();
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

                startServer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startServer() {
        if (bluetoothAcceptor != null) {
            bluetoothAcceptor.cancel();
        }

        bluetoothAcceptor = new BluetoothAcceptor(this);
        bluetoothAcceptor.start();
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

    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            /* 블루투스 찾기 */
            bluetoothAdapter.startDiscovery();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            bluetoothAdapter.cancelDiscovery();
        }
    };

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public Handler getHandler() {
        return handler;
    }

    public void addLog(String message) {
        logArrayAdapter.add(message);
        logListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                logListView.setSelection(logArrayAdapter.getCount() - 1);
            }
        });
    }
}
