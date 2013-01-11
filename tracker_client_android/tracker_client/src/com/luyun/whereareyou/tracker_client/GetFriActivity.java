package com.luyun.whereareyou.tracker_client;

import java.util.ArrayList;

import com.luyun.whereareyou.shared.TrackEventProtos.TrackEvent;
import com.luyun.whereareyou.shared.TrackEventProtos.TrackEvent.EventType;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// 选取联系人
public class GetFriActivity extends Activity {
	private final static String TAG = "GetFriActivity";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.get_fri_activity);

		ImageView fri_view = (ImageView) findViewById(R.id.btn_fri);
		fri_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, Constants.PICK_CONTACT);
			}
		});

		TextView txt_sms = (TextView) findViewById(R.id.txt_sms);
		TextView txt_tips = (TextView) findViewById(R.id.txt_tips);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			// 发送目的地
			MKPoiInfoHelper mpi = (MKPoiInfoHelper) bundle
					.getSerializable(Constants.POI_RETURN_KEY);

			Log.d(TAG, mpi.getAddress());
			txt_sms.setText(Constants.DESTSMS + mpi.getCity() + mpi.getName());
			txt_tips.setText(Constants.DESTTIP);
			
		} else {
			//发送 短信请求定位
			txt_sms.setText(Constants.LOCSMS);
			txt_tips.setText(Constants.LOCTIP);
		}
	}

	protected void onActivityResult(int reqCode, int resultCode, Intent intent) {
		super.onActivityResult(reqCode, resultCode, intent);

		Intent it = new Intent();

		switch (reqCode) {
		case (Constants.PICK_CONTACT):
			Log.d(TAG, "get contact");
			if (resultCode == RESULT_OK) {
				Uri contactData = intent.getData();
				if (contactData == null) {
					Log.d(TAG, "null contactData");
					setResult(RESULT_CANCELED, it);
					finish();
				}

				Cursor c = managedQuery(contactData, null, null, null, null);
				String phoneNo = null;

				if (c.moveToFirst()) {
					String name = c
							.getString(c
									.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

					String id = c.getString(c
							.getColumnIndex(ContactsContract.Contacts._ID));
					if (Integer
							.parseInt(c.getString(c
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
						Cursor pCur = getContentResolver()
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = ?", new String[] { id },
										null);
						while (pCur.moveToNext()) {
							// Do something with phones
							phoneNo = pCur
									.getString(pCur
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
							Toast.makeText(getApplicationContext(), phoneNo,
									Toast.LENGTH_SHORT).show();
						}
						pCur.close();
					}
					if (phoneNo == null) {
						Log.d(TAG, "null contact");
						setResult(RESULT_CANCELED, it);
						finish();
					}
					// Toast.makeText(getApplicationContext(), name,
					// Toast.LENGTH_SHORT).show();

					Bundle bundle = new Bundle();
					String rslt[] = { name, phoneNo };
					Log.d(TAG, name + " " + phoneNo);
					bundle.putStringArray("contact", rslt);
					it.putExtras(bundle);
					setResult(RESULT_OK, it);
					finish();
				}
			}
			break;
		}
	}
}
