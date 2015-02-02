package com.example.chat.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.chat.R;
import com.example.chat.model.LocationInfo;

/**
 * AMapV2地图中介绍定位三种模式的使用，包括定位，追随，旋转
 */
public class LocationModeSourceActivity extends FragmentActivity implements LocationSource,
		AMapLocationListener,
		OnGeocodeSearchListener, OnMapScreenShotListener, OnMapLoadedListener, 
		OnMarkerClickListener, OnInfoWindowClickListener, OnCameraChangeListener {
	private SupportMapFragment mapFragment;
	private AMap aMap;
	private LocationManagerProxy mAMapLocationManager;
	
	/**
	 * 定位的半径，默认是200，单位米
	 */
	public static float locationRadius = 200F;
	
	/**
	 * 默认的地图缩放级别为16,有效范围是4--20
	 */
	public static int zoomLevel = 16;
	
	/**
	 * 地图加载的周期为1秒
	 */
	private static int loadDelay = 1000;
	/**
	 * 当前的地图是否加载完毕，只有当地图加载完毕后才可定位，否则会抛异常
	 */
	private boolean isMapLoaded = false;
	
	private GeocodeSearch mGeocoderSearch;
	private List<LocationInfo> mLocationInfos = new ArrayList<>();
	private Handler mHandler = new Handler();
	private OnLocationChangedListener mLocationChangedListener;
	
	private ListView lvData;
	
	private LocationAdapter mAdapter;
	
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_share);
        /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
	    //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
//        MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
//		mapView = (MapView) findViewById(R.id.map);
//		mapView.onCreate(savedInstanceState);// 此方法必须重写
		context = this;
		mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		lvData = (ListView) findViewById(R.id.lv_data);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapFragment.getMap();
			setUpMap();
		}
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
//		aMap.setLocationSource(this);// 设置定位监听
//		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
//		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
//		//设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种 
//		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		
		if (aMap == null) {
			aMap = mapFragment.getMap();
		}
		UiSettings uiSettings = aMap.getUiSettings();
		uiSettings.setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		uiSettings.setScaleControlsEnabled(true);
