package com.luyun.whereareyou.tracker_client;

public class Constants {
	
	public static final int POISEARCH=1000;
	
	public static final int ERROR=1001;
	public static final int FIRST_LOCATION=1002;
	
	public static final int MAIN_GET_CONTACT = 2;  //从通讯录获取联系人
	public static final int PICK_CONTACT = 3;  //从通讯录获取联系人
	public static final int SEARCH_DEST = 4;  //从地图搜索目的地址
	
	public static final int REOCODER_RESULT=3000;//地理编码结果
	public static final int DIALOG_LAYER=4000;
	public static final int POISEARCH_NEXT=5000;
	
	public static final int BUSLINE_RESULT=6000;
	public static final int BUSLINE_DETAIL_RESULT=6001;	

	public static final int TRACKER_SERVER_PORT=8007; 
	public static final int TRACKEE_SERVER_PORT=8008;	
	public static final String TRACKER_SERVER_HOST="42.121.99.247";	
	
	public static final String ZMQ_QUITTING_CMD = "QT";
	public static final String ZMQ_RECONNECT_CMD = "RC";
	public static final String DOWN_SRV_DATA = "DOWN_SRV_DATA";
	
	public static final String USERS_PROFILE_URL="http://www.roadclouding.com/users/profile";
	public static final String PROMOTION_URL="http://www.roadclouding.com/commerce";
	public static final String FAQ_URL="http://www.roadclouding.com/faq";
	public static final String DOWNLOAD_URL="http://www.roadclouding.com/download/easyway.apk";	
	
	public static final String GET_WEIBO_URL="http://www.roadclouding.com/weibos/get";
	public static final String WEIBO_URL_OAUTH2="https://api.weibo.com";
	
	//路云主页
//	public static final String WEIBO_CONSUMER_KEY = "3480490775";
//	public static final String WEIBO_CONSUMER_SECRET = "876dd843606f5d99d86e716cc1c69264";
	//路云
	public static final String WEIBO_CONSUMER_KEY = "1443429908";
	public static final String WEIBO_CONSUMER_SECRET = "7c3e7ab52eaa67644b49d923a837c01c";
	public static final String WEIBO_REDIRECT_URL = "http://www.roadclouding.com/users/auth/weibo/callback";
	
	//wexin by chenfeng
//	public static final String WEIXIN_APP_ID = "wx9facda03786fc8af";	//测试
	public static final String WEIXIN_APP_ID = "wxd69fbd18ca12e5f3";	//发布
	public static final String SHARE_MESSAGE = "分享自路易95 http://www.roadclouding.com";
	public static final String MAIN_ACTIVTY = "com.luyun.whereareyou.tracker_client_android.TrackerActivity";
}
