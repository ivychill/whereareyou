package com.luyun.whereareyou.tracker_client;

import java.util.ArrayList;
import java.util.Iterator;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.google.protobuf.InvalidProtocolBufferException;
import com.luyun.whereareyou.shared.TrackEventProtos.TrackEvent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

public class MainActivity extends MapActivity implements LocationListener {
	TrackerApp app;
	private final static String TAG = "MainActivity";
	private ZMQService mzService;
	private boolean mIsZmqBound;

	private ConnectivityChangeReceiver mConnectivityChangeReceiver;
	private MapView mMapView = null;
	private MyLocationOverlay mLocationOverlay = null; // 定位图层
	private MapController mMapController = null;
	private String mSMS = "想知道你在哪，同意请点击";
	private String mDeviceID;
	private PopupWindow menuWindow;
	private boolean menu_display;
	private LayoutInflater inflater;
	private LinearLayout mClose;
	private LinearLayout mCloseBtn;
	private ArrayList<OverlayT> mOveritems = null;

	static MainActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.activity_main);

		app = (TrackerApp) this.getApplication();
		if (app.mBMapMan == null) {
			app.mBMapMan = new BMapManager(getApplication());
			app.mBMapMan.init(app.mStrKey, new TrackerApp.MyGeneralListener());
		}

		app.mBMapMan.start();
		super.initMapActivity(app.mBMapMan);
		mOveritems = new ArrayList<OverlayT>();
		createMap();

		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		mDeviceID = tm.getDeviceId();
		mConnectivityChangeReceiver = new ConnectivityChangeReceiver();
		registerReceiver(mConnectivityChangeReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));

		app.mBMapMan.getLocationManager().requestLocationUpdates(this);
	}

	private void createMap() {
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mMapController = mMapView.getController(); // 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		if (mMapController == null) {
			Log.d(TAG, "can't get controller");
			return;
		}
		// 添加定位图层
		mLocationOverlay = new MyLocationOverlay(this, mMapView);
		mLocationOverlay.enableMyLocation();
		mMapView.getOverlays().add(mLocationOverlay);
	}

	public void onLocationChanged(Location loc) {
		Log.d(TAG, "onLocationChanged " + loc.toString());
		if (app.mTrackees.size() == 0) {
			// app.mBMapMan.getLocationManager().removeUpdates(this);
			app.mBMapMan.getLocationManager().setNotifyInternal(10, 5); // max
																			// 60seconds
			mMapController.setZoom(15); // 设置地图zoom级别
		}

		if (loc != null) {
			GeoPoint mypoint = new GeoPoint((int) (loc.getLatitude() * 1E6),
					(int) (loc.getLongitude() * 1E6));

			app.mTrackees.put(mDeviceID, mypoint);
			updateViewMap();
			mMapController.animateTo(mypoint);
		}
	}

	public boolean isConnectedToInternet() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		// mobile 3G Data Network
		State mobile = mConnectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).getState();
		// wifi
		State wifi = mConnectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState();

		// 如果3G网络和wifi网络都未连接，且不是处于正在连接状态 则进入Network Setting界面 由用户配置网络连接
		if (mobile == State.CONNECTED || mobile == State.CONNECTING)
			return true;
		if (wifi == State.CONNECTED || wifi == State.CONNECTING)
			return true;
		return false;
	}

	public class ConnectivityChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d(TAG, "ConnectivityChangeReceiver");
			if (isConnectedToInternet()) {
				if (mzService == null) { // ZMQService not ready yet
					return;
				}
				Log.d(TAG, "reconnect");
				ZMQreconnect();
			}
		}
	}

	private void postMsg(String recipient, String msg) {
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),
				0, new Intent(getApplicationContext(), MainActivity.class), 0);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(recipient, null, msg, pi, null);
		Toast.makeText(getApplicationContext(), "发送短消息成功!", Toast.LENGTH_SHORT)
				.show();
		Log.d(TAG, msg);
	}

	private void procLocationRpt(TrackEvent event) {

		// update or save location
		GeoPoint pt = new GeoPoint(
				(int) (Float.parseFloat(event.getTrackeeY()) * 1E6),
				(int) (Float.parseFloat(event.getTrackeeX()) * 1E6));

		app.mTrackees.put(event.getTrackee(), pt);

		// 如果在地图activity, handle message , update location
		Log.d(TAG, "getCurrentActivityName " + app.getCurrentActivityName());

		// map view get focus
		if (app.getCurrentActivityName() == Constants.MAIN_ACTIVTY) {
			Log.d(TAG, "update TrackerMap");
			updateViewMap();
			return;
		}

		Intent intent = new Intent(MainActivity.instance, MainActivity.class);

		// 如果在后台， notify
		NotificationManager mNotificationManager = (NotificationManager) MainActivity.instance
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent pendIntent = PendingIntent.getActivity(
				MainActivity.instance, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		String title = "定位成功";
		String address = event.getTrackeeDesc();
		String ticker = app.getPeopleNameFromPerson(event.getTrackee()) + "在"
				+ address;
		String content = app.getPeopleNameFromPerson(event.getTrackee()) + "在"
				+ address + ",点击查看地图";

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				MainActivity.instance).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title).setTicker(ticker).setAutoCancel(true);

		Notification notf = mBuilder.getNotification();
		notf.defaults = Notification.DEFAULT_SOUND;
		notf.setLatestEventInfo(MainActivity.instance, "你在哪儿", content,
				pendIntent);

		// 用mNotificationManager的notify方法通知用户生成标题栏消息通知
		mNotificationManager.notify(1, notf);
		Log.d(TAG, "notify to TrackerActivity");
	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			TrackEvent trackEventOnWire;

			try {
				trackEventOnWire = TrackEvent.parseFrom(msg.getData()
						.getByteArray(Constants.DOWN_SRV_DATA));
				switch (trackEventOnWire.getType()) {
				case START_TRACKING_REP:
					String strHyperLink = "" + mSMS + "http://"
							+ Constants.TRACKER_SERVER_HOST + ":"
							+ Constants.TRACKEE_SERVER_PORT + "/t/"
							+ trackEventOnWire.getId();

					// postMsg("18688776399", strHyperLink);
					postMsg(trackEventOnWire.getTrackee(), strHyperLink);
					break;
				case FWD_LOC_REQ:
					procLocationRpt(trackEventOnWire);
					break;
				default:
					break;
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
	};

	private void addMarker() {
		Log.d(TAG, "addMarker");

		String tips = "定位结果";
		GeoPoint point = null;
		GeoPoint anchor = null;

		if (app.mTrackees.size() > 0) {
			Iterator<String> it = app.mTrackees.keySet().iterator();
			while (it.hasNext()) {
				String phone = it.next();
				point = app.mTrackees.get(phone);
				if (point == null) {
					continue;
				}

				Drawable marker;
				OverlayT item;
				if (phone == mDeviceID) {
					// my location
					// marker =
					// getResources().getDrawable(R.drawable.bubble_32);
					// item = new OverlayT(marker, point, TrackerActivity.this,
					// "我在这里", "我的位置");
				} else {
					// marker = getResources()
					// .getDrawable(R.drawable.bubble_other);
					//
					// item = new OverlayT(marker, point, TrackerActivity.this,
					// app.getPeopleNameFromPerson(phone), tips);

					anchor = point;
				}

				// if (mMapView.getOverlays() != null) {
				// mMapView.getOverlays().add(item);
				// }
				//
				// mOveritems.add(item);
			}
			// move to the latest point
			if (anchor != null) {
				mMapController.setCenter(anchor);
			} else {
				mMapController.animateTo(point);
			}

			mMapController.setZoom(15); // 设置地图zoom级别
		}
	}

	private void delAllMarker() {
		if (mMapView.getOverlays() != null) {
			mMapView.getOverlays().removeAll(mOveritems);
		}

		mOveritems.clear();
	}

	private void updateViewMap() {
		delAllMarker();
		addMarker();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 获取
																				// back键

			if (menu_display) { // 如果 Menu已经打开 ，先关闭Menu
				menuWindow.dismiss();
				menu_display = false;
			} else {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Exit.class);
				startActivity(intent);
			}
		}

		else if (keyCode == KeyEvent.KEYCODE_MENU) { // 获取 Menu键
			if (!menu_display) {
				// 获取LayoutInflater实例
				inflater = (LayoutInflater) this
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				// 这里的main布局是在inflate中加入的哦，以前都是直接this.setContentView()的吧？呵呵
				// 该方法返回的是一个View的对象，是布局中的根
				View layout = inflater.inflate(R.layout.exit_menu, null);

				// 下面我们要考虑了，我怎样将我的layout加入到PopupWindow中呢？？？很简单
				menuWindow = new PopupWindow(layout, LayoutParams.FILL_PARENT,
						LayoutParams.WRAP_CONTENT); // 后两个参数是width和height
				// menuWindow.showAsDropDown(layout); //设置弹出效果
				// menuWindow.showAsDropDown(null, 0, layout.getHeight());
				menuWindow.showAtLocation(this.findViewById(R.id.main),
						Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
				// 如何获取我们main中的控件呢？也很简单
				mClose = (LinearLayout) layout.findViewById(R.id.menu_close);
				mCloseBtn = (LinearLayout) layout
						.findViewById(R.id.menu_close_btn);

				// 下面对每一个Layout进行单击事件的注册吧。。。
				// 比如单击某个MenuItem的时候，他的背景色改变
				// 事先准备好一些背景图片或者颜色
				mCloseBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// Toast.makeText(Main.this, "退出",
						// Toast.LENGTH_LONG).show();
						Intent intent = new Intent();
						intent.setClass(MainActivity.this, Exit.class);
						startActivity(intent);
						menuWindow.dismiss(); // 响应点击事件之后关闭Menu
					}
				});
				menu_display = true;
			} else {
				// 如果当前已经为显示状态，则隐藏起来
				menuWindow.dismiss();
				menu_display = false;
			}

			return false;
		}
		return false;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private ServiceConnection mConnectionZMQ = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mzService = ((ZMQService.LocalBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			mzService = null;
		}
	};

	void bindZMQService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		Log.d(TAG, "in bindZMQService");
		bindService(new Intent(MainActivity.this, ZMQService.class),
				mConnectionZMQ, Context.BIND_AUTO_CREATE);

		mIsZmqBound = true;
	}

	void unbindService() {
		if (mIsZmqBound) {
			// Detach our existing connection.
			unbindService(mConnectionZMQ);
			mIsZmqBound = false;
		}
	}

	public void ZMQreconnect() {
		if (mzService != null) {
			mzService.reconnect();
		}
	}
	
	protected void onResume() {
		// SharedPreferences sp = getSharedPreferences("com.luyun.easyway95",
		// MODE_PRIVATE);
		// UserProfile up = new UserProfile(sp);
		// //Log.d(TAG, up.toString());
		// mHomeAddr = up.getHomeAddr();

		Log.d(TAG, "onResume");
		super.onResume();
		app.mBMapMan.start();
		updateViewMap();
	}
	
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		unbindService();
		unregisterReceiver(mConnectivityChangeReceiver);
	}

}