//			MyLocationStyle locationStyle = new MyLocationStyle();
//			locationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_markers));
//			int color = getResources().getColor(android.R.color.transparent);
//			locationStyle.strokeColor(color);
//			locationStyle.radiusFillColor(color);
//			aMap.setMyLocationStyle(locationStyle);
		aMap.setLocationSource(this);// 设置定位监听
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		//设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种 
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		aMap.setOnMapLoadedListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
		aMap.setOnCameraChangeListener(this);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		setUpMap();
		if (mGeocoderSearch == null) {
			mGeocoderSearch = new GeocodeSearch(this);
			mGeocoderSearch.setOnGeocodeSearchListener(this);
		}
		activate(mLocationChangedListener);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		deactivate();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 此方法已经废弃
	 */
	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mLocationChangedListener != null && aLocation != null) {
			drawLocation(aLocation);
		}
	}
	
	/**
	 * 将定位后的数据显示在地图上
	 * @update 2015年1月5日 下午10:25:06
	 * @param location
	 */
	private void drawLocation(final AMapLocation location) {
		if (isMapLoaded) {
			mLocationChangedListener.onLocationChanged(location);	//显示定位后的当前位置的小圆点
			LatLonPoint latLonPoint = new LatLonPoint(location.getLatitude(), location.getLongitude());
			RegeocodeQuery query = new RegeocodeQuery(latLonPoint, locationRadius,
					GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
			mGeocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
			aMap.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel));
		} else {
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					drawLocation(location);
				}
			}, loadDelay);
		}
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mLocationChangedListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
			mAMapLocationManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, 2000, 10, this);
		}
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mLocationChangedListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
		}
		mAMapLocationManager = null;
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onMapLoaded() {
		isMapLoaded = true;
	}

	@Override
	public void onMapScreenShot(Bitmap arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 位置适配器
	 * @author huanghui1
	 * @update 2015年1月6日 上午10:06:46
	 */
	class LocationAdapter extends BaseAdapter {
		private Context context;
		private List<LocationInfo> list;
		private LayoutInflater inflater;
		
		public LocationAdapter(List<LocationInfo> list, Context context) {
			this.context = context;
			this.list = list;
			
			inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LocationViewHolder holder = null;
			if (convertView == null) {
				holder = new LocationViewHolder();
				convertView = inflater.inflate(R.layout.item_list_single_choice, parent, false);
				
				holder.checkedTextView = (CheckedTextView) convertView.findViewById(R.id.tv_content);
				convertView.setTag(holder);
			} else {
				holder = (LocationViewHolder) convertView.getTag();
			}
			
			final LocationInfo locationInfo = list.get(position);
			
			holder.checkedTextView.setText(locationInfo.getAddress());
			
			return convertView;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
	}

	final class LocationViewHolder {
		CheckedTextView checkedTextView;
	}

	@Override
	public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
		
	}

	@Override
	public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
		if (rCode == 0) {	//反编码成功
			if (regeocodeResult != null) {
				RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
				RegeocodeQuery regeocodeQuery = regeocodeResult.getRegeocodeQuery();
				if (regeocodeAddress != null) {
//					StringBuilder sb = new StringBuilder();
//					List<Marker> markers = mAMap.getMapScreenMarkers();
//					if (markers != null && markers.size() > 0) {
//						mMarker = markers.get(0);
//						mMarker.setSnippet(regeocodeAddress.getFormatAddress());
//					}
					mLocationInfos.clear();
					LatLonPoint latLonPoint = regeocodeQuery.getPoint();
					LocationInfo info = new LocationInfo(latLonPoint.getLatitude(), latLonPoint.getLongitude(), regeocodeQuery.getRadius(), getString(R.string.location_prefix) + regeocodeAddress.getFormatAddress());
					mLocationInfos.add(info);
					List<PoiItem> poiItems = regeocodeAddress.getPois();
					if (poiItems != null) {
						for (PoiItem poiItem : poiItems) {
							latLonPoint = poiItem.getLatLonPoint();
							info = new LocationInfo(latLonPoint.getLatitude(), latLonPoint.getLongitude(), locationRadius, poiItem.getTitle());
//							locationArray.add(poiItem.getTitle());
//							sb.append("=============================\r\n");
//							sb.append("poiItem.getLatLonPoint:").append(poiItem.getLatLonPoint()).append("\r\n")
//								.append("poiItem.getAdCode:").append(poiItem.getAdCode()).append("\r\n")
//								.append("poiItem.getAdName:").append(poiItem.getAdName()).append("\r\n")
//								.append("poiItem.getCityCode:").append(poiItem.getCityCode()).append("\r\n")
//								.append("poiItem.getCityName:").append(poiItem.getCityName()).append("\r\n")
//								.append("poiItem.getDirection:").append(poiItem.getDirection()).append("\r\n")
//								.append("poiItem.getDistance:").append(poiItem.getDistance()).append("\r\n")
//								.append("poiItem.getEmail:").append(poiItem.getEmail()).append("\r\n")
//								.append("poiItem.getPoiId:").append(poiItem.getPoiId()).append("\r\n")
//								.append("poiItem.getProvinceCode:").append(poiItem.getProvinceCode()).append("\r\n")
//								.append("poiItem.getProvinceName:").append(poiItem.getProvinceName()).append("\r\n")
//								.append("poiItem.getSnippet:").append(poiItem.getSnippet()).append("\r\n")
//								.append("poiItem.getTel:").append(poiItem.getTel()).append("\r\n")
//								.append("poiItem.getTitle:").append(poiItem.getTitle()).append("\r\n")
//								.append("poiItem.getTypeDes:").append(poiItem.getTypeDes()).append("\r\n")
//								.append("poiItem.getWebsite:").append(poiItem.getWebsite()).append("\r\n");
							mLocationInfos.add(info);
						}
//						sb.append("=============================\r\n");
					}
					//TODO 添加适配器
					if (mAdapter == null) {
						mAdapter = new LocationAdapter(mLocationInfos, context);
						lvData.setAdapter(mAdapter);
						lvData.setItemChecked(0, true);	//第一位默认选中
					} else {
						mAdapter.notifyDataSetChanged();
					}
//					Log.d(TAG, sb.toString());
//					tvLocationInfo.setText(sb.toString());
				}
			}
		}
	}

}
