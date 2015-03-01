package net.ibaixin.chat.listener;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import net.ibaixin.chat.ChatApplication;
import net.ibaixin.chat.model.SystemConfig;
import net.ibaixin.chat.service.CoreService;
import net.ibaixin.chat.util.Constants;
import net.ibaixin.chat.util.XmppConnectionManager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.AlreadyLoggedInException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.content.Intent;

/**
 * 客户端连接监听器
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年11月8日 下午2:54:16
 */
public class ChatConnectionListener implements ConnectionListener {
	private Timer mTimer;
	private AbstractXMPPConnection mConnection = XmppConnectionManager.getInstance().getConnection();
	private long timeDelay = 2000;

	@Override
	public void connected(XMPPConnection connection) {
		// TODO Auto-generated catch block
	}

	@Override
	public void authenticated(XMPPConnection connection) {
		// TODO Auto-generated method stub
	}

	@Override
	public void connectionClosed() {
		// TODO Auto-generated method stub
		try {
			mConnection.disconnect();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mTimer = new Timer();
		mTimer.schedule(new ReConnectTask(), timeDelay);
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		// TODO Auto-generated method stub
		if (!(e instanceof AlreadyLoggedInException)) {	//账号没有登录
			try {
				mConnection.disconnect();
			} catch (NotConnectedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			mTimer = new Timer();
			mTimer.schedule(new ReConnectTask(), timeDelay);
		}
	}

	@Override
	public void reconnectingIn(int seconds) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reconnectionSuccessful() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reconnectionFailed(Exception e) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * 客户端与服务器的重连后台线程
	 * @author huanghui1
	 * @update 2014年11月8日 上午11:28:04
	 */
	class ReConnectTask extends TimerTask {

		@Override
		public void run() {
			SystemConfig systemConfig = ChatApplication.getInstance().getSystemConfig();
			String username = systemConfig.getAccount();
			String password = systemConfig.getPassword();
			if (username != null && password != null) {
				AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
				try {
					if (!connection.isConnected()) {
						connection.connect();
						if (!connection.isAuthenticated()) {
							connection.login(username, password, Constants.CLIENT_RESOURCE);
						}
						//接收离线消息
						Intent service = new Intent(ChatApplication.getInstance(), CoreService.class);
						service.putExtra(CoreService.FLAG_RECEIVE_OFFINE_MSG, CoreService.FLAG_RECEIVE_OFFINE);
						ChatApplication.getInstance().startService(service);
					}
				} catch (XMPPException | SmackException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

}
