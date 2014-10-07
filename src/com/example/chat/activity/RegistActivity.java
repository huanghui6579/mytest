package com.example.chat.activity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Registration;

import com.example.chat.R;
import com.example.chat.model.SystemConfig;
import com.example.chat.util.Constants;
import com.example.chat.util.Log;
import com.example.chat.util.SystemUtil;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 注册界面
 * @author Administrator
 * @version 2014年10月7日 下午3:13:07
 */
public class RegistActivity extends BaseActivity implements OnClickListener {
	
	private static final int REGIST_RESULT_SUCCESS = 1;
	private static final int REGIST_RESULT_CONFLICT = 2;	//账号已存在
	private static final int REGIST_RESULT_FAIL = 3;
	
	private EditText etAccount;
	private EditText etNickname;
	private EditText etEmail;
	private EditText etPassword;
	private EditText etConfirmPassword;
	
	private Button btnRegist;
	
	private TextView tvLogin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
	}

	@Override
	protected int getContentView() {
		return R.layout.activity_regist;
	}

	@Override
	protected void initView() {
		etAccount = (EditText) findViewById(R.id.et_account);
		etNickname = (EditText) findViewById(R.id.et_nickname);
		etEmail = (EditText) findViewById(R.id.et_email);
		etPassword = (EditText) findViewById(R.id.et_password);
		etConfirmPassword = (EditText) findViewById(R.id.et_confirm_password);
		
		btnRegist = (Button) findViewById(R.id.btn_regist);
		tvLogin = (TextView) findViewById(R.id.tv_login);
	}

	@Override
	protected void initData() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub
		etAccount.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String password = etPassword.getText().toString();
				String confirmPassword = etConfirmPassword.getText().toString();
				if (TextUtils.isEmpty(s) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
					setRegistBtnState(false);
				} else if (!password.equals(confirmPassword)) {
					setRegistBtnState(false);
				} else {
					setRegistBtnState(true);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		etPassword.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String confirmPassword = etConfirmPassword.getText().toString();
				if (TextUtils.isEmpty(s) || TextUtils.isEmpty(etAccount.getText().toString()) || TextUtils.isEmpty(confirmPassword)) {
					setRegistBtnState(false);
				} else if (!s.toString().equals(confirmPassword)) {
					setRegistBtnState(false);
				} else {
					setRegistBtnState(true);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		etConfirmPassword.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String password = etPassword.getText().toString();
				if (TextUtils.isEmpty(s) || TextUtils.isEmpty(etAccount.getText().toString()) || TextUtils.isEmpty(password)) {
					setRegistBtnState(false);
				} else if (!s.toString().equals(password)) {
					setRegistBtnState(false);
				} else {
					setRegistBtnState(true);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		etConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE || actionId == KeyEvent.ACTION_DOWN) {
					SystemUtil.hideSoftInput(v);
					v.clearFocus();
					if (!systemConfig.isOnline()) {
						new RegistTask().execute(getConnection());
					}
					return true;
				}
				return false;
			}
		});
		
		btnRegist.setOnClickListener(this);
		tvLogin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_regist:	//注册
			new RegistTask().execute(getConnection());
			break;
		case R.id.tv_login:	//返回登录界面
			Intent intent = new Intent(mContext, LoginActivity.class);
			startActivity(intent);
			finish();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 注册的后台任务
	 * @author Administrator
	 * @update 2014年10月7日 下午4:56:24
	 *
	 */
	class RegistTask extends AsyncTask<AbstractXMPPConnection, Void, Integer> {
		@Override
		protected void onPreExecute() {
			pDialog.setMessage(getString(R.string.registing));
			pDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(AbstractXMPPConnection... params) {
			return regist(params[0]);
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			pDialog.dismiss();
			switch (result) {
			case REGIST_RESULT_SUCCESS:	//注册成功
				String username = etAccount.getText().toString();
				String password = etPassword.getText().toString();
				//保存用户信息
				Editor editor = preferences.edit();
				editor.putString(Constants.LOGIN_ACCOUNT, username);
				editor.putString(Constants.LOGIN_PASSWORD, password);
				editor.putBoolean(Constants.LOGIN_ISFIRST, false);
				editor.commit();
				systemConfig.setOnline(true);
				btnRegist.setText("已登录");
				btnRegist.setEnabled(false);
				break;
			case REGIST_RESULT_FAIL:	//失败
				SystemUtil.makeLongToast(R.string.regist_failed);
				break;
			case REGIST_RESULT_CONFLICT:	//用户已存在
				SystemUtil.makeLongToast(R.string.regist_account_conflict);
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * 设置注册按钮的状态
	 * @author Administrator
	 * @update 2014年10月7日 下午3:47:36
	 * @param isEnable
	 */
	private void setRegistBtnState(boolean enable) {
		btnRegist.setEnabled(enable);
	}
	
	/**
	 * 账号注册
	 * @author Administrator
	 * @update 2014年10月7日 下午6:00:55
	 * @param connection
	 * @param config
	 * @return
	 */
	private int regist(AbstractXMPPConnection connection) {
		try {
			connection.connect();
			Registration registration = new Registration();
			registration.setType(IQ.Type.set);
			registration.setTo(connection.getServiceName());
			Map<String, String> attr = new HashMap<String, String>();
			String username = etAccount.getText().toString();
			String password = etPassword.getText().toString();
			attr.put("username", username);
			attr.put("password", password);
			attr.put("name", etNickname.getText().toString());
			attr.put("email", etEmail.getText().toString());
			registration.setAttributes(attr);
			PacketFilter filter = new AndFilter(new PacketIDFilter(registration.getPacketID()), new PacketTypeFilter(IQ.class));
			PacketCollector collector = connection.createPacketCollector(filter);
			connection.sendPacket(registration);
			IQ result = (IQ) collector.nextResult(SmackConfiguration.getDefaultPacketReplyTimeout());
			collector.cancel();
			if (result == null) {
				Log.d("regist failed");
				return REGIST_RESULT_FAIL;
			} else if (IQ.Type.result == result.getType()) {
				if(!connection.isConnected()) {
					connection.connect();
				}
				connection.login(username, password);
				return REGIST_RESULT_SUCCESS;
			} else {
				if("conflict".equalsIgnoreCase(result.getError().toString())) {
					Log.d("regist conflict");
					return REGIST_RESULT_CONFLICT;
				} else {
					Log.d("regist error");
					return REGIST_RESULT_FAIL;
				}
			}
		} catch (SmackException | IOException | XMPPException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return REGIST_RESULT_FAIL;
		
	}

}
