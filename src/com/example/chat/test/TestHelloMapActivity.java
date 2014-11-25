package com.example.chat.test;

import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.share.OnGetShareUrlResultListener;
import com.baidu.mapapi.search.share.PoiDetailShareURLOption;
import com.baidu.mapapi.search.share.ShareUrlResult;
import com.baidu.mapapi.search.share.ShareUrlSearch;
import com.example.chat.R;
import com.example.chat.activity.BaseActivity;

/**
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年11月25日 下午7:23:05
 */
public class TestHelloMapActivity extends BaseActivity {
	private MapView mapView;
	private BaiduMap mBaiduMap;
	
	private ShareUrlSearch mShareUrlSearch;

	@Override
	protected int getContentView() {
		return R.layout.test_activity_hello_map;
	}

	@Override
	protected void initView() {
		mapView = (MapView) findViewById(R.id.bmapView);
	}
	
	OnGetShareUrlResultListener listener = new OnGetShareUrlResultListener() {  
	    public void onGetPoiDetailShareUrlResult(ShareUrlResult result) {  
	        //分享POI详情  
	    }  
	    public void onGetLocationShareUrlResult(ShareUrlResult result) {  
	        //分享位置信息  
	    }  
	};

	@Override
	protected void initData() {
		mBaiduMap = mapView.getMap();
		//普通地图  
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); 
		
		mShareUrlSearch = ShareUrlSearch.newInstance();
		
		mShareUrlSearch.setOnGetShareUrlResultListener(listener);
		
		
		//卫星地图  
//		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
		
		
		//定义Maker坐标点  
		LatLng point = new LatLng(39.963175, 116.400244);
		//构建Marker图标  
		BitmapDescriptor bitmap = BitmapDescriptorFactory  
		    .fromResource(R.drawable.test_mark);
		//构建MarkerOption，用于在地图上添加Marker  
		OverlayOptions option = new MarkerOptions()  
		    .position(point)  
		    .icon(bitmap);  
		//在地图上添加Marker，并显示  
		mBaiduMap.addOverlay(option);
		
		
//		OverlayOptions options = new MarkerOptions()
//		    .position(llA)  //设置marker的位置
//		    .icon(bdA)  //设置marker图标
//		    .zIndex(9)  //设置marker所在层级
//		    .draggable(true);  //设置手势拖拽
//		//将marker添加到地图上
//		marker = (Marker) (mBaiduMap.addOverlay(options));
	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub

	}
	
	public void clear(View view) {
	}
	
	public void reset(View view) {
		
	}
	
	@Override
	protected void onPause() {
		mapView.onPause();
		super.onPause();
	}

	
	@Override
	protected void onResume() {
		mapView.onResume();
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		mapView.onDestroy();
		super.onDestroy();
	}
}
