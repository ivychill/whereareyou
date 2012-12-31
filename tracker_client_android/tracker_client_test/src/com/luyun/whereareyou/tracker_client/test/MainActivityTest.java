package com.luyun.whereareyou.tracker_client.test;

import com.luyun.whereareyou.tracker_client.MainActivity;
import com.jayway.android.robotium.solo.Solo;
import android.test.ActivityInstrumentationTestCase2;

public class MainActivityTest extends
		ActivityInstrumentationTestCase2<MainActivity> {

	private Solo solo;
	
	@Override
	public void setUp() throws Exception {
		//setUp() is run before a test case is started. 
		//This is where the solo object is created.
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	@Override
	public void tearDown() throws Exception {
		//tearDown() is run after a test case has finished. 
		//finishOpenedActivities() will finish all the activities that have been opened during the test execution.
		solo.finishOpenedActivities();
	}
	public MainActivityTest() {
		super(MainActivity.class);

	}

	public void testStartNormal() throws Exception {
		solo.assertCurrentActivity("Expected Main activity", "MainActivity"); 
		solo.goBack(); 
		solo.clickOnButton(1); // exit btn no
		solo.goBack();
		solo.clickOnButton(0);  //exit btn yes
		solo.assertCurrentActivity("Expected Main activity", "Exit");
	}
}
