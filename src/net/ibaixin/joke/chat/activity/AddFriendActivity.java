package net.ibaixin.joke.chat.activity;

import java.util.ArrayList;
import java.util.List;

import net.ibaixin.joke.chat.ChatApplication;
import net.ibaixin.joke.chat.R;
import net.ibaixin.joke.chat.manage.UserManager;
import net.ibaixin.joke.chat.model.User;
import net.ibaixin.joke.chat.util.Constants;
import net.ibaixin.joke.chat.util.SystemUtil;
import net.ibaixin.joke.chat.util.XmppConnectionManager;
import net.ibaixin.joke.chat.util.XmppUtil;

import org.jivesoftware.smack.SmackException.NotConnectedException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 添加好友界面，主要是查询好友
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月9日 下午9:11:37
 */
public class AddFriendActivity extends BaseActivity {
	
	private EditText etUsername;
	private Button btnSearch;
	private ListView lvResult;
	private TextView emptyView;
	
	private List<User> users = new ArrayList<>();
	private FriendResultAdapter adapter;
	private ProgressDialog pDialog;
	
	private UserManager userManager = UserManager.getInstance();
	
	private int userType = UserInfoActivity.TYPE_STRANGER;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
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
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected int getContentView() {
		return R.layout.activity_search_friend;
	}

	@Override
	protected void initView() {
		etUsername = (EditText) findViewById(R.id.et_username);
		btnSearch = (Button) findViewById(R.id.btn_search);
		lvResult = (ListView) findViewById(R.id.lv_result);
		emptyView = (TextView) findViewById(R.id.empty_view);
	}

	@Override
	protected void initData() {
//		users = new ArrayList<>();
//		adapter = new FriendResultAdapter(users, mContext);
//		lvResult.setAdapter(adapter);
		
		lvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ViewHolder holder = (ViewHolder) view.getTag();
				User user = users.get(position);
				Intent intent = new Intent(mContext, UserInfoActivity.class);
				intent.putExtra(UserInfoActivity.ARG_USER, user);
				intent.putExtra(UserInfoActivity.ARG_OPTION, UserInfoActivity.OPTION_SEARCH);
				intent.putExtra(UserInfoActivity.ARG_USER_TYPE, holder.typtTag);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void addListener() {
		etUsername.addTextChangedListener(new TextWatcher() {
			
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
				// TODO Auto-generated method stub
				if(TextUtils.isEmpty(s)) {
					btnSearch.setEnabled(false);
				} else {
					btnSearch.setEnabled(true);
				}
			}
		});
		
		btnSearch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String username = etUsername.getText().toString();
				new SearchTask().execute(username);
			}
		});
	}
	
	/**
	 * 搜索好友
	 * @author huanghui1
	 * @update 2014年10月9日 下午9:38:32
	 */
	class SearchTask extends AsyncTask<String, Void, List<User>> {
		@Override
		protected void onPreExecute() {
			if (pDialog == null) {
				pDialog = ProgressDialog.show(mContext, null, getString(R.string.contact_searching), true, true);
			} else {
				pDialog.show();
			}
		}

		@Override
		protected List<User> doInBackground(String... params) {
			List<User> list = XmppUtil.searchUser(XmppConnectionManager.getInstance().getConnection(), params[0]);
			if (list != null && list.size() > 0) {
				users.clear();
				users.addAll(list);
				return list;
			} else {
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(List<User> result) {
			if(adapter == null) {
				adapter = new FriendResultAdapter(users, mContext);
				lvResult.setAdapter(adapter);
				lvResult.setEmptyView(emptyView);
			} else {
				adapter.notifyDataSetChanged();
			}
			hideLoadingDialog(pDialog);
			
		}
		
	}
	
	/**
	 * 搜索好友的适配器
	 * @author huanghui1
	 * @update 2014年10月9日 下午10:46:08
	 */
	class FriendResultAdapter extends CommonAdapter<User> {

		public FriendResultAdapter(List<User> list, Context context) {
			super(list, context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.item_search_friend, parent, false);
				
				holder.tvUsername = (TextView) convertView.findViewById(R.id.tv_username);
				holder.tvNickname = (TextView) convertView.findViewById(R.id.tv_nickname);
				holder.btnAdd = (Button) convertView.findViewById(R.id.btn_add);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final User user = list.get(position);
			String username = user.getUsername();
			holder.tvUsername.setText(username);
			holder.tvNickname.setText(user.getNickname());
			
			//是否是自己
			boolean isSelf = ChatApplication.getInstance().isSelf(username);
			if (isSelf) {
				userType = UserInfoActivity.TYPE_SELF;
			} else {
				boolean isFriend = userManager.isLocalFriend(username);
				if (isFriend) {//是本地好友
					userType = UserInfoActivity.TYPE_FRIEND;
				} else {//本地没有该人的信息，则从网上加载
					userType = UserInfoActivity.TYPE_STRANGER;
				}
			}
			holder.typtTag = userType;
			final String jid = user.getJID();
			switch (userType) {
			case UserInfoActivity.TYPE_STRANGER:	//陌生人
				holder.btnAdd.setText(R.string.add);
				break;
			case UserInfoActivity.TYPE_SELF:
			case UserInfoActivity.TYPE_FRIEND:
				holder.btnAdd.setText(R.string.show);
				break;
			default:
				break;
			}
			holder.btnAdd.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					switch (userType) {
					case UserInfoActivity.TYPE_STRANGER:	//陌生人
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								Message msg = mHandler.obtainMessage();
								try {
									XmppUtil.addFriend(XmppConnectionManager.getInstance().getConnection(), jid);
									msg.what = Constants.MSG_SEND_ADD_FRIEND_REQUEST;
								} catch (NotConnectedException e) {
									e.printStackTrace();
									msg.what = Constants.MSG_CONNECTION_UNAVAILABLE;
								}
								mHandler.sendMessage(msg);
							}
						});
						break;
					case UserInfoActivity.TYPE_SELF:
					case UserInfoActivity.TYPE_FRIEND:
						Intent intent = new Intent(mContext, UserInfoActivity.class);
						intent.putExtra(UserInfoActivity.ARG_USER, user);
						intent.putExtra(UserInfoActivity.ARG_OPTION, UserInfoActivity.OPTION_SEARCH);
						intent.putExtra(UserInfoActivity.ARG_USER_TYPE, userType);
						startActivity(intent);
						break;
					default:
						break;
					}
				}
			});
			return convertView;
		}
		
	}
	
	/**
	 * listview item的缓存
	 * @author huanghui1
	 * @update 2014年10月9日 下午10:47:22
	 */
	private final class ViewHolder {
		TextView tvUsername;
		TextView tvNickname;
		Button btnAdd;
		int typtTag = UserInfoActivity.TYPE_STRANGER;
	}

}
