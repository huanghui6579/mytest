package com.example.chat;

import java.util.LinkedList;

import com.example.chat.model.Personal;
import com.example.chat.model.SystemConfig;
import com.example.chat.service.CoreService;
import com.example.chat.util.Constants;
import com.example.chat.util.XmppConnectionManager;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;

/**
 * 应用程序入口
 * @author huanghui1
 *
 */
public class ChatApplication extends Application {
	private LinkedList<Activity> activities = new LinkedList<>();
	
	private static SystemConfig systemConfig;
	
	private static ChatApplication instance;
	
	private SharedPreferences preferences;
	
	private static Personal currentUser = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		
		preferences = getSharedPreferences(Constants.SETTTING_LOGIN, Context.MODE_PRIVATE);
		systemConfig = new SystemConfig();
		currentUser = new Personal();
		
		initSystemConfig();
		
		initImageLoaderConfig();
	}
	
	/**
	 * 配置图片加载的工具
	 * @update 2014年10月24日 上午11:08:16
	 */
	private void initImageLoaderConfig() {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
					.memoryCacheExtraOptions(480, 800) // default = device screen dimensions
			        .diskCacheExtraOptions(480, 800, null)
			        .denyCacheImageMultipleSizesInMemory()
			        .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
			        .memoryCacheSize(2 * 1024 * 1024)
			        .diskCacheSize(50 * 1024 * 1024)
			        .defaultDisplayImageOptions(getDefaultDisplayOptions())
			        .writeDebugLogs()
			        .build();
		ImageLoader.getInstance().init(config);
	}
	
	/**
	 * 获取图片加载默认的图片显示配置
	 * @update 2014年10月24日 上午11:17:14
	 * @return
	 */
	private DisplayImageOptions getDefaultDisplayOptions() {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.bitmapConfig(Bitmap.Config.RGB_565)	//防止内存溢出
				.displayer(new FadeInBitmapDisplayer(200))
				.build();
		return options;
	}
	
	/**
	 * 初始化系统配置
	 * @update 2014年10月9日 上午8:17:20
	 */
	private void initSystemConfig() {
		systemConfig.setAccount(preferences.getString(Constants.USER_ACCOUNT, null));
		systemConfig.setPassword(preferences.getString(Constants.USER_PASSWORD, null));
		systemConfig.setFirstLogin(preferences.getBoolean(Constants.USER_ISFIRST, true));
//		systemConfig.setResource(preferences.getString(Constants.USER_RESOURCE, SystemUtil.getPhoneModel()));
//		systemConfig.setHost(preferences.getString(Constants.NAME_SERVER_HOST, Constants.SERVER_HOST));
//		systemConfig.setPort(preferences.getInt(Constants.NAME_SERVER_PORT, Constants.SERVER_PORT));
//		systemConfig.setServerName(preferences.getString(Constants.NAME_SERVER_NAME, Constants.SERVER_NAME));
	}
	
	/**
	 * 保存系统配置信息
	 * @update 2014年10月9日 上午8:22:30
	 * @param config
	 */
	public void saveSystemConfig() {
		Editor editor = preferences.edit();
		editor.putString(Constants.USER_ACCOUNT, systemConfig.getAccount());
		editor.putString(Constants.USER_PASSWORD, systemConfig.getPassword());
		editor.putBoolean(Constants.USER_ISFIRST, systemConfig.isFirstLogin());
//		editor.putString(Constants.USER_RESOURCE, systemConfig.getResource());
//		editor.putString(Constants.NAME_SERVER_HOST, systemConfig.getHost());
//		editor.putString(Constants.NAME_SERVER_NAME, systemConfig.getServerName());
//		editor.putInt(Constants.NAME_SERVER_PORT, systemConfig.getPort());
		editor.commit();
	}
	
	/**
	 * systemconfig
	 * @update 2014年10月9日 上午8:17:49
	 * @return
	 */
	public SystemConfig getSystemConfig() {
		return systemConfig;
	}
	
	public Personal getCurrentUser() {
		return currentUser;
	}
	
	/**
	 * 获得全局的application
	 * @return 全局的application
	 */
	public static ChatApplication getInstance() {
		return instance;
	}
	
	/**
	 * 添加Activity的队列中，用于软件的退出
	 * @update 2014年10月8日 下午10:22:30
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		activities.add(activity);
	}
	
	/**
	 * 退出应用应用程序
	 * @update 2014年10月8日 下午10:30:04
	 */
	public void exit() {
		XmppConnectionManager.getInstance().disconnect();
		Intent intent = new Intent(instance, CoreService.class);
		stopService(intent);
		for(Activity activity : activities) {
			activity.finish();
		}
		System.exit(0);
	}
	
	/**
	 * 将Activity从队列中移除
	 * @update 2014年10月8日 下午10:23:15
	 * @param activity
	 */
	public void removeActivity(Activity activity) {
		activities.remove(activity);
	}
	
	/**
	 * 检查该好友是否是自己
	 * @update 2014年10月24日 下午5:21:33
	 * @param username
	 * @return
	 */
	public boolean isSelf(String username) {
		return username.equals(currentUser.getUsername());
	}
}
