package com.mshop.movilidad.beacons;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.mshop.movilidad.beacons.ArrayAdapters.BeaconsArrayAdapter;
import com.mshop.movilidad.beacons.DataModels.BeaconDataModel;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;


public class MainActivity extends Activity implements AbsListView.OnScrollListener, BeaconConsumer, View.OnClickListener{

    ListView lstBeacons;
    View lstBeaconsHeaderView;
    FrameLayout layoutButtonBack;


    BluetoothManager bluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BeaconManager beaconManager;


    ArrayList<BeaconDataModel> beaconsArrayList = new ArrayList<>();
    BeaconsArrayAdapter beaconsArrayAdapter;
    int REQUEST_ENABLE_BT = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            initializeBeaconClient();
        }


        BeaconDataModel emptyListPlaceholderItem = new BeaconDataModel(getString(R.string.detecting_beacons), "--", "--");
        beaconsArrayList.add(emptyListPlaceholderItem);
        beaconsArrayAdapter = new BeaconsArrayAdapter(this, beaconsArrayList);

        layoutButtonBack = (FrameLayout) findViewById(R.id.layout_button_backward);
        layoutButtonBack.setOnClickListener(this);

        lstBeacons = (ListView) findViewById(R.id.listView);
        lstBeaconsHeaderView = getLayoutInflater().inflate(R.layout.view_header_refresh, null);
        lstBeacons.addHeaderView(lstBeaconsHeaderView);
        lstBeacons.setAdapter(beaconsArrayAdapter);

        lstBeacons.smoothScrollToPosition(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            initializeBeaconClient();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {

    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {

            }

            @Override
            public void didExitRegion(Region region) {

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 1) {
                    Log.i("", "Beacon a: " + beacons.iterator().next().getDistance() + " m");

                    beaconsArrayList.clear();
                    beaconsArrayAdapter.clear();

                    while (beacons.iterator().hasNext()){
                        BeaconDataModel beacon = new BeaconDataModel(
                                beacons.iterator().next().getBluetoothAddress(),
                                Integer.toString(beacons.iterator().next().getTxPower()),
                                Double.toString(beacons.iterator().next().getDistance())
                        );

                        beaconsArrayList.add(beacon);
                    }

                    beaconsArrayAdapter = new BeaconsArrayAdapter(MainActivity.this, beaconsArrayList);
                    lstBeacons.setAdapter(beaconsArrayAdapter);
                }
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myRegion", null, null, null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangeRegion", null, null, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeBeaconClient () {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        /*
        Aqui hay que probar con otros beacons que tengan el BeaconLayout más estandarizado
         */
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @Override
    public void onClick(View view) {
        if (view == layoutButtonBack) {
            startActivity(new Intent(MainActivity.this, NativeBluetothLeActivity.class));
            finish();
            overridePendingTransition(R.anim.fragment_enter_from_left, R.anim.fragment_exit_to_right);
        }
    }
}
