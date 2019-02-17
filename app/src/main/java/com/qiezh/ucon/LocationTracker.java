package com.qiezh.ucon;

import android.content.Context;
import android.util.Log;
import android.os.Build;

import com.amap.api.track.AMapTrackClient;
import com.amap.api.track.ErrorCode;
import com.amap.api.track.OnTrackLifecycleListener;
import com.amap.api.track.TrackParam;
import com.amap.api.track.query.entity.LocationMode;
import com.amap.api.track.query.model.AddTerminalRequest;
import com.amap.api.track.query.model.AddTerminalResponse;
import com.amap.api.track.query.model.AddTrackRequest;
import com.amap.api.track.query.model.AddTrackResponse;
import com.amap.api.track.query.model.OnTrackListener;
import com.amap.api.track.query.model.ParamErrorResponse;
import com.amap.api.track.query.model.QueryTerminalRequest;
import com.amap.api.track.query.model.QueryTerminalResponse;
import com.amap.api.track.query.model.QueryTrackResponse;
import com.qiezh.ucon.util.Constants;
import com.qiezh.ucon.util.SimpleOnTrackLifecycleListener;
import com.qiezh.ucon.util.SimpleOnTrackListener;

public class LocationTracker {
    AMapTrackClient aMapTrackClient;
    OnTrackLifecycleListener onTrackLifecycleListener;
    Context cxt;
    private boolean uploadToTrack = false;
    private long trackId;
    private long terminalId;
    private boolean isServiceRunning;
    private boolean isGatherRunning;
    private String TAG = "LocationTracker";

    public LocationTracker(Context context) {
        cxt = context;
        aMapTrackClient = new AMapTrackClient(cxt);
        aMapTrackClient.setInterval(60, 1800);
        aMapTrackClient.setCacheSize(20);
        aMapTrackClient.setLocationMode(LocationMode.BATTERY_SAVING);

        onTrackLifecycleListener = new SimpleOnTrackLifecycleListener() {
            @Override
            public void onBindServiceCallback(int status, String msg) {
                Log.w(TAG, "onBindServiceCallback, status: " + status + ", msg: " + msg);
            }

            @Override
            public void onStartTrackCallback(int status, String msg) {
                if (status == ErrorCode.TrackListen.START_TRACK_SUCEE || status == ErrorCode.TrackListen.START_TRACK_SUCEE_NO_NETWORK) {
                    // 成功启动
                    Log.d(TAG, "Start Track Service Success.");
                    isServiceRunning = true;
                } else if (status == ErrorCode.TrackListen.START_TRACK_ALREADY_STARTED) {
                    // 已经启动
                    Log.d(TAG, "Track Service Already started.");
                    isServiceRunning = true;
                } else {
                    Log.w(TAG, "error onStartTrackCallback, status: " + status + ", msg: " + msg);
                }
            }

            @Override
            public void onStopTrackCallback(int status, String msg) {
                if (status == ErrorCode.TrackListen.STOP_TRACK_SUCCE) {
                    // 成功停止
                    Log.d(TAG, "Stop Track Service Success.");
                    isServiceRunning = false;
                    isGatherRunning = false;
                } else {
                    Log.w(TAG, "error onStopTrackCallback, status: " + status + ", msg: " + msg);
                }
            }

            @Override
            public void onStartGatherCallback(int status, String msg) {
                if (status == ErrorCode.TrackListen.START_GATHER_SUCEE) {
                    Log.d(TAG, "Start Gather Service Success.");
                    isGatherRunning = true;
                } else if (status == ErrorCode.TrackListen.START_GATHER_ALREADY_STARTED) {
                    Log.d(TAG, "Gather Service Already Started.");
                    isGatherRunning = true;
                } else {
                    Log.w(TAG, "error onStartGatherCallback, status: " + status + ", msg: " + msg);
                }
            }

            @Override
            public void onStopGatherCallback(int status, String msg) {
                if (status == ErrorCode.TrackListen.STOP_GATHER_SUCCE) {
                    Log.d(TAG, "Stop Gather Service Success.");
                    isGatherRunning = false;
                } else {
                    Log.w(TAG, "error onStopGatherCallback, status: " + status + ", msg: " + msg);
                }
            }
        };
    }

    public void startTrack() {
        // 先根据Terminal名称查询Terminal ID，如果Terminal还不存在，就尝试创建，拿到Terminal ID后，
        // 用Terminal ID开启轨迹服务
        aMapTrackClient.queryTerminal(new QueryTerminalRequest(Constants.SERVICE_ID,
                Constants.TERMINAL_NAME), new SimpleOnTrackListener() {
            @Override
            public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse) {
                if (queryTerminalResponse.isSuccess()) {
                    if (queryTerminalResponse.isTerminalExist()) {
                        // 当前终端已经创建过，直接使用查询到的terminal id
                        terminalId = queryTerminalResponse.getTid();
                        if (uploadToTrack) {
                            aMapTrackClient.addTrack(new AddTrackRequest(Constants.SERVICE_ID, terminalId), new SimpleOnTrackListener() {
                                @Override
                                public void onAddTrackCallback(AddTrackResponse addTrackResponse) {
                                    if (addTrackResponse.isSuccess()) {
                                        // trackId需要在启动服务后设置才能生效，因此这里不设置，而是在startGather之前设置了track id
                                        trackId = addTrackResponse.getTrid();
                                        TrackParam trackParam = new TrackParam(Constants.SERVICE_ID, terminalId);
                                        trackParam.setTrackId(trackId);
                                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            //trackParam.setNotification(createNotification());
                                        }
                                        aMapTrackClient.startTrack(trackParam, onTrackLifecycleListener);
                                    } else {
                                        Log.d(TAG, "网络请求失败，" + addTrackResponse.getErrorMsg());
                                    }
                                }
                            });
                        } else {
                            // 不指定track id，上报的轨迹点是该终端的散点轨迹
                            TrackParam trackParam = new TrackParam(Constants.SERVICE_ID, terminalId);
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                //trackParam.setNotification(createNotification());
                            }
                            aMapTrackClient.startTrack(trackParam, onTrackLifecycleListener);
                        }
                    } else {
                        // 当前终端是新终端，还未创建过，创建该终端并使用新生成的terminal id
                        aMapTrackClient.addTerminal(new AddTerminalRequest(Constants.TERMINAL_NAME,
                                Constants.SERVICE_ID), new SimpleOnTrackListener() {
                            @Override
                            public void onCreateTerminalCallback(AddTerminalResponse addTerminalResponse) {
                                if (addTerminalResponse.isSuccess()) {
                                    terminalId = addTerminalResponse.getTid();
                                    TrackParam trackParam = new TrackParam(Constants.SERVICE_ID, terminalId);
                                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        //trackParam.setNotification(createNotification());
                                    }
                                    aMapTrackClient.startTrack(trackParam, onTrackLifecycleListener);
                                } else {
                                    Log.d(TAG, "网络请求失败，" + addTerminalResponse.getErrorMsg());
                                }
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "网络请求失败，" + queryTerminalResponse.getErrorMsg());
                }
            }
        });
    }

    public void stopTrack() {
        if (isServiceRunning) {
            aMapTrackClient.stopTrack(new TrackParam(Constants.SERVICE_ID, terminalId), new SimpleOnTrackLifecycleListener());
        }
    }


}
