package com.luyun.whereareyou.tracker_client_android;

import java.util.LinkedList;

import android.content.Context;
import android.location.Location;

import com.amap.mapapi.map.MapView;
import com.amap.mapapi.map.MyLocationOverlay;

public class MyLocationOverlayProxy extends MyLocationOverlay{

	 private Location mLocation;
     private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
	
	 public MyLocationOverlayProxy(Context arg0, MapView arg1) {
		super(arg0, arg1);
	 }
	
	@Override
	public boolean runOnFirstFix(final Runnable runnable) {
		if (mLocation != null) {
			new Thread(runnable).start();
			return true;
		} else {
			mRunOnFirstFix.addLast(runnable);
			return false;
		}
    }
	
	
	@Override
	public void onLocationChanged(Location location) {
        mLocation = location;
        for(final Runnable runnable : mRunOnFirstFix) {
			new Thread(runnable).start();
		}
		mRunOnFirstFix.clear();
		super.onLocationChanged(location);
	}
	
	
	

}
