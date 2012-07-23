package com.luyun.whereareyou.tracker_client_android;

import java.util.List;

import com.amap.mapapi.core.AMapException;
import com.amap.mapapi.core.GeoPoint;
import com.amap.mapapi.geocoder.Geocoder;
import com.amap.mapapi.map.MapActivity;
import com.amap.mapapi.map.MapController;
import com.amap.mapapi.map.MapView;
import com.amap.mapapi.map.Overlay;
import com.luyun.whereareyou.tracker_client_android.Constants;
import com.luyun.whereareyou.tracker_client_android.MyLocationOverlayProxy;
import com.luyun.whereareyou.tracker_client_android.R;
import com.luyun.whereareyou.tracker_client_android.ShakeEventListener;
import com.luyun.whereareyou.shared.TrackEventProtos;
import com.luyun.whereareyou.shared.TrackEventProtos.TrackEvent;
import com.luyun.whereareyou.shared.TrackEventProtos.TrackEvent.Builder;
import com.luyun.whereareyou.shared.TrackEventProtos.TrackEvent.EventType;
import com.google.protobuf.InvalidProtocolBufferException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Window;
import android.widget.Toast;

import org.zeromq.ZMQ;

public class TrackerActivity extends MapActivity {
	private MapView mMapView;
	private MapController mMapController;
	private GeoPoint point;
	private MyLocationOverlayProxy mLocationOverlay;
	private Geocoder mCoder;// 逆地理编码
	  
	//detect shaking gesture event
	private SensorManager mSensorManager;
	private ShakeEventListener mSensorListener;
    
	//pick contact
	private static final int PICK_CONTACT = 3;  
	
	private Handler mOnMsgRecv;
	private NWThread mIOThread;
	private TrackEvent mTrackEventOnAir;  //first version only support one track event on air at the same time;
	
	//phone information
	private String mDeviceID;
	
	@Override
	/**
	*显示矢量地图，将libminimapv300.so复制到工程目录下的libs\armeabi。
	*启用内置缩放控件，并用MapController控制地图的中心点及Zoom级别
	*/
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        mDeviceID = tm.getDeviceId();
        System.out.println(mDeviceID);
        
