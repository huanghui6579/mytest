package net.ibaixin.chat.activity;

import net.ibaixin.chat.R;
import net.ibaixin.chat.fragment.PhotoFragment;
import net.ibaixin.chat.model.PhotoItem;
import net.ibaixin.chat.util.SystemUtil;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts.PhotosColumns;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * 聊天图片查看界面
 * @author tiger
 * @version 2015年3月3日 下午10:53:37
 */
public class ChatImagePreviewActivity extends BaseActivity implements PhotoFragment.FinishCallBackListener {
	
	public static final String ARG_IMAGE_PATH = "arg_image_path";

	@Override
	protected int getContentView() {
		return R.layout.activity_container;
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected boolean isFullScreen() {
		return true;
	}

	@Override
	protected void initData() {
		Intent intent = getIntent();
		String filePath = intent.getStringExtra(ARG_IMAGE_PATH);
		boolean onFinish =  intent.getBooleanExtra(PhotoFragment.ARG_TOUCH_FINISH, true);
		if (SystemUtil.isFileExists(filePath)) {
			PhotoItem photoItem = new PhotoItem();
			photoItem.setFilePath(filePath);
			Bundle args = new Bundle();
			args.putParcelable(PhotoFragment.ARG_PHOTO, photoItem);
			args.putBoolean(PhotoFragment.ARG_TOUCH_FINISH, onFinish);
			Fragment fragment = Fragment.instantiate(mContext, PhotoFragment.class.getCanonicalName(), args);
			getSupportFragmentManager().beginTransaction().replace(R.id.main_container, fragment).commit();
		} else {
			SystemUtil.makeShortToast(R.string.file_not_exists);
			finish();
		}
	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub

	}


	@Override
	public void onFinish() {
		finish();
	}

}
