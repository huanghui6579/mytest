package net.ibaixin.chat.activity;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import net.ibaixin.chat.ChatApplication;
import net.ibaixin.chat.R;
import net.ibaixin.chat.fragment.JokeFragment;
import net.ibaixin.chat.util.Constants;
import net.ibaixin.chat.util.Log;
import net.ibaixin.chat.util.StreamTool;
import net.ibaixin.chat.util.SystemUtil;
import net.ibaixin.chat.view.ProgressDialog;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

/**
 * 所有Activity的父类
 * @author huanghui1
 *
 */
public abstract class BaseActivity extends ActionBarActivity {
	protected Context mContext;
	protected SharedPreferences preferences;
	protected ChatApplication application;
	
	/**
	 * ActionBar是否允许有返回按钮
	 */
	private boolean homeAsUpEnabled = true;
	
	/**
	 * 是否设置为全屏
	 */
	private boolean mIsFullScreen = false;
	
	protected static String TAG = null; 
	
	protected Toolbar toolbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		TAG = this.getClass().getCanonicalName();
		
		application = ChatApplication.getInstance();
		
		mIsFullScreen = isFullScreen();

		initWidow();
		
		super.onCreate(savedInstanceState);
		
		homeAsUpEnabled = isHomeAsUpEnabled();
		
		application.addActivity(this);
		
		preferences = getSharedPreferences(Constants.SETTTING_LOGIN, Context.MODE_PRIVATE);
		
		setContentView(getContentView());
		
		initToolBar();
		
		forceShowActionBarOverflowMenu();
		
		initView();
		
		initData();
		
		addListener();
		
//		startSupportActionMode(new ActionModeCallback());
	}
	
	class ActionModeCallback implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/** 
     * 强制显示 overflow menu 
     */  
    protected void forceShowActionBarOverflowMenu() {  
        try {  
            ViewConfiguration config = ViewConfiguration.get(this);  
            if (ViewConfigurationCompat.hasPermanentMenuKey(config)) {
            	Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");  
                if (menuKeyField != null) {  
                    menuKeyField.setAccessible(true);  
                    menuKeyField.setBoolean(config, false);  
                }
            }
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }
	
	/**
	 * 是否有允许ActionBar左上角显示返回按钮
	 * @update 2014年11月10日 下午3:48:11
	 * @return
	 */
	protected boolean isHomeAsUpEnabled() {
		return homeAsUpEnabled;
	}
	
	/**
	 * 是否设置为全屏
	 * @update 2015年3月4日 下午7:41:21
	 * @return
	 */
	protected boolean isFullScreen() {
		return mIsFullScreen;
	}
	
	/**
	 * 开启沉浸模式
	 * @author tiger
	 * @update 2015年3月3日 下午11:09:34
	 * @param view
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	protected void hideSystemUi(View view) {
		int uiOptions = view.getSystemUiVisibility();
		if (SystemUtil.hasSDK16()) {
			uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
		}
		if (SystemUtil.hasSDK19()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}
		view.setSystemUiVisibility(uiOptions);
		//开启全屏模式
//		view.setSystemUiVisibility(
//	        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//	        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//	        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//	        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}
	
	/**
	 * 取消沉浸模式
	 * @author tiger
	 * @update 2015年3月3日 下午11:10:01
	 * @param view
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void showSystemUi(View view) {
		if (SystemUtil.hasSDK16()) {
			view.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}
	}
	
	/**
	 * 初始化一些窗口信息
	 * @update 2014年10月10日 下午9:29:18
	 */
	protected void initWidow() {
		if (mIsFullScreen) {
			if (SystemUtil.hasSDK19()) {
				View view = getWindow().getDecorView();
				if (view != null) {
					hideSystemUi(view);
				} else {
					//全屏模式
					getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
			} else {
				//全屏模式
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		}
		/*ActionBar actionBar = getSupportActionBar();
		if (homeAsUpEnabled) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		} else {
			actionBar.setDisplayHomeAsUpEnabled(false);
		}*/
	}
	
	/**
	 * 初始化ToolBar
	 * @update 2015年1月21日 上午10:04:23
	 */
	protected void initToolBar() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			if (homeAsUpEnabled) {
				actionBar.setDisplayHomeAsUpEnabled(true);
			} else {
				actionBar.setDisplayHomeAsUpEnabled(false);
			}
		}/* else {
			SystemUtil.makeShortToast("没有ActionBar或者ToolBar");
		}*/
	}
	
	/**
	 * 获得界面的布局文件id
	 * @return 布局文件id
	 */
	protected abstract int getContentView();
	
	/**
	 * 初始化界面
	 */
	protected abstract void initView();
	
	/**
	 * 初始化数据
	 * @update 2015年1月5日 下午9:34:01
	 */
	protected abstract void initData();
	
	/**
	 * 为控件注册监听器
	 */
	protected abstract void addListener();
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:	//返回
			beforeBack();
			finish();
			return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 在按下左上角的返回按钮时，可以处理的操作，该方法在finish()前调用
	 * @update 2015年1月9日 上午9:41:44
	 */
	protected void beforeBack() {
		
	}
	
	/**
	 * 隐藏dialog
	 * @update 2014年10月10日 上午8:10:47
	 * @param pDialog
	 */
	public void hideLoadingDialog(ProgressDialog pDialog) {
		if(pDialog != null && pDialog.isShowing()) {
			pDialog.dismiss();
		}
	}
	
	@Override
	protected void onDestroy() {
		application.removeActivity(this);
		super.onDestroy();
	}
	

	/**
	 * 登录web服务器
	 * 	add by dudejin 2015-03-05
	 * @return
	 */
	public boolean loginIbaixinJoke() {
		byte[] data = null;
		String json ="";
			try {
				String urlstr = JokeFragment.loginUrl+"?loginName="+application.getSystemConfig().getAccount()+"&loginPassword="+application.getSystemConfig().getPassword() ;
				URL url=new URL(urlstr);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(30000);
				conn.setConnectTimeout(10000);
				conn.setRequestMethod("POST");
				InputStream inStream = conn.getInputStream();
				data = StreamTool.readInputStream(inStream);
				json = new String(data);
				if("Y".equals(json)){
					// 取得sessionid.
					String cookieval = conn.getHeaderField("Set-Cookie");
					if(cookieval != null) {
						Constants.WEB_COOKIE = cookieval/*.substring(0, cookieval.indexOf(";"))*/;
						return true ;
					}
				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		return false;
	}
	/**
	 * 注册web服务器
	 * 	add by dudejin 2015-03-06
	 * @return
	 */
	public boolean registerIbaixinJoke(String nickname) {
		byte[] data = null;
		String json ="";
		try {
			String urlstr = JokeFragment.registerUrl+"?loginName="
					+application.getSystemConfig().getAccount()
					+"&loginPassword="+application.getSystemConfig().getPassword()
					+"&realName="+URLEncoder.encode(nickname,"utf-8") ;
			URL url=new URL(urlstr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(30000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			InputStream inStream = conn.getInputStream();
			data = StreamTool.readInputStream(inStream);
			json = new String(data);
			if("Y".equals(json)){
				// 取得sessionid.
				String cookieval = conn.getHeaderField("Set-Cookie");
				if(cookieval != null) {
					Constants.WEB_COOKIE = cookieval/*.substring(0, cookieval.indexOf(";"))*/;
					return true ;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return false;
	}
}
