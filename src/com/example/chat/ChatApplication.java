package com.example.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;

import com.example.chat.model.Emoji;
import com.example.chat.model.EmojiType;
import com.example.chat.model.Personal;
import com.example.chat.model.SystemConfig;
import com.example.chat.service.CoreService;
import com.example.chat.util.Constants;
import com.example.chat.util.SystemUtil;
import com.example.chat.util.XmppConnectionManager;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

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
	
	/**
	 * 当前的用户
	 */
	private static Personal currentUser = null;
	/**
	 * 当前用户的账号
	 */
	private static String currentAccount = null;
	
	/**
	 * 经典表情集合
	 */
	private static Map<String, Emoji> mEmojiMap = null;
	private static List<Emoji> mEmojis = null;
	/**
	 * 表情类型集合
	 */
	private static List<EmojiType> mEmojiTypes = null;
	public static int emojiTypeCount = 0;
	
	public static int emojiPageCount = 0;
	
	/**
	 * 每页显示的表情数量，不含删除按钮
	 */
	private static final int PAGE_SIZE = 20;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		preferences = getSharedPreferences(Constants.SETTTING_LOGIN, Context.MODE_PRIVATE);
		systemConfig = new SystemConfig();
		currentUser = new Personal();
		
		initSystemConfig();
		
		initImageLoaderConfig();

		initEmojiType();
		
		initEmoji();
		
	}

	public String getCurrentAccount() {
		return currentAccount;
	}

	public void setCurrentAccount(String currentAccount) {
		ChatApplication.currentAccount = currentAccount;
	}

	/**
	 * 初始化表情
	 * @update 2014年10月27日 上午11:20:53
	 */
	private void initEmoji() {
		if (mEmojiMap == null) {
			mEmojiMap = new HashMap<>();
		} else {
			mEmojiMap.clear();
		}
		if (mEmojis == null) {
			mEmojis = new ArrayList<>();
		} else {
			mEmojis.clear();
		}
		List<String> list = SystemUtil.getEmojiFromFile("emoji");
		//表情格式为“f_static_000,[微笑]”
		if (list != null && list.size() > 0) {
			for (String str : list) {
				String[] arr = str.split(",");
				String faceName = arr[0];
				String description = arr[1];
				int resId = SystemUtil.getRespurceIdByName(faceName);
				if (resId > 0) {
					Emoji emoji = new Emoji();
					emoji.setResId(resId);
					emoji.setFaceName(faceName);
					emoji.setDescription(description);
					mEmojis.add(emoji);
					mEmojiMap.put(description, emoji);
				}
			}
		}
		int emojiSize = mEmojis.size();
		if (emojiSize > 0) {
			//向上取整：Math.ceil(1.4)=2.0 
			emojiPageCount = (int) Math.ceil(emojiSize / 20 + 0.1);
		}
	}
	
	/**
	 * 初始化表情的类型
	 * @update 2014年10月27日 下午7:59:06
	 */
	private void initEmojiType() {
		if (mEmojiTypes == null) {
			mEmojiTypes = new ArrayList<>();
		} else {
			mEmojiTypes.clear();
		}
		//assets文件内容格式：emotionstore_emoji_icon,经典表情,1,最后一个字段是表情的操作类型，分为“显示表情”、“管理本地表情”、“添加表情”
		List<String> list = SystemUtil.getEmojiFromFile("emojiType");
		if (list != null && list.size() > 0) {
			for (String str : list) {
				String[] arr = str.split(",");
				String fileName = arr[0];
				String description = arr[1];
				int optType = Integer.parseInt(arr[2]);
				int resId = SystemUtil.getRespurceIdByName(fileName);
				if (resId > 0) {
					EmojiType emojiType = new EmojiType();
					emojiType.setResId(resId);
					emojiType.setFileName(fileName);
					emojiType.setDescription(description);
					emojiType.setOptType(optType);
					mEmojiTypes.add(emojiType);
				}
			}
			emojiTypeCount = mEmojiTypes.size();
		}
	}
	
	/**
	 * 获得所有的表情集合
	 * @update 2014年10月27日 下午3:02:25
	 * @return
	 */
	public static List<Emoji> getEmojis() {
		return mEmojis;
	}
	
	/**
	 * 获得所有的表情集合
	 * @update 2014年10月27日 下午3:02:25
	 * @return
	 */
	public static Map<String, Emoji> getEmojiMap() {
		return mEmojiMap;
	}
	
	public static List<EmojiType> geEmojiTypes() {
		return mEmojiTypes;
	}
	
	/**
	 * 根据当前页面的索引获得当前页面的所有表情
	 * @update 2014年10月27日 下午3:03:10
	 * @param position
	 * @return
	 */
	public static List<Emoji> getCurrentPageEmojis(int position) {
		int startIndex = position * PAGE_SIZE;
		int endIndex = startIndex + PAGE_SIZE;
		int emojiSize = mEmojiMap.size();
		if (endIndex > emojiSize) {
			endIndex = emojiSize;
		}
		List<Emoji> subList = new ArrayList<>();
		subList.addAll(mEmojis.subList(startIndex, endIndex));
		if (subList.size() < PAGE_SIZE) {	//最后一页不足20个时，就补充空的占位置
			for (int i = subList.size(); i < PAGE_SIZE; i++) {
				Emoji emoji = new Emoji();
				emoji.setResTpe(Emoji.TYPE_EMPTY);
				subList.add(emoji);
			}
		}
		if (subList.size() == PAGE_SIZE) {
			Emoji emoji = new Emoji();
			emoji.setResId(R.drawable.chat_emoji_del_selector);
			emoji.setResTpe(Emoji.TYPE_DEL);
			subList.add(emoji);
		}
		return subList;
	}

	/**
	 * 配置图片加载的工具
	 * @update 2014年10月24日 上午11:08:16
	 */
	private void initImageLoaderConfig() {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
					.memoryCacheExtraOptions(480, 800)
			        .diskCacheExtraOptions(480, 800, null)
			        .denyCacheImageMultipleSizesInMemory()	//同一个imageUri只允许在内存中有一个缓存的bitmap
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
