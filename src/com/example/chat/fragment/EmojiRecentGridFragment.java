package com.example.chat.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.activity.CommonAdapter;
import com.example.chat.fragment.EmojiFragment.OnEmojiconClickedListener;
import com.example.chat.model.emoji.Emojicon;
import com.example.chat.model.emoji.EmojiconRecents;
import com.example.chat.model.emoji.EmojiconRecentsManager;
import com.example.chat.model.emoji.People;
import com.example.chat.util.Log;
import com.example.chat.util.SystemUtil;
import com.example.chat.view.EmojiconTextView;

/**
 * 显示一页表情 
 * @author huanghui1
 * @update 2015年1月29日 下午9:34:26
 */
public class EmojiRecentGridFragment extends EmojiGridFragment implements EmojiconRecents {
	
	protected static EmojiRecentGridFragment newInstance(int emojiType, ArrayList<Emojicon> list, EmojiconRecents recents, boolean useSystemDefault) {
		EmojiRecentGridFragment fragment = new EmojiRecentGridFragment();
		Bundle args = new Bundle();
		args.putInt(EmojiFragment.ARG_EMOJI_TYPE, emojiType);
		args.putParcelableArrayList(ARG_EMOJI_DATA, list);
		args.putBoolean(EmojiFragment.ARGS_USE_SYSTEM_DEFAULT_KEY, useSystemDefault);
		if (recents != null) {
			fragment.setRecents(recents);
		}
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d("----EmojiRecentGridFragment----onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		Log.d("----EmojiRecentGridFragment----onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		Log.d("----EmojiRecentGridFragment----onDetach");
		super.onDetach();
	}

	@Override
	public void onStart() {
		Log.d("----EmojiRecentGridFragment----onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		Log.d("----EmojiRecentGridFragment----onResume");
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d("----EmojiRecentGridFragment----onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		Log.d("----EmojiRecentGridFragment----onPause");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.d("----EmojiRecentGridFragment----onStop");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		Log.d("----EmojiRecentGridFragment----onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		Log.d("----EmojiRecentGridFragment----onDestroy");
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("----EmojiRecentGridFragment----onCreateView");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d("----EmojiRecentGridFragment----onViewCreated");
		super.onViewCreated(view, savedInstanceState);
	}

	protected static EmojiRecentGridFragment newInstance(int emojiType, ArrayList<Emojicon> list) {
		return newInstance(emojiType, list, null, false);
	}

	@Override
	public void addRecentEmoji(Context context, Emojicon emojicon) {
		EmojiconRecentsManager recents = EmojiconRecentsManager
	            .getInstance(context);
	        recents.push(emojicon);

        // notify dataset changed
        if (mAdapter != null) {
            notifyDataSetChanged(recents);
        }
	}
	
}