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
import android.widget.Toast;

// 选取联系人
public class GetFriActivity extends Activity {

	private String mSMS = "想知道你在哪，同意请点击";
	private final static String TAG = "GetFriActivity";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.get_fri_activity);

		ImageView fri_view = (ImageView) findViewById(R.id.btn_fri);
		fri_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Toast.makeText(getApplicationContext(), "invite friend!",
						Toast.LENGTH_SHORT).show();

				Intent intent = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, Constants.PICK_CONTACT);
			}

		});
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
					Toast.makeText(getApplicationContext(), name,
							Toast.LENGTH_SHORT).show();

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
