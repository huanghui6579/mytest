package com.example.chat.util;

import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.debugger.android.AndroidDebugger;

import com.example.chat.model.SystemConfig;

/**
 * 
 * @author Administrator
 * @update 2014年10月7日 上午9:37:01
 *
 */
public class XmppConnectionManager {
	private AbstractXMPPConnection connection;
	
	private static XmppConnectionManager instance = null;
	private static ConnectionConfiguration configuration;
	
	private XmppConnectionManager() {}
	
	public static XmppConnectionManager getInstance() {
		if (instance == null) {
			synchronized (XmppConnectionManager.class) {
				if (instance == null) {
					instance = new XmppConnectionManager();
				}
			}
		}
		return instance;
	}
	
	/**
	 * 初始化连接
	 * @param systemConfig
	 * @return
	 */
	public AbstractXMPPConnection init(SystemConfig systemConfig) {
		SmackConfiguration.DEBUG_ENABLED = true;
		configuration = new ConnectionConfiguration(Constants.SERVER_HOST, Constants.SERVER_PORT, Constants.SERVER_NAME);
		configuration.setSecurityMode(SecurityMode.disabled);
		
		//允许自动连接
//		configuration.setReconnectionAllowed(true);
		//允许登录成功后更新状态
		configuration.setSendPresence(true);
		// 收到好友邀请后manual表示需要经过同意,accept_all表示不经同意自动为好友
		Roster.setDefaultSubscriptionMode(SubscriptionMode.manual);
		connection = new XMPPTCPConnection(configuration);
		connection.setPacketReplyTimeout(10000);
		AndroidDebugger androidDebugger = new AndroidDebugger(connection, new PrintWriter(System.out), new InputStreamReader(System.in));
		System.setProperty("smack.debuggerClass", androidDebugger.getClass().getCanonicalName());
		return connection;
	}
	
	/**
	 * 返回一个有效的xmpp连接
	 * @return
	 */
	public AbstractXMPPConnection getConnection() {
		return connection;
	}
	
	/**
	 * 断开连接
	 * @author Administrator
	 * @update 2014年10月7日 上午9:35:03
	 */
	public void disconnect() {
		if(connection != null) {
			try {
				connection.disconnect();
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
	}
}
