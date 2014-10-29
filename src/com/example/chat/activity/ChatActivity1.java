package com.example.chat.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.example.chat.model.AttachItem;
import com.example.chat.model.Emoji;
import com.example.chat.model.EmojiType;
import com.example.chat.model.MsgInfo;
import com.example.chat.model.MsgInfo.SendState;
import com.example.chat.util.DensityUtil;
import com.example.chat.util.Log;
import com.example.chat.util.SystemUtil;
import com.example.chat.view.CirclePageIndicator;

/**
 * 聊天界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月25日 上午10:38:11
 */
public class ChatActivity1 extends BaseActivity implements OnClickListener/*, OnItemClickListener*/ {
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
	/**
	 * 默认的编辑模式，文本框内没有任何内容
	 */
	private static final int MODE_DEFAULT = 0;
	/**
	 * 表情选择模式，此时，会显示表情选择面板
	 */
//	private static final int MODE_EMOJI = 1;
	
	/**
	 * 语音发送模式，此时会显示录音按钮
	 */
	private static final int MODE_VOICE = 1;
	
	/**
	 * 附件模式，此时会显示附件的选择面板
	 */
	private static final int MODE_ATTACH = 2;
	
	/**
	 * 发送模式，此时同时会显示文本输入框，但文本框里有内容
	 */
	private static final int MODE_SEND = 3;
	
	
	/**
	 * 编辑模式
	 */
	private int editMode = MODE_DEFAULT;
	
	private static int[] attachItemRes = {
		R.drawable.att_item_image,
		R.drawable.att_item_audio,
		R.drawable.att_item_video,
		R.drawable.att_item_location,
		R.drawable.att_item_vcard,
		R.drawable.att_item_file
	};
	
	private static String[] attachItemNames;
	
	/**
	 * 是否正在显示表情面板
	 */
	private boolean isEmojiShow = false;
	
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
	
	private AttachPannelAdapter attachPannelAdapter;
	
	/**
	 * 添加附件的数据
	 */
	private List<AttachItem> mAttachItems = new ArrayList<>();
	
	private LinkedList<MsgInfo> mMsgInfos = new LinkedList<>();
	
	private MsgAdapter msgAdapter;
	
	/**
	 * 屏幕尺寸
	 */
	public static int[] screenSize = null;
	
	private static SendState[] states = {
		SendState.SENDING,
		SendState.FAILED,
		SendState.SUCCESS
	};
	
	private static boolean[] comtype = {true, false};
	
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
	
	private SendState getRandomState() {
		Random random = new Random();
		int index = random.nextInt(states.length);
		return states[index];
	}
	
	private void initMsgInfo() {
		for (int i = 0; i < 23; i++) {
			MsgInfo mi = new MsgInfo();
			mi.setContent("测试内容方erence to my TextView (to access in the onGlobalLayout() method). Next, I get the ViewTreeObserver from my TextView, and add an OnGlobalLayoutListener, overriding onGLobalLayout (there does not seem to be a superclass method to invoke here...) and adding my code which requires knowing the measurements of the view into" + i);
			mi.setCreationDate(new Date().getTime());
			mi.setSendState(getRandomState());
			mi.setComming(comtype[new Random().nextInt(2)]);
			mMsgInfos.add(mi);
		}
	}

