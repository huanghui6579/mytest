package net.ibaixin.chat.fragment;

import net.ibaixin.chat.R;
import net.ibaixin.chat.activity.JokeReadMainActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
/**
 * "我"的fragment界面，包含有个人信息，设置等
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月8日 下午7:46:37
 */
public class MineFragment extends BaseFragment implements OnClickListener{
	/** 我的资料 */
	private View item_mine;
	/** 趣味阅读 */
	private View item_readjokestext;
	/** 趣味视频 */
	private View item_readjokesvideo;
	/** 设置 */
	private View item_setting;
	
	/**
	 * 初始化fragment
	 * @update 2014年10月8日 下午10:08:39
	 * @return
	 */
	public static MineFragment newInstance() {
		MineFragment fragment = new MineFragment();
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mine, container, false);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		item_mine = view.findViewById(R.id.item_mine);
		item_readjokestext = view.findViewById(R.id.item_readjokestext);
		item_readjokesvideo = view.findViewById(R.id.item_readjokesvideo);
		item_setting = view.findViewById(R.id.item_setting);
		item_mine.setOnClickListener(this);
		item_readjokestext.setOnClickListener(this);
		item_readjokesvideo.setOnClickListener(this);
		item_setting.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null ;
		switch (v.getId()) {
		case R.id.item_mine:
			break;
		case R.id.item_readjokestext:
			intent = new Intent(mContext, JokeReadMainActivity.class);
			startActivity(intent);
			break;
		case R.id.item_readjokesvideo:
			
			break;
		case R.id.item_setting:
			
			break;
		default:
			break;
		}
	}
}
