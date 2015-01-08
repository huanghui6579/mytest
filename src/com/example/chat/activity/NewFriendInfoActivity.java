package com.example.chat.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NotConnectedException;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.fragment.ContactFragment.LoadDataBroadcastReceiver;
import com.example.chat.loader.NewFriendInfoLoader;
import com.example.chat.manage.MsgManager;
import com.example.chat.manage.UserManager;
import com.example.chat.model.NewFriendInfo;
import com.example.chat.model.NewFriendInfo.FriendStatus;
import com.example.chat.model.User;
import com.example.chat.model.UserVcard;
import com.example.chat.provider.Provider;
import com.example.chat.util.Constants;
import com.example.chat.util.SystemUtil;
import com.example.chat.util.XmppConnectionManager;
import com.example.chat.util.XmppUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;

/**
 * 新的朋友信息列表
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年11月10日 下午3:34:41
 */
public class NewFriendInfoActivity extends BaseActivity implements LoaderCallbacks<List<NewFriendInfo>> {
	
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	
	private ListView lvNewInfos;
	private View emptyView;
	private ProgressBar pbLoading;
	
	private List<NewFriendInfo> mNewInfos = new ArrayList<>();
	private NewFriendAdapter mNewFriendAdapter;
	
	private UserManager userManager = UserManager.getInstance();
	private MsgManager msgManager = MsgManager.getInstance();
	
	ProgressDialog pDialog;
	
	/**
	 * 更新数据库后是否自动刷新，只有删除才不会自动刷新
	 */
	private boolean autoRefresh = true;
	
	private Handler mHandler = new MyHandler();
	
	@Override
	protected int getContentView() {
		return R.layout.activity_new_friend_info_list;
	}

	@Override
	protected void initView() {
		lvNewInfos = (ListView) findViewById(R.id.lv_new_friend_info);
		emptyView = findViewById(R.id.empty_view);
		pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
	}

