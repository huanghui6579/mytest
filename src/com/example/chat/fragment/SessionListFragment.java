package com.example.chat.fragment;

import java.util.Arrays;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.chat.R;

/**
 * 聊天会话列表
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月8日 下午7:36:50
 */
public class SessionListFragment extends BaseFragment {
	private ListView mListView;
	
	private ArrayAdapter<String> mAdapter;
	
	/**
	 * 初始化fragment
	 * @update 2014年10月8日 下午10:09:08
	 * @return
	 */
	public static SessionListFragment newInstance() {
		SessionListFragment fragment = new SessionListFragment();
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_list, container, false);
		mListView = (ListView) view.findViewById(R.id.lv_session);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}
		});
		mAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, Arrays.asList("Adsfsf", "dsafddsfdsf", "fdsfsdfgd", "范德萨发的说法", "fdsfdsfdsf", "发生的发生的范德萨"));
		mListView.setAdapter(mAdapter);
	}
}
