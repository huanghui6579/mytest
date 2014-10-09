package com.example.chat.activity;

import java.io.IOException;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import com.example.chat.R;
import com.example.chat.model.SystemConfig;
import com.example.chat.util.SystemUtil;
import com.example.chat.util.XmppConnectionManager;

import android.content.Intent;
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
 * 登录主界面
 * @author huanghui1
 *
 */
public class LoginActivity extends BaseActivity implements OnClickListener {
	private EditText etAccount;
	private EditText etPassword;
	private Button btnLogin;
	
	private TextView tvRegist;
	
	private SystemConfig systemConfig;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle(getString(R.string.activity_lable_login));
	}

	@Override
	protected int getContentView() {
		return R.layout.activity_login;
	}

	@Override
	protected void initView() {
		etAccount = (EditText) findViewById(R.id.et_account);
		etPassword = (EditText) findViewById(R.id.et_password);
		btnLogin = (Button) findViewById(R.id.btn_login);
		tvRegist = (TextView) findViewById(R.id.tv_regist);
	}

	@Override
	protected void initData() {
		systemConfig = application.getSystemConfig();
		XmppConnectionManager.getInstance().init(systemConfig);
		
		String tAccount = systemConfig.getAccount();
		String tPassword = systemConfig.getPassword();
		if (!TextUtils.isEmpty(tAccount)) {
			etAccount.setText(tAccount);
			
			if (!TextUtils.isEmpty(tPassword)) {
				etPassword.setText(tPassword);
			}
		}
		
	}

	@Override
	protected void addListener() {
		etAccount.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if(TextUtils.isEmpty(s) || TextUtils.isEmpty(etPassword.getText().toString())) {
					setLoginBtnState(false);
				} else {
					setLoginBtnState(true);
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
				// TODO Auto-generated method stub
				if(TextUtils.isEmpty(s) || TextUtils.isEmpty(etAccount.getText().toString())) {
					setLoginBtnState(false);
				} else {
					setLoginBtnState(true);
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
		
		etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE || actionId == KeyEvent.ACTION_DOWN) {
					SystemUtil.hideSoftInput(v);
					v.clearFocus();
					new LoginTask().execute(systemConfig);
					return true;
				}
				return false;
			}
		});
		
		btnLogin.setOnClickListener(this);
		tvRegist.setOnClickListener(this);
	}
	
	/**
	 * 设置登录按钮状态，true表示可用，false表示不可用
	 * @param isEnable 使用、否可用
	 */
	private void setLoginBtnState(boolean enable) {
		btnLogin.setEnabled(enable);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:	//登录
			if (!SystemUtil.isNetworkOnline()) {
				SystemUtil.makeShortToast(R.string.network_error);
				return;
			}
			
			systemConfig.setAccount(etAccount.getText().toString());
			systemConfig.setPassword(etPassword.getText().toString());
			
			new LoginTask().execute(systemConfig);
			break;
		case R.id.tv_regist:	//进入注册界面
//			Intent intent = new Intent(mContext, RegistActivity.class);
			Intent intent = new Intent(mContext, MainActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 登录的异步任务
	 * @author Administrator
	 * @update 2014年10月7日 上午9:55:00
	 *
	 */
	class LoginTask extends AsyncTask<SystemConfig, Void, Boolean> {
		
		@Override
		protected void onPreExecute() {
			if(pDialog != null) {
				pDialog.setMessage(getString(R.string.logining));
				pDialog.show();
			}
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(SystemConfig... params) {
			return login(params[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			pDialog.dismiss();
			if(result) {	//登录成功
				systemConfig.setOnline(true);
				systemConfig.setFirstLogin(false);
				application.saveSystemConfig();
				Intent intent = new Intent(mContext, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			} else {
				SystemUtil.makeShortToast(R.string.login_failed);
			}
		}
		
	}
	
	/**
	 * 登录
	 * @author Administrator
	 * @update 2014年10月7日 下午12:20:10
	 * @param config
	 * @return
	 */
	private boolean login(SystemConfig config) {
		String account = config.getAccount();
		String password = config.getPassword();
		try {
			AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
			connection.connect();
			connection.login(account, password, config.getResource());
			return true;
		} catch (SaslException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SmackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void onBackPressed() {
		application.exit();
	}
	
}
