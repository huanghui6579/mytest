package com.example.chat.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.activity.CommonAdapter;
import com.example.chat.activity.UserInfoActivity;
import com.example.chat.activity.MainActivity.LazyLoadCallBack;
import com.example.chat.manage.UserManager;
import com.example.chat.model.User;
import com.example.chat.model.UserVcard;
import com.example.chat.provider.Provider;
import com.example.chat.util.SystemUtil;
import com.example.chat.view.SideBar;
import com.example.chat.view.SideBar.OnTouchingLetterChangedListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;

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
		
//		View headView = LayoutInflater.from(mContext).inflate(R.layout.layout_contact_head, null);
//		lvContact.addHeaderView(headView, null, false);
		/*TextView headView = new TextView(mContext);
		headView.setText("头部");
		lvContact.addHeaderView(headView);*/
		
		lvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int type = mAdapter.getItemViewType(position);
				switch (type) {
				case ContactAdapter.TYPE_NEW_FRIEND:	//新的朋友
					SystemUtil.makeShortToast("选择的新朋友");
					break;
				case ContactAdapter.TYPE_GROUP_CHAT:	//群聊
					SystemUtil.makeShortToast("选择群聊");
					
					break;
				case ContactAdapter.TYPE_CONTACT:	//联系人列表
					User target = (User) mAdapter.getItem(position);
					Intent intent = new Intent(mContext, UserInfoActivity.class);
					intent.putExtra(UserInfoActivity.ARG_USER, target);
					intent.putExtra(UserInfoActivity.ARG_OPTION, UserInfoActivity.OPTION_LOAD);
					startActivity(intent);
					break;
				default:
					break;
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
		filter.addAction(LoadDataBroadcastReceiver.ACTION_USER_INFOS);
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
		//listview头部的特殊分类数量
		int headCount = 2;
		/**
		 * 新的好友
		 */
		private static final int TYPE_NEW_FRIEND = 0;
		/**
		 * 群聊
		 */
		private static final int TYPE_GROUP_CHAT = 1;
		/**
		 * 好友列表
		 */
		private static final int TYPE_CONTACT = 2;
		
		private ImageLoader imageLoader = ImageLoader.getInstance();
		
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.contact_head_icon_default)
				.showImageForEmptyUri(R.drawable.contact_head_icon_default)
				.showImageOnFail(R.drawable.contact_head_icon_default)
				.cacheInMemory(true)
				.cacheOnDisk(false)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.bitmapConfig(Bitmap.Config.RGB_565)	//防止内存溢出
				.displayer(new FadeInBitmapDisplayer(200))
				.build();

		public ContactAdapter(List<User> list, Context context) {
			super(list, context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			int type = getItemViewType(position);
			if (convertView == null) {
				holder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater.from(context);
				
				switch (type) {
				case TYPE_NEW_FRIEND:	//新的朋友
				case TYPE_GROUP_CHAT:	//群聊
					convertView = inflater.inflate(R.layout.layout_contact_head, parent, false);
					break;
				case TYPE_CONTACT:	//联系人
					convertView = inflater.inflate(R.layout.item_contact, parent, false);
					holder.tvCatalog = (TextView) convertView.findViewById(R.id.tv_catalog);
					break;
				}
				
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_head_icon);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			switch (type) {
			case TYPE_NEW_FRIEND:	//新的朋友
				holder.tvName.setText(R.string.contact_list_new_friend);
				holder.ivIcon.setImageResource(R.drawable.contact_new_friend);
				break;
			case TYPE_GROUP_CHAT:	//群聊
				holder.tvName.setText(R.string.contact_list_group_chat);
				holder.ivIcon.setImageResource(R.drawable.contact_group_chat);
				break;
			case TYPE_CONTACT:	//联系人
				final User user = (User) getItem(position);
				if (user != null) {
					String sortLetter = user.getSortLetter();
					final UserVcard userVcard = user.getUserVcard();
					holder.tvName.setText(user.getName());
					
					if (userVcard != null) {
						String iconPath = userVcard.getIconPath();
						if (SystemUtil.isFileExists(iconPath)) {
							imageLoader.displayImage(Scheme.FILE.wrap(iconPath), holder.ivIcon, options);
						} else {
							imageLoader.displayImage(Scheme.DRAWABLE.wrap(String.valueOf(R.drawable.contact_head_icon_default)), holder.ivIcon, options);
						}
					} else {
						imageLoader.displayImage(Scheme.DRAWABLE.wrap(String.valueOf(R.drawable.contact_head_icon_default)), holder.ivIcon, options);
					}
					
					//根据position获取分类的首字母的Char ascii值
					int section = getSectionForPosition(position);
					if (position == getPositionForSection(section)) {
						holder.tvCatalog.setVisibility(View.VISIBLE);
						holder.tvCatalog.setText(sortLetter);
					} else {
						holder.tvCatalog.setVisibility(View.GONE);
					}
				}
				
				break;
			}
			
			return convertView;
		}

		@Override
		public Object[] getSections() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Object getItem(int position) {
			if (position < headCount) {
				return null;
			} else {
				return list.get(position - headCount);
			}
		}
		
		@Override
		public int getItemViewType(int position) {
			int type = TYPE_CONTACT;
			switch (position) {
			case 0:	//新的朋友
				type = TYPE_NEW_FRIEND;
				break;
			case 1:	//群聊
				type = TYPE_GROUP_CHAT;
				break;
			default:
				type = TYPE_CONTACT;
				break;
			}
			return type;
		}

		@Override
		public int getViewTypeCount() {
			return headCount + 1;
		}

		@Override
		public int getCount() {
			return list.size() + headCount;
		}

		@Override
		public int getPositionForSection(int sectionIndex) {
			if (sectionIndex == SystemUtil.getContactListFirtSection()) {
				return 0;
			} else {
				for (int i = headCount; i < getCount(); i++) {
					String sortStr = list.get(i - headCount).getSortLetter();
					char fisrtChar = sortStr.charAt(0);
					if (fisrtChar == sectionIndex) {
						return i;
					}
				}
				return -1;
			}
		}

		/*
		 * 根据ListView的当前位置获取分类的首字母的Char ascii值
		 */
		@Override
		public int getSectionForPosition(int position) {
			if (position < headCount) {
				return SystemUtil.getContactListFirtSection();
			} else {
				return list.get(position - headCount).getSortLetter().charAt(0);
			}
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
