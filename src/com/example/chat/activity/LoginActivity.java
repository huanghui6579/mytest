package com.example.chat.activity;

import java.io.IOException;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.AlreadyLoggedInException;
import org.jivesoftware.smack.SmackException.ConnectionException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.XMPPException;

import com.example.chat.R;
import com.example.chat.model.SystemConfig;
import com.example.chat.util.Constants;
import com.example.chat.util.Log;
import com.example.chat.util.SystemUtil;
import com.example.chat.util.XmppConnectionManager;

import android.app.ProgressDialog;
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
	private ProgressDialog pDialog;
	
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
			
			new LoginTask().execute(systemConfig);
			break;
		case R.id.tv_regist:	//进入注册界面
//			Intent intent = new Intent(mContext, MainActivity.class);
			Intent intent = new Intent(mContext, RegistActivity.class);
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
	class LoginTask extends AsyncTask<SystemConfig, Void, Integer> {
		
		@Override
		protected void onPreExecute() {
			if (pDialog == null) {
				pDialog = ProgressDialog.show(mContext, null, getString(R.string.logining), true, true);
			} else {
				pDialog.show();
			}
		}

		@Override
		protected Integer doInBackground(SystemConfig... params) {
			systemConfig.setAccount(etAccount.getText().toString());
			systemConfig.setPassword(etPassword.getText().toString());
			int result = login(params[0]);
			if (Constants.MSG_SUCCESS == result) {
				systemConfig.setOnline(true);
				systemConfig.setFirstLogin(false);
				application.saveSystemConfig();
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			hideLoadingDialog(pDialog);
			switch (result) {
			case Constants.MSG_SUCCESS:	//登录成功
				Intent intent = new Intent(mContext, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				break;
			case Constants.MSG_REQUEST_ADDRESS_FAILED:	//网络请求的地址不对
				SystemUtil.makeShortToast(R.string.request_address_failed);
				break;
			case Constants.MSG_REQUEST_ALREADY_LOGIN:	//用户已经登录过了
				SystemUtil.makeShortToast(R.string.request_address_failed);
				break;
			case Constants.MSG_NO_RESPONSE:	//服务器没有响应
				SystemUtil.makeShortToast(R.string.request_no_response);
				break;
			case Constants.MSG_FAILED:	//登录失败
				SystemUtil.makeShortToast(R.string.login_failed);
				break;
			default:
				SystemUtil.makeShortToast(R.string.login_failed);
				break;
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
	private int login(SystemConfig config) {
		String account = etAccount.getText().toString();
		String password = etPassword.getText().toString();
		int code = Constants.MSG_FAILED;	//登录是否成功的标识
		try {
			AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
			connection.connect();
			connection.login(account, password, Constants.CLIENT_RESOURCE);
			code = Constants.MSG_SUCCESS;
		} catch (SaslException e) {
			Log.e(e.toString());
		} catch (SmackException e) {
			if (e instanceof ConnectionException) {	//连接地址不可用
				code = Constants.MSG_REQUEST_ADDRESS_FAILED;
			} else if (e instanceof AlreadyLoggedInException) {
				code = Constants.MSG_REQUEST_ALREADY_LOGIN;	//用户已经登录过了
			} else if (e instanceof NoResponseException) {
				code = Constants.MSG_NO_RESPONSE;	//服务器没有响应
			} else {
				code = Constants.MSG_FAILED;
			}
			Log.e(e.toString());
		} catch (IOException e) {
			Log.e(e.toString());
		} catch (XMPPException e) {
			Log.e(e.toString());
		}
		return code;
	}
	
	@Override
	public void onBackPressed() {
		application.exit();
	}
	
}
