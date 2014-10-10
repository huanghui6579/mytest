package com.example.chat.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.model.User;
import com.example.chat.util.SystemUtil;
import com.example.chat.util.XmppConnectionManager;

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
	
	private List<User> users;
	private FriendResultAdapter adapter;
	private ProgressDialog pDialog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

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
			if(pDialog == null) {
				pDialog = ProgressDialog.show(mContext, null, getString(R.string.contact_searching));
			}
		}

		@Override
		protected List<User> doInBackground(String... params) {
			return XmppUtil.searchUser(XmppConnectionManager.getInstance().getConnection(), params[0]);
		}
		
		@Override
		protected void onPostExecute(List<User> result) {
			if(adapter == null) {
				users = new ArrayList<>();
				adapter = new FriendResultAdapter(users, mContext);
				lvResult.setAdapter(adapter);
				lvResult.setEmptyView(emptyView);
			}
			hideLoadingDialog(pDialog);
			users.clear();
			if(result != null && result.size() > 0) {
				users.addAll(result);
			}
			adapter.notifyDataSetChanged();
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
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_search_friend, parent, false);
				
				holder.tvUsername = (TextView) convertView.findViewById(R.id.tv_username);
				holder.tvNickname = (TextView) convertView.findViewById(R.id.tv_nickname);
				holder.btnAdd = (Button) convertView.findViewById(R.id.btn_add);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final User user = list.get(position);
			holder.tvUsername.setText(user.getUsername());
			holder.tvNickname.setText(user.getNickname());
			holder.btnAdd.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					SystemUtil.makeShortToast("添加好友：" + user.getUsername());
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
	}

}
