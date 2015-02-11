package net.ibaixin.joke.chat.fragment;

import net.ibaixin.joke.chat.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * "我"的fragment界面，包含有个人信息，设置等
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月8日 下午7:46:37
 */
public class MineFragment extends BaseFragment {
	
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
}
