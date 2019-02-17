package com.qiezh.ucon;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.hs.gpxparser.GPXWriter;
import com.hs.gpxparser.modal.GPX;
import com.hs.gpxparser.modal.Track;
import com.hs.gpxparser.modal.Waypoint;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class MyLocalService extends Service {
    private static final String TAG = "MyLocalService";
    private LocationManager locationManager;
    private String locationProvider;
    private QiniuUploader uploader = new QiniuUploader();
    Handler mHandler = null;
    List<Waypoint> locations_list;
    GPX gpx = null;
    GPXWriter gpx_writer = null;
    int count;

    public MyLocalService() {
        locations_list =new ArrayList<>();
        count = 0;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate...");

        mHandler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                //do something
                add_location(get_location());
                if (count % 720 == 0) {
                    write_gpx();
                }
                mHandler.postDelayed(this, 60000);
            }
        };
        mHandler.postDelayed(r, 1000);
    }

    private void write_gpx() {
        gpx_writer = new GPXWriter();
        gpx = new GPX();
        gpx.addTrack(new Track());
        for (int i = 0; i < locations_list.size(); i++) {
            gpx.addWaypoint(locations_list.get(i));
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date(System.currentTimeMillis());
        String currentDate = simpleDateFormat.format(date);
        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();

        FileOutputStream out = null;
        String gpxPath = mFilePath + "/" + currentDate + ".gpx";
        try {
            out = new FileOutputStream(gpxPath);
            try {
                gpx_writer.writeGPX(gpx, out);
            } catch (ParserConfigurationException | TransformerException ex) {
                Log.e("write_gpx", ex.toString());
            }
            locations_list.clear();
        } catch (IOException  ex) {
            Log.e(TAG, "write_gpx exception -- " + ex.toString());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Log.e(TAG, "write_gpx exception -- " + ex.toString());
                }
            }
        }
        //MyUploadService.uploadBinary(this, gpxPath);
        uploader.up(gpxPath, currentDate + ".gpx");
    }
    private void add_location(Location loc) {
        if (locations_list != null && loc != null) {
            Waypoint wp = new Waypoint(loc.getLatitude(),loc.getLongitude());
            wp.setTime(new Date(System.currentTimeMillis()));
            locations_list.add(wp);
            count++;
        }
    }
    private Location get_location() {
        locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);

        Log.d(TAG, "get_location: " + getApplicationContext().LOCATION_SERVICE + " " + locationManager.toString());
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);//低精度，如果设置为高精度，依然获取不了location。
        criteria.setAltitudeRequired(false);//不要求海拔
        criteria.setBearingRequired(false);//不要求方位
        criteria.setCostAllowed(true);//允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗

        //从可用的位置提供器中，匹配以上标准的最佳提供器
        locationProvider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " 没有权限 ");
            return null;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        Log.d(TAG, "location is null? -- " + (location == null) + "..");
        if (location != null) {
            //Log.d(TAG, "onCreate: location");
            //不为空,显示地理位置经纬度
            showLocation(location);
        }
        return location;
    }

    private void showLocation(Location location) {
        Log.d(TAG, "定位成功------->" + "location------>经度为：" + location.getLatitude() + "\n纬度为" + location.getLongitude());
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
