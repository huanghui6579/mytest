package com.example.chat.fragment;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.activity.CommonAdapter;
import com.example.chat.manage.MsgManager;
import com.example.chat.model.MsgThread;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 聊天会话列表
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月8日 下午7:36:50
 */
public class ThreadListFragment extends BaseFragment implements LoaderCallbacks<List<MsgThread>> {
	private MsgManager msgManager = MsgManager.getInstance();
	
	private ListView mListView;
	
	private ArrayAdapter<String> mAdapter;
	
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
		mAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, Arrays.asList("Adsfsf", "dsafddsfdsf", "fdsfsdfgd", "范德萨发的说法", "fdsfdsfdsf", "发生的发生的范德萨"));
		mListView.setAdapter(mAdapter);
	}
	
	/**
	 * 会话列表的适配器
	 * @author huanghui1
	 * @update 2014年10月31日 下午9:18:43
	 */
	class MsgThreadAdapter extends CommonAdapter<MsgThread> {
		private ImageLoader imageLoader = ImageLoader.getInstance();

		public MsgThreadAdapter(List<MsgThread> list, Context context) {
			super(list, context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MsgThreadViewHolder holder = null;
			if (convertView == null) {
				holder = new MsgThreadViewHolder();
				
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_msg_thread, parent, false);
				
				holder.ivHeadIcon = (ImageView) convertView.findViewById(R.id.iv_head_icon);
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_time);
				holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
				holder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
				
				convertView.setTag(holder);
			} else {
				holder = (MsgThreadViewHolder) convertView.getTag();
			}
			
			final MsgThread msgThread = list.get(position);
			holder.tvTitle.setText(msgThread.getMsgThreadName());
			return convertView;
		}
		
	}
	
	final static class MsgThreadViewHolder {
		ImageView ivHeadIcon;
		TextView tvTime;
		TextView tvTitle;
		TextView tvContent;
	}
	
	/**
	 * 会话列表加载的后台任务
	 * @author huanghui1
	 * @update 2014年10月31日 下午8:59:03
	 */
	class ThreadListLoader extends AsyncTaskLoader<List<MsgThread>> {

		public ThreadListLoader(Context context) {
			super(context);
		}

		@Override
		public List<MsgThread> loadInBackground() {
			List<MsgThread> list = msgManager.getMsgThreadList();
			return list;
		}
		
	}

	@Override
	public Loader<List<MsgThread>> onCreateLoader(int id, Bundle args) {
		return new ThreadListLoader(mContext);
	}

	@Override
	public void onLoadFinished(Loader<List<MsgThread>> loader,
			List<MsgThread> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<List<MsgThread>> loader) {
		// TODO Auto-generated method stub
		
	}
}
