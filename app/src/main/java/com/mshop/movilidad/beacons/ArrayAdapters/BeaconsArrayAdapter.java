package com.mshop.movilidad.beacons.ArrayAdapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mshop.movilidad.beacons.DataModels.BeaconDataModel;
import com.mshop.movilidad.beacons.NativeBluetothLeActivity;
import com.mshop.movilidad.beacons.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Victor on 11/7/15.
 */
public class BeaconsArrayAdapter extends ArrayAdapter<BeaconDataModel> {
    private ArrayList<BeaconDataModel> beaconsArrayList;
    private Activity aContext;


    public BeaconsArrayAdapter(Activity aContext, ArrayList<BeaconDataModel> beaconsArrayList) {
        super(aContext, R.layout.row_beacon_item, beaconsArrayList);

        this.aContext = aContext;
        this.beaconsArrayList = beaconsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = aContext.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_beacon_item, null);

            holder = new ViewHolder();
            holder.txtMac = (TextView) convertView.findViewById(R.id.textView);
            holder.txtData = (TextView) convertView.findViewById(R.id.textView2);
            holder.txtRange = (TextView) convertView.findViewById(R.id.textView3);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtMac.setText(beaconsArrayList.get(position).getMac());

        if (aContext instanceof NativeBluetothLeActivity) {
            holder.txtData.setText("UUID: " + beaconsArrayList.get(position).getData());
            holder.txtRange.setText("Rssi: " + beaconsArrayList.get(position).getRange());
        } else {
            DecimalFormat format = new DecimalFormat("0.00");
            double distance = 0;

            try {
                distance = Double.parseDouble(beaconsArrayList.get(position).getRange());
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.txtData.setText("Rssi: " + beaconsArrayList.get(position).getData());
            holder.txtRange.setText("Distancia: " + format.format(distance) + " m");
        }



        return convertView;
    }

    public class ViewHolder{
        TextView txtMac;
        TextView txtData;
        TextView txtRange;
    }
}
