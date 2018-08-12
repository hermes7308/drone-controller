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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.park.dronecontroller.bluetooth.BluetoothAcceptor;
import com.example.park.dronecontroller.bluetooth.BluetoothManager;
import com.example.park.dronecontroller.handler.EventHandler;
import com.example.park.dronecontroller.model.JoystickItem;
import com.example.park.dronecontroller.util.GsonFactory;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.zerokol.views.JoystickView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    public static final String SEND_PREFIX = "send : ";
    public static final String RECEIVE_PREFIX = "receive : ";
    /* 블루투스 활성화 값 */
    private static final int REQUEST_ENABLE_BT = 1;
    /* 블루투스 서버 활성화 값 */
    private static final int REQUEST_ENABLE_BT_SERVER = 2;
    /* 성공 값 */
    private static final int RESULT_OK = -1;
    /* 취소 값 */
    private static final int RESULT_CANCELED = 0;
    /* 블루투스 검색 가능 기간 */
    public static final int BLUETOOTH_DISCOVERY_TIME = 300;

    private Handler handler;

    private MenuItem bluetoothStatusMenuItem;
    private MenuItem bluetoothSearchMenuItem;
    private MenuItem bluetoothRunServerMenuItem;

    private ListView logListView;
    private ScrollView scrollView;
    private AlertDialog searchBluetoothDialog;
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

        /* 로그 제거 버튼 */
        ImageButton removeButton = findViewById(R.id.remove_button);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logArrayAdapter.clear();
            }
        });

        /* 조이스틱 설정 */
        JoystickView leftJoyStick = findViewById(R.id.left_joystick);
        leftJoyStick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                send("LEFT", angle, power, direction);
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

        JoystickView rightJoyStick = findViewById(R.id.right_joystick);
        rightJoyStick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                send("RIGHT", angle, power, direction);
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

    }

    @Override
    protected void onStart() {
        super.onStart();

        /* 블루투스 작동 가능 기기인지 확인 */
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            printMessage("해당기기는 블루투스 기능을 지원하지 않습니다.");
            Toast.makeText(getApplicationContext(), "해당기기는 블루투스 기능을 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        /* bluetooth_enable list view Adapter */
        bluetoothListViewAdapter = new BluetoothListViewAdapter(this);

        /* 사용가능한 블루투스 검색 브로드케스트 등록 */
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

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

        if (requestCode == REQUEST_ENABLE_BT) {
            switch (resultCode) {
                case RESULT_OK:
                    printMessage("블루투스를 활성화 하였습니다.");
                    break;
                case RESULT_CANCELED:
                    printMessage("블루투스 기능이 필요합니다.");
                    break;
            }
            return;
        }

        if (requestCode == REQUEST_ENABLE_BT_SERVER) {
            switch (resultCode) {
                case BLUETOOTH_DISCOVERY_TIME:
                    runServer();
                    break;
                case RESULT_CANCELED:
                    printMessage("블루투스 서버 실행을 취소 하였습니다.");
                    break;
            }
            return;
        }
    }


    private void send(String from, int angle, int power, int direction) {
        JoystickItem item = new JoystickItem(from, angle, power, direction);
        String message = GsonFactory.get().toJson(item);

        send(message);
    }

    public void send(String message) {
        BluetoothManager bluetoothManager = getBluetoothManager();
        if (bluetoothManager == null || !bluetoothManager.isConnected()) {
            printMessage("블루투스 연결되지 않은 상태입니다.");
            return;
        }

        printMessage(SEND_PREFIX + message);
        bluetoothManager.write(message);
    }

    public void receive(String message) {
        printMessage(RECEIVE_PREFIX + message);
    }

    private void runServer() {
        if (bluetoothAcceptor != null) {
            bluetoothAcceptor.cancel();
        }

        bluetoothAcceptor = new BluetoothAcceptor(handler);
        bluetoothAcceptor.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        /* 메뉴 */
        bluetoothStatusMenuItem = menu.getItem(0);
        bluetoothSearchMenuItem = menu.getItem(1);
        bluetoothRunServerMenuItem = menu.getItem(2);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_bluetooth_status:
                if (bluetoothAdapter.isEnabled()) {
                    /* 블루투스 비활성화 */
                    bluetoothAdapter.disable();
                } else {
                    /* 블루투스 활성화 */
                    bluetoothAdapter.enable();
                }

                return true;
            case R.id.menu_search_bluetooth_list:
                bluetoothListViewAdapter.clear();

                TedPermission.with(this)
                        .setPermissionListener(permissionListener)
                        .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                        .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                        .check();

                return true;
            case R.id.menu_run_bluetooth_server:
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERY_TIME);
                startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT_SERVER);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (StringUtils.equals(BluetoothDevice.ACTION_FOUND, action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                bluetoothListViewAdapter.addBluetooth(device);
                bluetoothListViewAdapter.notifyDataSetChanged();

                return;
            }

            if (StringUtils.equals(BluetoothAdapter.ACTION_STATE_CHANGED, action)) {
                int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                switch (status) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    case BluetoothAdapter.STATE_OFF:
                        bluetoothStatusMenuItem.setIcon(R.mipmap.bluetooth_disable);
                        bluetoothSearchMenuItem.setVisible(false);
                        bluetoothRunServerMenuItem.setVisible(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                    case BluetoothAdapter.STATE_ON:
                        bluetoothStatusMenuItem.setIcon(R.mipmap.bluetooth_enable);
                        bluetoothSearchMenuItem.setVisible(true);
                        bluetoothRunServerMenuItem.setVisible(true);
                        break;
                }

            }
        }
    };

    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            /* 블루투스 찾기 */
            bluetoothAdapter.startDiscovery();

            /* 블루투스 리스트 다이어로그 */
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle(getString(R.string.string_find_bluetooth));
            alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    bluetoothAdapter.cancelDiscovery();
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.string_cancel, null);

            View view = MainActivity.this.getLayoutInflater().inflate(R.layout.bluetooth_listview, null);
            ListView listView = view.findViewById(R.id.bluetooth_listview);
            listView.setAdapter(bluetoothListViewAdapter);
            alertDialogBuilder.setView(view);

            // create and show the alert dialog
            searchBluetoothDialog = alertDialogBuilder.create();
            searchBluetoothDialog.show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            bluetoothAdapter.cancelDiscovery();
        }
    };

    public void printMessage(String message) {
        logArrayAdapter.add(message);
        logListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                logListView.setSelection(logArrayAdapter.getCount() - 1);
            }
        });
    }

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public Handler getHandler() {
        return handler;
    }

    public void cancelSearchBluetoothDialog() {
        searchBluetoothDialog.cancel();
    }
}
