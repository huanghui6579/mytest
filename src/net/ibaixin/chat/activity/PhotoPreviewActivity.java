package net.ibaixin.chat.activity;

import java.util.ArrayList;
import java.util.List;

import net.ibaixin.chat.R;
import net.ibaixin.chat.fragment.PhotoFragment;
import net.ibaixin.chat.manage.MsgManager;
import net.ibaixin.chat.model.MsgInfo;
import net.ibaixin.chat.model.PhotoItem;
import net.ibaixin.chat.util.Constants;
import net.ibaixin.chat.util.SystemUtil;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * 相片预览容器
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年11月15日 上午9:37:58
 */
public class PhotoPreviewActivity extends BaseActivity {
	public static final String ARG_PHOTO_LIST = "arg_photo_list";
	public static final String ARG_POSITION = "arg_position";
	public static final String ARG_SHOW_MODE = "arg_show_mode";
	/**
	 * 是否原图发送
	 */
	public static final String ARG_ORIGINAO_IMAGE = "arg_originao_image";
	
	/**
	 * 浏览模式进入
	 */
	public static final int MODE_BROWSE = 1;
	/**
	 * 选择模式进入
	 */
	public static final int MODE_CHOSE = 2;
	
	private ViewPager mViewPager;
	private CheckBox cbOrigianlImage;
	private CheckBox cbChose;
	private TextView btnOpt;
	
	private ProgressDialog pDialog;
	private MsgManager msgManager = MsgManager.getInstance();
	
	/**
	 * 所选图片的数量
	 */
	private int selectCount = 0;
	private SparseBooleanArray selectArray = new SparseBooleanArray();
	
	/**
	 * 所浏览的图片集合
	 */
	private List<PhotoItem> mPhotos;
	private MsgInfo msgInfo;
	
	/**
	 * 选择的图片集合
	 */
	private ArrayList<PhotoItem> mSelectList = new ArrayList<>();
	private int currentPostion = 0;
	private int totalCount;
	private int showMode = 0;
	
	/**
	 * 选择的图片的原始大小
	 */
	private long selectOriginalSize = 0;
	
