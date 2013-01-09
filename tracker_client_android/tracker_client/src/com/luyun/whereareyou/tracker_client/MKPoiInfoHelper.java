package com.luyun.whereareyou.tracker_client;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKPoiInfo;

public class MKPoiInfoHelper implements Serializable{ // helper for MKPoiInfo to be conveyed on air
	private String name;
	private String address;
	private String city;
	private String phoneNum;
	private String postCode;
	private int latitudeE6;
	private int longitudeE6;
//	private GeoPoint pt;
	private int ePoiType;
	//private String searchKey;
	private String searchPlace;
	
	@Override
	public String toString() {
		String foramtedString = String.format(
				"name=(%s), address=(%s), city=(%s), phoneNum=(%s), postCode=(%s), pt.lat=(%d), pt.lng=(%d), ePoiType=(%d), searchPlace=(%s)", 
				name, address, city, phoneNum, postCode, latitudeE6, longitudeE6, ePoiType, searchPlace);
		return foramtedString;
	}
	
	MKPoiInfoHelper(MKPoiInfo mpi) {
		this.name = mpi.name;
		this.address = mpi.address;
		this.city = mpi.city;
		this.phoneNum = mpi.phoneNum;
		this.postCode = mpi.postCode;
//		this.pt = mpi.pt;
		this.latitudeE6 = mpi.pt.getLatitudeE6();
		this.longitudeE6 = mpi.pt.getLongitudeE6();
		this.ePoiType = mpi.ePoiType;
	}

	MKPoiInfoHelper(MKAddrInfo mdi) {
		//对返回的结果进行正则表达式的处理
		//返回的结果形式为：广东省深圳市宝安区公园路1号
		String addr = mdi.strAddr;
		//首先获取省的信息
		String tmpStrings[] = addr.split("省");
		String province = "";
		String addrWithCity = addr;
		if (tmpStrings.length >= 2) {
			province = tmpStrings[0]+"省";
			addrWithCity = addr.replaceAll(province, "");
		}
		//然后获取市的信息
		tmpStrings = addrWithCity.split("市");
		String city = "";
		String addrWithDist = addrWithCity;
		if (tmpStrings.length >= 2) {
			city = tmpStrings[0]+"市";
			addrWithDist = addrWithCity.replaceAll(city, "");
		}
		//然后获取区的信息
		tmpStrings = addrWithDist.split("区");
		String dist = "";
		String addrWithStreet = addrWithDist;
		if (tmpStrings.length >= 2) {
			dist = tmpStrings[0]+"区";
			addrWithStreet = addrWithStreet.replaceAll(dist, "");
		}
		
		this.name = addrWithStreet;
		this.address = "";
		this.city = city;
		this.phoneNum = "";
		this.postCode = "";
//		this.pt = mdi.geoPt;
		this.latitudeE6 = mdi.geoPt.getLatitudeE6();
		this.longitudeE6 = mdi.geoPt.getLongitudeE6();
		//this.ePoiType = ;
	}

	MKPoiInfoHelper() {
		this.name = "";
		this.address = "";
		this.city = "";
		this.phoneNum = "";
		this.postCode = "";
//		this.pt = new GeoPoint((int) (22.551541 * 1E6), (int) (113.94750 * 1E6));
		this.latitudeE6 = (int) (22.551541 * 1E6);
		this.longitudeE6 = (int) (113.94750 * 1E6);
		this.ePoiType = 0;
		this.searchPlace = "";
	}
	
