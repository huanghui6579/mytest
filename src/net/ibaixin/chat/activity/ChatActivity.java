package net.ibaixin.chat.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import net.ibaixin.chat.ChatApplication;
import net.ibaixin.chat.R;
import net.ibaixin.chat.fragment.EmojiFragment;
import net.ibaixin.chat.fragment.EmojiTypeFragment;
import net.ibaixin.chat.model.AttachItem;
import net.ibaixin.chat.model.EmojiType;
import net.ibaixin.chat.model.FileItem;
import net.ibaixin.chat.model.MsgInfo;
import net.ibaixin.chat.model.MsgInfo.SendState;
import net.ibaixin.chat.model.MsgInfo.Type;
import net.ibaixin.chat.model.MsgPart;
import net.ibaixin.chat.model.MsgSenderInfo;
import net.ibaixin.chat.model.MsgThread;
import net.ibaixin.chat.model.Personal;
import net.ibaixin.chat.model.User;
import net.ibaixin.chat.model.UserVcard;
import net.ibaixin.chat.model.emoji.Emojicon;
import net.ibaixin.chat.provider.Provider;
import net.ibaixin.chat.service.CoreService;
import net.ibaixin.chat.service.CoreService.MainBinder;
import net.ibaixin.chat.util.Constants;
import net.ibaixin.chat.util.DensityUtil;
import net.ibaixin.chat.util.ImageUtil;
import net.ibaixin.chat.util.MimeUtils;
import net.ibaixin.chat.util.SoundMeter;
import net.ibaixin.chat.util.SystemUtil;
import net.ibaixin.chat.util.XmppConnectionManager;
import net.ibaixin.chat.view.EmojiconEditText;
import net.ibaixin.chat.view.TextViewAware;
import net.ibaixin.manage.MsgManager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.internal.widget.TintManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * 聊天界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月25日 上午10:38:11
 */
public class ChatActivity extends BaseActivity implements OnClickListener/*, OnItemClickListener*/, EmojiFragment.OnEmojiconClickedListener, EmojiTypeFragment.OnEmojiconBackspaceClickedListener {
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
	
	public static final String ARG_MSG_INFO = "arg_msg_info";
	public static final String ARG_MSG_INFO_LIST = "arg_msg_info_list";
	
	/**
	 * 调用相册的请请求码
	 */
	public static final int REQ_ALBUM = 100;
	/**
	 * 调用视频的请请求码
	 */
	public static final int REQ_VIDEO = 101;
	/**
	 * 调用文件的请请求码
	 */
	public static final int REQ_FILE = 102;
	/**
	 * 调用音频的请请求码
	 */
	public static final int REQ_AUDIO = 103;
	/**
	 * 地理位置请求码
	 */
	public static final int REQ_LOCATION = 104;
	
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
	
	public static final String ARG_THREAD = "arg_thread";
	
	/**
	 * 聊天的对方
	 */
	private User otherSide = null;
	private Personal mine = null;
	
	/**
	 * 当前的会话
	 */
	private MsgThread msgThread;
	
	private MsgManager msgManager = MsgManager.getInstance();
	
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
	private EmojiconEditText etContent;
	
	/**
	 * 输入框底部的面板
	 */
	private FrameLayout layoutBottom;
	/**
	 * 表情面板
	 */
	private FrameLayout layoutEmoji;
	/**
	 * 中间的消息输入框
	 */
	private RelativeLayout layoutEdit;
	/**
	 * 语音模式下按住说话按钮
	 */
	private TextView btnMakeVoice;
	
	/**
	 * 录音的根布局
	 */
	private View recordRootLayout;
	
//	private FragmentTabHost mTabHost;
	
	/**
	 * 附件面板
	 */
	private GridView gvAttach;
	
	private LinearLayout layoutVoiceRecording;
	private LinearLayout layoutRecord;
	/**
	 * 声音大小
	 */
	private ImageView ivVolume;
	/**
	 * 取消录音的提示图片
	 */
	private ImageView ivCancelTip;
	private ImageView ivDelTip;
	/**
	 * 删除录音
	 */
	private LinearLayout layoutDelRecord;
	private LinearLayout layoutVoiceRecordLoading;
	private LinearLayout layoutVoiceRecordTooshort;
	
	private SoundMeter mSensor;
	
	private AttachPannelAdapter attachPannelAdapter;
	
	/**
	 * 添加附件的数据
	 */
	private List<AttachItem> mAttachItems = new ArrayList<>();
	
	private LinkedList<MsgInfo> mMsgInfos = new LinkedList<>();
	
	private MsgAdapter msgAdapter;
	
	/**
	 * 消息处理的广播
	 */
	MsgProcessReceiver msgProcessReceiver;
	
	/**
	 * 录音开始时间
	 */
	private long recordStartTime = 0L;
	/**
	 * 录音结束时间
	 */
	private long recordEndTime = 0L;
	
	/**
	 * 录音文件的全路径名，含文件名
	 */
	private File volumeFile;
	
	/**
	 * 录音时间是否太短
	 */
	private boolean isShort;
	
	/**
	 * 语音是否正在播放
	 */
	private boolean mIsPlaying = false;
	/**
	 * 当前播放语音的位置
	 */
	private int mPlayingPosition = -1;
	/**
	 * 语音播放器
	 */
	private MediaPlayer mPlayer = null;
	/**
	 * 当前播放语音的view
	 */
	private TextView mPlayingView;
	private int mPlayingType;
	/**
	 * 播放语音的动画
	 */
	private AnimationDrawable mPlayingAnimation;
	
	private static final int POLL_INTERVAL = 300;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constants.MSG_MODIFY_CHAT_MSG_SEND_STATE:	//改变聊天消息的发送状态
				msgAdapter.notifyDataSetChanged();
				scrollMyListViewToBottom(lvMsgs);
				break;

