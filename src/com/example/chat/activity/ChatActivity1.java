package com.example.chat.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TabHost.TabSpec;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chat.ChatApplication;
import com.example.chat.R;
import com.example.chat.fragment.EmojiFragment;
import com.example.chat.model.Emoji;
import com.example.chat.model.EmojiType;
import com.example.chat.util.SystemUtil;
import com.example.chat.view.CirclePageIndicator;

/**
 * 聊天界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月25日 上午10:38:11
 */
public class ChatActivity1 extends BaseActivity implements OnClickListener/*, OnItemClickListener*/ {
	/**
	 * 默认的编辑模式，文本框内没有任何内容
	 */
	private static final int MODE_DEFAULT = 0;
	/**
	 * 表情选择模式，此时，会显示表情选择面板
	 */
	private static final int MODE_EMOJI = 1;
	
	/**
	 * 语音发送模式，此时会显示录音按钮
	 */
	private static final int MODE_VOICE = 2;
	
	/**
	 * 附件模式，此时会显示附件的选择面板
	 */
	private static final int MODE_ATTACH = 3;
	
	/**
	 * 发送模式，此时同时会显示文本输入框，但文本框里有内容
	 */
	private static final int MODE_SEND = 4;
	
	
	/**
	 * 编辑模式
	 */
	private int editMode = MODE_DEFAULT;
	
	private ListView lvMsgs;
	private TextView btnVoice;
	private TextView btnSend;
	private TextView btnEmoji;
	private EditText etContent;
	
	/**
	 * 输入框底部的面板
	 */
	private FrameLayout layoutBottom;
	/**
	 * 表情面板
	 */
	private LinearLayout layoutEmoji;
	/**
	 * 中间的消息输入框
	 */
	private RelativeLayout layoutEdit;
	/**
	 * 语音模式下按住说话按钮
	 */
	private TextView btnMakeVoice;
	
	private FragmentTabHost mTabHost;
	
	/**
	 * 附件面板
	 */
	private GridView gvAttach;
	
	@Override
	protected void initWidow() {
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected int getContentView() {
		// TODO Auto-generated method stub
		return R.layout.activity_chat1;
	}

	@Override
	protected void initView() {
		lvMsgs = (ListView) findViewById(R.id.lv_msgs);
		btnVoice = (TextView) findViewById(R.id.btn_voice);
		btnEmoji = (TextView) findViewById(R.id.btn_emoji);
		btnSend = (TextView) findViewById(R.id.btn_send);
		
		etContent = (EditText) findViewById(R.id.et_content);
		layoutBottom = (FrameLayout) findViewById(R.id.layout_bottom);
		layoutEmoji = (LinearLayout) findViewById(R.id.layout_emoji);
		
		btnMakeVoice = (TextView) findViewById(R.id.btn_make_voice);
		layoutEdit = (RelativeLayout) findViewById(R.id.layout_edit);
		
		gvAttach = (GridView) findViewById(R.id.gv_attach);
		
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(mContext, getSupportFragmentManager(), R.id.realtabcontent);
//		mTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
//		mTabHost.getTabWidget().setDividerDrawable(R.drawable.list_divider_drawable);
	}

	@Override
	protected void initData() {
		
		//初始化表情分类数据
		List<EmojiType> emojiTypes = ChatApplication.geEmojiTypes();
		for (int i = 0; i < ChatApplication.emojiTypeCount; i++) {
			EmojiType emojiType = emojiTypes.get(i);
			TabSpec tabSpec = mTabHost.newTabSpec(emojiType.getFileName()).setIndicator(getTabIndicatorView(emojiType));
			Bundle args = new Bundle();
			args.putParcelable(EmojiFragment.ARG_EMOJI_TYPE, emojiType);
			mTabHost.addTab(tabSpec, EmojiFragment.class, args);
			mTabHost.setTag(i);
			mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.item_tab_selector);
		}
	}
	
	/**
	 * 根据表情的分类获取对应的view
	 * @update 2014年10月27日 下午8:14:11
	 * @param emojiType
	 * @return
	 */
	private View getTabIndicatorView(EmojiType emojiType) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.layout_emoji_tab_view, null);
		
		ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
		ivIcon.setImageResource(emojiType.getResId());
		return view;
	}

	@Override
	protected void addListener() {
		btnEmoji.setOnClickListener(this);
		btnVoice.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		etContent.setOnClickListener(this);
		etContent.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {	//输入框内没有内容
					
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_emoji:	//表情按钮
			//1、判断点击之前的模式是否为“表情模式”，若是，切换到文本输入模式
			//2、消息输入文本框获得焦点
//			etContent.requestFocus();
			//3、若键盘为显示模式，则隐藏键盘
			boolean isSoftKeyboard = SystemUtil.isSoftInputActive();
			if (isSoftKeyboard) {	//键盘已显示，则隐藏
				SystemUtil.hideSoftInput(v);
			}
			//
			break;

		default:
			break;
		}
	}
	
	/**
	 * 切换到表情选择模式
	 * @update 2014年10月25日 下午4:43:17
	 */
	private void changeToEmojiMode() {
		//点击表情按钮之前的模式不可能为“语音模式”，“语音模式时“，表情按钮式隐藏的
		switch (editMode) {
		case MODE_ATTACH:	//点击之前是附件选择模式，则隐藏附件面板，其他的模式已经处理
			layoutBottom.setVisibility(View.VISIBLE);
			gvAttach.setVisibility(View.GONE);
			break;
		default:
			break;
		}
		//1、改变表情按钮为选中的背景
		btnEmoji.setBackgroundResource(R.drawable.ic_facial_pressed);
		//2、显示表情面板
		layoutEmoji.setVisibility(View.VISIBLE);
		//改变模式
		editMode = MODE_EMOJI;
	}

}
