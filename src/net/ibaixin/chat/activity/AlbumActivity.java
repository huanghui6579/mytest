package net.ibaixin.chat.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.ibaixin.chat.R;
import net.ibaixin.chat.model.Album;
import net.ibaixin.chat.model.MsgInfo;
import net.ibaixin.chat.model.PhotoItem;
import net.ibaixin.chat.util.Constants;
import net.ibaixin.chat.util.DensityUtil;
import net.ibaixin.chat.util.SystemUtil;
import net.ibaixin.chat.view.ProgressDialog;
import net.ibaixin.chat.view.ProgressWheel;
import net.ibaixin.manage.MsgManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.MenuItemCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;

/**
 * 图片选择界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年11月13日 下午9:08:50
 */
public class AlbumActivity extends BaseActivity implements OnClickListener {
	public static final int REQ_PREVIEW_IMAGE = 1001;
	
	public static final String ARG_IS_IMAGE = "arg_is_image";
	
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	
	private MsgManager msgManager = MsgManager.getInstance();
	
	private GridView gvPhoto;
	private ProgressWheel pbLoading;
	private TextView tvAllPhoto;
	private TextView tvPreview;
	private TextView tvTime;
	private TextView btnOpt;
	
	private RelativeLayout layoutBottom;
	
	/**
	 * 相册分组的listview
	 */
	private ListView lvAlbum;
	
	//单元格的宽
	public int columnWith = 0;
	/**
	 * 文件列表
	 */
	private ArrayList<PhotoItem> mPhotos = new ArrayList<>();
	/**
	 * 文件按分组的集合
	 */
	private Map<String, List<PhotoItem>> folderMap = new HashMap<>();
	
	private PhotoAdapter mPhotoAdapter;
	
	PopupWindow mPopupWindow;
	
	private ProgressDialog pDialog;

	static int[] screenSize = null;
	
	private MsgInfo msgInfo;
	
