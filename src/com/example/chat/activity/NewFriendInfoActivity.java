package com.example.chat.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.model.NewFriendInfo;
import com.example.chat.model.NewFriendInfo.FriendStatus;
import com.example.chat.model.User;
import com.example.chat.model.UserVcard;
import com.example.chat.util.SystemUtil;
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
	private NewFriendAdapter newFriendAdapter;
	
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
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public Loader<List<NewFriendInfo>> onCreateLoader(int id, Bundle args) {
		return new NewFriendInfoLoader(mContext);
	}

	@Override
	public void onLoadFinished(Loader<List<NewFriendInfo>> loader,
			List<NewFriendInfo> data) {
		if (!SystemUtil.isEmpty(data)) {
			if (newFriendAdapter == null) {
				mNewInfos.addAll(data);
				newFriendAdapter = new NewFriendAdapter(mNewInfos, mContext);
				lvNewInfos.setAdapter(newFriendAdapter);
				lvNewInfos.setEmptyView(emptyView);
			} else {
				newFriendAdapter.swapData(data);
			}
		} else {
			lvNewInfos.setEmptyView(emptyView);
		}
		if (pbLoading.getVisibility() == View.VISIBLE) {
			pbLoading.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<NewFriendInfo>> loader) {
		if (newFriendAdapter != null) {
			newFriendAdapter.swapData(null);
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
		
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NewInfoViewHolder holder = null;
			if (convertView == null) {
				holder = new NewInfoViewHolder();
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_new_friend_info, parent, false);
				
				holder.ivHeadIcon = (ImageView) convertView.findViewById(R.id.iv_head_icon);
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
				holder.tvState = (TextView) convertView.findViewById(R.id.tv_state);
				
				convertView.setTag(holder);
			} else {
				holder = (NewInfoViewHolder) convertView.getTag();
			}
			NewFriendInfo newInfo = list.get(position);
			
			User user = newInfo.getUser();
			UserVcard uCard = user.getUserVcard();
			if (uCard != null) {
				String iconPath = uCard.getIconPath();
				if (SystemUtil.isFileExists(iconPath)) {
					mImageLoader.displayImage(Scheme.FILE.wrap(iconPath), holder.ivHeadIcon, options);
				} else {
					mImageLoader.displayImage(null, holder.ivHeadIcon, options);
				}
			} else {
				mImageLoader.displayImage(null, holder.ivHeadIcon, options);
			}
			holder.tvTitle.setText(user.getNickname());
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
						// TODO Auto-generated method stub
						
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
	
	final class NewInfoViewHolder {
		ImageView ivHeadIcon;
		TextView tvTitle;
		TextView tvContent;
		TextView tvState;
	}

}
