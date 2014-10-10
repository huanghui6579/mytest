package com.example.chat.activity;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.model.User;
import com.example.chat.util.Constants;
import com.example.chat.util.XmppConnectionManager;

/**
 * 好友详情界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月10日 下午8:16:59
 */
public class UserInfoActivity extends BaseActivity {

	private ImageView ivHeadIcon;
	private TextView tvUsername;
	private TextView tvNickname;
	private TextView tvEmail;
	private TextView tvAddress;
	private TextView tvSignature;
	private Button btnAddFriend;
	
	ProgressDialog pDialog;
	
	private User user;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.MSG_SHOW_USR_ICON:	//显示好友头像
				ivHeadIcon.setImageBitmap((Bitmap) msg.obj);
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
		tvEmail = (TextView) findViewById(R.id.tv_email);
		tvAddress = (TextView) findViewById(R.id.tv_address);
		tvSignature = (TextView) findViewById(R.id.tv_signature);
		btnAddFriend = (Button) findViewById(R.id.btn_add_friend);
	}

	@Override
	protected void initData() {
		//显示进度条
		setProgressBarIndeterminateVisibility(true);
		user = getIntent().getParcelableExtra(AddFriendActivity.ARG_USER);
		if (user != null) {
			new LoadVcardTask().execute(user.getJID());
			
			tvUsername.setText("用户名：" + user.getUsername());
//			tvNickname.setText("昵称：" + user.getNickname());
			tvEmail.setText("邮箱：" + user.getEmail());
			
		}
	}

	@Override
	protected void addListener() {
		btnAddFriend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				if (pDialog == null) {
//					pDialog = ProgressDialog.show(mContext, null, "正在处理数据，请稍后...");
//				}
			}
		});
	}
	
	/**
	 * 加载用户电子名片
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
				tvAddress.setText(result.getAddressFieldHome("REGION") + " " + result.getAddressFieldHome("LOCALITY"));	//省市
				tvNickname.setText("昵称：" + result.getNickName());
			}
			setProgressBarIndeterminateVisibility(false);
		}
		
	}

}
