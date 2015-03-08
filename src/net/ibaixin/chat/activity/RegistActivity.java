package net.ibaixin.chat.activity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.ibaixin.chat.R;
import net.ibaixin.chat.model.SystemConfig;
import net.ibaixin.chat.util.Constants;
import net.ibaixin.chat.util.Log;
import net.ibaixin.chat.util.SystemUtil;
import net.ibaixin.chat.util.XmppConnectionManager;
import net.ibaixin.chat.view.ProgressDialog;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FlexiblePacketTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Bind;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.iqregister.packet.Registration;

import android.content.Intent;
import android.os.AsyncTask;
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
	
	private SystemConfig systemConfig;
	private ProgressDialog pDialog;

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
		systemConfig = application.getSystemConfig();
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
					if (!application.getSystemConfig().isOnline()) {
						new RegistTask().execute();
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
			new RegistTask().execute();
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
	class RegistTask extends AsyncTask<Void, Void, Integer> {
		@Override
		protected void onPreExecute() {
			if (pDialog == null) {
				pDialog = ProgressDialog.show(mContext, null, getString(R.string.registing), true);
			} else {
				pDialog.show();
			}
		}

		@Override
		protected Integer doInBackground(Void... params) {
			return regist();
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			hideLoadingDialog(pDialog);
			switch (result) {
			case REGIST_RESULT_SUCCESS:	//注册成功
				//保存用户信息
				systemConfig.setOnline(true);
				systemConfig.setFirstLogin(false);
				application.saveSystemConfig();
				Intent intent = new Intent(mContext, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
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
	private int regist() {
		try {
			AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
			connection.connect();
			connection.addPacketListener(new PacketListener() {
				
				@Override
				public void processPacket(Packet packet) throws NotConnectedException {
					if (packet instanceof Bind) {
						Bind bind = (Bind) packet;
						bind.getJid();
					}
				}
			}, new AndFilter(new PacketTypeFilter(Bind.class), new FlexiblePacketTypeFilter<IQ>() {
				
				@Override
				protected boolean acceptSpecific(IQ iq) {
					// TODO Auto-generated method stub
					return iq.getType().equals(IQ.Type.result);
				}
			}));
			
			Map<String, String> attr = new HashMap<String, String>();
			String username = etAccount.getText().toString();
			String password = etPassword.getText().toString();
			systemConfig.setAccount(username);
			systemConfig.setPassword(password);
			attr.put("username", username);
			attr.put("password", password);
			attr.put("name", etNickname.getText().toString());
			attr.put("email", etEmail.getText().toString());
			
			Registration registration = new Registration(attr);
			registration.setType(IQ.Type.set);
			registration.setTo(connection.getServiceName());
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
				connection.login(username, password, Constants.CLIENT_RESOURCE);
				if(!registerIbaixinJoke(etNickname.getText().toString())){//注册web服务器 add by dudejin 2015-03-06
					return REGIST_RESULT_FAIL;
				}
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
