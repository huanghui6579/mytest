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
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.chat.R;
import com.example.chat.model.LocationInfo;
import com.example.chat.util.SystemUtil;
import com.example.chat.view.ProgressWheel;

/**
 * 发送地理位置的界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2015年1月5日 下午9:32:22
 */
public class LocationShareActivity extends BaseActivity implements OnGetGeoCoderResultListener, 
	BaiduMap.OnMapLoadedCallback, BaiduMap.SnapshotReadyCallback {
	
	/**
	 * 刷新菜单按钮的消息
	 */
	private static final int MSG_REFRESH_MENU = 0x1;
	
	private BaiduMap mBaiduMap;

	// UI相关
	boolean isFirstLoc = true;// 是否首次定位
	
	/**
	 * 我的 位置的经纬度
	 */
	private LatLng myLatLng;
	
	/**
	 * 地图支持的fragment
	 */
	private SupportMapFragment mMapFragment;
	private ImageButton btnMyLocation;
	private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	// 定位相关
	private LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private List<LocationInfo> mLocationInfos = new ArrayList<>();
	private InfoWindow mInfoWindow;
	private Marker mMarker;
	private BitmapDescriptor mCurrentBitmap;
	
	/**
	 * 我的地理位置
	 */
	private BDLocation mLocation;

	private LocationAdapter mAdapter;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_REFRESH_MENU:	//刷新菜单按钮
				if (btnOpt != null && !btnOpt.isEnabled()) {
					btnOpt.setEnabled(true);
				}
				break;

			default:
				break;
			}
		}
	};
	
	private ListView lvData;
	
	private ProgressWheel pbLoading;
	
	/**
	 * 显示popup信息的view
	 */
	private TextView mPopuInfoView;
	
	/**
	 * 当前的地图是否加载完毕，只有当地图加载完毕后才可定位，否则会抛异常
	 */
	private boolean isMapLoaded = false;

	private TextView btnOpt;

	/**
	 * 当前选中的位置
	 */
	private int mCurrentPosition;

	@Override
	protected int getContentView() {
		return R.layout.activity_location_share;
	}

	@Override
	protected void initView() {
		if (mMapFragment == null) {
			mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		}
		lvData = (ListView) findViewById(R.id.lv_data);
		pbLoading = (ProgressWheel) findViewById(R.id.pb_loading);
		btnMyLocation = (ImageButton) findViewById(R.id.btn_my_location);
	}

	@Override
	protected void initData() {
		//初始化百度地图
		setUpMap();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.common_opt, menu);
		MenuItem menuDone = menu.findItem(R.id.action_select_complete);
		btnOpt = (TextView) MenuItemCompat.getActionView(menuDone);
		btnOpt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mBaiduMap.snapshot(LocationShareActivity.this);
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
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(final BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			drawLocation(location);
		}
	}
	
	/**
	 * 绘制地理位置信息
	 * @update 2015年2月7日 下午3:34:00
	 * @param location
	 */
	private void drawLocation(final BDLocation location) {
		if (!isMapLoaded) {
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					drawLocation(location);
				}
			}, 500);
		} else {
			if (location == null || mBaiduMap == null) {
				return;
			}
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				mHandler.sendEmptyMessage(MSG_REFRESH_MENU);	//刷新菜单按钮
				isFirstLoc = false;
				mLocation = location;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				myLatLng = ll;
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
				// 反Geo搜索
				mSearch.reverseGeoCode(new ReverseGeoCodeOption()
						.location(ll));
			}
		}
	}
	
	/**
	 * 设置map对象的相关参数
	 * @update 2015年1月5日 下午9:59:30
	 */
	private void setUpMap() {
		if (mBaiduMap == null) {
			mBaiduMap = mMapFragment.getBaiduMap();
		}
		
		MapView mapView = mMapFragment.getMapView();
		//去掉缩放控件
		mapView.showZoomControls(false);

		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		
		//地图加载完毕的监听器
		mBaiduMap.setOnMapLoadedCallback(this);
		
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(18));
		
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		//设置定位选项
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(2000);	//定位的间隔时间,单位毫秒
		option.setIsNeedAddress(true);
		mLocClient.setLocOption(option);
		mLocClient.start();
	}

	@Override
	protected void addListener() {
		btnMyLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(myLatLng);
				mBaiduMap.animateMapStatus(u);
			}
		});
		
		mBaiduMap.setOnMyLocationClickListener(new BaiduMap.OnMyLocationClickListener() {
			
			@Override
			public boolean onMyLocationClick() {
				if (mLocation != null) {
					if (mInfoWindow != null) {
						mBaiduMap.hideInfoWindow();
					}
					showInfoWindow("[我的位置]\r\n" + mLocation.getAddrStr(), myLatLng);
					return true;
				}
				return false;
			}
		});
		
		mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				showInfoWindow(marker.getTitle(), marker.getPosition());
				return true;
			}
		});
		lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position != 0 && position != mCurrentPosition) {
					mCurrentPosition = position;
					LocationInfo locationInfo = mLocationInfos.get(position);
					LatLng location = new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude());
					if (mCurrentBitmap == null) {
						mCurrentBitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_markers);
					}
					OverlayOptions ooA = new MarkerOptions().position(location).icon(mCurrentBitmap)
							.zIndex(9);
					mBaiduMap.clear();
					mMarker = (Marker) mBaiduMap.addOverlay(ooA);
					mMarker.setTitle(locationInfo.getAddress());
					mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(location));
				}
			}
		});
	}
	
	/**
	 * 显示popupwindow信息
	 * @update 2015年2月7日 下午5:04:36
	 * @param msg
	 * @param latLng
	 */
	private void showInfoWindow(String msg, LatLng latLng) {
		if (mPopuInfoView == null) {
			mPopuInfoView = new TextView(getApplicationContext());
			mPopuInfoView.setBackgroundResource(R.drawable.location_popu_info_bg_normal);
			mPopuInfoView.setTextColor(Color.WHITE);
			mPopuInfoView.setText(msg);
		}
		mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(mPopuInfoView), latLng, -40, new InfoWindow.OnInfoWindowClickListener() {
			
			@Override
			public void onInfoWindowClick() {
				// TODO Auto-generated method stub
				mBaiduMap.hideInfoWindow();
			}
		});
		mBaiduMap.showInfoWindow(mInfoWindow);
	}

	@Override
	public void onMapLoaded() {
		isMapLoaded = true;
	}
	
	@Override
	protected void onDestroy() {
		if (mCurrentBitmap != null) {
			mCurrentBitmap.recycle();
		}
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		super.onDestroy();
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result != null && result.error == SearchResult.ERRORNO.NO_ERROR) {
			if (mLocationInfos.size() > 0) {
				mLocationInfos.clear();
			}
			LatLng location = result.getLocation();
			LocationInfo info = new LocationInfo(location.latitude, location.longitude, "[位置]" + result.getAddress());
			mLocationInfos.add(info);
			List<PoiInfo> pois = result.getPoiList();
			if (pois != null && pois.size() > 0) {
				for (PoiInfo poiInfo : pois) {
					location = poiInfo.location;
					info = new LocationInfo(location.latitude, location.longitude, poiInfo.address + poiInfo.name);
					mLocationInfos.add(info);
				}
			}
			if (mAdapter == null) {
				mAdapter = new LocationAdapter(mLocationInfos, getApplicationContext());
				lvData.setAdapter(mAdapter);
				lvData.setItemChecked(0, true);	//第一位默认选中
			} else {
				mAdapter.notifyDataSetChanged();
			}
			if (pbLoading.getVisibility() == View.VISIBLE) {
				pbLoading.setVisibility(View.GONE);
			}
		}
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

	@Override
	public void onSnapshotReady(Bitmap snapshot) {
		if (snapshot != null) {
			FileOutputStream fos = null;
			try {
				File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
				fos = new FileOutputStream(file);
				snapshot.compress(CompressFormat.JPEG, 20, fos);
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
				if (snapshot != null) {
					snapshot.recycle();
				}
				
			}
		}
	}
}
