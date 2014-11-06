package com.example.chat.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chat.ChatApplication;
import com.example.chat.R;
import com.example.chat.model.Emoji;
import com.example.chat.util.SystemUtil;
import com.example.chat.view.CirclePageIndicator;

/**
 * 聊天界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月25日 上午10:38:11
 */
public class ChatActivity extends BaseActivity implements OnClickListener, OnItemClickListener {
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
	
	private EmojiPagerAdapter mEmojiPagerAdapter;
	private List<View> emojiViews;
	
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
		
		initEmojiViews();
		
		mEmojiPagerAdapter = new EmojiPagerAdapter(emojiViews);
		mViewPager.setAdapter(mEmojiPagerAdapter);
		
		mIndicator.setViewPager(mViewPager);
	}

	@Override
	protected void addListener() {
		btnFacial.setOnClickListener(this);
		btnVoice.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		etContent.setOnClickListener(this);
	}

	/**
	 * 初始化表情的个页面
	 * @update 2014年10月27日 下午2:53:04
	 */
	private void initEmojiViews() {
		emojiViews = new ArrayList<>();
		LayoutInflater inflater = LayoutInflater.from(mContext);
		for (int i = 0; i < ChatApplication.emojiPageCount; i++) {
			View view = inflater.inflate(R.layout.layout_emoji_grid, null);
			GridView gridView = (GridView) view.findViewById(R.id.gv_emoji);
			List<Emoji> list = ChatApplication.getCurrentPageEmojis(i);
			EmojiAdapter adapter = new EmojiAdapter(list, mContext);
			gridView.setAdapter(adapter);
			
			gridView.setOnItemClickListener(this);
			
			emojiViews.add(gridView);
		}
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
	
	/**
	 * 表情的网格适配器
	 * @author huanghui1
	 * @update 2014年10月27日 下午2:48:31
	 */
	class EmojiAdapter extends CommonAdapter<Emoji> {

		public EmojiAdapter(List<Emoji> list, Context context) {
			super(list, context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			EmojiViewHolder holder = null;
			if (convertView == null) {
				holder = new EmojiViewHolder();
				
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_emoji, parent, false);
				
				holder.ivEmoji = (ImageView) convertView.findViewById(R.id.iv_emoji);
				
				convertView.setTag(holder);
			} else {
				holder = (EmojiViewHolder) convertView.getTag();
			}
			
			final Emoji emoji = list.get(position);
			int resId = emoji.getResId();
			int resType = emoji.getResTpe();
			switch (resType) {
			case Emoji.TYPE_EMOJI:	//表情类型
				holder.ivEmoji.setImageResource(resId);
				break;
			case Emoji.TYPE_DEL:	//删除类型
				convertView.setBackgroundDrawable(null);
				holder.ivEmoji.setImageResource(resId);
				break;
			case Emoji.TYPE_EMPTY:	//空的类型
				convertView.setBackgroundDrawable(null);
				holder.ivEmoji.setImageDrawable(null);
				break;
			default:
				break;
			}
			
			return convertView;
		}
		
	}
	
	final class EmojiViewHolder {
		ImageView ivEmoji;
	}
	
	class EmojiPagerAdapter extends PagerAdapter {
		private List<View> views;

		public EmojiPagerAdapter(List<View> views) {
			super();
			this.views = views;
		}

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = views.get(position);
			((ViewPager) container).addView(view);
			view.setTag("emojiPage" + position);
			return view;
		}

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView(views.get(position));
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		//当前页面的索引
		int currentIndex = mViewPager.getCurrentItem();
		GridView gridView = (GridView) mViewPager.findViewWithTag("emojiPage" + currentIndex);
		EmojiAdapter emojiAdapter = (EmojiAdapter) gridView.getAdapter();
		Emoji emoji = (Emoji) emojiAdapter.getItem(position);
		int resType = emoji.getResTpe();
		switch (resType) {
		case Emoji.TYPE_EMOJI:	//表情类型
			int cursorStart = etContent.getSelectionStart();
			int cursorEnd = etContent.getSelectionEnd();
			if (cursorStart != cursorEnd) {
				etContent.getText().replace(cursorStart, cursorEnd, "");
			}
			int cursorIndex = Selection.getSelectionEnd(etContent.getText());
			SpannableStringBuilder emojiText = SystemUtil.addEmojiString(emoji);
			etContent.getText().insert(cursorIndex, emojiText);
			break;
		case Emoji.TYPE_DEL:	//删除类型
			int selectionStart = etContent.getSelectionStart();	//光标开始索引位置
			String content = etContent.getText().toString();
			if (selectionStart > 0) {
				String text = content.substring(selectionStart - 1, selectionStart);
				if ("]".equals(text)) {
					int start = content.lastIndexOf("[");
					int end = selectionStart;
					etContent.getText().delete(start, end);
					return;
				}
				etContent.getText().delete(selectionStart - 1, selectionStart);
			}
			break;
		default:
			break;
		}
	}
	
}