	@Override
	protected void initData() {
		registerContentOberver();
		getSupportLoaderManager().initLoader(0, null, this);
		
		registerForContextMenu(lvNewInfos);
	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public Loader<List<NewFriendInfo>> onCreateLoader(int id, Bundle args) {
		return new NewFriendInfoLoader(mContext);
	}
	
	/**
	 * 注册观察者
	 * @update 2014年11月7日 下午10:05:30
	 */
	private void registerContentOberver() {
		NewFriendInfoContentObserver newFriendObserver = new NewFriendInfoContentObserver(mHandler);
		mContext.getContentResolver().registerContentObserver(Provider.NewFriendColumns.CONTENT_URI, true, newFriendObserver);
	}

	@Override
	public void onLoadFinished(Loader<List<NewFriendInfo>> loader,
			List<NewFriendInfo> data) {
		if (mNewFriendAdapter == null) {
			mNewFriendAdapter = new NewFriendAdapter(mNewInfos, mContext);
			lvNewInfos.setAdapter(mNewFriendAdapter);
		}
		mNewInfos.clear();
		if (!SystemUtil.isEmpty(data)) {
			mNewInfos.addAll(data);
		}
		if (lvNewInfos.getEmptyView() == null) {
			lvNewInfos.setEmptyView(emptyView);
		}
		mNewFriendAdapter.notifyDataSetChanged();
		if (pbLoading.getVisibility() == View.VISIBLE) {
			pbLoading.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<NewFriendInfo>> loader) {
		if (mNewFriendAdapter != null) {
			mNewFriendAdapter.swapData(null);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.context_new_friend, menu);
		AdapterView.AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
		NewFriendInfo newInfo = mNewInfos.get(acmi.position);
		menu.setHeaderTitle(newInfo.getTitle());
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		NewFriendInfo newInfo = mNewInfos.get(menuInfo.position);
		switch (item.getItemId()) {
		case R.id.action_context_delete:	//删除
			autoRefresh = false;
			boolean flag = userManager.deleteNewFriendInfo(newInfo.getId());
			if (flag) {	//删除成功
				SystemUtil.deleteFile(newInfo.getIconPath());
				mNewInfos.remove(newInfo);
				mNewFriendAdapter.notifyDataSetChanged();
			} else {
				SystemUtil.makeShortToast(R.string.delete_failed);
			}
			autoRefresh = true;
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}
	
	/**
	 * 新的朋友信息适配器
	 * @author huanghui1
	 * @update 2014年11月10日 下午4:10:37
	 */
	class NewFriendAdapter extends CommonAdapter<NewFriendInfo> {
		DisplayImageOptions options = SystemUtil.getGeneralImageOptions();

		public NewFriendAdapter(List<NewFriendInfo> list, Context context) {
			super(list, context);
		}

		/**
		 * 包装数据
		 * @update 2014年11月10日 下午4:45:32
		 * @param data
		 */
		public void swapData(List<NewFriendInfo> data) {
			list.clear();
			if (data != null) {
				list.addAll(data);
			}
			notifyDataSetChanged();
		}
		
		@SuppressWarnings("deprecation")
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			NewInfoViewHolder holder = null;
			if (convertView == null) {
				holder = new NewInfoViewHolder();
				convertView = inflater.inflate(R.layout.item_new_friend_info, parent, false);
				
				holder.ivHeadIcon = (ImageView) convertView.findViewById(R.id.iv_head_icon);
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
				holder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
				holder.tvState = (TextView) convertView.findViewById(R.id.tv_state);
				
				convertView.setTag(holder);
			} else {
				holder = (NewInfoViewHolder) convertView.getTag();
			}
			final NewFriendInfo newInfo = list.get(position);
			
			User user = newInfo.getUser();
			String iconPath = null;
			if (user != null) {
				UserVcard uCard = user.getUserVcard();
				if (uCard != null) {
					iconPath = uCard.getIconPath();
				} else {
					iconPath = newInfo.getIconPath();
				}
			} else {
				iconPath = newInfo.getIconPath();
			}
			if (SystemUtil.isFileExists(iconPath)) {
				mImageLoader.displayImage(Scheme.FILE.wrap(iconPath), holder.ivHeadIcon, options);
			} else {
				mImageLoader.displayImage(null, holder.ivHeadIcon, options);
			}
			String title = newInfo.getTitle();
			holder.tvTitle.setText(title);
			holder.tvContent.setText(newInfo.getContent());
			FriendStatus friendStatus = newInfo.getFriendStatus();
			holder.tvState.setText(friendStatus.getTitle());
			switch (friendStatus) {
			case UNADD:	//还未添加，则显示“添加”的按钮样式
			case ACCEPT:	//别人请求添加自己为好友，则显示“接受”的样式
				holder.tvState.setTextColor(getResources().getColor(android.R.color.white));
				holder.tvState.setBackgroundResource(R.drawable.common_button_green_selector);
				holder.tvState.setClickable(true);
				holder.tvState.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//接受对方添加自己为好友,并且也将对方添加为自己的好友
						pDialog = ProgressDialog.show(context, null, getString(R.string.loading), false, true);
						SystemUtil.getCachedThreadPool().execute(new Runnable() {
							
							@Override
							public void run() {
								Message msg = mHandler.obtainMessage();
								AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
								try {
									String otherJid = SystemUtil.wrapJid(newInfo.getFrom());
									XmppUtil.acceptFriend(connection, otherJid);
									XmppUtil.addFriend(connection, otherJid);
									//添加该好友
									User user = XmppUtil.getUserEntry(connection, newInfo.getFrom());
									if (user != null) {
										UserVcard uCard = new UserVcard();
										uCard.setIconPath(newInfo.getIconPath());
										uCard.setIconHash(newInfo.getIconHash());
										user.setUserVcard(uCard);
										user = userManager.saveOrUpdateFriend(user);
										newInfo.setUser(user);
										newInfo.setFriendStatus(FriendStatus.ADDED);
										newInfo.setTitle(user.getUsername());
										newInfo.setContent(user.getName());
										userManager.updateNewFriendInfoState(newInfo);
										//通知好友列表更新好友
										Intent intent = new Intent(LoadDataBroadcastReceiver.ACTION_USER_LIST);
										sendBroadcast(intent);
										
										msg.what = Constants.MSG_SUCCESS;
										//更改状态
									} else {
										msg.what = Constants.MSG_FAILED;
									}
								} catch (NotConnectedException e) {
									msg.what = Constants.MSG_FAILED;
									e.printStackTrace();
								}
								mHandler.sendMessage(msg);
							}
						});
					}
				});
				break;
			default:
				holder.tvState.setTextColor(getResources().getColor(R.color.session_list_item_content));
				if (SystemUtil.getCurrentSDK() >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
					holder.tvState.setBackground(null);
				} else {
					holder.tvState.setBackgroundDrawable(null);
				}
				holder.tvState.setClickable(false);
				break;
			}
			return convertView;
		}
		
	}
	
	/**
	 * 重新加载数据
	 * @update 2014年11月7日 下午10:01:23
	 */
	private void reLoadData() {
		getSupportLoaderManager().restartLoader(0, null, this);
	}
	
	final class NewInfoViewHolder {
		ImageView ivHeadIcon;
		TextView tvTitle;
		TextView tvContent;
		TextView tvState;
	}
	
	/**
	 * 新的朋友信息的观察者
	 * @author huanghui1
	 * @update 2014年11月11日 下午3:49:06
	 */
	class NewFriendInfoContentObserver extends ContentObserver {

		public NewFriendInfoContentObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			if (autoRefresh) {
				reLoadData();
			}
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if (autoRefresh) {
				if (uri != null) {
					NewFriendInfo newInfo = userManager.getNewFriendInfoByUri(uri);
					if (newInfo != null) {
						if (mNewInfos.contains(newInfo)) {
							mNewInfos.remove(newInfo);
						}
						mNewInfos.add(newInfo);
						Collections.sort(mNewInfos, newInfo);
						mNewFriendAdapter.notifyDataSetChanged();
					} else {
						onChange(selfChange);
					}
				} else {
					onChange(selfChange);
				}
			}
		}
		
	}
	
	public class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			if (pDialog != null && pDialog.isShowing()) {
				pDialog.dismiss();
			}
			switch (msg.what) {
			case Constants.MSG_SUCCESS:	//操作成功
				SystemUtil.makeShortToast(R.string.add_success);
				break;
			case Constants.MSG_FAILED:	//操作失败
				SystemUtil.makeShortToast(R.string.add_failed);
				break;

			default:
				break;
			}
		}
		
	}
	
}
