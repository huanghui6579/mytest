package com.example.chat.activity;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.jivesoftware.smack.packet.Presence;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;

import com.example.chat.R;
import com.example.chat.fragment.ContactFragment;
import com.example.chat.fragment.MineFragment;
import com.example.chat.fragment.SessionListFragment;
import com.example.chat.model.Personal;
import com.example.chat.model.SystemConfig;
import com.example.chat.service.CoreService;
import com.example.chat.service.CoreService.MainBinder;
import com.example.chat.util.Constants;
import com.example.chat.util.Log;
import com.example.chat.view.IconPagerAdapter;
import com.example.chat.view.IconTabPageIndicator;

/**
 * 系统主界面
 * @author huanghui1
 * @update 2014年10月8日 下午9:15:47
 */
public class MainActivity extends BaseActivity {
	private static final int FRAGMENT_SESSION_LIST = 0;
	private static final int FRAGMENT_CONTACT = 1;
	private static final int FRAGMENT_MINE = 2;
	
	private IconTabPageIndicator mPageIndicator;
	private ViewPager mViewPager;
	
	private static String[] CONTENT = null;
    private static int[] ICONS = new int[] {
    	R.drawable.main_fun_session_selector,
    	R.drawable.main_fun_contact_selector,
    	R.drawable.main_fun_mine_selector
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			MainBinder mBinder = (MainBinder) service;
			CoreService coreService = mBinder.getService();
			coreService.initCurrentUser(initCurrentUserInfo());
		}
	};
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		forceShowActionBarOverflowMenu();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
	    if(featureId == Window.FEATURE_ACTION_BAR && menu != null){
	        if(menu.getClass().getSimpleName().equals("MenuBuilder")){
	            try{
	                Method m = menu.getClass().getDeclaredMethod(
	                    "setOptionalIconsVisible", Boolean.TYPE);
	                m.setAccessible(true);
	                m.invoke(menu, true);
	            }
	            catch(NoSuchMethodException e){
	                Log.e(TAG, "onMenuOpened", e);
	            }
	            catch(Exception e){
	                throw new RuntimeException(e);
	            }
	        }
	    }
	    return super.onMenuOpened(featureId, menu);
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.action_add_friend:	//添加好友
			intent = new Intent(mContext, AddFriendActivity.class);
			startActivity(intent);
			break;
		case R.id.action_exit:	//退出
			application.exit();
			return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/** 
     * 强制显示 overflow menu 
     */  
    private void forceShowActionBarOverflowMenu() {  
        try {  
            ViewConfiguration config = ViewConfiguration.get(this);  
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");  
            if (menuKeyField != null) {  
                menuKeyField.setAccessible(true);  
                menuKeyField.setBoolean(config, false);  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }
    
    /**
     * 初始化当前用户的信息
     */
    private Personal initCurrentUserInfo() {
    	Personal temp = application.getCurrentUser();
    	SystemConfig sc = application.getSystemConfig();
    	temp.setUsername(sc.getAccount());
    	temp.setPassword(sc.getPassword());
    	temp.setStatus(Presence.Type.available.name());
    	temp.setMode(Presence.Mode.available.name());
    	temp.setResource(Constants.CLIENT_RESOURCE);
    	return temp;
    }

	@Override
	protected int getContentView() {
		return R.layout.activity_main;
	}

	@Override
	protected void initView() {
		mPageIndicator = (IconTabPageIndicator) findViewById(R.id.page_indicator);
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
	}

	@Override
	protected void initData() {
		CONTENT = getResources().getStringArray(R.array.main_function_lable);
		FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(adapter);
		mPageIndicator.setViewPager(mViewPager);
		
		Intent service = new Intent(mContext, CoreService.class);
		bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
		
		//从网络上更新好友列表的数据
		service.setFlags(CoreService.FLAG_SYNC_FRENDS);
		startService(service);
	}
	
	@Override
	protected void addListener() {
		// TODO Auto-generated method stub
		mPageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		unbindService(serviceConnection);
		super.onDestroy();
	}
	
	class FragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

		public FragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
			case FRAGMENT_SESSION_LIST:	//会话聊天列表
				fragment = SessionListFragment.newInstance();
				break;
			case FRAGMENT_CONTACT:	//好友列表
				fragment = ContactFragment.newInstance();
				break;
			case FRAGMENT_MINE:	//我
				fragment = MineFragment.newInstance();
				break;
			default:
				fragment = SessionListFragment.newInstance();
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return CONTENT.length;
		}

		@Override
		public int getIconResId(int index) {
			// TODO Auto-generated method stub
			return ICONS[index];
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return CONTENT[position % CONTENT.length];
		}
		
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
}
