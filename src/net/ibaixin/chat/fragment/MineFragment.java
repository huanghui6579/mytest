package net.ibaixin.chat.fragment;

import net.ibaixin.chat.R;
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
	/** 笑话 */
	private View item_readjokestext;
	/** 趣图 */
	private View item_readjokesimg;
	/** 感悟 */
	private View item_readjokeslife;
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
		item_readjokesimg = view.findViewById(R.id.item_readjokesimg);
		item_readjokeslife = view.findViewById(R.id.item_readjokeslife);
		item_setting = view.findViewById(R.id.item_setting);
		item_mine.setOnClickListener(this);
		item_readjokestext.setOnClickListener(this);
		item_readjokesimg.setOnClickListener(this);
		item_readjokeslife.setOnClickListener(this);
		item_setting.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.item_mine:
			
			break;
		case R.id.item_readjokestext:
			
			break;
		case R.id.item_readjokesimg:
			
			break;
		case R.id.item_readjokeslife:
			
			break;
		case R.id.item_setting:
			
			break;
		default:
			break;
		}
	}
}
