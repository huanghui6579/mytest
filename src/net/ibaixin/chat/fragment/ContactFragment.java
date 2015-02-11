package net.ibaixin.chat.fragment;

import java.util.ArrayList;
import java.util.List;

import net.ibaixin.chat.R;
import net.ibaixin.chat.activity.CommonAdapter;
import net.ibaixin.chat.activity.NewFriendInfoActivity;
import net.ibaixin.chat.activity.UserInfoActivity;
import net.ibaixin.chat.activity.MainActivity.LazyLoadCallBack;
import net.ibaixin.chat.model.User;
import net.ibaixin.chat.model.UserVcard;
import net.ibaixin.chat.provider.Provider;
import net.ibaixin.chat.util.Constants;
import net.ibaixin.chat.util.SystemUtil;
import net.ibaixin.chat.util.XmppConnectionManager;
import net.ibaixin.chat.util.XmppUtil;
import net.ibaixin.chat.view.MyAlertDialogFragment;
import net.ibaixin.chat.view.ProgressWheel;
import net.ibaixin.chat.view.SideBar;
import net.ibaixin.chat.view.SideBar.OnTouchingLetterChangedListener;
import net.ibaixin.manage.UserManager;

import org.jivesoftware.smack.AbstractXMPPConnection;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

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
	private ProgressWheel pbLoading;
	
	private ContactAdapter mAdapter;
	
	private List<User> mUsers = new ArrayList<>();
	
	private UserManager userManager = UserManager.getInstance();
	
	private LoadDataBroadcastReceiver loadDataReceiver;
	
	/**
	 * 是否已经加载数据，该变量作为fragment初始化是否需要加载数据的依据
	 */
	private boolean isLoaded = false;
	
	/**
	 * 当数据库数据发生变化时，是否自动刷新该列表界面，只有删除时才不自动刷新
	 */
	private boolean autoRefresh = true;
	
	ProgressDialog pDialog;
	
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
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (pDialog != null && pDialog.isShowing()) {
				pDialog.dismiss();
			}
			autoRefresh = true;
			switch (msg.what) {
			case Constants.MSG_SUCCESS:	//操作成功
				mAdapter.notifyDataSetChanged();
				SystemUtil.makeShortToast(R.string.delete_success);
				break;
			case Constants.MSG_FAILED:	//操作失败
				SystemUtil.makeShortToast(R.string.delete_failed);
				break;

			default:
				break;
			}
		}
		
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
		pbLoading = (ProgressWheel) view.findViewById(R.id.pb_loading);
		
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
				Intent intent = null;
				switch (type) {
				case ContactAdapter.TYPE_NEW_FRIEND:	//新的朋友
					intent = new Intent(mContext, NewFriendInfoActivity.class);
					startActivity(intent);
					break;
				case ContactAdapter.TYPE_GROUP_CHAT:	//群聊
					SystemUtil.makeShortToast("选择群聊");
					
					break;
				case ContactAdapter.TYPE_CONTACT:	//联系人列表
					User target = (User) mAdapter.getItem(position);
					intent = new Intent(mContext, UserInfoActivity.class);
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
		User user = (User) mAdapter.getItem(acmi.position);
		if (user != null) {	//选中的是好友
			MenuInflater menuInflater = getActivity().getMenuInflater();
			menuInflater.inflate(R.menu.context_contacts, menu);
			menu.setHeaderTitle(user.getName());
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterView.AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		final User user = (User) mAdapter.getItem(acmi.position);
		if (user != null) {
			switch (item.getItemId()) {
			case R.id.action_context_set_nickname:	//设置备注
				
				return true;
			case R.id.action_context_delete:	//删除
				MyAlertDialogFragment alertDialogFragment = (MyAlertDialogFragment) new MyAlertDialogFragment.Builder()
					.setTitle(getString(R.string.prompt))
					.setMessage(getString(R.string.contact_list_content_delete_prompt, user.getName()))
					.setPositiveButtonText(getString(android.R.string.ok))
					.setNegativeButtonText(getString(android.R.string.cancel))
					.setPositiveButtonListener(new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							pDialog = ProgressDialog.show(mContext, null, getString(R.string.loading), false, true);
							
							SystemUtil.getCachedThreadPool().execute(new Runnable() {
								
								@Override
								public void run() {
									AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
									//发送删除好友的信息
									boolean success = XmppUtil.deleteUser(connection, user.getUsername());
									if (success) {
										autoRefresh = false;
										//删除好友
										success = userManager.deleteUser(user);
										//是否成功删除该好友
										if (success) {
											mUsers.remove(user);
											mHandler.sendEmptyMessage(Constants.MSG_SUCCESS);
										} else {
											mHandler.sendEmptyMessage(Constants.MSG_FAILED);
										}
									} else {
										mHandler.sendEmptyMessage(Constants.MSG_FAILED);
									}
								}
							});
						}
					})
					.setNegativeButtonListener(null)
					.create();
				alertDialogFragment.show(getFragmentManager(), alertDialogFragment.getClass().getCanonicalName());
				return true;

			default:
				return super.onContextItemSelected(item);
			}
		} else {
			return super.onContextItemSelected(item);
		}
		
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
				mAdapter = new ContactAdapter(mUsers, mContext);
				lvContact.setAdapter(mAdapter);
			}
			
			new LoadDataTask().execute();
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		registerForContextMenu(lvContact);
		
		//注册加载好友列表的广播
		loadDataReceiver = new LoadDataBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(LoadDataBroadcastReceiver.ACTION_USER_LIST);
		filter.addAction(LoadDataBroadcastReceiver.ACTION_USER_INFOS);
		filter.addAction(LoadDataBroadcastReceiver.ACTION_USER_ADD);
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
				if (mUsers != null && mUsers.size() > 0) {
					mUsers.clear();
				}
				mUsers.addAll(list);
			}
			return mUsers;
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
		public static final String ACTION_USER_LIST = "net.ibaixin.chat.USER_LIST_RECEIVER";
		public static final String ACTION_USER_INFOS = "net.ibaixin.chat.USER_INFOS_RECEIVER";
		public static final String ACTION_USER_ADD = "net.ibaixin.chat.USER_ADD_RECEIVER";

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent != null) {
				String action = intent.getAction();
				switch (action) {
				case ACTION_USER_LIST:	//更新好友列表
				case ACTION_USER_INFOS:	//从网上更新好友列表信息到本地数据库
					if (isLoaded) {	//只有已经加载过数据并在界面上显示了才响应service发过来的广播
						new LoadDataTask().execute();
					}
					break;
				case ACTION_USER_ADD:	//列表中添加一个好友信息
					User user = intent.getParcelableExtra(UserInfoActivity.ARG_USER);
					if (user != null) {
						mUsers.add(user);
						if (mAdapter == null) {
							mAdapter= new ContactAdapter(mUsers, mContext);
							lvContact.setAdapter(mAdapter);
						} else {
							mAdapter.notifyDataSetChanged();
						}
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
