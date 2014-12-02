package com.example.chat.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemStatus;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.example.chat.model.HeadIcon;
import com.example.chat.model.Personal;
import com.example.chat.model.User;
import com.example.chat.model.UserVcard;

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
	 * 获取用户的好友列表
	 * @param connection
	 * @return
	 */
	public static List<User> getFriends(AbstractXMPPConnection connection) {
		List<User> users = null;
		Roster roster = connection.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();
		if (entries != null && entries.size() > 0) {
			users = new ArrayList<User>();
			for (RosterEntry entry : entries) {
				User user = new User();
				String jid = entry.getUser();
				String name = entry.getName();
				if (jid.contains("/")) {
					String[] arr = jid.split("/");
					String resource = arr[1];
					user.setResource(resource);
					user.setJID(arr[0]);
				} else {
					user.setJID(jid);
				}
				String username = SystemUtil.unwrapJid(jid);
				user.setNickname(name);
				user.setUsername(username);
				user.setFullPinyin(user.initFullPinyin());
				user.setShortPinyin(user.initShortPinyin());
				user.setSortLetter(user.initSortLetter(user.getShortPinyin()));
				users.add(user);
			}
		}
		return users;
	}
	
	/**
	 * 同步好友的头像等基本信息
	 * @update 2014年10月23日 下午4:38:54
	 * @param connection
	 * @param list
	 * @return
	 */
	public static List<User> syncFriendsVcard(AbstractXMPPConnection connection, List<User> list) {
		if (list != null && list.size() > 0) {
			for (User user : list) {
				syncUserVcard(connection, user);
			}
		}
		return list;
	}
	
	/**
	 * 将好友信息与服务器端同步
	 * @update 2014年10月23日 下午7:17:56
	 * @param user
	 * @return
	 */
	public static User syncUserVcard(AbstractXMPPConnection connection, User user) {
		VCard card = getUserVcard(connection, user.getJID());
		if (card != null) {
			UserVcard uv = user.getUserVcard();
			if (uv == null) {
				uv = new UserVcard();
				uv.setUserId(user.getId());
			}
			uv.setCity(card.getAddressFieldHome("LOCALITY"));
			uv.setProvince(card.getAddressFieldHome("REGION"));
			uv.setStreet(card.getAddressFieldHome("STREET"));
			uv.setEmail(card.getEmailHome());
			uv.setMobile(card.getPhoneHome("CELL"));
			uv.setNickname(card.getNickName());
			uv.setRealName(card.getLastName());
			uv.setZipCode(card.getAddressFieldHome("PCODE"));
			String iconHash = uv.getIconHash();
			boolean isIconExists = SystemUtil.isFileExists(uv.getIconPath());
			if (!isIconExists || TextUtils.isEmpty(iconHash) || !iconHash.equals(card.getAvatarHash())) {	//没有头像或者头像已经改变就需要更新头像
				File icon = SystemUtil.saveFile(card.getAvatar(), SystemUtil.generateIconFile(user.getUsername()));
				if (icon != null) {
					uv.setIconPath(icon.getAbsolutePath());
					iconHash = SystemUtil.getFileHash(icon);
					uv.setIconHash(iconHash);
				}
			}
			if (TextUtils.isEmpty(user.getEmail())) {
				user.setEmail(card.getEmailHome());
			}
			user.setPhone(uv.getMobile());
			String resource = SystemUtil.getResourceWithJID(card.getJabberId());
			if (!TextUtils.isEmpty(resource)) {
				user.setResource(resource);
			}
			user.setUserVcard(uv);
		}
		return user;
	}
	
	/**
	 * 获取用户电子名片
	 * @update 2014年10月10日 下午8:49:35
	 * @param connection
	 * @param user 完整用户账号，格式为xxx@domain或者xxx@domain/resource
	 * @return
	 */
	public static VCard getUserVcard(AbstractXMPPConnection connection, String user) {
		VCard card = null;
		try {
			card = new VCard();
			card.load(connection, user);
		} catch (NoResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return card;
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
	 * 从服务器上保存用户的头像
	 * @update 2014年11月11日 下午10:15:33
	 * @param connection
	 * @param username 用户的账号，不含有@及其之后的信息
	 * @return
	 */
	public static HeadIcon downloadUserIcon(AbstractXMPPConnection connection, String username) {
		VCard card = getUserVcard(connection, SystemUtil.wrapJid(username));
		HeadIcon headIcon = null;
		if (card != null) {
			byte[] data = card.getAvatar();
			if (data != null && data.length > 0) {
				headIcon = new HeadIcon();
				File file = SystemUtil.saveFile(data, SystemUtil.generateIconFile(username));
				headIcon.setFilePath(file.getAbsolutePath());
				headIcon.setHash(card.getAvatarHash());
			}
		}
		return headIcon;
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
	
	/**
	 * 接受别人的添加我为好友
	 * @update 2014年11月12日 下午2:35:57
	 * @param connection
	 * @param toUser
	 * @throws NotConnectedException
	 */
	public static void acceptFriend(AbstractXMPPConnection connection, String toUser) throws NotConnectedException {
		Presence presence = new Presence(Presence.Type.subscribed);
		presence.setTo(toUser);
		connection.sendPacket(presence);
	}
	
	/**
	 * 将对方从好友列表中删除
	 * @update 2014年12月1日 上午9:53:41
	 * @param connection
	 * @param toUser
	 * @throws NotConnectedException
	 */
	public static void removeFriend(AbstractXMPPConnection connection, String toUser) throws NotConnectedException {
		Presence presence = new Presence(Presence.Type.unavailable);
		presence.setTo(toUser);
		connection.sendPacket(presence);
	}
	
	/**
	 * 根据用户账号获得用户基本信息
	 * @update 2014年11月12日 下午3:39:01
	 * @param connection
	 * @param username
	 * @return
	 * @throws NotConnectedException
	 */
	public static User getUserEntry(AbstractXMPPConnection connection, String username) throws NotConnectedException {
		String jid = SystemUtil.wrapJid(username);
		Roster roster = connection.getRoster();
		RosterEntry entry = roster.getEntry(jid);
		Presence presence = roster.getPresence(jid);
		User user = new User();
		user.setUsername(username);
		user.setFullPinyin(user.initFullPinyin());
		user.setShortPinyin(user.initShortPinyin());
		user.setSortLetter(user.initSortLetter(user.getShortPinyin()));
		if (entry != null) {
			user.setNickname(entry.getName());
			Presence.Mode mode = presence.getMode();
			if (mode != null) {
				user.setMode(mode.name());
			}
			user.setStatus(presence.getStatus());
		}
		return user;
	}
	
	/**
	 * 删除指定用户
	 * @update 2014年11月12日 下午9:01:10
	 * @param connection
	 * @param username 用户名，不含有@及其之后的信息
	 * @return
	 */
	public static boolean deleteUser(AbstractXMPPConnection connection, String username) {
		Roster roster = connection.getRoster();
		if (roster == null) {	//已经没有改好友了
			return true;
		}
		RosterEntry rosterEntry = roster.getEntry(SystemUtil.wrapJid(username));
		boolean flag = false;
		try {
			roster.removeEntry(rosterEntry);
			flag = true;
		} catch (NotLoggedInException | NoResponseException
				| XMPPErrorException | NotConnectedException e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 从服务器上同步个人信息
	 * @update 2014年10月24日 下午5:59:33
	 * @param personal
	 * @return
	 */
	public static Personal syncPersonalInfo(AbstractXMPPConnection connection, Personal personal) {
		VCard card = getUserVcard(connection, personal.getJID());
		if (card != null) {
			personal.setCity(card.getAddressFieldHome("LOCALITY"));
			personal.setProvince(card.getAddressFieldHome("REGION"));
			personal.setStreet(card.getAddressFieldHome("STREET"));
			personal.setEmail(card.getEmailHome());
			personal.setPhone(card.getPhoneHome("CELL"));
			personal.setNickname(card.getNickName());
			personal.setRealName(card.getLastName());
			personal.setZipCode(card.getAddressFieldHome("PCODE"));
			String iconHash = personal.getIconHash();
			boolean isIconExists = SystemUtil.isFileExists(personal.getIconPath());
			if (!isIconExists || TextUtils.isEmpty(iconHash) || !iconHash.equals(card.getAvatarHash())) {	//没有头像或者头像已经改变就需要更新头像
				File icon = SystemUtil.saveFile(card.getAvatar(), SystemUtil.generateIconFile(personal.getUsername()));
				if (icon != null) {
					personal.setIconPath(icon.getAbsolutePath());
					iconHash = SystemUtil.getFileHash(icon);
					personal.setIconHash(iconHash);
				}
			}
		}
		return personal;
	}
}
