package com.example.chat.activity;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chat.ChatApplication;
import com.example.chat.R;
import com.example.chat.manage.UserManager;
import com.example.chat.model.Personal;
import com.example.chat.model.User;
import com.example.chat.model.UserVcard;
import com.example.chat.util.Constants;
import com.example.chat.util.SystemUtil;
import com.example.chat.util.XmppConnectionManager;
import com.example.chat.util.XmppUtil;

/**
 * 好友详情界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月10日 下午8:16:59
 */
public class UserInfoActivity extends BaseActivity {
	public static final String ARG_USER = "arg_user";
	public static final String ARG_OPTION = "arg_option";
	public static final String ARG_USER_TYPE = "arg_user_type";
	
	public static final int TYPE_SELF = 1;
	public static final int TYPE_FRIEND = 2;
	public static final int TYPE_STRANGER = 3;
	
	/**
	 * 搜索好友进入的该界面
	 */
	public static final int OPTION_SEARCH = 1;
	/**
	 * 查看好友详情进入的该界面
	 */
	public static final int OPTION_LOAD = 2;

	private ImageView ivHeadIcon;
	private TextView tvUsername;
	private TextView tvNickname;
	private TextView tvRealname;
	private TextView tvEmail;
	private TextView tvAddress;
	private TextView tvMobile;
	private Button btnOpt;
	
	/**
	 * 被查看的人的类型，有"自己"、"本地好友"、"陌生人"，默认是陌生人
	 */
	private int userType = TYPE_STRANGER;
	
	ProgressDialog pDialog;
	
	private User user;
	
	private UserManager userManager = UserManager.getInstance();
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.MSG_SHOW_USR_ICON:	//显示好友头像
				ivHeadIcon.setImageBitmap((Bitmap) msg.obj);
				break;
			case Constants.MSG_CONNECTION_UNAVAILABLE:	//客户端与服务器没有连接
				hideLoadingDialog(pDialog);
				new AlertDialog.Builder(mContext)
					.setTitle(R.string.prompt)
					.setMessage(R.string.connection_unavailable)
					.setNegativeButton(android.R.string.cancel, null)
					.setCancelable(false)
					.setPositiveButton(android.R.string.ok, null).show();
				break;
			case Constants.MSG_SEND_ADD_FRIEND_REQUEST:	//发送添加好友的请求
				hideLoadingDialog(pDialog);
				SystemUtil.makeShortToast(R.string.contact_send_add_friend_request_success);
				btnOpt.setEnabled(false);
				break;
			default:
				break;
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected int getContentView() {
		return R.layout.activity_user_info;
	}

	@Override
	protected void initWidow() {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	}

	@Override
	protected void initView() {
		ivHeadIcon = (ImageView) findViewById(R.id.iv_head_icon);
		tvUsername = (TextView) findViewById(R.id.tv_username);
		tvNickname = (TextView) findViewById(R.id.tv_nickname);
		tvRealname = (TextView) findViewById(R.id.tv_realname);
		tvEmail = (TextView) findViewById(R.id.tv_email);
		tvAddress = (TextView) findViewById(R.id.tv_address);
		tvMobile = (TextView) findViewById(R.id.tv_mobile);
		btnOpt = (Button) findViewById(R.id.btn_opt);
	}

