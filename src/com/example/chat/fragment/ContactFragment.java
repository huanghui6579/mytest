package com.example.chat.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.activity.CommonAdapter;
import com.example.chat.activity.MainActivity.LazyLoadCallBack;
import com.example.chat.manage.UserManager;
import com.example.chat.model.User;
import com.example.chat.provider.Provider;
import com.example.chat.view.SideBar;
import com.example.chat.view.SideBar.OnTouchingLetterChangedListener;

/**
 * 好友列表界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月8日 下午7:44:40
 */
public class ContactFragment extends BaseFragment implements LazyLoadCallBack {
	
	private ListView lvContact;
	private TextView tvIndexDialog;
	private SideBar sideBar;
	private ProgressBar pbLoading;
	
	private ContactAdapter mAdapter;
	
	private List<User> users = new ArrayList<>();
	
	private UserManager userManager = UserManager.getInstance();
	
	private LoadDataBroadcastReceiver loadDataReceiver;
	
	/**
	 * 是否已经加载数据，该变量作为fragment初始化是否需要加载数据的依据
	 */
	private boolean isLoaded = false;
	
	public static String[] USER_PROJECTION = {
		Provider.UserColumns._ID,
		Provider.UserColumns.USERNAME,
		Provider.UserColumns.NICKNAME,
		Provider.UserColumns.EMAIL,
		Provider.UserColumns.PHONE,
		Provider.UserColumns.RESOURCE,
		Provider.UserColumns.MODE,
		Provider.UserColumns.STATUS,
		Provider.UserColumns.FULLPINYIN,
		Provider.UserColumns.SHORTPINYIN,
		Provider.UserColumns.SORTLETTER
	};
	
	/**
	 * 初始化fragment
	 * @update 2014年10月8日 下午10:07:58
	 * @return
	 */
	public static ContactFragment newInstance() {
		ContactFragment fragment = new ContactFragment();
		return fragment;
	}
	
	public boolean isLoaded() {
		return isLoaded;
	}

	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contact, container, false);
		
		lvContact = (ListView) view.findViewById(R.id.lv_contact);
		tvIndexDialog = (TextView) view.findViewById(R.id.tv_text_dialog);
		sideBar = (SideBar) view.findViewById(R.id.sidrbar);
		pbLoading = (ProgressBar) view.findViewById(R.id.pb_loading);
		
		sideBar.setTextView(tvIndexDialog);
		
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			
			@Override
			public void onTouchingLetterChanged(String s) {
				//该字母首次出现的位置
				int position = mAdapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					lvContact.setSelection(position);
				}
				
			}
		});
		return view;
	}
	
	/**
	 * 改变sideBar的显示和隐藏的状态
	 * @update 2014年10月13日 上午9:53:10
	 * @param flag
	 */
	public void setHideSideBar(boolean flag) {
		if (sideBar != null && tvIndexDialog != null) {
			if (flag) {	//需要隐藏
				sideBar.setVisibility(View.GONE);
				tvIndexDialog.setVisibility(View.GONE);
			} else {
				sideBar.setVisibility(View.VISIBLE);
				tvIndexDialog.setVisibility(View.VISIBLE);
			}
		}
	}
	
	/**
	 * 初始化数据
	 * @update 2014年10月11日 下午8:43:34
	 */
	private void initData() {
		if (!isLoaded) {	//没有加载过数据
			if (mAdapter == null) {
				mAdapter = new ContactAdapter(users, mContext);
				lvContact.setAdapter(mAdapter);
			}
			
			new LoadDataTask().execute();
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//注册加载好友列表的广播
		loadDataReceiver = new LoadDataBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(LoadDataBroadcastReceiver.ACTION_USER_LIST);
		mContext.registerReceiver(loadDataReceiver, filter);
		
		//初始化数据
//		initData();
		
	}
	
	@Override
	public void onDestroy() {
		mContext.unregisterReceiver(loadDataReceiver);
		super.onDestroy();
	}
	
	/**
	 * 异步加载数据的后台任务线程
	 * @author huanghui1
	 * @update 2014年10月23日 下午2:13:48
	 */
	class LoadDataTask extends AsyncTask<Void, Void, List<User>> {

		@Override
		protected List<User> doInBackground(Void... params) {
			List<User> list = userManager.getFriends();
			if (list != null && list.size() > 0) {
				if (users != null && users.size() > 0) {
					users.clear();
				}
				users.addAll(list);
			}
			return users;
		}
		
		@Override
		protected void onPostExecute(List<User> result) {
			pbLoading.setVisibility(View.GONE);
			mAdapter.notifyDataSetChanged();
			isLoaded = true;
		}
		
	}
	
	/**
	 * 联系人适配器
	 * @author huanghui1
	 * @update 2014年10月11日 下午10:10:14
	 */
	class ContactAdapter extends CommonAdapter<User> implements SectionIndexer {

		public ContactAdapter(List<User> list, Context context) {
			super(list, context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_contact, parent, false);
				
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tvCatalog = (TextView) convertView.findViewById(R.id.tv_catalog);
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_head_icon);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final User user = list.get(position);
			holder.tvName.setText(user.getName());
			
			//根据position获取分类的首字母的Char ascii值
			int section = getSectionForPosition(position);
			if (position == getPositionForSection(section)) {
				holder.tvCatalog.setVisibility(View.VISIBLE);
				holder.tvCatalog.setText(user.getSortLetter());
			} else {
				holder.tvCatalog.setVisibility(View.GONE);
			}
			
			return convertView;
		}

		@Override
		public Object[] getSections() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getPositionForSection(int sectionIndex) {
			for (int i = 0; i < getCount(); i++) {
				String sortStr = list.get(i).getSortLetter();
				char fisrtChar = sortStr.charAt(0);
				if (fisrtChar == sectionIndex) {
					return i;
				}
			}
			return -1;
		}

		/*
		 * 根据ListView的当前位置获取分类的首字母的Char ascii值
		 */
		@Override
		public int getSectionForPosition(int position) {
			return list.get(position).getSortLetter().charAt(0);
		}
		
	}
	
	final class ViewHolder {
		TextView tvCatalog;
		TextView tvName;
		ImageView ivIcon;
	}
	
	/**
	 * 加载数据完成后的广播
	 * @author huanghui1
	 * @update 2014年10月23日 下午3:39:25
	 */
	public class LoadDataBroadcastReceiver extends BroadcastReceiver {
		public static final String ACTION_USER_LIST = "com.example.chat.USER_LIST_RECEIVER";
		public static final String ACTION_USER_INFOS = "com.example.chat.USER_INFOS_RECEIVER";

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent != null) {
				String action = intent.getAction();
				switch (action) {
				case ACTION_USER_LIST:	//更新好友列表
				case ACTION_USER_INFOS:	//从网上更新好友列表信息到本地数据库
					if (isLoaded) {	//只有已经加载过数据并在界面上显示了才相应service发过来的广播
						new LoadDataTask().execute();
					}
					break;
				
				default:
					break;
				}
			}
		}
	}

	@Override
	public void onload() {
		// TODO Auto-generated method stub
		initData();
	}
}
