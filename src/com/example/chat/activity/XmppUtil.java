package com.example.chat.activity;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.chat.model.User;

/**
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月9日 下午9:18:10
 */
public class XmppUtil {
	/**
	 * 搜索好友
	 * @update 2014年10月10日 下午8:45:50
	 * @param connection
	 * @param username
	 * @return
	 */
	public static List<User> searchUser(AbstractXMPPConnection connection, String username) {
		List<User> users = null;
		try {
			UserSearchManager searchManager = new UserSearchManager(connection);
			String searchService = "search." + connection.getServiceName();
			Form searchForm = searchManager.getSearchForm(searchService);
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("Username", true);
			answerForm.setAnswer("Name", true);
			answerForm.setAnswer("Email", true);
			answerForm.setAnswer("search", username);
			ReportedData reportedData = searchManager.getSearchResults(answerForm, searchService);
			List<Row> rows = reportedData.getRows();
			if(rows != null && rows.size() > 0) {
				users = new ArrayList<>();
				for(Row row : rows) {
					User user = new User();
					user.setUsername(row.getValues("Username").get(0));
					user.setJID(row.getValues("jid").get(0));
					user.setEmail(row.getValues("Email").get(0));
					user.setNickname(row.getValues("Name").get(0));
					users.add(user);
				}
			}
		} catch (NoResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return users;
	}
	
	/**
	 * 获取用户电子名片
	 * @update 2014年10月10日 下午8:49:35
	 * @param connection
	 * @param user 完整用户账号，格式为xxx@domain或者xxx@domain/resource
	 * @return
	 */
	public static VCard getUserVcard(AbstractXMPPConnection connection, String user) {
		try {
			VCard card = new VCard();
			card.load(connection, user);
			return card;
		} catch (NoResponseException | XMPPErrorException
				| NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取用户的头像
	 * @update 2014年10月10日 下午8:51:25
	 * @param connection
	 * @param user 完整用户账号，格式为xxx@domain或者xxx@domain/resource
	 * @return
	 */
	public static Bitmap getUserIcon(AbstractXMPPConnection connection, String user) {
		Bitmap icon = null;
		VCard card = getUserVcard(connection, user);
		if(card != null) {
			byte[] data = card.getAvatar();
			if (data != null && data.length > 0) {
				icon = BitmapFactory.decodeByteArray(data, 0, data.length);
			}
		}
		return icon;
	}
	
	/**
	 * 获取用户的头像
	 * @update 2014年10月10日 下午8:51:25
	 * @param card
	 * @return
	 */
	public static Bitmap getUserIcon(VCard card) {
		Bitmap icon = null;
		if(card != null) {
			byte[] data = card.getAvatar();
			if (data != null && data.length > 0) {
				icon = BitmapFactory.decodeByteArray(data, 0, data.length);
			}
		}
		return icon;
	}
	
	/**
	 * 像对方发送一个添加好友的请求
	 * @update 2014年10月10日 下午10:29:35
	 * @param connection
	 * @param toUser
	 * @throws NotConnectedException 
	 */
	public static void addFriend(AbstractXMPPConnection connection, String toUser) throws NotConnectedException {
		Presence presence = new Presence(Presence.Type.subscribe);
		presence.setTo(toUser);
		connection.sendPacket(presence);
	}
}