	/**
	 * 资源内容是否是图片，分为视频和图片两类
	 */
	private boolean isImage = true;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.MSG_SUCCESS:
				Intent data = new Intent();
				data.putParcelableArrayListExtra(ChatActivity.ARG_MSG_INFO_LIST, (ArrayList<MsgInfo>)msg.obj);
				setResult(RESULT_OK, data);
				break;
			case Constants.MSG_FAILED:
				SystemUtil.makeShortToast(R.string.album_photo_chose_error);
				setResult(RESULT_CANCELED);
				break;
			default:
				break;
			}
			if (pDialog != null && pDialog.isShowing()) {
				pDialog.dismiss();
			}
			finish();
		}
		
	};

	@Override
	protected int getContentView() {
		return R.layout.activity_album;
	}

	@Override
	protected void initView() {
		gvPhoto = (GridView) findViewById(R.id.gv_photo);
		pbLoading = (ProgressWheel) findViewById(R.id.pb_loading);
		tvAllPhoto = (TextView) findViewById(R.id.tv_all_photo);
		tvPreview = (TextView) findViewById(R.id.tv_preview);
		tvTime = (TextView) findViewById(R.id.tv_time);
		layoutBottom = (RelativeLayout) findViewById(R.id.layout_bottom);
		
		screenSize = SystemUtil.getScreenSize();
		
	}

	@Override
	protected void initData() {
		mPhotoAdapter = new PhotoAdapter(mPhotos, mContext);
		gvPhoto.setAdapter(mPhotoAdapter);
		
		Intent intent = getIntent();
		isImage = intent.getBooleanExtra(ARG_IS_IMAGE, true);
		msgInfo = getIntent().getParcelableExtra(ChatActivity.ARG_MSG_INFO);
		
		if (!isImage) {	//不是图片，则不显示预览选项
			tvPreview.setVisibility(View.GONE);
			tvAllPhoto.setText(R.string.album_all_video);
			setTitle(R.string.activity_lable_video);
		}
		
		new LoadPhotoTask().execute();
	}

	@Override
	protected void addListener() {
		tvAllPhoto.setOnClickListener(this);
		tvPreview.setOnClickListener(this);
		gvPhoto.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {	//拍照
					if (isImage) {	//
						SystemUtil.makeShortToast("拍照");
					} else {
						SystemUtil.makeShortToast("录视频");
					}
				} else {
					if (isImage) {	//进行图片预览
						Intent intent = new Intent(mContext, PhotoPreviewActivity.class);
						intent.putExtra(PhotoPreviewActivity.ARG_POSITION, position - 1);
						intent.putExtra(PhotoPreviewActivity.ARG_SHOW_MODE, PhotoPreviewActivity.MODE_BROWSE);
						intent.putParcelableArrayListExtra(PhotoPreviewActivity.ARG_PHOTO_LIST, mPhotos);
						intent.putExtra(ChatActivity.ARG_MSG_INFO, msgInfo);
						ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
						ActivityCompat.startActivityForResult(AlbumActivity.this, intent, REQ_PREVIEW_IMAGE, options.toBundle());
					} else {	//直接选择视频
						SystemUtil.makeShortToast("选择" + position + "位置的视频");
					}
					
				}
			}
		});
		gvPhoto.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {	//闲置状态，没有滚动，则不显示时间
					AlphaAnimation animation = null;
					if (tvTime.getVisibility() == View.VISIBLE) {	//隐藏
						animation = (AlphaAnimation) AnimationUtils.loadAnimation(mContext, R.anim.album_time_fade_out);
						tvTime.setVisibility(View.GONE);
					} else {	//显示
						animation = (AlphaAnimation) AnimationUtils.loadAnimation(mContext, R.anim.album_time_fade_in);
						tvTime.setVisibility(View.VISIBLE);
					}
					tvTime.startAnimation(animation);
				} else {
					if (tvTime.getVisibility() == View.GONE) {
						tvTime.setVisibility(View.VISIBLE);
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem > 0) {
					PhotoItem photo = (PhotoItem) mPhotoAdapter.getItem(firstVisibleItem);
					String preTime = tvTime.getText().toString();
					String curTime = SystemUtil.formatTime(photo.getTime(), Constants.DATEFORMA_TPATTERN_ALBUM_TIP);
					if (TextUtils.isEmpty(preTime) || !curTime.equals(preTime)) {
						tvTime.setText(curTime);
					}
				}
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (isImage) {	//是图片才加载该菜单
			MenuInflater menuInflater = getMenuInflater();
			menuInflater.inflate(R.menu.common_opt, menu);
			MenuItem menuDone = menu.findItem(R.id.action_select_complete);
			btnOpt = (TextView) MenuItemCompat.getActionView(menuDone);
			btnOpt.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final List<PhotoItem> selects = mPhotoAdapter.getSelectList();
					pDialog = ProgressDialog.show(mContext, null, getString(R.string.chat_sending_file), true);
					//发送图片
					SystemUtil.getCachedThreadPool().execute(new Runnable() {
						
						@Override
						public void run() {
							final ArrayList<MsgInfo> msgList = msgManager.getMsgInfoListByPhotos(msgInfo, selects, false);
							Message msg = mHandler.obtainMessage();
							if (!SystemUtil.isEmpty(msgList)) {	//消息集合
								msg.what = Constants.MSG_SUCCESS;
								msg.obj = msgList;
							} else {
								msg.what = Constants.MSG_FAILED;
							}
							mHandler.sendMessage(msg);
						}
					});
					
				}
			});
		}
		
		/*mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				View view = findViewById(R.id.action_select_complete);
				view.setBackgroundResource(R.drawable.common_button_grey_selector);
			}
		});*/
		
		return super.onCreateOptionsMenu(menu);
	}
	
	
	/**
	 * 处理所选图片的 任务
	 * @author huanghui1
	 * @update 2014年11月17日 下午8:07:34
	 */
	class ProcessPhotosTask implements Runnable {
		private MsgInfo info;

		public ProcessPhotosTask(MsgInfo info) {
			super();
			this.info = info;
		}

		@Override
		public void run() {
//			Intent intent = new Intent(CoreService.SendChatMessageReceiver.ACTION_SEND_CHAT_MSG);	//发送消息
//			intent.putExtra(ChatActivity1.ARG_MSG_INFO, info);
//			sendBroadcast(intent);
		}
		
	}
	
	/**
	 * 发送图片的任务
	 * @author huanghui1
	 * @update 2014年11月17日 下午8:16:13
	 */
	class SendPhotosTask implements Runnable {
		private PhotoItem photo;

		public SendPhotosTask(PhotoItem photo) {
			super();
			this.photo = photo;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * 获取手机状态栏高度
	 * @update 2014年11月14日 下午4:08:56
	 * @param context
	 * @return
	 */
    public int getStatusBarHeight(Context context){
    	Rect frame = new Rect();  
    	getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
    	int statusBarHeight = frame.top;
    	return statusBarHeight;
    }
    
    /**
     * 获得ActionBar的高度
     * @return
     */
    public int getActionBarHeight() {
    	TypedValue tv = new TypedValue();
    	int actionBarHeight = 0;
    	if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
    	    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
    	}
    	return actionBarHeight;
    }
    
    /**
     * 根据文件夹获取对应的相册的数据
     * @update 2014年11月14日 下午4:28:20
     * @param map
     * @param isIamge 是否是图片
     * @return
     */
    private List<AlbumItem> getAlbumList(Map<String, List<PhotoItem>> map, boolean isIamge) {
    	if (SystemUtil.isEmpty(map)) {
			return null;
		}
    	List<AlbumItem> list = new ArrayList<>();
    	AlbumItem defaultAlbum = new AlbumItem(getString(R.string.album_all_photo), mPhotos.size(), mPhotos.get(0).getFilePath());
    	list.add(0, defaultAlbum);
    	Set<String> keys = map.keySet();
    	for (String key : keys) {
    		List<PhotoItem> temp = map.get(key);
    		String topPath = null;
    		if (isIamge) {	//图片
    			topPath = temp.get(0).getFilePath();
			} else {	//视频
				topPath = temp.get(0).getThumbPath();
			}
    		AlbumItem item = new AlbumItem(key, temp.size(), topPath);
    		list.add(item);
		}
    	return list;
    }
	
	/**
	 * 显示弹出菜单
	 * @update 2014年11月14日 下午4:02:16
	 * @param view
	 */
	private void showPopupWindow(final View author) {
		if (mPopupWindow == null) {
			int bottomHeight = SystemUtil.getViewSize(layoutBottom)[1];
			int statusHeight = getStatusBarHeight(mContext);
			int actionBarHeight = getActionBarHeight();
			
			LayoutInflater inflater = LayoutInflater.from(mContext);
			View contentView = inflater.inflate(R.layout.layout_album_list, null);
			lvAlbum = (ListView) contentView.findViewById(R.id.lv_album);
			
			final List<AlbumItem> list = getAlbumList(folderMap, isImage);
			final AlbumAdapter albumAdapter = new AlbumAdapter(list, mContext);
			lvAlbum.setAdapter(albumAdapter);
			
			//列表距上面的距离
			int topSpacing = DensityUtil.dip2px(mContext, getResources().getDimension(R.dimen.album_list_top_spacing));
			
			int maxHeight = screenSize[1] - statusHeight - actionBarHeight - bottomHeight - topSpacing;
			
			int listViewHeight = SystemUtil.getListViewHeight(lvAlbum);
			if (listViewHeight > maxHeight) {
				listViewHeight = maxHeight;
			}
			
			lvAlbum.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					albumAdapter.setCurrentPosition(position);
					togglewindow(mPopupWindow, author);
					if (position == 0) {	//加载全部
						new LoadPhotoTask().execute();
					} else {
						AlbumItem albumItem = list.get(position);
						List<PhotoItem> temp = folderMap.get(albumItem.getAlbumName());
						mPhotos.clear();
						mPhotos.addAll(temp);
						resetActionMenu();
						
						mPhotoAdapter.clearSelect();
					}
				}
			});
			
			mPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			mPopupWindow.setHeight(listViewHeight);
			mPopupWindow.setContentView(lvAlbum);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setFocusable(true);
			mPopupWindow.update();
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap)null));
		}
		togglewindow(mPopupWindow, author);
	}
	
	/**
	 * 显示和隐藏相册菜单
	 * @update 2014年11月14日 下午5:44:37
	 * @param window
	 * @param anchor
	 */
	private void togglewindow(PopupWindow window, View anchor) {
		if(window.isShowing()) {
			window.dismiss();
		} else {
			window.showAsDropDown(anchor, 0, 0);
		}
	}
	
	/**
	 * 恢复菜单原样
	 * @update 2014年11月14日 下午9:06:53
	 */
	private void resetActionMenu() {
		tvPreview.setEnabled(false);
		tvPreview.setText(R.string.album_preview_photo);
		btnOpt.setEnabled(false);
		btnOpt.setText(R.string.action_select_complete);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_all_photo:	//所有相册菜单
			if (SystemUtil.isEmpty(mPhotos)) {
				SystemUtil.makeShortToast("没有更多记录");
			} else {
				showPopupWindow(v);
			}
			break;
		case R.id.tv_preview:	//预览选中的图片
			ArrayList<PhotoItem> selects = mPhotoAdapter.getSelectList();
			Intent intent = new Intent(mContext, PhotoPreviewActivity.class);
			intent.putExtra(PhotoPreviewActivity.ARG_SHOW_MODE, PhotoPreviewActivity.MODE_CHOSE);
			intent.putParcelableArrayListExtra(PhotoPreviewActivity.ARG_PHOTO_LIST, selects);
			intent.putExtra(ChatActivity.ARG_MSG_INFO, msgInfo);
			startActivityForResult(intent, REQ_PREVIEW_IMAGE);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (RESULT_OK == resultCode) {	//成功
			if (requestCode == REQ_PREVIEW_IMAGE) {	//预览相片
				setResult(RESULT_OK, data);
				finish();
			}
		} else if (RESULT_CANCELED == resultCode) {
			
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 异步加载图片的任务
	 * @author huanghui1
	 * @update 2014年11月13日 下午10:04:54
	 */
	class LoadPhotoTask extends AsyncTask<String, Void, List<PhotoItem>> {
		
		@Override
		protected void onPreExecute() {
			if (pbLoading.getVisibility() == View.GONE) {
				pbLoading.setVisibility(View.VISIBLE);
			}
			mPhotos.clear();
			folderMap.clear();
		}

		@Override
		protected List<PhotoItem> doInBackground(String... params) {
			if (SystemUtil.isEmpty(params)) {	//默认加载全部
				Album album = msgManager.getAlbum(isImage);
				if (album != null) {
					List<PhotoItem> list = album.getmPhotos();
					folderMap = album.getFolderMap();
					if (!SystemUtil.isEmpty(list)) {
						mPhotos.addAll(list);
						return list;
					}
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(List<PhotoItem> result) {
			if (pbLoading.getVisibility() == View.VISIBLE) {
				pbLoading.setVisibility(View.GONE);
			}
			mPhotoAdapter.notifyDataSetChanged();
		}
		
	}
	
	/**
	 * 相册适配器
	 * @author huanghui1
	 * @update 2014年11月14日 下午4:56:09
	 */
	class AlbumAdapter extends CommonAdapter<AlbumItem> {
		DisplayImageOptions options = SystemUtil.getAlbumImageOptions();
		private int currentPosition = 0;

		public AlbumAdapter(List<AlbumItem> list, Context context) {
			super(list, context);
		}
		
		public void setCurrentPosition(int currentPosition) {
			this.currentPosition = currentPosition;
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AlbumViewHolder holder = null;
			if (convertView == null) {
				holder = new AlbumViewHolder();
				convertView = inflater.inflate(R.layout.item_album, parent, false);
				
				holder.ivCon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.tvContent = (CheckedTextView) convertView.findViewById(R.id.tv_content);
				
				convertView.setTag(holder);
			} else {
				holder = (AlbumViewHolder) convertView.getTag();
			}
			
			final AlbumItem albumItem = list.get(position);
			String albumName = albumItem.getAlbumName();
			int count = albumItem.getPhotoCount();
			String str = getString(R.string.album_item_content, albumName, count);
			SpannableStringBuilder spannableString = new SpannableStringBuilder(str);
			spannableString.setSpan(new TextAppearanceSpan(context, R.style.AlbumItemTitleStyle), 0, albumName.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			spannableString.setSpan(new TextAppearanceSpan(context, R.style.AlbumItemSubTitleStyle), albumName.length(), str.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			holder.tvContent.setText(spannableString, TextView.BufferType.SPANNABLE);
			String filePath = albumItem.getTopPhotoPath();
			holder.ivCon.setTag(filePath);
			mImageLoader.displayImage(Scheme.FILE.wrap(filePath), holder.ivCon, options);
			
			if(position == currentPosition) {	//与上次点击的不同
				holder.tvContent.setChecked(true);
			} else {
				holder.tvContent.setChecked(false);
			}
			
			return convertView;
		}
		
	}
	
	final class AlbumViewHolder {
		ImageView ivCon;
		CheckedTextView tvContent;
	}
	
	/**
	 * 图片的适配器
	 * @author huanghui1
	 * @update 2014年11月13日 下午9:12:17
	 */
	class PhotoAdapter extends CommonAdapter<PhotoItem> {
		DisplayImageOptions options = SystemUtil.getAlbumImageOptions();
		
		private SparseBooleanArray selectArray = new SparseBooleanArray();
		int selectSize = 0;
		
		private int viewTypeCount = 2;
		
		public PhotoAdapter(List<PhotoItem> list, Context context) {
			super(list, context);
		}
		
		/**
		 * 清除所选项
		 * @update 2014年11月14日 下午9:09:06
		 */
		public void clearSelect() {
			selectArray.clear();
			selectSize = 0;
			notifyDataSetChanged();
		}
		
		/**
		 * 获得所选中的图片列表
		 * @update 2014年11月14日 下午10:18:54
		 * @return
		 */
		public ArrayList<PhotoItem> getSelectList() {
			ArrayList<PhotoItem> selects = new ArrayList<>();
			int len = selectArray.size();
			for (int i = 0; i < len; i++) {
				boolean value = selectArray.valueAt(i);
				if (value) {
					int position = selectArray.keyAt(i);
					selects.add(mPhotos.get(position - 1));
				}
			}
			return selects;
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
		public int getItemViewType(int position) {
			if (position == 0) {
				return 0;
			} else {
				return 1;
			}
		}
		
		@Override
		public int getViewTypeCount() {
			return viewTypeCount;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PhotoViewHolder holder = null;
			if (convertView == null) {
				if (columnWith == 0) {
					columnWith = (int)((float)(screenSize[0] - DensityUtil.dip2px(mContext, 1.0f) * 2) / (float) gvPhoto.getNumColumns());
				}
				gvPhoto.setColumnWidth(columnWith);
				holder = new PhotoViewHolder();
				convertView = inflater.inflate(R.layout.item_photo, parent, false);
				
				holder.ivPhoto = (ImageView) convertView.findViewById(R.id.iv_photo);
				holder.viewAplha = convertView.findViewById(R.id.view_alpha);
				holder.cbChose = (CheckBox) convertView.findViewById(R.id.cb_chose);
				
				FrameLayout frameLayout = (FrameLayout) convertView.findViewById(R.id.layout_item);
				AbsListView.LayoutParams layoutParams = (LayoutParams) frameLayout.getLayoutParams();
				layoutParams.width = columnWith;
				layoutParams.height = columnWith;
				frameLayout.setLayoutParams(layoutParams);
				
				convertView.setTag(holder);
			} else {
				holder = (PhotoViewHolder) convertView.getTag();
			}
			
			if (position == 0) {	//拍照
				holder.viewAplha.setVisibility(View.GONE);
				holder.cbChose.setVisibility(View.GONE);
				holder.ivPhoto.setScaleType(ScaleType.CENTER);
				holder.ivPhoto.setImageResource(R.drawable.album_take_pic_selector);
			} else {
				PhotoItem photo = (PhotoItem) getItem(position);
				holder.ivPhoto.setScaleType(ScaleType.FIT_XY);
				
				String filePath = null;
				if (isImage) {
					holder.cbChose.setVisibility(View.VISIBLE);
					holder.viewAplha.setVisibility(View.VISIBLE);
					holder.cbChose.setOnCheckedChangeListener(null);
					holder.cbChose.setChecked((selectArray.indexOfKey(position) >= 0) ? selectArray.get(position) : false);
					if(holder.cbChose.isChecked()) {
						holder.viewAplha.setVisibility(View.VISIBLE);
					} else {
						holder.viewAplha.setVisibility(View.GONE);
					}
					holder.cbChose.setOnCheckedChangeListener(new OnCheckedChangeListenerImpl(holder, position));
					filePath = photo.getFilePath();
				} else {
					holder.viewAplha.setVisibility(View.GONE);
					holder.cbChose.setVisibility(View.GONE);
					filePath = photo.getThumbPath();
				}
				
				mImageLoader.displayImage(Scheme.FILE.wrap(filePath), holder.ivPhoto, options);
				
			}
			
			return convertView;
		}
		
		/**
		 * 相片选择的监听器
		 * @author huanghui1
		 * @update 2014年11月14日 上午10:47:48
		 */
		class OnCheckedChangeListenerImpl implements CompoundButton.OnCheckedChangeListener {
			PhotoViewHolder holder;
			int position;
			
			public OnCheckedChangeListenerImpl(PhotoViewHolder holder,
					int position) {
				super();
				this.holder = holder;
				this.position = position;
			}

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					selectSize ++;
				} else {
					selectSize --;
				}
				if (selectSize <= Constants.ALBUM_SELECT_SIZE) {	//少于9张
					selectSize = selectSize < 0 ? 0 : selectSize;
					if (isChecked) {
						holder.viewAplha.setVisibility(View.VISIBLE);
					} else {
						holder.viewAplha.setVisibility(View.GONE);
					}
					selectArray.put(position, isChecked);
					if (selectSize == 0) {	//没有图片选中
						resetActionMenu();
					} else {
						tvPreview.setEnabled(true);
						tvPreview.setText(getString(R.string.album_preview_photo_num, selectSize));
						btnOpt.setEnabled(true);
						btnOpt.setText(getString(R.string.action_select_complete) + "(" + selectSize + "/" + Constants.ALBUM_SELECT_SIZE + ")");
					}
				} else {	//多于9张
					selectSize = selectSize > Constants.ALBUM_SELECT_SIZE ? Constants.ALBUM_SELECT_SIZE : selectSize;
					tvPreview.setEnabled(false);
					holder.cbChose.setChecked(false);
					SystemUtil.makeShortToast(getString(R.string.album_tip_max_select, Constants.ALBUM_SELECT_SIZE));
				}
			}
			
		}
		
	}
	
	final class PhotoViewHolder {
		ImageView ivPhoto;
		View viewAplha;
		CheckBox cbChose;
	}
	
	/**
	 * 相册的实体
	 * @author huanghui1
	 * @version 1.0.0
	 * @update 2014年11月14日 下午4:18:06
	 */
	class AlbumItem {
		/**
		 * 相册名称，分组名称
		 */
		private String albumName;
		/**
		 * 相册里的相片数量
		 */
		private int photoCount;
		/**
		 * 第一张相片的本地完整路径
		 */
		private String topPhotoPath;

		public String getAlbumName() {
			return albumName;
		}

		public void setAlbumName(String albumName) {
			this.albumName = albumName;
		}

		public int getPhotoCount() {
			return photoCount;
		}

		public void setPhotoCount(int photoCount) {
			this.photoCount = photoCount;
		}

		public String getTopPhotoPath() {
			return topPhotoPath;
		}

		public void setTopPhotoPath(String topPhotoPath) {
			this.topPhotoPath = topPhotoPath;
		}

		public AlbumItem(String albumName, int photoCount, String topPhotoPath) {
			super();
			this.albumName = albumName;
			this.photoCount = photoCount;
			this.topPhotoPath = topPhotoPath;
		}

		public AlbumItem() {
			super();
		}
	}
	
}
