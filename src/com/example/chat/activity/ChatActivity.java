package com.example.chat.activity;

import android.app.ActionBar;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.view.CirclePageIndicator;

/**
 * 聊天界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月25日 上午10:38:11
 */
public class ChatActivity extends BaseActivity implements OnClickListener {
	/**
	 * 默认的编辑模式，文本框内没有任何内容
	 */
	private static final int MODE_DEFAULT = 0;
	/**
	 * 表情选择模式，此时，会显示表情选择面板
	 */
	private static final int MODE_FACIAL = 1;
	
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
	private TextView btnFacial;
	private EditText etContent;
	
	private FrameLayout layoutBottom;
	private LinearLayout layoutFacial;
	private ViewPager mViewPager;
	private CirclePageIndicator mIndicator;
	
	private GridView gvPannel; 
	
	@Override
	protected void initWidow() {
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected int getContentView() {
		// TODO Auto-generated method stub
		return R.layout.activity_chat;
	}

	@Override
	protected void initView() {
		lvMsgs = (ListView) findViewById(R.id.lv_msgs);
		btnVoice = (TextView) findViewById(R.id.btn_voice);
		btnFacial = (TextView) findViewById(R.id.btn_facial);
		btnSend = (TextView) findViewById(R.id.btn_send);
		
		etContent = (EditText) findViewById(R.id.et_content);
		layoutBottom = (FrameLayout) findViewById(R.id.layout_bottom);
		layoutFacial = (LinearLayout) findViewById(R.id.layout_facial);
		
		gvPannel = (GridView) findViewById(R.id.gv_pannel);
		
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
	}

	@Override
	protected void initData() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addListener() {
		btnFacial.setOnClickListener(this);
		btnVoice.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		etContent.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_facial:	//表情按钮
			
			break;

		default:
			break;
		}
	}
	
	/**
	 * 切换到表情选择模式
	 * @update 2014年10月25日 下午4:43:17
	 */
	private void changeToFacialMode() {
		switch (editMode) {
		case MODE_VOICE:	//当前的模式语音发送模式，则隐藏语音按钮，切换到表情面板模式
			
			break;

		default:
			break;
		}
		//1、改变模式
		editMode = MODE_FACIAL;
		//2、隐藏以显示的面板和输入法面板
	}

}
