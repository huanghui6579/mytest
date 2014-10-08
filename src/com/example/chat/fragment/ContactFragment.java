package com.example.chat.fragment;

import com.example.chat.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 好友列表界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月8日 下午7:44:40
 */
public class ContactFragment extends BaseFragment {
	
	/**
	 * 初始化fragment
	 * @update 2014年10月8日 下午10:07:58
	 * @return
	 */
	public static ContactFragment newInstance() {
		ContactFragment fragment = new ContactFragment();
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contact, container, false);
		return view;
	}
}
