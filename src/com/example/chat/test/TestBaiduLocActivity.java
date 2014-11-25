package com.example.chat.test;

import android.util.Log;
import android.view.View;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.chat.R;
import com.example.chat.activity.BaseActivity;

/**
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年11月25日 下午8:31:50
 */
public class TestBaiduLocActivity extends BaseActivity {
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	
    private boolean isRequest = false;//是否手动触发请求定位  
    private boolean isFirstLoc = true;//是否首次定位  
    
    private MapView mapView;
    private BaiduMap mBaiduMap;
      
    /** 
     * 弹出窗口图层的View 
     */  
    private View mPopupView;  
    private BDLocation location;  

	@Override
	protected int getContentView() {
		return R.layout.test_loc;
	}

	@Override
	protected void initView() {
		mapView = (MapView) findViewById(R.id.bmapView);
	}

	@Override
	protected void initData() {
		
		mBaiduMap = mapView.getMap();
		
		mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
	    mLocationClient.registerLocationListener( myListener );    //注册监听函数
	    LocationClientOption clientOption = new LocationClientOption();
	    
	 // 设置自定义图标  
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory  
                .fromResource(R.drawable.test_navi_map_gps_locked);  
//        MyLocationConfigeration config = new MyLocationConfigeration(  
//                mCurrentMode, true, mCurrentMarker);
        MyLocationConfiguration paramMyLocationConfiguration = new MyLocationConfiguration(LocationMode.NORMAL, true, mCurrentMarker);
        mBaiduMap.setMyLocationConfigeration(paramMyLocationConfiguration);
	    
	 // 设置定位的相关配置  
        clientOption.setOpenGps(true);// 打开gps  
        clientOption.setCoorType("bd09ll"); // 设置坐标类型  
        clientOption.setScanSpan(1000); 
        
        mLocationClient.setLocOption(clientOption);
	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void onStart() {
		//开启图层定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		// 关闭图层定位  
        mBaiduMap.setMyLocationEnabled(false);  
        mLocationClient.stop(); 
		super.onStop();
	}
	
	class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			MyLocationData locationData = new MyLocationData.Builder()
				.accuracy(location.getRadius())
				.latitude(location.getLatitude())
				.longitude(location.getLongitude())
				.build();
			
			mBaiduMap.setMyLocationData(locationData);
			
			// 第一次定位时，将地图位置移动到当前位置  
            if (isFirstLoc)  
            {  
            	isFirstLoc = false;  
                LatLng ll = new LatLng(location.getLatitude(),  
                        location.getLongitude());  
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);  
                mBaiduMap.animateMapStatus(u);  
            }
			
			//Receive Location 
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation){
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				sb.append("\ndirection : ");
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append(location.getDirection());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				//运营商信息
				sb.append("\noperationers : ");
				sb.append(location.getOperators());
			}
			Log.i("BaiduLocationApiDem", sb.toString());
		}
		
	}

}
