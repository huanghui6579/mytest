package net.ibaixin.chat.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.ibaixin.chat.R;
import net.ibaixin.chat.activity.ChatActivity;
import net.ibaixin.chat.activity.CommonAdapter;
import net.ibaixin.chat.loader.ThreadListLoader;
import net.ibaixin.chat.model.MsgThread;
import net.ibaixin.chat.model.User;
import net.ibaixin.chat.model.UserVcard;
import net.ibaixin.chat.provider.Provider;
import net.ibaixin.chat.util.Constants;
import net.ibaixin.chat.util.SystemUtil;
import net.ibaixin.chat.view.ProgressDialog;
import net.ibaixin.chat.view.ProgressWheel;
import net.ibaixin.manage.MsgManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;

/**
 * 聊天会话列表
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月8日 下午7:36:50
 */
public class ThreadListFragment extends BaseFragment implements LoaderCallbacks<List<MsgThread>> {
	private static final int MENU_TOP = 0;
	private static final int MENU_DELETE = 0x1;
	
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	
	private ListView mListView;
	private ProgressWheel pbLoading;
	private View emptyView;
	
	private MsgManager msgManager = MsgManager.getInstance();
	
	/**
	 * 是否需要listview重设置adapter,一般用在fragment的stop后载onresume时需要
	 */
	private boolean resetAdapter = false;
	/**
	 * 是否自动刷新，当数据库数据发生变动时，只有删除时才不会自动刷新
	 */
	private boolean autoRefresh = true;
	
	private ProgressDialog pDialog;
	
