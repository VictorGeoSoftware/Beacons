package com.mshop.movilidad.beacons.DataModels;

/**
 * Created by Victor on 11/7/15.
 */
public class BeaconDataModel {
    private String mac;
    private String data;
    private String range;

    public BeaconDataModel (String mac, String data, String range) {
        this.mac = mac;
        this.data = data;
        this.range = range;
    }


    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