	PhotoFragmentViewPager photoAdapter;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.MSG_SUCCESS:
				Intent data = new Intent();
				data.putExtra(ARG_ORIGINAO_IMAGE, cbOrigianlImage.isChecked());
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
		return R.layout.activity_photo_preview;
	}

	@Override
	protected void initView() {
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		cbOrigianlImage = (CheckBox) findViewById(R.id.cb_original_image);
		cbChose = (CheckBox) findViewById(R.id.cb_chose);
	}

	@Override
	protected void initData() {
		Intent intent = getIntent();
		mPhotos = intent.getParcelableArrayListExtra(ARG_PHOTO_LIST);
		currentPostion = intent.getIntExtra(ARG_POSITION, 0);
		showMode = intent.getIntExtra(ARG_SHOW_MODE, MODE_BROWSE);
		msgInfo = intent.getParcelableExtra(ChatActivity.ARG_MSG_INFO);
		photoAdapter = new PhotoFragmentViewPager(getSupportFragmentManager());
		mViewPager.setAdapter(photoAdapter);
		if (currentPostion != 0) {
			mViewPager.setCurrentItem(currentPostion);
		}
		if (showMode == MODE_CHOSE) {	//选择模式，则默认选中的就是所有列表
			mSelectList.addAll(mPhotos);
			selectCount = mSelectList.size();
			for (int i = 0; i < selectCount; i++) {
				selectArray.put(i, true);
			}
			cbChose.setChecked(true);
			selectOriginalSize = SystemUtil.getFileListSize(mSelectList);
			cbOrigianlImage.setText(getString(R.string.album_preview_original_image_size, SystemUtil.sizeToString(selectOriginalSize)));
		}
		totalCount = mPhotos.size();
		setTitle(getString(R.string.album_preview_photo_index, currentPostion + 1, totalCount));
	}
	
	/**
	 * 刷新发送按钮
	 * @update 2014年11月15日 下午4:05:34
	 * @param selectCount
	 */
	private void updateBtnOpt(int selectCount) {
		if (selectCount <= 0) {	//没有选中的
			btnOpt.setEnabled(false);
			btnOpt.setText(getString(R.string.action_select_complete));
		} else {
			btnOpt.setEnabled(true);
			btnOpt.setText(getString(R.string.action_select_complete) + "(" + selectCount + "/" + Constants.ALBUM_SELECT_SIZE + ")");
		}
	}
	
	/**
	 * 更新选择原图发送的复选框的样式
	 * @update 2014年11月15日 下午5:52:01
	 * @param list
	 */
	private void updateOriginalCheckbox(long selectSize) {
		if (selectSize > 0) {	//选中了图片
			cbOrigianlImage.setText(getString(R.string.album_preview_original_image_size, SystemUtil.sizeToString(selectOriginalSize)));
		} else {
			cbOrigianlImage.setText(R.string.album_preview_original_image);
		}
	}
	
	/**
	 * 为呃照片复选框添加监听器
	 * @update 2015年2月12日 下午7:26:31
	 */
	private void addCheckImageListener() {
		cbChose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					selectCount ++;
					if (selectCount > Constants.ALBUM_SELECT_SIZE) {	//选择的多于9张
						selectCount = Constants.ALBUM_SELECT_SIZE;
						SystemUtil.makeShortToast(getString(R.string.album_tip_max_select, Constants.ALBUM_SELECT_SIZE));
						cbChose.setChecked(false);
						return;
					}
					mSelectList.add(mPhotos.get(currentPostion));
				} else {
					mSelectList.remove(mPhotos.get(currentPostion));
					selectCount --;
					selectCount = selectCount < 0 ? 0 : selectCount;
				}
				selectOriginalSize = SystemUtil.getFileListSize(mSelectList);
				selectArray.put(currentPostion, isChecked);
				updateBtnOpt(selectCount);
				updateOriginalCheckbox(selectOriginalSize);
			}
		});
	}
	
	@Override
	protected void addListener() {
		cbOrigianlImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				
			}
		});
		addCheckImageListener();
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				currentPostion = position;
				setTitle(getString(R.string.album_preview_photo_index, currentPostion + 1, totalCount));
				cbChose.setOnCheckedChangeListener(null);
				cbChose.setChecked(selectArray.indexOfKey(position) >= 0 ? selectArray.get(position) : false);
				addCheckImageListener();
				if (btnOpt != null) {
					updateBtnOpt(selectCount);
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.common_opt, menu);
		MenuItem menuDone = menu.findItem(R.id.action_select_complete);
		btnOpt = (TextView) MenuItemCompat.getActionView(menuDone);
		if (showMode == MODE_CHOSE) {	//选择模式，则默认选中的就是所有列表
			btnOpt.setEnabled(true);
			btnOpt.setText(getString(R.string.action_select_complete) + "(" + selectCount + "/" + Constants.ALBUM_SELECT_SIZE + ")");
		}
		btnOpt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pDialog = ProgressDialog.show(mContext, null, getString(R.string.chat_sending_file), false, true);
				SystemUtil.getCachedThreadPool().execute(new Runnable() {
					
					@Override
					public void run() {
						ArrayList<MsgInfo> msgList = msgManager.getMsgInfoListByPhotos(msgInfo, mSelectList, cbOrigianlImage.isChecked());
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
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * 相片预览适配器
	 * @author huanghui1
	 * @update 2014年11月15日 上午10:49:30
	 */
	class PhotoFragmentViewPager extends FragmentStatePagerAdapter {

		public PhotoFragmentViewPager(FragmentManager fm) {
			super(fm);
		}

		@Override
		public android.support.v4.app.Fragment getItem(int position) {
			Bundle args = new Bundle();
			if (!SystemUtil.isEmpty(mPhotos)) {
				PhotoItem photoItem = mPhotos.get(position);
				args.putParcelable(PhotoFragment.ARG_PHOTO, photoItem);
			}
			return android.support.v4.app.Fragment.instantiate(mContext, PhotoFragment.class.getCanonicalName(), args);
		}

		@Override
		public int getCount() {
			return mPhotos.size();
		}
		
	}

}
