package com.example.chat.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chat.ChatApplication;
import com.example.chat.R;
import com.example.chat.activity.CommonAdapter;
import com.example.chat.model.Emoji;
import com.example.chat.model.EmojiType;
import com.example.chat.util.SystemUtil;
import com.example.chat.view.CirclePageIndicator;

/**
 * 表情界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月27日 下午8:16:15
 */
public class EmojiFragment2 extends BaseFragment implements OnItemClickListener{
	public static final String ARG_EMOJI_TYPE = "arg_emoji_type";
	
	private ViewPager mViewPager;
	private CirclePageIndicator mIndicator;
	
	private EmojiPagerAdapter mEmojiPageAdapter;
	private List<View> emojiViews;
	
	private EmojiType mEmojiType;
	
	private TextView mTvPrompt;
	
	/**
	 * 消息编辑输入框，在activty中
	 */
	private EditText mEtContent;
	
	public EmojiFragment2() {
		Bundle args = getArguments();
		if (args == null) {
			args = new Bundle();
			setArguments(args);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mEmojiType = getArguments().getParcelable(EmojiFragment2.ARG_EMOJI_TYPE);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mEmojiType == null) {
			return super.onCreateView(inflater, container, savedInstanceState);
		}
		View view = null;
		switch (mEmojiType.getOptType()) {
		case EmojiType.OPT_EMOJI:	//显示表情
			view = inflater.inflate(R.layout.fragment_emoji, container, false);
			
			mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
			mIndicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
			break;
		case EmojiType.OPT_ADD:
		case EmojiType.OPT_MANAGE:
			view = inflater.inflate(R.layout.layout_emoji_pager_prompt, container, false);
			mTvPrompt = (TextView) view.findViewById(R.id.tv_prompt);
			break;
		default:
			break;
		}
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (mEmojiType != null) {
			switch (mEmojiType.getOptType()) {
			case EmojiType.OPT_EMOJI:	//显示表情
				mEtContent = (EditText) getActivity().findViewById(R.id.et_content);
				
				initEmojiViews();
				
				mEmojiPageAdapter = new EmojiPagerAdapter(emojiViews);
				mViewPager.setAdapter(mEmojiPageAdapter);
				
				mIndicator.setViewPager(mViewPager);
				break;
			case EmojiType.OPT_MANAGE:	//管理表情
			case EmojiType.OPT_ADD:	//添加表情
				String text = mEmojiType.getDescription();
				mTvPrompt.setText(text);
				break;
			default:
				break;
			}
			
		}
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
	
	/**
	 * 表情分页的适配器
	 * @author huanghui1
	 * @update 2014年10月27日 下午8:32:37
	 */
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
		//当前页面的索引
		int currentIndex = mViewPager.getCurrentItem();
		GridView gridView = (GridView) mViewPager.findViewWithTag("emojiPage" + currentIndex);
		EmojiAdapter emojiAdapter = (EmojiAdapter) gridView.getAdapter();
		Emoji emoji = (Emoji) emojiAdapter.getItem(position);
		int resType = emoji.getResTpe();
		switch (resType) {
		case Emoji.TYPE_EMOJI:	//表情类型
			int cursorStart = mEtContent.getSelectionStart();
			int cursorEnd = mEtContent.getSelectionEnd();
			if (cursorStart != cursorEnd) {
				mEtContent.getText().replace(cursorStart, cursorEnd, "");
			}
			int cursorIndex = Selection.getSelectionEnd(mEtContent.getText());
			SpannableStringBuilder emojiText = SystemUtil.addEmojiString(emoji);
			mEtContent.getText().insert(cursorIndex, emojiText);
			break;
		case Emoji.TYPE_DEL:	//删除类型
			int selectionStart = mEtContent.getSelectionStart();	//光标开始索引位置
			String content = mEtContent.getText().toString();
			if (selectionStart > 0) {
				String text = content.substring(selectionStart - 1, selectionStart);
				if ("]".equals(text)) {
					int start = content.lastIndexOf("[");
					int end = selectionStart;
					mEtContent.getText().delete(start, end);
					return;
				}
				mEtContent.getText().delete(selectionStart - 1, selectionStart);
			}
			break;
		default:
			break;
		}
	}
}