			default:
				break;
			}
		}
	};
	
	/**
	 * 屏幕尺寸
	 */
	public static int[] screenSize = null;
	
	/**
	 * 加开始加载数据的索引
	 */
	private int pageOffset = 0;
	
	private AbstractXMPPConnection connection;
	private ChatManager chatManager = null;
	private Chat chat = null;
	
	//图片加载器
	private ImageLoader mImageLoader = null;
	
	CoreService coreService;
	
	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MainBinder mBinder = (MainBinder) service;
			coreService = mBinder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	@Override
	protected int getContentView() {
		// TODO Auto-generated method stub
		return R.layout.activity_chat;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void initView() {
		lvMsgs = (ListView) findViewById(R.id.lv_msgs);
		btnVoice = (TextView) findViewById(R.id.btn_voice);
		btnEmoji = (TextView) findViewById(R.id.btn_emoji);
		btnSend = (TextView) findViewById(R.id.btn_send);
		
		etContent = (EmojiconEditText) findViewById(R.id.et_content);
		layoutBottom = (FrameLayout) findViewById(R.id.layout_bottom);
		layoutEmoji = (FrameLayout) findViewById(R.id.layout_emoji);
		
		btnMakeVoice = (TextView) findViewById(R.id.btn_make_voice);
		layoutEdit = (RelativeLayout) findViewById(R.id.layout_edit);
		
		recordRootLayout = findViewById(R.id.record_root_layout);
		
		gvAttach = (GridView) findViewById(R.id.gv_attach);
		
//		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
//		mTabHost.setup(mContext, getSupportFragmentManager(), R.id.realtabcontent);
		
//		mTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
//		mTabHost.getTabWidget().setDividerDrawable(R.drawable.list_divider_drawable);
		
		layoutVoiceRecording = (LinearLayout) findViewById(R.id.layout_voice_recording);
		layoutDelRecord = (LinearLayout) findViewById(R.id.layout_del_record);
		layoutRecord = (LinearLayout) findViewById(R.id.layout_record);
		layoutVoiceRecordLoading = (LinearLayout) findViewById(R.id.layout_voice_record_loading);
		layoutVoiceRecordTooshort = (LinearLayout) findViewById(R.id.layout_voice_record_tooshort);
		ivVolume = (ImageView) findViewById(R.id.iv_volume);
		ivCancelTip = (ImageView) findViewById(R.id.iv_cancel_tip);
		ivDelTip = (ImageView) findViewById(R.id.iv_del_tip);
		Drawable drawable = TintManager.getDrawable(mContext, SystemUtil.getResourceId(mContext, android.R.attr.editTextStyle, android.R.attr.background));
//		Drawable drawable = TintManager.getDrawable(mContext, SystemUtil.getResourceId(mContext, R.attr.editTextBackground));
		if (SystemUtil.hasSDK16()) {
			layoutEdit.setBackground(drawable);
		} else {
			layoutEdit.setBackgroundDrawable(drawable);
		}
//		etContent.setBackgroundResource(0);
		
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.layout_emoji, EmojiTypeFragment.instantiate(mContext, EmojiTypeFragment.class.getCanonicalName()), "emojiFragment")
			.commit();
	}
	
	/**
	 * 初始化会话消息
	 * @update 2014年10月31日 下午3:26:57
	 */
	private void initMsgInfo() {
		new LoadDataTask(true).execute();
	}

	@Override
	protected void initData() {
		
		Intent service = new Intent(mContext, CoreService.class);
		bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
		
		mSensor = new SoundMeter();
		
		//注册处理聊天消息的广播
		msgProcessReceiver = new MsgProcessReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MsgProcessReceiver.ACTION_PROCESS_MSG);
		intentFilter.addAction(MsgProcessReceiver.ACTION_REFRESH_MSG);
		registerReceiver(msgProcessReceiver, intentFilter);
		
		mImageLoader = ImageLoader.getInstance();
		
		if (screenSize == null) {
			screenSize = SystemUtil.getScreenSize();
		}
		
		Intent intent = getIntent();
		if (intent != null) {
			otherSide = intent.getParcelableExtra(UserInfoActivity.ARG_USER);
			msgThread = intent.getParcelableExtra(ARG_THREAD);
		}

		//获取个人信息
		mine = ChatApplication.getInstance().getCurrentUser();
		
		initMsgInfo();
		
		msgAdapter = new MsgAdapter(mMsgInfos, mContext);
		lvMsgs.setAdapter(msgAdapter);
		
		//初始化表情分类数据
//		List<EmojiType> emojiTypes = ChatApplication.geEmojiTypes();
//		for (int i = 0; i < ChatApplication.emojiTypeCount; i++) {
//			EmojiType emojiType = emojiTypes.get(i);
//			TabSpec tabSpec = mTabHost.newTabSpec(emojiType.getFileName()).setIndicator(getTabIndicatorView(emojiType));
//			Bundle args = new Bundle();
//			args.putParcelable(EmojiFragment.ARG_EMOJI_TYPE, emojiType);
//			mTabHost.addTab(tabSpec, EmojiFragment.class, args);
//			mTabHost.setTag(i);
//			mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.item_tab_selector);
//		}
		
		attachItemNames = getResources().getStringArray(R.array.att_item_name);
		//初始化添加附件选项的数据
		for (int i = 0; i < attachItemRes.length; i++) {
			AttachItem item = new AttachItem();
			item.setResId(attachItemRes[i]);
			item.setName(attachItemNames[i]);
			item.setAction(i + 1);
			
			mAttachItems.add(item);
		}
		
		attachPannelAdapter = new AttachPannelAdapter(mAttachItems, mContext);
		gvAttach.setAdapter(attachPannelAdapter);
		
		connection = XmppConnectionManager.getInstance().getConnection();
		//注册消息观察者
