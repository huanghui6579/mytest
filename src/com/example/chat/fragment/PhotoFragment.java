package com.example.chat.fragment;

import uk.co.senab.photoview.PhotoView;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.chat.R;
import com.example.chat.model.PhotoItem;
import com.example.chat.util.SystemUtil;
import com.example.chat.view.ProgressWheel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * 照片预览的界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年11月15日 上午10:04:06
 */
public class PhotoFragment extends BaseFragment {
	public static final String ARG_PHOTO = "arg_photo";
	
	private PhotoView ivPhoto;
	private ProgressWheel pbLoading;
	private PhotoItem mPhoto;
	
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options = SystemUtil.getPhotoPreviewOptions();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_preview, container, false);
		ivPhoto = (PhotoView) view.findViewById(R.id.iv_photo);
		pbLoading = (ProgressWheel) view.findViewById(R.id.pb_loading);
		pbLoading.setVisibility(View.GONE);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPhoto = getArguments().getParcelable(ARG_PHOTO);
		
		if (mPhoto != null) {
			String filePath = mPhoto.getFilePath();
			if (SystemUtil.isFileExists(filePath)) {
				mImageLoader.displayImage(Scheme.FILE.wrap(filePath), ivPhoto, options, new ImageLoadingListener() {
					
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						if (!SystemUtil.isViewVisible(pbLoading)) {
							pbLoading.setVisibility(View.VISIBLE);
						}
					}
					
					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						if (SystemUtil.isViewVisible(pbLoading)) {
							pbLoading.setVisibility(View.GONE);
						}
					}
					
					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						if (SystemUtil.isViewVisible(pbLoading)) {
							pbLoading.setVisibility(View.GONE);
						}
					}
					
					@Override
					public void onLoadingCancelled(String imageUri, View view) {
						
					}
				});
			} else {
				ivPhoto.setImageResource(R.drawable.ic_default_icon_error);
			}
		} else {
			ivPhoto.setImageResource(R.drawable.ic_default_icon_error);
		}
	}
}
