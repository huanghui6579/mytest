package com.example.chat.activity;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NotConnectedException;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.example.chat.R;
import com.example.chat.fragment.ContactFragment;
import com.example.chat.fragment.MineFragment;
import com.example.chat.fragment.SessionListFragment;
import com.example.chat.util.XmppConnectionManager;
import com.example.chat.view.IconPagerAdapter;
import com.example.chat.view.IconTabPageIndicator;

/**
 * 系统主界面
 * @author huanghui1
 * @update 2014年10月8日 下午9:15:47
 */
public class MainActivity extends BaseActivity {
	private IconTabPageIndicator mPageIndicator;
	private ViewPager mViewPager;
	
	private static String[] CONTENT = null;
    private static int[] ICONS = new int[] {
    	R.drawable.main_fun_session_selector,
    	R.drawable.main_fun_contact_selector,
    	R.drawable.main_fun_mine_selector
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_exit) {
			application.exit();
			return true;
		}
		return super.onOptionsItemSelected(item);
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
	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub
		
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
	
	class FragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

		public FragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
			case 0:	//会话聊天列表
				fragment = SessionListFragment.newInstance();
				break;
			case 1:	//好友列表
				fragment = ContactFragment.newInstance();
				break;
			case 2:	//我
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
