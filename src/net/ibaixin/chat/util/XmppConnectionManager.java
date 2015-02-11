package net.ibaixin.chat.util;

import java.io.InputStreamReader;
import java.io.PrintWriter;

import net.ibaixin.chat.listener.ChatConnectionListener;
import net.ibaixin.chat.listener.ChatPacketListener;
import net.ibaixin.chat.model.SystemConfig;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.filter.NotFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.debugger.android.AndroidDebugger;

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
		connection.setPacketReplyTimeout(15000);	//毫秒为单位
		
		//添加监听器
		connection.addConnectionListener(new ChatConnectionListener());
		PacketFilter packetFilter = new OrFilter(new PacketTypeFilter(IQ.class), new PacketTypeFilter(Presence.class));
		connection.addPacketListener(new ChatPacketListener(), packetFilter);
		
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
