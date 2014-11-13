package com.example.chat.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.manage.MsgManager;
import com.example.chat.model.Photo;
import com.example.chat.util.SystemUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;

/**
 * 图片选择界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年11月13日 下午9:08:50
 */
public class AlbumActivity extends BaseActivity {
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	
	private MsgManager msgManager = MsgManager.getInstance();
	
	private GridView gvPhoto;
	private ProgressBar pbLoading;
	private TextView tvAllPhoto;
	private TextView tvPreview;
	
	//单元格的宽
	public int columnWith = 0;
	
	private List<Photo> mPhotos = new ArrayList<>();
	
	private PhotoAdapter mPhotoAdapter;
	
	static int[] screenSize = null;

	@Override
	protected int getContentView() {
		return R.layout.activity_album;
	}

	@Override
	protected void initView() {
		gvPhoto = (GridView) findViewById(R.id.gv_photo);
		pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
		tvAllPhoto = (TextView) findViewById(R.id.tv_all_photo);
		tvPreview = (TextView) findViewById(R.id.tv_preview);
		
		screenSize = SystemUtil.getScreenSize();
	}

	@Override
	protected void initData() {
		mPhotoAdapter = new PhotoAdapter(mPhotos, mContext);
		gvPhoto.setAdapter(mPhotoAdapter);
		
		if (columnWith == 0) {
			columnWith = (screenSize[0] / gvPhoto.getNumColumns());
			gvPhoto.setColumnWidth(columnWith);
		}
		
		new LoadPhotoTask().execute();
	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * 异步加载图片的任务
	 * @author huanghui1
	 * @update 2014年11月13日 下午10:04:54
	 */
	class LoadPhotoTask extends AsyncTask<String, Void, List<Photo>> {

		@Override
		protected List<Photo> doInBackground(String... params) {
			if (SystemUtil.isEmpty(params)) {	//默认加载全部
				mPhotos.clear();
				List<Photo> list = msgManager.getAllPhotos();
				if (!SystemUtil.isEmpty(list)) {
					mPhotos.addAll(list);
				}
				return list;
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(List<Photo> result) {
			if (pbLoading.getVisibility() == View.VISIBLE) {
				pbLoading.setVisibility(View.GONE);
			}
			mPhotoAdapter.notifyDataSetChanged();
		}
		
	}
	
	/**
	 * 图片的适配器
	 * @author huanghui1
	 * @update 2014年11月13日 下午9:12:17
	 */
	class PhotoAdapter extends CommonAdapter<Photo> {
		DisplayImageOptions options = SystemUtil.getGeneralImageOptions();
		

		public PhotoAdapter(List<Photo> list, Context context) {
			super(list, context);
		}
		
		@Override
		public Object getItem(int position) {
			if (position == 0) {	//拍照
				return null;
			}
			return list.get(position - 1);
		}

		@Override
		public int getCount() {
			return list.size() + 1;	//第一个为拍照
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PhotoViewHolder holder = null;
			if (convertView == null) {
				holder = new PhotoViewHolder();
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_photo, parent, false);
				
				holder.ivPhoto = (ImageView) convertView.findViewById(R.id.iv_photo);
				holder.viewAplha = convertView.findViewById(R.id.view_alpha);
				holder.cbChose = (CheckBox) convertView.findViewById(R.id.cb_chose);
				
				convertView.setTag(holder);
			} else {
				holder = (PhotoViewHolder) convertView.getTag();
			}
			
			if (position == 0) {	//拍照
				holder.viewAplha.setVisibility(View.GONE);
				holder.cbChose.setVisibility(View.GONE);
				holder.ivPhoto.setBackgroundResource(R.drawable.album_take_pic_bg_selector);
				holder.ivPhoto.setImageResource(R.drawable.album_take_pic_selector);
			} else {
				Photo photo = (Photo) getItem(position);
				holder.cbChose.setVisibility(View.VISIBLE);
				holder.ivPhoto.setBackgroundResource(0);
				String filePath = photo.getFilePath();
				mImageLoader.displayImage(Scheme.FILE.wrap(filePath), holder.ivPhoto, options);
			}
			
			return convertView;
		}
		
	}
	
	final class PhotoViewHolder {
		ImageView ivPhoto;
		View viewAplha;
		CheckBox cbChose;
	}

}