	@Override
	protected void initData() {
		
		if (screenSize == null) {
			screenSize = SystemUtil.getScreenSize();
		}
		
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
		
		attachItemNames = getResources().getStringArray(R.array.att_item_name);
		//初始化添加附件选项的数据
		for (int i = 0; i < attachItemRes.length; i++) {
			AttachItem item = new AttachItem();
			item.setResId(attachItemRes[i]);
			item.setName(attachItemNames[i]);
			item.setAction(i + 1);
			
			mAttachItems.add(item);
		}
		
		initMsgInfo();
		
		msgAdapter = new MsgAdapter(mMsgInfos, mContext);
		lvMsgs.setAdapter(msgAdapter);
		
		attachPannelAdapter = new AttachPannelAdapter(mAttachItems, mContext);
		gvAttach.setAdapter(attachPannelAdapter);
		
		scrollMyListViewToBottom(lvMsgs);
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
//		etContent.setOnClickListener(this);
		lvMsgs.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:	//点击外面
					//隐藏底部所有内容
					hideBottomLayout(true);
					//隐藏输入法
					hideKeybroad();
					break;

				default:
					break;
				}
				return false;
			}
		});
		etContent.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					//文本框获取焦点，显示软键盘
					showKeybroad();
					//隐藏底部所有的面板
					hideBottomLayout(true);
					break;

				default:
					break;
				}
				return false;
			}
		});
		etContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {	//没有焦点就隐藏键盘
					hideKeybroad();
				}
			}
		});
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
				setEditMode(s);	//设置对应的模式
				setChangeSendBtnStyle(editMode);
				if (gvAttach.getVisibility() == View.VISIBLE) {
					layoutBottom.setVisibility(View.GONE);
				}
			}
		});
	}
	
	/**
	 * 将表情按钮的背景设置为正常
	 * @update 2014年10月28日 上午9:09:47
	 */
	private void changeEmojiBtnBackground2Normal() {
		btnEmoji.setBackgroundResource(R.drawable.chat_facial_selector);
	}
	
	/**
	 * 将表情按钮的背景设置为按下的背景
	 * @update 2014年10月28日 上午9:09:47
	 */
	private void changeEmojiBtnBackground2Pressed() {
		btnEmoji.setBackgroundResource(R.drawable.ic_facial_pressed);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_emoji:	//表情按钮
			handleEmojiMode();	//切换到表情模式
			break;
		case R.id.btn_send:	//发送或者附件选择
			handleSendMode();
			break;
		case R.id.btn_voice:	//语音输入按钮
			handleVoiceMode();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 根据文本输入框里的内容来设置对应的模式
	 * @update 2014年10月28日 上午10:11:39
	 */
	private void setEditMode(CharSequence s) {
		//判断文本框里有无内容
		if (!TextUtils.isEmpty(s)) {	//有内容，则模式为发送模式，否则为添加模式
			editMode = MODE_SEND;
		} else {
			editMode = MODE_DEFAULT;
		}
	}
	
	/**
	 * 根据不同的模式设置发送按钮的样式
	 * @update 2014年10月28日 下午2:28:33
	 * @param mode
	 */
	private void setChangeSendBtnStyle(int mode) {
		if (editMode == MODE_SEND) {
			btnSend.setText(R.string.send);
			btnSend.setBackgroundResource(R.drawable.common_button_green_selector);
		} else {
			btnSend.setText("");
			btnSend.setBackgroundResource(R.drawable.chat_attach_selector);
		}
	}
	
	/**
	 * 根据文本输入框里的内容来设置对应的模式
	 * @update 2014年10月28日 上午10:11:39
	 */
	private void setEditMode() {
		setEditMode(etContent.getText().toString());
	}
	
	/**
	 * 隐藏键盘
	 * @update 2014年10月28日 上午10:04:56
	 */
	private void hideKeybroad() {
		//3、若键盘为显示模式，则隐藏键盘
		boolean isSoftKeyboard = SystemUtil.isSoftInputActive();
		if (isSoftKeyboard) {	//键盘已显示，则隐藏
			//显示键盘
			SystemUtil.hideSoftInput(etContent);
		}
	}
	
	/**
	 * 显示表情面板
	 * @update 2014年10月28日 上午9:46:45
	 */
	private void showEmojiLayout() {
		//显示表情面板的父面板
		layoutBottom.setVisibility(View.VISIBLE);
		//隐藏附件面板
		gvAttach.setVisibility(View.GONE);
		//改变表情按钮背景为选中背景
		changeEmojiBtnBackground2Pressed();
		//显示表情面板
		layoutEmoji.setVisibility(View.VISIBLE);
		setEditMode();
		isEmojiShow = true;
	}
	
	/**
	 * 隐藏表情面板
	 * @update 2014年10月28日 上午9:48:20
	 */
	private void hideEmojiLayout() {
		//显示表情面板的父面板
//		layoutBottom.setVisibility(View.GONE);
		//改变表情按钮背景为正常状态
		changeEmojiBtnBackground2Normal();
		//隐藏表情面板
		layoutEmoji.setVisibility(View.GONE);
		isEmojiShow = false;
	}
	
	/**
	 * 隐藏底部的所有面板，切换到“文本输入模式”或“者语音输入模式”,语音输入也会隐藏底部面板，但不会进入到文本输入模式
	 * @param isTextMode 是否进入到输入模式，
	 * @update 2014年10月28日 上午10:17:51
	 */
	private void hideBottomLayout(boolean isTextMode) {
		layoutBottom.setVisibility(View.GONE);
		hideEmojiLayout();
		
		if (isTextMode) {
			//根据文本内容切换到输入模式
			setEditMode();
		}
	}
	
	/**
	 * 消息输入框获得焦点并显示键盘
	 * @update 2014年10月28日 上午9:17:27
	 */
	private void showKeybroad() {
		//文本框获取焦点
		etContent.requestFocus();
		//显示键盘
		SystemUtil.showSoftInput(etContent);
	}
	
	/**
	 * 隐藏输入法，但文本框仍保留焦点
	 * @update 2014年10月28日 上午9:17:27
	 */
	private void editHideKeybroadWithFocus() {
		//显示键盘
		SystemUtil.hideSoftInput(etContent);
		//文本框获取焦点
		etContent.requestFocus();
	}
	
	/**
	 * 隐藏附件选择面板
	 * @update 2014年10月28日 上午9:52:40
	 */
	private void hideAttachLayout() {
		layoutBottom.setVisibility(View.GONE);
//		layoutEmoji.setVisibility(View.GONE);
//		gvAttach.setVisibility(View.GONE);
	}
	
	/**
	 * 隐藏输入法，同时文本框也失去焦点
	 * @update 2014年10月28日 上午9:17:27
	 */
	private void editHideKeybroadNoFocus() {
		//文本框获取焦点
		hideKeybroad();
		etContent.clearFocus();
	}
	
	/**
	 * 隐藏语音输入按钮
	 * @update 2014年10月28日 上午11:59:05
	 */
	private void hideVoiceLayout() {
		btnMakeVoice.setVisibility(View.GONE);
		btnVoice.setBackgroundResource(R.drawable.chat_voice_mode_selector);
	}
	
	/**
	 * 显示语音输入按钮
	 * @update 2014年10月28日 上午11:59:05
	 */
	private void showVoiceLayout() {
		btnMakeVoice.setVisibility(View.VISIBLE);
		btnVoice.setBackgroundResource(R.drawable.chat_keyboard_mode_selector);
	}
	
	/**
	 * 显示文本编辑面板
	 * @update 2014年10月28日 下午12:01:05
	 */
	private void showEditLayout() {
		layoutEdit.setVisibility(View.VISIBLE);
		//隐藏底部所有面板
		hideBottomLayout(true);
		//显示软键盘
		showKeybroad();
	}
	
	/**
	 * 显示附件面板
	 * @update 2014年10月28日 下午5:26:00
	 */
	private void showAttLayout() {
		layoutBottom.setVisibility(View.VISIBLE);
		layoutEmoji.setVisibility(View.GONE);
		gvAttach.setVisibility(View.VISIBLE);
		//隐藏输入法和文本框失去焦点
		editHideKeybroadNoFocus();
	}
	
	/**
	 * 隐藏输入框面板，主要用于语音输入模式，其他模式不可能隐藏该面板
	 * @update 2014年10月28日 下午12:03:08
	 */
	private void hideEditLayout() {
		//隐藏输入法
		hideKeybroad();
		layoutEdit.setVisibility(View.GONE);
		//隐藏底部所有面板
//		hideBottomLayout(false);
		layoutBottom.setVisibility(View.GONE);
	}
	
	/**
	 * 将发送按钮改变成附件按钮
	 * @update 2014年10月28日 下午8:25:01
	 */
	private void changeSendBtn2Att() {
		if (editMode == MODE_SEND) {	//之前为发送模式
			btnSend.setText("");
			btnSend.setBackgroundResource(R.drawable.chat_attach_selector);
		}
	}
	
	/**
	 * listview滚动到最底部
	 * @update 2014年10月29日 下午5:57:29
	 * @param listView
	 */
	private void scrollMyListViewToBottom(final ListView listView) {
		listView.post(new Runnable() {
	        @Override
	        public void run() {
	            // Select the last row so it will scroll into view...
	        	listView.setSelection(listView.getCount() - 1);
	        }
	    });
	}

	/**
	 * 切换到表情选择模式
	 * @update 2014年10月25日 下午4:43:17
	 */
	private void handleEmojiMode() {
		if (isEmojiShow) {	//判断点击之前的模式是否为“表情模式”，若是，则切换到文本输入模式
			//显示键盘
			showKeybroad();
			//直接隐藏底部面板
			hideBottomLayout(true);
			//隐藏表情面板
//			hideEmojiLayout();
			//判断文本框里有无内容
			//有内容，则模式为发送模式，否则为添加模式
//			setEditMode();
		} else {
			//点击表情按钮之前的模式不可能为“语音模式”，“语音模式时“，表情按钮式隐藏的
			switch (editMode) {
			case MODE_ATTACH:	//点击之前是附件选择模式，则隐藏附件面板，其他的模式已经处理
				//隐藏附件面板
				hideAttachLayout();
			case MODE_DEFAULT:
			case MODE_SEND:
				//2、消息输入文本框获得焦点
				editHideKeybroadWithFocus();
				//显示表情面板
				showEmojiLayout();
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * 
	 * 处理发送文本消息或者切换到附件模式
	 * @update 2014年10月28日 上午11:55:35
	 */
	private void handleSendMode() {
		if (isEmojiShow) {	//若显示有表情面板，则隐藏
			hideBottomLayout(false);
//			hideEmojiLayout();
		}
		switch (editMode) {
		case MODE_SEND:	//发送文本消息
			MsgInfo msg = new MsgInfo();
			String content = etContent.getText().toString();
			if (content.startsWith("0")) {	//发送
				msg.setComming(false);
			} else {
				msg.setComming(true);
			}
			msg.setCreationDate(new Date().getTime());
			msg.setContent(content);
			msg.setSendState(getRandomState());
			mMsgInfos.add(msg);
			msgAdapter.notifyDataSetChanged();
			
			scrollMyListViewToBottom(lvMsgs);
			
			etContent.setText("");
			//隐藏底部面板
			hideBottomLayout(false);
			editMode = MODE_DEFAULT;
			setChangeSendBtnStyle(editMode);
			break;
		case MODE_ATTACH:	//点击之前是附件模式
			//隐藏附件面板
			hideAttachLayout();
			//文本框获取焦点，弹出软键盘
			showKeybroad();
			editMode = MODE_DEFAULT;
			break;
		case MODE_VOICE:	//点击之前是语音模式，则隐藏语音按钮,显示附件选择面板
			//隐藏语音输入按钮
			hideVoiceLayout();
			//显示文本输入框
			layoutEdit.setVisibility(View.VISIBLE);
		case MODE_DEFAULT:
			
			//显示附件面板
			showAttLayout();
			editMode = MODE_ATTACH;
			break;
		default:
			break;
		}
	}
	
	/**
	 * 处理切换到“语音模式”
	 * @update 2014年10月28日 下午5:57:07
	 */
	private void handleVoiceMode() {
		if (isEmojiShow) {
//			hideEmojiLayout();	//隐藏表情面板
			hideBottomLayout(false);
		}
		switch (editMode) {
		case MODE_VOICE:	//点击之前是语音模式，则隐藏语音按钮
			hideVoiceLayout();
			//显示文本输入框
			showEditLayout();
			//根部不同的模式改变附件或发送按钮的样式
			setChangeSendBtnStyle(editMode);
			break;
		case MODE_DEFAULT:
		case MODE_SEND:	//隐藏输入法
		case MODE_ATTACH:	//隐藏附件面板
			hideAttachLayout();
			//隐藏文本输入框
			hideEditLayout();
			//显示语音按钮
			showVoiceLayout();
			//将发送按钮改变为附件按钮
			changeSendBtn2Att();
			editMode = MODE_VOICE;
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onPause() {
		//隐藏键盘
		hideKeybroad();
		super.onPause();
	}
	
	/**
	 * 添加附件的适配器
	 * @author huanghui1
	 * @update 2014年10月28日 下午4:29:25
	 */
	class AttachPannelAdapter extends CommonAdapter<AttachItem> {

		public AttachPannelAdapter(List<AttachItem> list, Context context) {
			super(list, context);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AttItemViewHolder holder = null;
			if (convertView == null) {
				holder = new AttItemViewHolder();
				
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_attach, parent, false);
				
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
				
				convertView.setTag(holder);
			} else {
				holder = (AttItemViewHolder) convertView.getTag();
			}
			
			final AttachItem item = list.get(position);
			holder.ivIcon.setImageResource(item.getResId());
			holder.tvName.setText(item.getName());
			return convertView;
		}
		
	}
	
	final static class AttItemViewHolder {
		ImageView ivIcon;
		TextView tvName;
	}
	
	/**
	 * 聊天消息的适配器
	 * @author huanghui1
	 * @update 2014年10月29日 下午4:36:14
	 */
	class MsgAdapter extends CommonAdapter<MsgInfo> {
		private static final int TYPE_OUT = 0;
		private static final int TYPE_IN = 1;
		/**
		 * item有两种类型
		 */
		private static final int TYPE_COUNT = 2;
		
		/**
		 * 消息内容view最大的宽度
		 */
		private int maxConentWidth = 0; 

		public MsgAdapter(List<MsgInfo> list, Context context) {
			super(list, context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MsgViewHolder holder = null;
			final MsgInfo msgInfo = list.get(position);
			LayoutInflater inflater = LayoutInflater.from(context);
			int type = getItemViewType(position);
			
			if (convertView == null) {
				holder = new MsgViewHolder();
				switch (type) {
				case TYPE_OUT:	//发出的消息
					convertView = inflater.inflate(R.layout.item_chat_msg_out, parent, false);
					
					break;
				case TYPE_IN:	//接收的消息
					convertView = inflater.inflate(R.layout.item_chat_msg_in, parent, false);
					
					break;
				}
				
				holder.tvMsgTime = (TextView) convertView.findViewById(R.id.tv_msg_time);
				holder.layoutBody = (RelativeLayout) convertView.findViewById(R.id.layout_body);
				holder.ivHeadIcon = (ImageView) convertView.findViewById(R.id.iv_head_icon);
				holder.ivMsgState = (ImageView) convertView.findViewById(R.id.iv_msg_state);
				holder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
				
				convertView.setTag(holder);
			} else {
				holder = (MsgViewHolder) convertView.getTag();
			}
			
			if (maxConentWidth == 0) {
				//获取头像的宽度
				int iconWith = SystemUtil.getViewSize(holder.ivHeadIcon)[0];
				int stateWidth = SystemUtil.getViewSize(holder.ivMsgState)[0];
				int textMargin = DensityUtil.dip2px(context, getResources().getDimension(R.dimen.chat_msg_item_content_margin_left_right));
				int stateMargin = DensityUtil.dip2px(context, getResources().getDimension(R.dimen.chat_msg_item_send_state_margin_left_right));
				maxConentWidth = screenSize[0] - 2 * (iconWith + textMargin) - stateWidth - stateMargin;
			}
			holder.tvContent.setMaxWidth(maxConentWidth);
			
			holder.tvMsgTime.setText(dateFormat.format(new Date(msgInfo.getCreationDate())));
//			holder.ivHeadIcon.setImageResource(R.drawable.ic_chat_default_big_head_icon);
			holder.tvContent.setText(msgInfo.getContent());
			switch (msgInfo.getSendState()) {
			case SENDING:	//正在发送
				holder.ivMsgState.setVisibility(View.VISIBLE);
				holder.ivMsgState.setImageResource(R.drawable.chat_msg_state_sending);
				Animation rotateAnim = AnimationUtils.loadAnimation(context, R.anim.chat_msg_sending);
				holder.ivMsgState.startAnimation(rotateAnim);
				break;
			case SUCCESS:	//发送成功
				holder.ivMsgState.clearAnimation();
				holder.ivMsgState.setVisibility(View.GONE);
				break;
			case FAILED:
				holder.ivMsgState.setVisibility(View.VISIBLE);
				holder.ivMsgState.clearAnimation();
				holder.ivMsgState.setImageResource(R.drawable.chat_msg_state_failed_selector);
				break;
			default:
				break;
			}
			holder.tvContent.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					SystemUtil.makeShortToast("长按了内容");
					return true;
				}
			});
			holder.ivHeadIcon.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					SystemUtil.makeShortToast("点击了头像");
				}
			});
			holder.ivMsgState.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//TODO 
				}
			});
			return convertView;
		}

		@Override
		public int getItemViewType(int position) {
			MsgInfo mi = list.get(position);
			if (mi.isComming()) {	//接收的消息
				return TYPE_IN;
			} else {	//发送的消息
				return TYPE_OUT;
			}
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT;
		}
		
	}
	
	final static class MsgViewHolder {
		TextView tvMsgTime;
		RelativeLayout layoutBody;
		ImageView ivHeadIcon;
		TextView tvContent;
		ImageView ivMsgState;
	}

}