	//constructor for formatedString (result of toString)
	MKPoiInfoHelper(String strMPI) {
		this.name = "";
		this.address = "";
		this.city = "";
		this.phoneNum = "";
		this.postCode = "";
//		this.pt = new GeoPoint((int) (22.551541 * 1E6), (int) (113.94750 * 1E6));
		this.latitudeE6 = (int) (22.551541 * 1E6);
		this.longitudeE6 = (int) (113.94750 * 1E6);
		this.ePoiType = 0;
		this.searchPlace = "";
		
		if (strMPI == null) return;
		//正则表达式的非贪婪匹配 http://www.wasw100.com/java/java.util.regex/Greedy.html
		Pattern p = Pattern.compile("name=\\((.*?)\\)", Pattern.CASE_INSENSITIVE+Pattern.UNICODE_CASE);
		Matcher m = p.matcher(strMPI);
		if (m.find()) {
			this.name = m.group(1);
		}
		p = Pattern.compile("address=\\((.*?)\\)", Pattern.CASE_INSENSITIVE+Pattern.UNICODE_CASE);
		m = p.matcher(strMPI);
		if (m.find()) {
			this.address = m.group(1);
			//Log.d(TAG, m.group(1));
		}
		p = Pattern.compile("city=\\((.*?)\\)", Pattern.CASE_INSENSITIVE+Pattern.UNICODE_CASE);
		m = p.matcher(strMPI);
		if (m.find()) {
			this.city = m.group(1);
			//Log.d(TAG, m.group(1));
		}
		p = Pattern.compile("phoneNum=\\((.*?)\\)", Pattern.CASE_INSENSITIVE+Pattern.UNICODE_CASE);
		m = p.matcher(strMPI);
		if (m.find()) {
			this.phoneNum = m.group(1);
			//Log.d(TAG, m.group(1));
		}
		p = Pattern.compile("postCode=\\((.*?)\\)", Pattern.CASE_INSENSITIVE+Pattern.UNICODE_CASE);
		m = p.matcher(strMPI);
		if (m.find()) {
			//Log.d(TAG, m.group(1));
			this.postCode = m.group(1);
		}
		p = Pattern.compile("ePoiType=\\((\\d*?)\\)", Pattern.CASE_INSENSITIVE+Pattern.UNICODE_CASE);
		m = p.matcher(strMPI);
		if (m.find()) {
			//Log.d(TAG, m.group(1));
			this.ePoiType = Integer.parseInt(m.group(1));
		}
		p = Pattern.compile("pt\\.lat=\\((\\d*?)\\)", Pattern.CASE_INSENSITIVE+Pattern.UNICODE_CASE);
		m = p.matcher(strMPI);
		if (m.find()) {
			//Log.d(TAG, m.group(1));
//			if (this.pt == null) {
//				this.pt = new GeoPoint((int) (22.551541 * 1E6), (int) (113.94750 * 1E6));
//			}
//			this.pt.setLatitudeE6(Integer.parseInt(m.group(1)));
			this.latitudeE6 = Integer.parseInt(m.group(1));
		}
		p = Pattern.compile("pt\\.lng=\\((\\d*?)\\)", Pattern.CASE_INSENSITIVE+Pattern.UNICODE_CASE);
		m = p.matcher(strMPI);
		if (m.find()) {
			//Log.d(TAG, m.group(1));
//			if (this.pt == null) {
//				this.pt = new GeoPoint((int) (22.551541 * 1E6), (int) (113.94750 * 1E6));
//			}
//			this.pt.setLongitudeE6(Integer.parseInt(m.group(1)));
			this.longitudeE6 = Integer.parseInt(m.group(1));
			
		}
	}
	
	public String getName() {
		return this.name;
	}
	public void setName(String nm) {
		this.name = nm;
	}
	public String getAddress() {
		return this.address;
	}
	public void setAddress(String addr) {
		this.address = addr;
	}
	public String getCity() {
		return this.city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getPhoneNum() {
		return this.phoneNum;
	}
	public void setPhoneNum(String pn) {
		this.phoneNum = pn;
	}
	public String getPostCode() {
		return this.postCode;
	}
	public void setPostCode(String pc) {
		this.postCode = pc;
	}
	public int getLatitudeE6() {
		return this.latitudeE6;
	}
	public int getLongitudeE6() {
		return this.longitudeE6;
	}
	public int getEPoiType() {
		return this.ePoiType;
	}
	public void setEPoiType(int pt) {
		this.ePoiType = pt;
	}
	public GeoPoint getPt() {
		return new GeoPoint(this.latitudeE6, this.longitudeE6);
	}
	public void setPt(GeoPoint pt) {
		this.latitudeE6 = pt.getLatitudeE6();
		this.longitudeE6 = pt.getLongitudeE6();
	}
	public void setSearchPlace(String sp) {
		this.searchPlace = sp;
	}
	public String getSearchPlace() {
		return this.searchPlace;
	}
	public String getLatLng() {
		String foramtedString = String.format(
				"(%d, %d)", this.latitudeE6, this.longitudeE6);
		return foramtedString;
	}
	
	public void copyFrom (MKPoiInfo poiInfo) {
		address = poiInfo.address;
		city = poiInfo.city;
		ePoiType = poiInfo.ePoiType;
		name = poiInfo.name;
		phoneNum = poiInfo.phoneNum;
		postCode = poiInfo.postCode;
		latitudeE6 = poiInfo.pt.getLatitudeE6();
		longitudeE6 = poiInfo.pt.getLongitudeE6();
	}
	
	public void copyTo (MKPoiInfo poiInfo) {
		poiInfo.address = address;
		poiInfo.city = city;
		poiInfo.ePoiType = ePoiType;
		poiInfo.name = name;
		poiInfo.phoneNum = phoneNum;
		poiInfo.postCode = postCode;
		poiInfo.pt = new GeoPoint(latitudeE6, longitudeE6);
	}
}
