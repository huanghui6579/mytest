package com.example.chat.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.activity.ChatActivity1;
import com.example.chat.activity.CommonAdapter;
import com.example.chat.manage.MsgManager;
import com.example.chat.model.MsgThread;
import com.example.chat.model.User;
import com.example.chat.model.UserVcard;
import com.example.chat.provider.Provider;
import com.example.chat.util.SystemUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;

/**
 * 聊天会话列表
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月8日 下午7:36:50
 */
public class ThreadListFragment extends BaseFragment implements LoaderCallbacks<List<MsgThread>> {
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	
	private ListView mListView;
	private ProgressBar pbLoading;
	private View emptyView;
	
	private MsgManager msgManager = MsgManager.getInstance();
	
	/**
	 * 是否需要listview重设置adapter,一般用在fragment的stop后载onresume时需要
	 */
	private boolean resetAdapter = false;
	
	/**
	 * 会话集合
	 */
	private List<MsgThread> mMsgThreads = new ArrayList<>();
	private MsgThreadAdapter mThreadAdapter;
	
	private Handler mHandler = new Handler();
	
	@Override
	public void onStop() {
		resetAdapter = true;
		super.onStop();
	}

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_list, container, false);
		mListView = (ListView) view.findViewById(R.id.lv_session);
		emptyView = view.findViewById(R.id.empty_view);
		pbLoading = (ProgressBar) view.findViewById(R.id.pb_loading);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//注册会话观察者
		registerContentOberver();
		
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MsgThread msgThread = mMsgThreads.get(position);
				Intent intent = new Intent(mContext, ChatActivity1.class);
				intent.putExtra(ChatActivity1.ARG_THREAD, msgThread);
				startActivity(intent);
			}
		});
		mThreadAdapter = new MsgThreadAdapter(mMsgThreads, mContext);
		mListView.setAdapter(mThreadAdapter);
		getLoaderManager().initLoader(0, null, this);
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
				
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_msg_thread, parent, false);
				
				holder.ivHeadIcon = (ImageView) convertView.findViewById(R.id.iv_head_icon);
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
				holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
				holder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
				
				convertView.setTag(holder);
			} else {
				holder = (MsgThreadViewHolder) convertView.getTag();
			}
			
			final MsgThread msgThread = list.get(position);
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
		if (!SystemUtil.isEmpty(data)) {
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
		}
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
			reLoadData();
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if (uri != null) {
				MsgThread thread = msgManager.getThreadByUri(uri);
				if (mMsgThreads.contains(thread)) {
					mMsgThreads.remove(thread);
				}
				mMsgThreads.add(thread);
				Collections.sort(mMsgThreads, thread);
				mThreadAdapter.notifyDataSetChanged();
			} else {
				onChange(selfChange);
			}
		}
		
	}
}