		setContentView(R.layout.mapview);
		mMapView = (MapView) findViewById(R.id.mapView);  
		mMapView.setVectorMap(true);//设置地图为矢量模式
		mMapView.setBuiltInZoomControls(true);  //设置启用内置的缩放控件
		mMapController = mMapView.getController();  // 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		point = new GeoPoint((int) (39.90923 * 1E6),
				(int) (116.397428 * 1E6));  //用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
		mMapController.setCenter(point);  //设置地图中心点
		mMapController.setZoom(12);    //设置地图zoom级别
		mCoder = new Geocoder(this);

        mOnMsgRecv = new Handler() {
        	public void handleMessage(Message msg) {
        		System.out.println("get message from server!");
        		TrackEvent trackEventOnWire;
	            try {
					trackEventOnWire  = TrackEvent.parseFrom(msg.getData().getByteArray("event"));
					switch (trackEventOnWire.getType()) {
					case START_TRACKING_REP:
			            String strHyperLink = ""
			            					+"想知道你在哪，同意请点击"
			            					+"http://"+Constants.TRACKER_SERVER_HOST+":"
			            					+Constants.TRACKEE_SERVER_PORT
			            					+"/t/"
			            					+trackEventOnWire.getId();
			            System.out.println(strHyperLink);
			            postMsg(mTrackEventOnAir.getTrackee(), strHyperLink);
			            break;
					case FWD_LOC_REQ:
			            System.out.println(trackEventOnWire.getTrackeeX());
			            System.out.println(trackEventOnWire.getTrackeeY());
			            //convert it to mapabc's address firstly
			            try  {
							List<Address> listAddress = mCoder.getFromRawGpsLocation(Double.parseDouble(trackEventOnWire.getTrackeeX()),
									Double.parseDouble(trackEventOnWire.getTrackeeY()), 3);
							if (listAddress != null) {
								Address address = listAddress.get(0);
								addMarker(address.getLongitude(), address.getLatitude());
							}
			            } catch (AMapException e) {
							// TODO Auto-generated catch block
							handler.sendMessage(Message
									.obtain(handler, Constants.ERROR));
						}
			            break;
			        default:
			        	break;
					}
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
        	}
        };
        mIOThread = new NWThread();
        mIOThread.start();
        
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();   
    	
        mMapController.setZoom(14);    //设置地图zoom级别
    	mLocationOverlay = new MyLocationOverlayProxy(getApplicationContext(), mMapView);
    	mMapView.getOverlays().add(mLocationOverlay);
    	mLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                handler.sendMessage(Message.obtain(handler, Constants.FIRST_LOCATION));
            }
        });
        mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {
          public void onShake() {
            Toast.makeText(getApplicationContext(), "Shake!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        	//queryWhereAreYou();
          }

        });
		
	}
	
	private void postMsg(String recipient, String msg) {
			// TODO Auto-generated method stub
		PendingIntent pi =PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), TrackerActivity.class), 0);
		SmsManager sms=SmsManager.getDefault();
		sms.sendTextMessage(recipient, null, msg, pi, null);
        Toast.makeText(getApplicationContext(), "Successfully sent message!", Toast.LENGTH_SHORT).show();
	}
		  
    @Override
    protected void onResume() {
	  this.mLocationOverlay.enableMyLocation();
      super.onResume();
      mSensorManager.registerListener(mSensorListener,
          mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
          SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
      mSensorManager.unregisterListener(mSensorListener);
  	  this.mLocationOverlay.disableMyLocation();
      super.onStop();
    }
	
    private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == Constants.FIRST_LOCATION) {
				mMapController.animateTo(mLocationOverlay.getMyLocation());
			}
		}
    };
    
    private void addMarker(String lngX, String latY) {
		point = new GeoPoint((int) (Double.parseDouble(latY) * 1E6),
					(int) (Double.parseDouble(lngX) * 1E6));  //用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
    	mMapView.getOverlays().add(new MyMarker());        
    }
    
    private void addMarker(Double lngX, Double latY) {
		point = new GeoPoint((int) (latY * 1E6),
					(int) (lngX * 1E6));  //用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
    	mMapView.getOverlays().add(new MyMarker());        
    }
    
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
      super.onActivityResult(reqCode, resultCode, data);

      switch (reqCode) {
        case (PICK_CONTACT) :
          if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c =  managedQuery(contactData, null, null, null, null);
            if (c.moveToFirst()) {
              String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
              // TODO Whatever you want to do with the selected contact name.
		      String id = c.getString(
                        c.getColumnIndex(ContactsContract.Contacts._ID));
		      if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor pCur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
             		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{id}, null);
             	while (pCur.moveToNext()) {
             		    // Do something with phones
                    String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                    Toast.makeText(getApplicationContext(), phoneNo, Toast.LENGTH_SHORT).show();
                    //to be improved, only sent to last number of that contact!
                    //mTrackEventOnAir.toBuilder().clear();
                    mTrackEventOnAir = TrackEvent.newBuilder()
                    			.setTrackee(phoneNo)
                    			.setTracker(mDeviceID)
                    			.setType(EventType.START_TRACKING_REQ)
                    			.build();
                    byte[] dataToSvr = mTrackEventOnAir.toByteArray();
                    mIOThread.sendMsgToTrackerSvr(dataToSvr);
             	} 
             	pCur.close();
		      }
              
              Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
            }
          }
          break;
      }
    }

	private void queryWhereAreYou() {
		// TODO Auto-generated method stub
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
		    while (cur.moveToNext()) {
		        String id = cur.getString(
	                        cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(
		                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		 		if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
	                Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
	             		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{id}, null);
	             	while (pCur.moveToNext()) {
	             		    // Do something with phones
	                    String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
	             		//Toast.makeText(getApplicationContext(), phoneNo, Toast.LENGTH_SHORT).show();
	             	} 
	             	pCur.close();
	 	        }
	        }
        }
	}
	
	private class NWThread extends Thread {
		//zmq & protobuf
		private ZMQ.Context context;
		private ZMQ.Socket zsocket;
		
		@Override
		public void run() {
			context = ZMQ.context(1);
	        zsocket = context.socket(ZMQ.DEALER);
	        zsocket.setIdentity(mDeviceID.getBytes());
	        
	        System.out.println("Connecting to tracker server..."); 
	        zsocket.connect ("tcp://"
	        				+Constants.TRACKER_SERVER_HOST
	        				+":"
	        				+Constants.TRACKER_SERVER_PORT);
            
	        while (true) {
		        byte[] data = zsocket.recv(0); 
		        Message msg = new Message();
		        Bundle bdl = new Bundle();
		        System.out.println("get message.");
		        bdl.putByteArray("event", data);
		        msg.setData(bdl);
		        mOnMsgRecv.sendMessage(msg);
	        }
		}
		
		public void sendMsgToTrackerSvr(byte[] data) {
			zsocket.send(data, 0);
		}
	}
	
	public class MyMarker extends Overlay {
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			// TODO Auto-generated method stub
			 super.draw(canvas, mapView, shadow);
			 Point screenPts = new Point();
	         mapView.getProjection().toPixels(point, screenPts);
	         //---add the marker---
	         Bitmap bmp = BitmapFactory.decodeResource(
	                getResources(), R.drawable.da_marker_red);            
	         canvas.drawBitmap(bmp, screenPts.x, screenPts.y-50, null);  
		}
		
		@Override
		public boolean onTap(GeoPoint arg0, MapView arg1) {
			// TODO Auto-generated method stub
			return super.onTap(arg0, arg1);
		}
	}
}