package com.example.chat.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.amap.api.maps.MapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
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
import com.example.chat.util.SystemUtil;

/**
 * 发送地理位置的界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2015年1月5日 下午9:32:22
 */
public class LocationShareActivity extends BaseActivity implements LocationSource, AMapLocationListener, 
		OnGeocodeSearchListener, OnMapScreenShotListener, OnMapLoadedListener, 
		OnMarkerClickListener, OnInfoWindowClickListener, OnCameraChangeListener {
	
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
	
	private MapFragment mMapFragment;
	
	/**
	 * 地图对象，地图的关键操作都在这个对象中
	 */
	private AMap mAMap;
	
	private ListView lvData;
	
	private ProgressBar pbLoading;
	
	/**
	 * 当前的地图是否加载完毕，只有当地图加载完毕后才可定位，否则会抛异常
	 */
	private boolean isMapLoaded = false;
	
	private OnLocationChangedListener mLocationChangedListener;
	private LocationManagerProxy mLocationManager;
	private GeocodeSearch mGeocoderSearch;
	private Marker mMarker;
    private int mCurrentPosition;
    private boolean locationSuccessed = false;
    
    private List<LocationInfo> mLocationInfos = new ArrayList<>();
    
    private Handler mHandler = new Handler();

	private LocationAdapter mAdapter;

	private TextView btnOpt;

	@Override
	protected int getContentView() {
		return R.layout.activity_location_share;
	}

	@Override
	protected void initView() {
		if (mMapFragment == null) {
			mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		}
		lvData = (ListView) findViewById(R.id.lv_data);
		pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
	}

	@Override
	protected void initData() {
		// TODO Auto-generated method stub
		
	}
	
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
	
	@Override
	protected void onPause() {
		super.onPause();
		deactivate();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.common_opt, menu);
		MenuItem menuDone = menu.findItem(R.id.action_select_complete);
		btnOpt = (TextView) menuDone.getActionView();
		btnOpt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mAMap.getMapScreenShot(LocationShareActivity.this);
//				final List<PhotoItem> selects = mPhotoAdapter.getSelectList();
//				pDialog = ProgressDialog.show(mContext, null, getString(R.string.chat_sending_file), false, true);
//				//发送图片
//				SystemUtil.getCachedThreadPool().execute(new Runnable() {
//					
//					@Override
//					public void run() {
//						final ArrayList<MsgInfo> msgList = msgManager.getMsgInfoListByPhotos(msgInfo, selects, false);
//						Message msg = mHandler.obtainMessage();
//						if (!SystemUtil.isEmpty(msgList)) {	//消息集合
//							msg.what = Constants.MSG_SUCCESS;
//							msg.obj = msgList;
//						} else {
//							msg.what = Constants.MSG_FAILED;
//						}
//						mHandler.sendMessage(msg);
//					}
//				});
				
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * 设置map对象的相关参数
	 * @update 2015年1月5日 下午9:59:30
	 */
	private void setUpMap() {
		if (mAMap == null) {
			mAMap = mMapFragment.getMap();
			UiSettings uiSettings = mAMap.getUiSettings();
			uiSettings.setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
			uiSettings.setScaleControlsEnabled(true);
			MyLocationStyle locationStyle = new MyLocationStyle();
			locationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_markers));
			int color = getResources().getColor(android.R.color.transparent);
			locationStyle.strokeColor(color);
			locationStyle.radiusFillColor(color);
			mAMap.setMyLocationStyle(locationStyle);
			mAMap.setLocationSource(this);// 设置定位监听
			mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
			//设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种 
			mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
			mAMap.setOnMapLoadedListener(this);
			mAMap.setOnMarkerClickListener(this);
			mAMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
			mAMap.setOnCameraChangeListener(this);
			
		}
	}

	@Override
	protected void addListener() {
		lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (position != mCurrentPosition) {
					mCurrentPosition = position;
					LocationInfo locationInfo = mLocationInfos.get(position);
					if (mMarker != null) {
						mMarker.setSnippet(locationInfo.getAddress());
					}
					mAMap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude())));
					if (mMarker != null) {
						mMarker.setPosition(new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude()));
					}
				}
			}
		});
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraChangeFinish(CameraPosition position) {
		if (mMarker != null && mMarker.isInfoWindowShown()) {
			mMarker.showInfoWindow();
		}
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		marker.hideInfoWindow();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (!marker.isInfoWindowShown()) {
			marker.showInfoWindow();
		}
		return true;
	}

	@Override
	public void onMapLoaded() {
		isMapLoaded = true;
	}

	@Override
	public void onMapScreenShot(Bitmap bitmap) {
		if (bitmap != null) {
			FileOutputStream fos = null;
			try {
				File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
				fos = new FileOutputStream(file);
				bitmap.compress(CompressFormat.JPEG, 20, fos);
				SystemUtil.makeShortToast("截图成功：" + file.getAbsolutePath());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (bitmap != null) {
					bitmap.recycle();
				}
				
			}
		}
	}

	@Override
	public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
		if (rCode == 0) {	//反编码成功
			if (regeocodeResult != null) {
				RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
				RegeocodeQuery regeocodeQuery = regeocodeResult.getRegeocodeQuery();
				if (regeocodeAddress != null) {
					locationSuccessed = true;
					if (btnOpt != null) {
						btnOpt.setEnabled(true);
					}
//					StringBuilder sb = new StringBuilder();
					List<Marker> markers = mAMap.getMapScreenMarkers();
					if (markers != null && markers.size() > 0) {
						mMarker = markers.get(0);
						mMarker.setSnippet(regeocodeAddress.getFormatAddress());
					}
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
						mAdapter = new LocationAdapter(mLocationInfos, mContext);
						lvData.setAdapter(mAdapter);
						lvData.setItemChecked(0, true);	//第一位默认选中
					} else {
						mAdapter.notifyDataSetChanged();
					}
//					Log.d(TAG, sb.toString());
					pbLoading.setVisibility(View.GONE);
//					tvLocationInfo.setText(sb.toString());
				}
			}
		}
	}

	@Override
	public void onLocationChanged(AMapLocation location) {
		if (mLocationChangedListener != null && location != null) {
			if (!locationSuccessed) {
				drawLocation(location);
			}
		}
	}
	
	/**
	 * 将定位后的数据显示在地图上
	 * @update 2015年1月5日 下午10:25:06
	 * @param location
	 */
	private void drawLocation(final AMapLocation location) {
		if (!isMapLoaded) {
			mLocationChangedListener.onLocationChanged(location);	//显示定位后的当前位置的小圆点
			LatLonPoint latLonPoint = new LatLonPoint(location.getLatitude(), location.getLongitude());
			RegeocodeQuery query = new RegeocodeQuery(latLonPoint, locationRadius,
					GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
			mGeocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
			mAMap.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel));
		} else {
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					drawLocation(location);
				}
			}, loadDelay);
		}
	}

	@Override
	public void activate(OnLocationChangedListener locationChangedListener) {
		mLocationChangedListener = locationChangedListener;
		if (mLocationManager == null) {
			mLocationManager = LocationManagerProxy.getInstance(this);
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是3000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
			mLocationManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, 3000, 10, this);
		}
	}

	@Override
	public void deactivate() {
		mLocationChangedListener = null;
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(this);
			mLocationManager.destroy();
		}
		mLocationManager = null;
	}
	
	/**
	 * 位置适配器
	 * @author huanghui1
	 * @update 2015年1月6日 上午10:06:46
	 */
	class LocationAdapter extends CommonAdapter<LocationInfo> {

		public LocationAdapter(List<LocationInfo> list, Context context) {
			super(list, context);
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
		
	}

	final class LocationViewHolder {
		CheckedTextView checkedTextView;
	}
}
