package com.luyun.whereareyou.tracker_client;

import java.util.List;
import java.util.Map;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKLocationManager;  

import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

public class TrackerApp extends Application {
	final static String TAG = "TrackerApp";
	static TrackerApp instance;

	// 百度MapAPI的管理类
	BMapManager mBMapMan = null;
	public Map<String, GeoPoint > mTrackees; //phone number,  trackee_x, trackee_y

	// 授权Key
	// TODO: 请输入您的Key,
	// 申请地址：http://dev.baidu.com/wiki/static/imap/key/
	String mStrKey = "513CBE299AB953DDFAEBC4A608F1F6557C30D685";
	boolean m_bKeyRight = true; // 授权Key正确，验证通过

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	static class MyGeneralListener implements MKGeneralListener {
		public void onGetNetworkState(int iError) {
			Log.d("MyGeneralListener", "onGetNetworkState error is " + iError);
			Toast.makeText(TrackerApp.instance.getApplicationContext(),
					"您的网络出错啦！", Toast.LENGTH_LONG).show();
		}

		public void onGetPermissionState(int iError) {
			Log.d("MyGeneralListener", "onGetPermissionState error is "
					+ iError);
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				// 授权Key错误：
				Toast.makeText(TrackerApp.instance.getApplicationContext(),
						"请在Easyway95App.java文件输入正确的授权Key！", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("TrackerApp", "onCreate");
		instance = this;

		mBMapMan = new BMapManager(this);
		mBMapMan.init(this.mStrKey, new MyGeneralListener());
		mBMapMan.getLocationManager().setNotifyInternal(5, 2);
		mBMapMan.start();
		
		mTrackees = new java.util.HashMap<String, GeoPoint>();
	}

	@Override
	// 建议在您app的退出之前调用mapadpi的destroy()函数，避免重复初始化带来的时间消耗
	public void onTerminate() {
		// TODO Auto-generated method stub
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}
		Log.d(TAG, "on terminate");
		super.onTerminate();
	}

	//是否在主界面
	public boolean isHome(){  
	    ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);  
	    List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
	    String homePkgName = "com.luyun.whereareyou.tracker_client";
	    return homePkgName.contains(rti.get(0).topActivity.getPackageName());  
	}  
	
	public String getCurrentActivityName(){
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        RunningTaskInfo info = mActivityManager.getRunningTasks(1).get(0);
//        String shortClassName = info.topActivity.getShortClassName();    //类名
        String className = info.topActivity.getClassName();              //完整类名
        return className;
	}
	
    // 通过address手机号关联Contacts联系人的显示名字  
    public String getPeopleNameFromPerson(String address){
    	String strPerson= address;
        if( address == null){  
            return strPerson;  
        }  
          
        String[] projection = new String[] {Phone.DISPLAY_NAME, Phone.NUMBER};  
          
        Uri uri_Person = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, address);  // address 手机号过滤  
        Cursor cursor = getContentResolver().query(uri_Person, projection, null, null, null);  
          
        if(cursor.moveToFirst()){  
            int index_PeopleName = cursor.getColumnIndex(Phone.DISPLAY_NAME);  
            String strPeopleName = cursor.getString(index_PeopleName);  
            strPerson = strPeopleName;  
        }  
        cursor.close();  
        return strPerson;  
    }
}