	@Override
	protected void initData() {
		//显示进度条
		setProgressBarIndeterminateVisibility(true);
		user = getIntent().getParcelableExtra(ARG_USER);
		if (user != null) {
			
			tvUsername.setText(getString(R.string.username, user.getUsername()));
			String nickname = user.getNickname();
			if (!TextUtils.isEmpty(nickname)) {
				tvNickname.setVisibility(View.VISIBLE);
				tvNickname.setText(getString(R.string.nickname, user.getNickname()));
			} else {
				tvNickname.setVisibility(View.GONE);
			}
			String email = user.getEmail();
			if (!TextUtils.isEmpty(email)) {
				tvEmail.setVisibility(View.VISIBLE);
				tvEmail.setText(getString(R.string.email, email));
			} else {
				tvEmail.setVisibility(View.GONE);
			}
			
			int opt = getIntent().getIntExtra(ARG_OPTION, 0);
			switch (opt) {
			case OPTION_LOAD:	//查看好友详情
				showLocalFriendVcard(user);
				break;
			case OPTION_SEARCH:	//搜索好友的详情
				userType = getIntent().getIntExtra(ARG_USER_TYPE, TYPE_STRANGER);
				switch (userType) {
				case TYPE_STRANGER:	//陌生人
					new LoadVcardTask().execute(user.getJID());
					break;
				case TYPE_FRIEND:	//好友
					User localUser = userManager.loadLocalFriend(user.getUsername());
					if (localUser != null) {
						user = localUser;
						showLocalFriendVcard(user);
					}
					break;
				case TYPE_SELF:	//自己
					showSelfVcard(ChatApplication.getInstance().getCurrentUser());
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 显示本地好友的信息
	 * @update 2014年10月24日 下午5:06:34
	 * @param user
	 */
	private void showLocalFriendVcard(User user) {
		userType = TYPE_FRIEND;
		btnOpt.setText(R.string.contact_send_msg);
		UserVcard uCard = user.getUserVcard();
		if (uCard != null) {
			Bitmap icon = SystemUtil.getImageFromLocal(uCard.getIconPath());
			if (icon != null) {
				ivHeadIcon.setImageBitmap(icon);
			}
			new LoadFriendInfoTask().execute(uCard);
		}
	}
	
	/**
	 * 显示个人信息
	 * @update 2014年10月24日 下午5:27:11
	 * @param personal
	 */
	private void showSelfVcard(Personal personal) {
		btnOpt.setText(R.string.contact_modify);
		String province = personal.getProvince() == null ? "" : personal.getProvince();
		String city = personal.getCity() == null ? "" : personal.getCity();
		String address =  province + " " + city;
		tvAddress.setText(address);
		
		String phone = personal.getPhone() == null ? "" : personal.getPhone();
		tvMobile.setText(phone);
		
		String realname = personal.getRealName();
		if (!TextUtils.isEmpty(realname)) {
			tvRealname.setVisibility(View.VISIBLE);
			tvRealname.setText(getString(R.string.realname, realname));
		} else {
			tvRealname.setVisibility(View.GONE);
		}
		Bitmap icon = SystemUtil.getImageFromLocal(personal.getIconPath());
		if (icon != null) {
			ivHeadIcon.setImageBitmap(icon);
		}
	}
	
	@Override
	protected void addListener() {
		btnOpt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch (userType) {
				case TYPE_STRANGER:	//不是本地好友，则发送添加好友的请求
					if (pDialog == null) {
						pDialog = ProgressDialog.show(mContext, null, getString(R.string.loading));
					} else {
						pDialog.show();
					}
					mHandler.post(new Runnable() {
						
						@Override
						public void run() {
							Message msg = mHandler.obtainMessage();
							try {
								XmppUtil.addFriend(XmppConnectionManager.getInstance().getConnection(), user.getJID());
								msg.what = Constants.MSG_SEND_ADD_FRIEND_REQUEST;
							} catch (NotConnectedException e) {
								e.printStackTrace();
								msg.what = Constants.MSG_CONNECTION_UNAVAILABLE;
							}
							mHandler.sendMessage(msg);
						}
					});
					break;
				case TYPE_FRIEND:	//是本地好友，则发送消息
					SystemUtil.makeShortToast("发送消息");
					Intent intent = new Intent(mContext, ChatActivity.class);
					startActivity(intent);
					break;
				case TYPE_SELF:	//自己
					SystemUtil.makeShortToast("编辑个人信息");
					break;
				default:
					break;
				}
			}
		});
	}
	
	/**
	 * 加载好友电子名片
	 * @author huanghui1
	 * @update 2014年10月10日 下午10:25:58
	 */
	class LoadFriendInfoTask extends AsyncTask<UserVcard, Void, UserVcard> {

		@Override
		protected UserVcard doInBackground(UserVcard... params) {
			UserVcard uCard = userManager.getUserVcardById(params[0].getId());
			return uCard;
			/*VCard card = XmppUtil.getUserVcard(XmppConnectionManager.getInstance().getConnection(), params[0]);
			if (card != null) {
				Bitmap icon = XmppUtil.getUserIcon(card);
				if (icon != null) {
					Message msg = mHandler.obtainMessage(Constants.MSG_SHOW_USR_ICON);
					msg.obj = icon;
					mHandler.sendMessage(msg);
				}
			}
			return card;*/
		}
		
		@Override
		protected void onPostExecute(UserVcard result) {
			if (result != null) {
				user.setUserVcard(result);
				
				String province = result.getProvince() == null ? "" : result.getProvince();
				String city = result.getCity() == null ? "" : result.getCity();
				String address =  province + " " + city;
				tvAddress.setText(address);
				
				String phone = result.getMobile() == null ? "" : result.getMobile();
				tvMobile.setText(phone);
				
				String realname = result.getRealName();
				if (!TextUtils.isEmpty(realname)) {
					tvRealname.setVisibility(View.VISIBLE);
					tvRealname.setText(getString(R.string.realname, realname));
				} else {
					tvRealname.setVisibility(View.GONE);
				}
			}
//			if (result != null) {
//				tvAddress.setText(result.getAddressFieldHome("REGION") + " " + result.getAddressFieldHome("LOCALITY"));	//省市
//				tvNickname.setText(getString(R.string.nickname, result.getNickName()));
//			}
			setProgressBarIndeterminateVisibility(false);
		}
		
	}
	
	/**
	 * 加用户电子名片，用户搜索并显示好友的情况
	 * @author huanghui1
	 * @update 2014年10月10日 下午10:25:58
	 */
	class LoadVcardTask extends AsyncTask<String, Void, VCard> {
		
		@Override
		protected VCard doInBackground(String... params) {
			VCard card = XmppUtil.getUserVcard(XmppConnectionManager.getInstance().getConnection(), params[0]);
			if (card != null) {
				Bitmap icon = XmppUtil.getUserIcon(card);
				if (icon != null) {
					Message msg = mHandler.obtainMessage(Constants.MSG_SHOW_USR_ICON);
					msg.obj = icon;
					mHandler.sendMessage(msg);
				}
			}
			return card;
		}
		
		@Override
		protected void onPostExecute(VCard result) {
			if (result != null) {
				String province = result.getAddressFieldHome("REGION");
				province = province == null ? "" : province;
				String city = result.getAddressFieldHome("LOCALITY");
				city = city == null ? "" : city;
				tvAddress.setText(province + " " + city);	//省市
				
				String phone = result.getPhoneHome("CELL");
				phone = phone == null ? "" : phone;
				tvMobile.setText(phone);
				
				String realname = result.getLastName();
				if (!TextUtils.isEmpty(realname)) {
					tvRealname.setVisibility(View.VISIBLE);
					tvRealname.setText(getString(R.string.realname, realname));
				} else {
					tvRealname.setVisibility(View.GONE);
				}
				
				String nickname = result.getNickName();
				if (!TextUtils.isEmpty(nickname)) {
					tvNickname.setVisibility(View.VISIBLE);
					tvNickname.setText(getString(R.string.nickname, nickname));
				} else {
					tvNickname.setVisibility(View.GONE);
				}
			}
			setProgressBarIndeterminateVisibility(false);
		}
		
	}

}