//		registerContentOberver();
	}
	
	@Override
	protected void onDestroy() {
		//TODO 添加接触服务绑定
		unbindService(serviceConnection);
		unregisterReceiver(msgProcessReceiver);
		super.onDestroy();
	}
	
	/**
	 * 创建聊天对象
	 * @update 2014年11月6日 下午9:38:50
	 * @return
	 */
	private Chat createChat(AbstractXMPPConnection connection) {
		if (connection.isAuthenticated()) {	//是否登录
			if (chatManager == null) {
				chatManager = ChatManager.getInstanceFor(connection);
			}
			if (chat == null) {
				chat = chatManager.createChat(otherSide.getJID(), null);
			}
			return chat;
		} else {
			return null;
		}
	}
	
	/**
	 * 注册消息观察者
	 * @update 2014年11月6日 下午7:32:34
	 */
	private void registerContentOberver() {
		MsgContentObserver msgContentObserver = new MsgContentObserver(mHandler);
		getContentResolver().registerContentObserver(Provider.MsgInfoColumns.CONTENT_URI, true, msgContentObserver);
	}
	
	/**
	 * 加载聊天消息数据的后台任务
	 * @author huanghui1
	 * @update 2014年10月31日 上午9:18:23
	 */
	class LoadDataTask extends AsyncTask<Void, Void, List<MsgInfo>> {
		/**
		 * 是否需要滚动到最底部
		 */
		private boolean needScroll = true;

		public LoadDataTask(boolean needScroll) {
			this.needScroll = needScroll;
		}

		@Override
		protected List<MsgInfo> doInBackground(Void... params) {
			//根据参与者查询对应的会话
			MsgThread mt = null;
			List<MsgInfo> list = new ArrayList<>();
			if (otherSide != null) {
				mt = msgManager.getThreadByMember(otherSide);
				if (mt != null) {	//有该会话，才查询该会话下的消息
					msgThread = mt;
					list = msgManager.getMsgInfosByThreadId(mt.getId(), getPageOffset());
				} else {	//没有改会话，就创建一个
					mt = new MsgThread();
					mt.setMembers(Arrays.asList(otherSide));
					mt.setMsgThreadName(otherSide.getName());
					msgThread = msgManager.createMsgThread(mt);
				}
			} else if (msgThread != null) {	//已经有会话了
				list = msgManager.getMsgInfosByThreadId(msgThread.getId(), getPageOffset());
				msgThread = msgManager.getThreadById(msgThread.getId());
				//TODO 目前固定写死，有、后期会改有群聊的模式
				otherSide = msgThread.getMembers().get(0);
			}
			return list;
		}
		
		@Override
		protected void onPostExecute(List<MsgInfo> result) {
			if (otherSide != null) {
				setTitle(otherSide.getName());
			}
			if (!SystemUtil.isEmpty(result)) {
				mMsgInfos.clear();
				mMsgInfos.addAll(result);
				setPageOffset(result);
				msgAdapter.notifyDataSetChanged();
				if (needScroll) {
					scrollMyListViewToBottom(lvMsgs);
				}
			}
		}
	}
	
	/**
	 * 加载更多消息记录的后台任务
	 * @author huanghui1
	 * @update 2014年10月31日 下午3:16:09
	 */
	class LoadMoreDataTask extends AsyncTask<Integer, Void, List<MsgInfo>> {

		@Override
		protected List<MsgInfo> doInBackground(Integer... params) {
			List<MsgInfo> list = null;
			if (params != null && params.length == 2) {
				int msgThreadId = params[0];
				int offset = params[1];	//开始查询的索引
				list = msgManager.getMsgInfosByThreadId(msgThreadId, offset);
			}
			return list;
		}
		
		@Override
		protected void onPostExecute(List<MsgInfo> result) {
			if (!SystemUtil.isEmpty(result)) {	//有数据
				mMsgInfos.addAll(result);
				setPageOffset(mMsgInfos);
				msgAdapter.notifyDataSetChanged();
			}
		}
	}
	
	/**
	 * 获取分页时每次的分页开始索引
	 * @update 2014年10月31日 上午9:27:50
	 * @return
	 */
	private int getPageOffset() {
		return pageOffset * Constants.PAGE_SIZE_MSG;
	}
	
	/**
	 * 根据当前已经加载过的列表来设置新的开始查询索引
	 * @update 2014年10月31日 下午3:10:59
	 * @param list
	 */
	private void setPageOffset(List<MsgInfo> list) {
		if (list != null && list.size() > 0) {
			pageOffset = list.size() - 1;
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
	
	private Runnable mPollTask = new Runnable() {
		
		@Override
		public void run() {
			double amp = mSensor.getAmplitude();
			updateDisplay(amp);
			mHandler.postDelayed(mPollTask, POLL_INTERVAL);
		}
	};
	
	private Runnable mSleepTask = new Runnable() {
		public void run() {
			stopRecord();
		}
	};
	
	/**
	 * 开始录音
	 * @update 2014年11月24日 下午9:50:49
	 */
	private void startRecord() {
		volumeFile = SystemUtil.generateRecordFile(100);
		mSensor.start(volumeFile);
		mHandler.postDelayed(mPollTask, POLL_INTERVAL);
	}
	
	/**
	 * 停止录音
	 * @update 2014年11月24日 下午10:16:11
	 */
	private void stopRecord() {
		try {
			mHandler.removeCallbacks(mSleepTask);
			mHandler.removeCallbacks(mPollTask);
			mSensor.stop();
			ivVolume.setImageResource(R.drawable.amp1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据声音的大小来动态显示对应的图片
	 * @update 2014年11月24日 下午9:55:32
	 * @param signalEMA
	 */
	private void updateDisplay(double signalEMA) {
		
		switch ((int) signalEMA) {
		case 0:
		case 1:
			ivVolume.setImageResource(R.drawable.amp1);
			break;
		case 2:
		case 3:
			ivVolume.setImageResource(R.drawable.amp2);
			
			break;
		case 4:
		case 5:
			ivVolume.setImageResource(R.drawable.amp3);
			break;
		case 6:
		case 7:
			ivVolume.setImageResource(R.drawable.amp4);
			break;
		case 8:
		case 9:
			ivVolume.setImageResource(R.drawable.amp5);
			break;
		case 10:
		case 11:
			ivVolume.setImageResource(R.drawable.amp6);
			break;
		default:
			ivVolume.setImageResource(R.drawable.amp7);
			break;
		}
	}

	@Override
	protected void addListener() {
		btnEmoji.setOnClickListener(this);
		btnVoice.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		ivCancelTip.setOnClickListener(this);
		btnMakeVoice.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
//		etContent.setOnClickListener(this);
		gvAttach.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = null;
				
				AttachItem attachItem = mAttachItems.get(position);
				MsgInfo msgInfo = new MsgInfo();
				if (canSend()) {
					msgInfo.setComming(false);
					msgInfo.setFromUser(mine.getFullJID());
					msgInfo.setToUser(otherSide.getFullJid());
					msgInfo.setRead(true);
					msgInfo.setSendState(MsgInfo.SendState.SENDING);
					msgInfo.setThreadID(msgThread.getId());
				}
				
				int requestCode = 0;
				switch (attachItem.getAction()) {
				case AttachItem.ACTION_IMAGE:	//选择图片
					intent = new Intent(mContext, AlbumActivity.class);
					msgInfo.setMsgType(MsgInfo.Type.IMAGE);
					intent.putExtra(ARG_MSG_INFO, msgInfo);
					requestCode = REQ_ALBUM;
					break;
				case AttachItem.ACTION_VIDEO:	//选择视频
					intent = new Intent(mContext, AlbumActivity.class);
					msgInfo.setMsgType(MsgInfo.Type.VIDEO);
					intent.putExtra(ARG_MSG_INFO, msgInfo);
					intent.putExtra(AlbumActivity.ARG_IS_IMAGE, false);
					requestCode = REQ_VIDEO;
					break;
				case AttachItem.ACTION_FILE:	//选择文件
					intent = new Intent(mContext, FileExplorerActivity.class);
					msgInfo.setMsgType(MsgInfo.Type.FILE);
					requestCode = REQ_FILE;
					break;
				case AttachItem.ACTION_AUDIO:	//选择音频
					intent = new Intent(mContext, AudioListActivity.class);
					msgInfo.setMsgType(MsgInfo.Type.AUDIO);
					requestCode = REQ_AUDIO;
					break;
				case AttachItem.ACTION_LOCATION:	//地理位置
					intent = new Intent(mContext, LocationShareActivity.class);
					msgInfo.setMsgType(MsgInfo.Type.LOCATION);
					requestCode = REQ_LOCATION;
					break;
				default:
					break;
				}
				if (intent != null) {
					intent.putExtra(ARG_MSG_INFO, msgInfo);
					startActivityForResult(intent, requestCode);
				}
			}
		});
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
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (editMode == MODE_VOICE) {	//语音模式
			//获取语音按钮的坐标
			int[] recordPoint = new int[2];
			btnMakeVoice.getLocationInWindow(recordPoint);
			int recordX = recordPoint[0];
			int recordY = recordPoint[1];
			//获取删除按钮的位置
			int[] delPoint = new int[2];
			layoutDelRecord.getLocationInWindow(delPoint);
			int delX = delPoint[0];
			int delY = delPoint[1];
			if (event.getAction() == MotionEvent.ACTION_DOWN) {	//按下开始录音
//				//判断手指按下的坐标是否在按钮内部
				if (event.getRawY() > recordY && event.getRawX() > recordX) {
					recordRootLayout.setVisibility(View.VISIBLE);
					layoutVoiceRecordLoading.setVisibility(View.VISIBLE);
					layoutRecord.setVisibility(View.VISIBLE);
					layoutVoiceRecording.setVisibility(View.GONE);
					ivCancelTip.setVisibility(View.VISIBLE);
					
					layoutVoiceRecordTooshort.setVisibility(View.GONE);
					layoutDelRecord.setVisibility(View.GONE);
					
					btnMakeVoice.setPressed(true);
//					
//					//让加载按钮显示300毫秒
					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (!isShort) {
								layoutVoiceRecordLoading.setVisibility(View.GONE);
								layoutVoiceRecording.setVisibility(View.VISIBLE);
							}
						}
					}, POLL_INTERVAL);
//					
					recordStartTime = System.currentTimeMillis();
//					//开始录音
					startRecord();
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {	//松手
				btnMakeVoice.setPressed(false);
				float eX = event.getRawX();
				float eY = event.getRawY();
				layoutVoiceRecording.setVisibility(View.GONE);
				//判断松手时的坐标是否在删除区域内
				if (eY >= delY && eY <= delY + layoutDelRecord.getHeight() && eX >= delX && eX <= delX + layoutDelRecord.getWidth()) {	//在删除区域内
					cancelRecordVoice();
				} else {	//结束录音
					stopRecord();
					recordEndTime = System.currentTimeMillis();
					//计算时间差
					int time = (int) ((recordEndTime - recordStartTime) / 1000);
					if (time < Constants.COICE_RECORD_MIN_LENGTH) {	//少于1秒，则不发送，需重录
						deleteRecordFile();
						isShort = true;
						layoutVoiceRecordLoading.setVisibility(View.GONE);
						layoutDelRecord.setVisibility(View.GONE);
						layoutVoiceRecordTooshort.setVisibility(View.VISIBLE);
						//太短的提示信息显示300毫秒消失
						mHandler.postDelayed(new Runnable() {
							public void run() {
								layoutVoiceRecordTooshort.setVisibility(View.GONE);
								isShort = false;
							}
						}, POLL_INTERVAL);
					} else {
						//TODO 发送语音消息
						if (canSend()) {
							MsgInfo msgInfo = new MsgInfo();
							msgInfo.setComming(false);
							msgInfo.setFromUser(mine.getFullJID());
							msgInfo.setToUser(otherSide.getFullJid());
							msgInfo.setContent(SystemUtil.shortTimeToString(time));
							msgInfo.setRead(true);
							msgInfo.setSendState(MsgInfo.SendState.SENDING);
							msgInfo.setThreadID(msgThread.getId());
							msgInfo.setMsgType(MsgInfo.Type.VOICE);
							msgInfo.setCreationDate(System.currentTimeMillis());
							//设置附件信息
							MsgPart msgPart = new MsgPart();
							msgPart.setCreationDate(System.currentTimeMillis());
							msgPart.setFileName(volumeFile.getName());
							msgPart.setFilePath(volumeFile.getAbsolutePath());
//							String subfix = SystemUtil.getFileSubfix(volumeFile.getName());
//							msgPart.setMimeTye(MimeUtils.guessMimeTypeFromExtension(subfix));
							msgPart.setMimeTye(MimeUtils.MIME_TYPE_AUDIO_AMR);
							msgPart.setSize(volumeFile.length());
							
							msgInfo.setMsgPart(msgPart);
							
							mMsgInfos.add(msgInfo);
							
							msgAdapter.notifyDataSetChanged();

							sendMsg(msgInfo);
							
							//列表滚动到最底部
							scrollMyListViewToBottom(lvMsgs);
						}
					}
					
				}
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				if (event.getRawY() < recordY) {	//手按下的位置不在语音按钮的区域内
					float eX = event.getRawX();
					float eY = event.getRawY();
					Animation inAnim = AnimationUtils.loadAnimation(mContext, R.anim.chat_record_voice_in);
					Animation outAnim = AnimationUtils.loadAnimation(mContext, R.anim.chat_record_voice_out);
					layoutDelRecord.setVisibility(View.VISIBLE);
					ivCancelTip.setVisibility(View.GONE);
					layoutDelRecord.setPressed(false);
					if (eY >= delY && eY <= delY + layoutDelRecord.getHeight() && eX >= delX && eX <= delX + layoutDelRecord.getWidth()) {	//在删除区域内
						layoutDelRecord.setPressed(true);
						ivDelTip.startAnimation(inAnim);
						ivDelTip.startAnimation(outAnim);
					}
				} else {
					ivCancelTip.setVisibility(View.VISIBLE);
					layoutDelRecord.setVisibility(View.GONE);
				}
			}
			return true;
		}
		return super.onTouchEvent(event);
	}
	
	/**
	 * 取消录音
	 * @update 2015年2月9日 下午8:36:54
	 */
	private void cancelRecordVoice() {
		recordRootLayout.setVisibility(View.GONE);
		layoutVoiceRecordTooshort.setVisibility(View.GONE);
		layoutDelRecord.setVisibility(View.GONE);
		
		deleteRecordFile();
	}
	
	/**
	 * 删除录音文件
	 * @update 2014年11月25日 下午3:17:26
	 */
	private void deleteRecordFile() {
		if (volumeFile != null) {
			if (volumeFile.exists()) {
				volumeFile.delete();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQ_ALBUM:	//相册
			case REQ_VIDEO:	//视频
			case REQ_FILE:	//文件
				if (data != null) {
					final List<MsgInfo> msgList = data.getParcelableArrayListExtra(ARG_MSG_INFO_LIST);
					final boolean originalImage = data.getBooleanExtra(PhotoPreviewActivity.ARG_ORIGINAO_IMAGE, false);
					if (!SystemUtil.isEmpty(msgList)) {
						hideAttachLayout();
						setEditMode();
						mMsgInfos.addAll(msgList);
						msgAdapter.notifyDataSetChanged();
						scrollMyListViewToBottom(lvMsgs);
						SystemUtil.getCachedThreadPool().execute(new Runnable() {
							
							@Override
							public void run() {
								for (MsgInfo msgInfo : msgList) {
									MsgSenderInfo senderInfo = new MsgSenderInfo(chat, msgInfo, msgThread, mHandler);
									senderInfo.originalImage = originalImage;
									coreService.sendChatMsg(senderInfo);
								}
							}
						});
					}
				}
				break;
			case REQ_AUDIO:	//音频
			case REQ_LOCATION:	//地理位置分享
				if (data != null) {
					final MsgInfo mi = data.getParcelableExtra(ARG_MSG_INFO);
					if (mi != null) {
						hideAttachLayout();
						setEditMode();
						mMsgInfos.add(mi);
						msgAdapter.notifyDataSetChanged();
						scrollMyListViewToBottom(lvMsgs);
						SystemUtil.getCachedThreadPool().execute(new Runnable() {
							
							@Override
							public void run() {
								MsgSenderInfo senderInfo = new MsgSenderInfo(chat, mi, msgThread, mHandler);
								coreService.sendChatMsg(senderInfo);
							}
						});
					}
				}
				break;
			default:
				break;
			}
		} else if (resultCode == RESULT_CANCELED) {
			
		}
		super.onActivityResult(requestCode, resultCode, data);
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
		case R.id.iv_cancel_tip:	//取消录音
			layoutVoiceRecording.setVisibility(View.GONE);
			stopRecord();
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
//	        	listView.smoothScrollToPosition(0);
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
	 * 发送文本消息
	 * @update 2014年11月6日 下午9:46:44
	 * @param chat
	 * @param content
	 * @return
	 */
	private MsgInfo sendMsg(MsgInfo msgInfo) {
		if (chat == null) {
			chat = createChat(connection);
		}
		
		MsgSenderInfo msgSenderInfo = new MsgSenderInfo(chat, msgInfo, msgThread, mHandler);
		coreService.sendChatMsg(msgSenderInfo);
//		SystemUtil.getCachedThreadPool().execute(new SendMsgTask(msg));
		
		return msgInfo;
	}
	
	/**
	 * 新创建一个文本消息
	 * @update 2014年11月25日 下午4:55:28
	 * @param content
	 */
	private MsgInfo newTextMsgInfo(String content) {
		MsgInfo msg = new MsgInfo();
		msg.setComming(false);
		msg.setCreationDate(System.currentTimeMillis());
		msg.setContent(content);
		msg.setSendState(SendState.SENDING);
		msg.setFromUser(mine.getFullJID());
		msg.setToUser(otherSide.getFullJid());
		msg.setMsgType(Type.TEXT);
		msg.setRead(true);
		msg.setMsgPart(null);
		msg.setSubject(null);
		msg.setThreadID(msgThread.getId());
		
		return msg;
	}
	
	/**
	 * 检查是否可以发送信息，前提条件是有个人信息和对方信息
	 * @update 2015年1月8日 下午8:59:25
	 * @return
	 */
	private boolean canSend() {
		if (otherSide == null || mine == null || msgThread == null) {
			return false;
		} else {
			return true;
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
			String content = etContent.getText().toString();
			if (!canSend()) {
				break;
			}
			MsgInfo msg = newTextMsgInfo(content);
			
			if (msg != null) {
				sendMsg(msg);
				
				mMsgInfos.add(msg);
			}
			
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
		//取消录音
		recordRootLayout.setPressed(false);
		cancelRecordVoice();
		//隐藏键盘
		hideKeybroad();
		super.onPause();
	}
	
//	/**
//	 * 发送消息的线程
//	 * @author huanghui1
//	 * @update 2014年11月6日 下午9:56:22
//	 */
//	class SendMsgTask implements Runnable {
//		private MsgInfo msgInfo;
//
//		public SendMsgTask(MsgInfo msgInfo) {
//			this.msgInfo = msgInfo;
//		}
//
//		@Override
//		public void run() {
//			try {
//				msgInfo = msgManager.addMsgInfo(msgInfo);
//				msgThread.setSnippetId(msgInfo.getId());
//				msgThread.setSnippetContent(msgInfo.getContent());
//				msgThread.setModifyDate(System.currentTimeMillis());
//				msgThread = msgManager.updateMsgThread(msgThread);
//				if (msgInfo != null) {
//					if (chat == null) {
//						chat = createChat(connection);
//					}
//					if (chat != null) {
//						chat.sendMessage(msgInfo.getContent());
//						msgInfo.setSendState(SendState.SUCCESS);
//					} else {
//						msgInfo.setSendState(SendState.FAILED);
//					}
//				} else {
//					return;
//				}
//			} catch (NotConnectedException | XMPPException e) {
//				msgInfo.setSendState(SendState.FAILED);
//				e.printStackTrace();
//			}
//			msgInfo = msgManager.updateMsgInfo(msgInfo);
//			mHandler.sendEmptyMessage(Constants.MSG_MODIFY_CHAT_MSG_SEND_STATE);
//		}
//		
//	}
	
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
		DisplayImageOptions headIconOptions = SystemUtil.getGeneralImageOptions();
		DisplayImageOptions chatImageOptions = SystemUtil.getChatImageOptions();
		
		/**
		 * 发送的消息类型：0
		 */
		public static final int TYPE_OUT = 0;
		/**
		 * 接受的消息类型：1
		 */
		public static final int TYPE_IN = 1;
		/**
		 * item有两种类型
		 */
		private static final int TYPE_COUNT = 2;
		
		/**
		 * 消息内容view最大的宽度
		 */
		private int maxConentWidth = 0; 
		//十分钟，单位毫秒
		private long spliteTimeUnit = 600000;

		public MsgAdapter(List<MsgInfo> list, Context context) {
			super(list, context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MsgViewHolder holder = null;
			MsgInfo msgInfo = list.get(position);
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
				holder.tvContentDesc = (TextView) convertView.findViewById(R.id.tv_content_desc);
				
				convertView.setTag(holder);
			} else {
				holder = (MsgViewHolder) convertView.getTag();
			}
			
			holder.tvContent.setOnClickListener(new MsgItemClickListener(type, msgInfo, position));
			
			if (maxConentWidth == 0) {
				//获取头像的宽度
				int iconWith = SystemUtil.getViewSize(holder.ivHeadIcon)[0];
				int stateWidth = SystemUtil.getViewSize(holder.ivMsgState)[0];
				int textMargin = DensityUtil.dip2px(context, getResources().getDimension(R.dimen.chat_msg_item_content_margin_left_right));
				int stateMargin = DensityUtil.dip2px(context, getResources().getDimension(R.dimen.chat_msg_item_send_state_margin_left_right));
				maxConentWidth = screenSize[0] - 2 * (iconWith + textMargin) - stateWidth - stateMargin;
			}
			holder.tvContent.setMaxWidth(maxConentWidth);
			int paddingLeft = 0;
			int paddingRight = 0;
			int extraPad = 1;
			Resources resources = getResources();
			int paddingVertical = resources.getDimensionPixelSize(R.dimen.chat_msg_item_content_padding_top_bottom);
			if (type == TYPE_IN) {	//接收的消息
				paddingLeft = resources.getDimensionPixelSize(R.dimen.chat_msg_item_in_content_padding_left);
				paddingRight = resources.getDimensionPixelSize(R.dimen.chat_msg_item_in_content_padding_right);
			} else {
				paddingLeft = resources.getDimensionPixelSize(R.dimen.chat_msg_item_out_content_padding_left);
				paddingRight = resources.getDimensionPixelSize(R.dimen.chat_msg_item_out_content_padding_right);
			}
			
			holder.tvContent.setPadding(paddingLeft, paddingVertical, paddingRight, paddingVertical);
			long curDate = msgInfo.getCreationDate();
			if (position == 0) {	//第一条记录，一定显示时间
				holder.tvMsgTime.setVisibility(View.VISIBLE);
				holder.tvMsgTime.setText(dateFormat.format(new Date(curDate)));
			} else if (position >= 1) {	//至少有两条数据时才显示
				//当条记录的时间
				//上一条记录的时间
				long preDate = list.get(position - 1).getCreationDate();
				if (Math.abs(curDate - preDate) > spliteTimeUnit) {	//时间间隔超过10分钟，则显示时间分割条
					holder.tvMsgTime.setVisibility(View.VISIBLE);
					holder.tvMsgTime.setText(dateFormat.format(new Date(curDate)));
				} else {
					holder.tvMsgTime.setVisibility(View.GONE);
				}
			}
			holder.tvContentDesc.setVisibility(View.GONE);
			holder.tvContent.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			MsgInfo.Type msgType = msgInfo.getMsgType();
			MsgPart msgPart = msgInfo.getMsgPart();
			holder.tvContent.setGravity(Gravity.START | Gravity.TOP);
			holder.tvContent.setCompoundDrawablePadding(0);
			switch (msgType) {
			case TEXT:	//文本消息
				SpannableString spannableString = SystemUtil.getExpressionString(mContext, msgInfo.getContent());
				holder.tvContent.setText(spannableString);
				break;
			case IMAGE:	//图片消息
				holder.tvContent.setText("");
				int extraSpace = Math.abs(paddingLeft - paddingRight) + 3 * extraPad; 
				if (type == TYPE_IN) {	//接收的消息
					holder.tvContent.setPadding(extraSpace, extraPad, extraPad, extraPad);
				} else {
					holder.tvContent.setPadding(extraPad, extraPad, extraSpace, extraPad);
				}
				if (msgPart != null) {
					String filePath = msgPart.getFilePath();
					if (SystemUtil.isFileExists(filePath)) {
						mImageLoader.displayImage(Scheme.FILE.wrap(filePath), new TextViewAware(holder.tvContent), chatImageOptions);
					} else {
						mImageLoader.displayImage(null, new TextViewAware(holder.tvContent), chatImageOptions);
					}
				}
				
				break;
			case VOICE:	//语音文件
				//TODO 等待解决
				Drawable drawable = null;
				holder.tvContent.setText("");
				holder.tvContent.setCompoundDrawablePadding(10);
				if (type == TYPE_OUT) {	//自己发出去的消息
					drawable = getResources().getDrawable(R.drawable.chat_voice_out);
					/// 这一步必须要做,否则不会显示.
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					holder.tvContent.setCompoundDrawables(null, null, drawable, null);
				} else {
					drawable = getResources().getDrawable(R.drawable.chat_voice_in);
					/// 这一步必须要做,否则不会显示.
					drawable.setBounds(drawable.getMinimumWidth(), drawable.getMinimumHeight(), 0, 0);
					holder.tvContent.setCompoundDrawables(drawable, null, null, null);
				}
				break;
			case AUDIO:
			case VIDEO:
			case FILE:	//普通文件类型
				holder.tvContent.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
				holder.tvContent.setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.chat_msg_item_drawable_spacing));
				if (msgPart != null) {
					String partPath = msgPart.getFilePath();
					String fileName = msgPart.getFileName();
					String sizeStr = SystemUtil.sizeToString(msgPart.getSize());
					
					FileItem fileItem = SystemUtil.getFileItem(partPath, fileName, msgPart.getMimeTye());
					
					int fileNameLength = fileName.length();
					
					String str = getString(R.string.chat_attach_file_desc, fileName, sizeStr);
					SpannableStringBuilder spannableDesc = new SpannableStringBuilder(str);
					spannableDesc.setSpan(new TextAppearanceSpan(context, R.style.ChatItemContentTitleStyle), 0, fileNameLength, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					spannableDesc.setSpan(new TextAppearanceSpan(context, R.style.ChatItemContentSubTitleStyle), fileNameLength, str.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					holder.tvContent.setText(spannableDesc, TextView.BufferType.SPANNABLE);
					
					Integer resId = SystemUtil.getResIdByFile(fileItem, R.drawable.ic_attach_file);
					holder.tvContent.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
					
					switch (fileItem.getFileType()) {
					case IMAGE:	//图片,则直接加载图片缩略图
						holder.tvContent.setCompoundDrawablePadding(0);
						holder.tvContent.setText("");
						if (SystemUtil.isFileExists(partPath)) {
							mImageLoader.displayImage(Scheme.FILE.wrap(partPath), new TextViewAware(holder.tvContent), chatImageOptions);
						}
						break;
					case APK:	//安装文件
						new LoadApkIconTask(holder).execute(partPath);
						break;
					default:
						break;
					}
				}
				break;
			case LOCATION:	//地理位置
				holder.tvContent.setText("");
				int locationXxtraSpace = Math.abs(paddingLeft - paddingRight) + 3 * extraPad; 
				if (type == TYPE_IN) {	//接收的消息
					holder.tvContent.setPadding(locationXxtraSpace, extraPad, extraPad, extraPad);
				} else {
					holder.tvContent.setPadding(extraPad, extraPad, locationXxtraSpace, extraPad);
				}
				if (msgPart != null) {
					holder.tvContentDesc.setVisibility(View.VISIBLE);
					String filePath = msgPart.getFilePath();
					ImageSize imageSize = new ImageSize(Constants.IMAGE_LOCATION_THUMB_WIDTH, Constants.IMAGE_LOCATION_THUMB_HEIGHT);
					if (SystemUtil.isFileExists(filePath)) {
						mImageLoader.displayImage(Scheme.FILE.wrap(filePath), new TextViewAware(holder.tvContent, imageSize), chatImageOptions, new MyImageLoaderListener(holder.tvContentDesc, type, msgInfo));
					} else {
						mImageLoader.displayImage(null, new TextViewAware(holder.tvContent, imageSize), chatImageOptions, new MyImageLoaderListener(holder.tvContentDesc, type, msgInfo));
					}
				}
				break;
			default:
				break;
			}
			
//			holder.ivHeadIcon.setImageResource(R.drawable.ic_chat_default_big_head_icon);
			
			if (type == TYPE_OUT) {	//自己发送的消息
				
				//显示自己的头像
				String iconPath = mine.getIconPath();
				if (SystemUtil.isFileExists(iconPath)) {
					mImageLoader.displayImage(Scheme.FILE.wrap(iconPath), holder.ivHeadIcon, headIconOptions);
				} else {
					mImageLoader.displayImage(null, holder.ivHeadIcon, headIconOptions);
				}
				
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
			} else {	//接收的消息，对方发送的消息
				//显示用户图像
				UserVcard otherVcard = otherSide.getUserVcard();
				if (otherVcard != null) {
					String iconPath = otherVcard.getIconPath();
					if (SystemUtil.isFileExists(iconPath)) {
						mImageLoader.displayImage(Scheme.FILE.wrap(iconPath), holder.ivHeadIcon, headIconOptions);
					} else {
						mImageLoader.displayImage(null, holder.ivHeadIcon, headIconOptions);
					}
	 			} else {
	 				mImageLoader.displayImage(null, holder.ivHeadIcon, headIconOptions);
	 			}
				
				holder.ivMsgState.setVisibility(View.GONE);
				if (!msgInfo.isRead()) {	//未读，则更新读取状态
					msgInfo.setRead(true);
					msgInfo = msgManager.updateMsgInfo(msgInfo);
				}
			}
			final String title = msgInfo.getFromUser();
			holder.tvContent.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					AlertDialog dialog = builder.setTitle(title)
						.setItems(new CharSequence[] {"复制", "删除"}, null)
						.create();
					dialog.show();
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
	
	class MyImageLoaderListener implements ImageLoadingListener {
		private TextView tvDesc;
		private int itemType;
		private MsgInfo msgInfo;

		public MyImageLoaderListener(TextView tvDesc, int itemType, MsgInfo msgInfo) {
			super();
			this.tvDesc = tvDesc;
			this.itemType = itemType;
			this.msgInfo = msgInfo;
		}

		@Override
		public void onLoadingStarted(String imageUri, View view) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			// TODO Auto-generated method stub
			if (loadedImage != null) {
				if (view instanceof TextView) {
					TextView textView = (TextView) view;
					Drawable drawable = ImageUtil.bitmapToDrawable(loadedImage);
					if (itemType == MsgAdapter.TYPE_IN) {	//接收的消息
						textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
					} else {
						textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
					}
				}
				tvDesc.setMaxWidth(loadedImage.getWidth());
				tvDesc.setText(msgInfo.getContent());
			}
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * 消息item的点击事件
	 * @author huanghui1
	 * @update 2015年2月11日 上午11:14:35
	 */
	class MsgItemClickListener implements View.OnClickListener {
		/**
		 * 类型分为两种，TYPE_IN和TYPE_OUT
		 */
		private int itemType;
		private MsgInfo msgInfo;
		private int position;

		public MsgItemClickListener(int itemType, MsgInfo msgInfo, int position) {
			super();
			this.itemType = itemType;
			this.msgInfo = msgInfo;
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			MsgInfo.Type msgType = msgInfo.getMsgType();
			switch (msgType) {
			case VOICE:	//语音类型的消息
				MsgPart msgPart = msgInfo.getMsgPart();
				if (msgPart != null) {
					String filePath = msgPart.getFilePath();
					if (SystemUtil.isFileExists(filePath)) {
						playVoice(filePath, position, itemType, v);
					} else {
						SystemUtil.makeShortToast(R.string.file_not_exists);
					}
				}
				break;

			default:
				break;
			}
		}
		
	}
	
	/**
	 * 播放语音的初始化操作
	 * @update 2015年2月11日 下午9:25:15
	 * @param filePath 要播放的文件路径
	 * @param position 当前播放的位置索引
	 * @param itemType 当前播放的消息类型，主要分为接收的消息和发送的消息
	 * @param view
	 */
	private void playVoice(String filePath, int position, int itemType, View view) {
		TextView textView = (TextView) view;
		AnimationDrawable animationDrawable = null;
		if (itemType == MsgAdapter.TYPE_IN) {	//接收的消息
			animationDrawable = (AnimationDrawable) mContext.getResources().getDrawable(R.drawable.chat_voice_play_in_anim);
			animationDrawable.setBounds(animationDrawable.getMinimumWidth(), animationDrawable.getMinimumHeight(), 0, 0);
			textView.setCompoundDrawables(animationDrawable, null, null, null);
		} else {	//发送的消息
			animationDrawable = (AnimationDrawable) mContext.getResources().getDrawable(R.drawable.chat_voice_play_out_anim);
			animationDrawable.setBounds(0, 0, animationDrawable.getMinimumWidth(), animationDrawable.getMinimumHeight());
			textView.setCompoundDrawables(null, null, animationDrawable, null);
		}
		animationDrawable.setOneShot(false);
		if (mPlayingPosition == position) {
			if (mIsPlaying) {	//如果正在播放，就停止播放，反之，则开始播放
				stopPlaying(animationDrawable, itemType, textView);
			} else {
				startPlaying(filePath, animationDrawable, itemType, textView);
			}
		} else {	//如果点击的不是当前播放的条目
			if (mPlayingView != null) {
				stopPlaying(mPlayingAnimation, mPlayingType, mPlayingView);
			}
			startPlaying(filePath, animationDrawable, itemType, textView);
		}
		mPlayingPosition = position;
		mPlayingType = itemType;
		mPlayingView = textView;
		Drawable[] drawables = textView.getCompoundDrawables();
		if (SystemUtil.isNotEmpty(drawables)) {
			if (drawables[0] instanceof AnimationDrawable) {
				mPlayingAnimation = (AnimationDrawable) drawables[0];
			} else {
				mPlayingAnimation = null;
			}
		} else {
			mPlayingAnimation = null;
		}
	}
	
	/**
	 * 停止播放语音文件
	 * @update 2015年2月11日 下午9:10:13
	 * @param filePath 播放的文件的全路径
	 * @param animation 动画图片资源
	 * @param itemType 消息类型，分为接收的消息和发出的消息
	 * @param view 填充资源图片的view
	 */
	public void stopPlaying(AnimationDrawable animation, int itemType, TextView view) {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
		if (animation != null && animation.isRunning()) {
			animation.stop();
		}
		Drawable drawable = null;
		if (itemType == MsgAdapter.TYPE_IN) { //in
			drawable = mContext.getResources().getDrawable(R.drawable.chat_voice_in);
			drawable.setBounds(drawable.getMinimumWidth(), drawable.getMinimumHeight(), 0, 0);
			view.setCompoundDrawables(drawable, null, null, null);
		} else if (itemType == MsgAdapter.TYPE_OUT) {
			drawable = mContext.getResources().getDrawable(R.drawable.chat_voice_out);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			view.setCompoundDrawables(null, null, drawable, null);
		}
		mIsPlaying = false;
	}
	
	/**
	 * 停止播放语音文件
	 * @update 2015年2月11日 下午9:14:00
	 * @param animation
	 * @param itemType
	 * @param view
	 */
	public void startPlaying(String filePath, AnimationDrawable animation, int itemType, TextView view) {
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(new MyOnCompletionListener(animation, itemType, view));
		try {
			//设置要播放的文件
			mPlayer.setDataSource(filePath);
			mPlayer.prepare();
			//播放
			mPlayer.start();
			animation.start();
			mIsPlaying = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 语音播放完成的监听器
	 * @author huanghui1
	 * @update 2015年2月11日 下午9:14:57
	 */
	class MyOnCompletionListener implements OnCompletionListener {
		private AnimationDrawable animation;
		private int itemType;
		private TextView view;

		public MyOnCompletionListener(AnimationDrawable animation,
				int itemType, TextView view) {
			super();
			this.animation = animation;
			this.itemType = itemType;
			this.view = view;
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mp != null) {
				stopPlaying(animation, itemType, view);
			}
		}
		
	}
	
	/**
	 * 异步加载apk图标的线程
	 * @author huanghui1
	 * @update 2014年11月21日 下午6:03:44
	 */
	class LoadApkIconTask extends AsyncTask<String, Drawable, Drawable> {
		MsgViewHolder holder;
		public LoadApkIconTask(MsgViewHolder holder) {
			super();
			this.holder = holder;
		}
		@Override
		protected Drawable doInBackground(String... params) {
			Drawable drawable = SystemUtil.getApkIcon(params[0]);
			return drawable;
		}
		@Override
		protected void onPostExecute(Drawable result) {
			if(result != null) {
				holder.tvContent.setCompoundDrawablesWithIntrinsicBounds(result, null, null, null);
			}
			super.onPostExecute(result);
		}
	}
	
	final static class MsgViewHolder {
		TextView tvMsgTime;
		RelativeLayout layoutBody;
		ImageView ivHeadIcon;
		TextView tvContent;
		TextView tvContentDesc;
		ImageView ivMsgState;
	}
	
	/**
	 * 处理消息的广播
	 * @author huanghui1
	 * @update 2014年11月17日 上午9:20:42
	 */
	public class MsgProcessReceiver extends BroadcastReceiver {
		public static final String ACTION_PROCESS_MSG = "net.ibaixin.chat.PROCESS_ACCEPT_MSG_RECEIVER";
		public static final String ACTION_REFRESH_MSG = "net.ibaixin.chat.REFRESH_ACCEPT_MSG_RECEIVER";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action) {
			case ACTION_PROCESS_MSG:	//处理接收的聊天消息
				MsgInfo msgInfo = intent.getParcelableExtra(ARG_MSG_INFO);
				if (msgInfo != null) {
					mMsgInfos.add(msgInfo);
					msgAdapter.notifyDataSetChanged();
				}
				break;
			case ACTION_REFRESH_MSG:	//刷新消息列表
				msgAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
			if (etContent.hasFocus() && !lvMsgs.hasFocus()) {	//有焦点就滚动到最后一条记录
				scrollMyListViewToBottom(lvMsgs);
			}
		}
		
	}
	
	/**
	 * 消息监听的观察者
	 * @author huanghui1
	 * @update 2014年11月6日 下午7:27:19
	 */
	class MsgContentObserver extends ContentObserver {

		public MsgContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if (uri != null) {
				MsgInfo msgInfo = msgManager.getMsgInfoByUri(uri);
				if (msgInfo != null) {
					if (!mMsgInfos.contains(msgInfo)) {
						mMsgInfos.add(msgInfo);
					}
					msgAdapter.notifyDataSetChanged();
					if (etContent.hasFocus() && !lvMsgs.hasFocus()) {	//有焦点就滚动到最后一条记录
						scrollMyListViewToBottom(lvMsgs);
					}
				}
			} else {
				onChange(selfChange);
			}
		}

		@Override
		public void onChange(boolean selfChange) {
			pageOffset = 0;
			new LoadDataTask(false).execute();
		}
		
	}

	@Override
	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiTypeFragment.input(etContent, emojicon);
	}

	@Override
	public void onEmojiconBackspaceClicked(View v) {
		EmojiTypeFragment.backspace(etContent);
	}

}
