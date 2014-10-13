package com.example.chat.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.activity.CommonAdapter;
import com.example.chat.model.User;
import com.example.chat.view.SideBar;
import com.example.chat.view.SideBar.OnTouchingLetterChangedListener;

/**
 * 好友列表界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月8日 下午7:44:40
 */
public class ContactFragment extends BaseFragment {
	
	private ListView lvContact;
	private TextView tvIndexDialog;
	private SideBar sideBar;
	
	private ContactAdapter adapter;
	
	private List<User> users = new ArrayList<>();
	
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
		
		lvContact = (ListView) view.findViewById(R.id.lv_contact);
		tvIndexDialog = (TextView) view.findViewById(R.id.tv_text_dialog);
		sideBar = (SideBar) view.findViewById(R.id.sidrbar);
		
		sideBar.setTextView(tvIndexDialog);
		
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			
			@Override
			public void onTouchingLetterChanged(String s) {
				//该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					lvContact.setSelection(position);
				}
				
			}
		});
		return view;
	}
	
	/**
	 * 改变sideBar的显示和隐藏的状态
	 * @update 2014年10月13日 上午9:53:10
	 * @param flag
	 */
	public void setHideSideBar(boolean flag) {
		if (sideBar != null && tvIndexDialog != null) {
			if (flag) {	//需要隐藏
				sideBar.setVisibility(View.GONE);
				tvIndexDialog.setVisibility(View.GONE);
			} else {
				sideBar.setVisibility(View.VISIBLE);
				tvIndexDialog.setVisibility(View.VISIBLE);
			}
		}
	}
	
	/**
	 * 初始化数据
	 * @update 2014年10月11日 下午8:43:34
	 */
	private void initData() {
		String[] array = mContext.getResources().getStringArray(R.array.data);
		for(String str : array) {
			User user = new User();
			user.setUsername(str);
			user.setNickname(str);
			String sp = user.getShortPinyin().substring(0, 1).toUpperCase(Locale.getDefault());
			if (sp.matches("[A-Z]")) {
				user.setSortLetter(sp);
			} else {
				user.setSortLetter(User.TAG_OTHER);
			}
			users.add(user);
		}
		Collections.sort(users, new User());
		adapter = new ContactAdapter(users, mContext);
		lvContact.setAdapter(adapter);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		initData();
		
	}
	
	/**
	 * 联系人适配器
	 * @author huanghui1
	 * @update 2014年10月11日 下午10:10:14
	 */
	class ContactAdapter extends CommonAdapter<User> implements SectionIndexer {

		public ContactAdapter(List<User> list, Context context) {
			super(list, context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_contact, parent, false);
				
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tvCatalog = (TextView) convertView.findViewById(R.id.tv_catalog);
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_head_icon);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final User user = list.get(position);
			holder.tvName.setText(user.getName());
			
			//根据position获取分类的首字母的Char ascii值
			int section = getSectionForPosition(position);
			if (position == getPositionForSection(section)) {
				holder.tvCatalog.setVisibility(View.VISIBLE);
				holder.tvCatalog.setText(user.getSortLetter());
			} else {
				holder.tvCatalog.setVisibility(View.GONE);
			}
			
			return convertView;
		}

		@Override
		public Object[] getSections() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getPositionForSection(int sectionIndex) {
			for (int i = 0; i < getCount(); i++) {
				String sortStr = list.get(i).getSortLetter();
				char fisrtChar = sortStr.charAt(0);
				if (fisrtChar == sectionIndex) {
					return i;
				}
			}
			return -1;
		}

		/*
		 * 根据ListView的当前位置获取分类的首字母的Char ascii值
		 */
		@Override
		public int getSectionForPosition(int position) {
			return list.get(position).getSortLetter().charAt(0);
		}
		
	}
	
	final class ViewHolder {
		TextView tvCatalog;
		TextView tvName;
		ImageView ivIcon;
	}
}
