package com.mshop.movilidad.beacons;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import com.mshop.movilidad.beacons.ArrayAdapters.BeaconsArrayAdapter;
import com.mshop.movilidad.beacons.DataModels.BeaconDataModel;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Victor on 11/7/15.
 */
public class NativeBluetothLeActivity extends Activity implements View.OnClickListener, View.OnTouchListener {
    ListView lstBeacons;
    FrameLayout lstBeaconsHeaderView;
    FrameLayout layoutButtonFordward;
    ScrollView  scrBeaconList;


    BluetoothManager bluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    ScheduledExecutorService scheduledBeaconUpdateExecutor;


    ArrayList<BeaconDataModel> beaconsArrayList = new ArrayList<>();
    BeaconsArrayAdapter beaconsArrayAdapter;
    BeaconDataModel emptyListPlaceholderItem;
    int REQUEST_ENABLE_BT = 1001;
    int firstVisibleListViewItem = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_beacon_library);

        lstBeacons = (ListView) findViewById(R.id.listView);
        layoutButtonFordward = (FrameLayout) findViewById(R.id.layout_button_fordward);
        scrBeaconList = (ScrollView) findViewById(R.id.scrollview_beacon_list);
        lstBeaconsHeaderView = (FrameLayout) findViewById(R.id.header_beacon_list);


        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }


        emptyListPlaceholderItem = new BeaconDataModel(getString(R.string.detecting_beacons), "--", "--");
        beaconsArrayList.add(emptyListPlaceholderItem);
        beaconsArrayAdapter = new BeaconsArrayAdapter(this, beaconsArrayList);

        layoutButtonFordward.setOnClickListener(this);
        scrBeaconList.setOnTouchListener(this);
        lstBeacons.setAdapter(beaconsArrayAdapter);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = lstBeacons.getLayoutParams();
                params.height = scrBeaconList.getHeight();
                lstBeacons.setLayoutParams(params);
                lstBeacons.requestLayout();
            }
        }, 250);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scrBeaconList.setScrollY(lstBeaconsHeaderView.getHeight());
                    }
                });
            }
        }, 500);



        scheduledBeaconUpdateExecutor = Executors.newScheduledThreadPool(1);
        scheduledBeaconUpdateExecutor.scheduleAtFixedRate(runnableUpdateBeaconList, 2, 2, TimeUnit.SECONDS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        scheduledBeaconUpdateExecutor.shutdown();
        scheduledBeaconUpdateExecutor = null;
    }

    @Override
    public void onClick(View view) {
        if (view == layoutButtonFordward) {
            startActivity(new Intent(NativeBluetothLeActivity.this, MainActivity.class));
            finish();
            overridePendingTransition(R.anim.fragment_enter_from_right, R.anim.fragment_exit_to_left);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view == scrBeaconList) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                Log.i("", "entra : " + lstBeaconsHeaderView.getHeight());

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        scrBeaconList.smoothScrollTo(0, lstBeaconsHeaderView.getHeight());
                    }
                });
                
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.i("", "entra 1: " + lstBeaconsHeaderView.getHeight());
//                        scrBeaconList.scrollTo(0, lstBeaconsHeaderView.getHeight());
//                    }
//                });
            }
        }

        return false;
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i("", "dispositivo: " + device.getAddress() + " " + rssi + " " + device.getType());
            BeaconDataModel beacon = new BeaconDataModel(device.getAddress(), Integer.toString(device.getType()), Integer.toString(rssi));
            checkIncomingBeacon(beacon);
        }
    };

    Runnable runnableUpdateBeaconList = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    beaconsArrayAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    public void checkIncomingBeacon (BeaconDataModel beacon) {
        boolean alreadyRegistered = false;

        if (beaconsArrayList.contains(emptyListPlaceholderItem)) {
            beaconsArrayList.remove(emptyListPlaceholderItem);
        }

        for (int i = 0; i < beaconsArrayList.size(); i++) {
            if (beaconsArrayList.get(i).getMac().contentEquals(beacon.getMac())) {
                beaconsArrayList.get(i).setData(beacon.getData());
                beaconsArrayList.get(i).setRange(beacon.getRange());
                alreadyRegistered = true;
                break;
            }
        }

        if (!alreadyRegistered) {
            beaconsArrayList.add(beacon);
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null){
            return;
        }

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;

        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