	/**
	 * 会话集合
	 */
	private List<MsgThread> mMsgThreads = new ArrayList<>();
	private MsgThreadAdapter mThreadAdapter;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (pDialog != null && pDialog.isShowing()) {
				pDialog.dismiss();
			}
			switch (msg.what) {
			case Constants.MSG_SUCCESS:	//会话删除成功
				mThreadAdapter.notifyDataSetChanged();
				break;
			case Constants.MSG_FAILED:	//删除失败
				SystemUtil.makeShortToast(R.string.delete_failed);
				break;
			case Constants.MSG_THREAD_TOP_SUCCESS:	//会话置顶/取消置顶成功
				mThreadAdapter.notifyDataSetChanged();
				break;
			case Constants.MSG_THREAD_TOP_FAILED:	//会话置顶/取消置顶失败
				SystemUtil.makeShortToast(R.string.opt_failed);
				break;

			default:
				break;
			}
		}
	};
	
	/**
	 * 初始化fragment
	 * @update 2014年10月8日 下午10:09:08
	 * @return
	 */
	public static ThreadListFragment newInstance() {
		ThreadListFragment fragment = new ThreadListFragment();
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//注册会话观察者
		registerContentOberver();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_list, container, false);
		mListView = (ListView) view.findViewById(R.id.lv_session);
		emptyView = view.findViewById(R.id.empty_view);
		pbLoading = (ProgressWheel) view.findViewById(R.id.pb_loading);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MsgThread msgThread = mMsgThreads.get(position);
				Intent intent = new Intent(mContext, ChatActivity.class);
				intent.putExtra(ChatActivity.ARG_THREAD, msgThread);
				startActivity(intent);
			}
		});
		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final MsgThread thread = (MsgThread) mThreadAdapter.getItem(position);
				if (thread != null) {
					String[] menuArray = getResources().getStringArray(R.array.thread_list_context_menu);
					if (thread.isTop()) {	//已经置顶了，就取消置顶
						menuArray[0] = getString(R.string.thread_list_context_menu_top_cancel);
					}
					MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
					builder.title(thread.getMsgThreadName())
						.items(menuArray)
						.itemsCallback(new MaterialDialog.ListCallback() {
							
							@Override
							public void onSelection(MaterialDialog dialog, View itemView, int which,
									CharSequence text) {
								switch (which) {
								case MENU_TOP:	//置顶/取消置顶该聊天
									pDialog = ProgressDialog.show(mContext, null, getString(R.string.loading));
									SystemUtil.getCachedThreadPool().execute(new Runnable() {
										
										@Override
										public void run() {
											thread.setTop(!thread.isTop());
											boolean success = msgManager.updateMsgThreadTop(thread);
											Collections.sort(mMsgThreads, thread);
											if (success) {
												mHandler.sendEmptyMessage(Constants.MSG_THREAD_TOP_SUCCESS);
											} else {
												mHandler.sendEmptyMessage(Constants.MSG_THREAD_TOP_FAILED);
											}
										}
									});
									break;
								case MENU_DELETE:	//删除该聊天会话
									MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
									builder.title(R.string.prompt)
										.content(R.string.contact_list_content_delete_prompt, thread.getMsgThreadName())
										.positiveText(android.R.string.ok)
										.negativeText(android.R.string.cancel)
										.callback(new MaterialDialog.ButtonCallback() {

											@Override
											public void onPositive(
													MaterialDialog dialog) {
												pDialog = ProgressDialog.show(mContext, null, getString(R.string.loading));
												SystemUtil.getCachedThreadPool().execute(new Runnable() {
													
													@Override
													public void run() {
														boolean success = msgManager.deleteMsgThreadById(thread.getId());
														if (success) {	//
															mMsgThreads.remove(thread);
															mHandler.sendEmptyMessage(Constants.MSG_SUCCESS);
														} else {
															mHandler.sendEmptyMessage(Constants.MSG_FAILED);
														}
													}
												});
											}
											
										}).show();
									
									break;
								default:
									break;
								}
							}
						})
						.show();
				}
				return true;
			}
		});
		mThreadAdapter = new MsgThreadAdapter(mMsgThreads, mContext);
		mListView.setAdapter(mThreadAdapter);
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onStop() {
		resetAdapter = true;
		super.onStop();
	}
	
	@Override
	public void onDestroyView() {
		getLoaderManager().destroyLoader(0);
		super.onDestroyView();
	}
	
	/**
	 * 注册会话观察者
	 * @update 2014年11月7日 下午10:05:30
	 */
	private void registerContentOberver() {
		MsgThreadContentObserver msgContentObserver = new MsgThreadContentObserver(mHandler);
		mContext.getContentResolver().registerContentObserver(Provider.MsgThreadColumns.CONTENT_URI, true, msgContentObserver);
	}
	
	/**
	 * 会话列表的适配器
	 * @author huanghui1
	 * @update 2014年10月31日 下午9:18:43
	 */
	class MsgThreadAdapter extends CommonAdapter<MsgThread> {
		
		DisplayImageOptions options = SystemUtil.getGeneralImageOptions();
		
		public MsgThreadAdapter(List<MsgThread> list, Context context) {
			super(list, context);
		}
		
		/**
		 * 包装数据
		 * @update 2014年11月1日 上午10:56:41
		 * @param data
		 */
		public void swapData(List<MsgThread> data) {
			list.clear();
			if (data != null) {
				list.addAll(data);
			}
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MsgThreadViewHolder holder = null;
			if (convertView == null) {
				holder = new MsgThreadViewHolder();
				
				convertView = inflater.inflate(R.layout.item_msg_thread, parent, false);
				
				holder.itemThreadLayout = convertView.findViewById(R.id.item_thread_layout);
				holder.ivHeadIcon = (ImageView) convertView.findViewById(R.id.iv_head_icon);
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
				holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
				holder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
				
				convertView.setTag(holder);
			} else {
				holder = (MsgThreadViewHolder) convertView.getTag();
			}
			
			final MsgThread msgThread = list.get(position);
			if (msgThread.isTop()) {	//该会话已置顶
				holder.itemThreadLayout.setBackgroundColor(getResources().getColor(R.color.primary_light_color));
			} else {
				holder.itemThreadLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			}
			holder.tvTitle.setText(msgThread.getMsgThreadName());
			holder.tvTime.setText(SystemUtil.formatMsgThreadTime(msgThread.getModifyDate()));
			String snippetContent = msgThread.getSnippetContent();
			if (TextUtils.isEmpty(snippetContent)) {
				snippetContent = "";
			}
			holder.tvContent.setText(snippetContent);
			Bitmap icon = msgThread.getIcon();
			final User member = msgThread.getMembers().get(0);
			final UserVcard uCard = member.getUserVcard();
			String iconPath = uCard.getIconPath();
			if (icon != null) {
				holder.ivHeadIcon.setImageBitmap(icon);
			} else {
				if (SystemUtil.isFileExists(iconPath)) {
					mImageLoader.displayImage(Scheme.FILE.wrap(iconPath), holder.ivHeadIcon, options);
				} else {
					mImageLoader.displayImage(null, holder.ivHeadIcon, options);
				}
			}
			return convertView;
		}
		
	}
	
	final static class MsgThreadViewHolder {
		View itemThreadLayout;
		ImageView ivHeadIcon;
		TextView tvTime;
		TextView tvTitle;
		TextView tvContent;
	}
	
	@Override
	public Loader<List<MsgThread>> onCreateLoader(int id, Bundle args) {
		return new ThreadListLoader(mContext);
	}

	@Override
	public void onLoadFinished(Loader<List<MsgThread>> loader,
			List<MsgThread> data) {
		/*if (!SystemUtil.isEmpty(data)) {
			if (mThreadAdapter == null) {
				mMsgThreads.addAll(data);
				mThreadAdapter = new MsgThreadAdapter(mMsgThreads, mContext);
				mListView.setAdapter(mThreadAdapter);
				mListView.setEmptyView(emptyView);
			} else {
				if (resetAdapter) {
					mListView.setAdapter(mThreadAdapter);
					mListView.setEmptyView(emptyView);
				} else {
					mThreadAdapter.swapData(data);
				}
			}
		} else {
			mListView.setEmptyView(emptyView);
		}*/
		if (mThreadAdapter == null) {
			mThreadAdapter = new MsgThreadAdapter(mMsgThreads, mContext);
			mListView.setAdapter(mThreadAdapter);
		}
		mMsgThreads.clear();
		if (!SystemUtil.isEmpty(data)) {
			mMsgThreads.addAll(data);
			if (resetAdapter) {
				mListView.setAdapter(mThreadAdapter);
				mListView.setEmptyView(emptyView);
			}
		}
		if (mListView.getEmptyView() == null) {
			mListView.setEmptyView(emptyView);
		}
		mThreadAdapter.notifyDataSetChanged();
		pbLoading.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<List<MsgThread>> loader) {
		if (mThreadAdapter != null) {
			mThreadAdapter.swapData(null);
		}
	}
	
	/**
	 * 重新加载数据
	 * @update 2014年11月7日 下午10:01:23
	 */
	private void reLoadData() {
		getLoaderManager().restartLoader(0, null, this);
	}
	
	/**
	 * 会话的内容观察者
	 * @author huanghui1
	 * @update 2014年11月7日 下午9:39:06
	 */
	class MsgThreadContentObserver extends ContentObserver {

		public MsgThreadContentObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			if (autoRefresh) {
				reLoadData();
			}
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if (autoRefresh) {
				if (uri != null) {
					MsgThread thread = msgManager.getThreadByUri(uri);
					if (thread != null) {	//非删除行为
						if (mMsgThreads.contains(thread)) {
							mMsgThreads.remove(thread);
						}
						mMsgThreads.add(thread);
						Collections.sort(mMsgThreads, thread);
						mThreadAdapter.notifyDataSetChanged();
					} else {
						onChange(selfChange);
					}
				} else {
					onChange(selfChange);
				}
			}
		}
		
	}
}
