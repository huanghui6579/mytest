package com.example.chat.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.example.chat.activity.CommonAdapter;
import com.example.chat.model.MsgThread;
import com.example.chat.model.User;
import com.example.chat.model.UserVcard;
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
	
	/**
	 * 是否需要listview重设置adapter,一般用在fragment的stop后载onresume时需要
	 */
	private boolean resetAdapter = false;
	
	@Override
	public void onStop() {
		resetAdapter = true;
		super.onStop();
	}

	/**
	 * 会话集合
	 */
	private List<MsgThread> mMsgThreads = new ArrayList<>();
	private MsgThreadAdapter mThreadAdapter;
	
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
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}
		});
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	/**
	 * 会话列表的适配器
	 * @author huanghui1
	 * @update 2014年10月31日 下午9:18:43
	 */
	class MsgThreadAdapter extends CommonAdapter<MsgThread> {
		
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
			holder.tvContent.setText(msgThread.getSnippetContent());
			Bitmap icon = msgThread.getIcon();
			final User member = msgThread.getMembers().get(0);
			final UserVcard uCard = member.getUserVcard();
			String iconPath = uCard.getIconPath();
			if (icon != null) {
				holder.ivHeadIcon.setImageBitmap(icon);
			} else {
				if (TextUtils.isEmpty(iconPath)) {
					mImageLoader.displayImage(null, holder.ivHeadIcon, options);
				} else {
					mImageLoader.displayImage(Scheme.FILE.wrap(uCard.getIconPath()), holder.ivHeadIcon, options);
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
		}
		pbLoading.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<List<MsgThread>> loader) {
		mThreadAdapter.swapData(null);
	}
}
