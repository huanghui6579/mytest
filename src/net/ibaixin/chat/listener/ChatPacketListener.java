package net.ibaixin.chat.listener;

import java.io.File;
import java.util.Collection;

import net.ibaixin.chat.ChatApplication;
import net.ibaixin.chat.R;
import net.ibaixin.chat.activity.UserInfoActivity;
import net.ibaixin.chat.fragment.ContactFragment.LoadDataBroadcastReceiver;
import net.ibaixin.chat.model.HeadIcon;
import net.ibaixin.chat.model.NewFriendInfo;
import net.ibaixin.chat.model.User;
import net.ibaixin.chat.model.UserVcard;
import net.ibaixin.chat.model.NewFriendInfo.FriendStatus;
import net.ibaixin.chat.util.SystemUtil;
import net.ibaixin.chat.util.XmppConnectionManager;
import net.ibaixin.chat.util.XmppUtil;
import net.ibaixin.manage.UserManager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jxmpp.util.XmppStringUtils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * 接收消息的监听器
 * @author huanghui1
 * @update 2014年11月10日 下午6:10:15
 */
public class ChatPacketListener implements PacketListener {
	private UserManager mUserManager;
	private Context mContext;
	
	public ChatPacketListener() {
		mUserManager = UserManager.getInstance();
		mContext = ChatApplication.getInstance();
	}

	@Override
	public void processPacket(Packet packet) throws NotConnectedException {
		//TODO 其他各种消息处理
		if (packet instanceof Presence) {
			Presence presence = (Presence) packet;
			String from = presence.getFrom();
			if (!TextUtils.isEmpty(from)) {
				if (!from.startsWith(ChatApplication.getInstance().getCurrentAccount())) {	//只处理发起消息的不是自己的情况
					SystemUtil.getCachedThreadPool().execute(new HandlePresenceTask(presence));
				}
			}
		}
	}
	
	/**
	 * 处理添加好友请求等任务
	 * @author huanghui1
	 * @update 2014年11月10日 下午8:57:51
	 */
	class HandlePresenceTask implements Runnable {
		private Presence presence;
		
		private AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();

		public HandlePresenceTask(Presence presence) {
			super();
			this.presence = presence;
		}

		@Override
		public void run() {
			Collection<PacketExtension> extensions = presence.getExtensions();
			boolean isEmpty = SystemUtil.isEmpty(extensions);
			Presence.Type type = presence.getType();
			/*
			 *  •	available: 表示处于在线状态
				•	unavailable: 表示处于离线状态
				•	subscribe: 表示发出添加好友的申请
				•	unsubscribe: 表示发出删除好友的申请
				•	unsubscribed: 表示拒绝添加对方为好友
				•	error: 表示presence信息报中包含了一个错误消息。

			 */
			switch (type) {
			case available:	//用户上线
				if (!ChatRostListener.hasRosterListener) {	
					/*
					 * 如果没有相关的监听器了，那就在这里重复处理了，否则，在{@linkplain ChatRostListener}中处理
					 */
					mUserManager.updateUserPresence(presence);
				}
				break;
			case subscribe:	//添加好友的申请(对方发出添加我为好友的消息)
				String from = SystemUtil.unwrapJid(presence.getFrom());
				String to = SystemUtil.unwrapJid(presence.getTo());
				
				//查找数据库是否有我主动请求添加对方为好友的信息
				NewFriendInfo newInfo = mUserManager.getNewFriendInfoByAccounts(to, from);
				//自己主动添加对方为好友，此时，对方同意了，并且添加我为好友，则自己直接同意并添加对方为好友
				if (newInfo != null && newInfo.getFriendStatus() == FriendStatus.VERIFYING) {
					
					try {
						
						XmppUtil.acceptFriend(connection, presence.getFrom());
						
						//修改状态为“已添加”
						newInfo.setFriendStatus(FriendStatus.ADDED);
						//将该好友添加至本地数据库
						User user = newInfo.getUser();
						if (user == null) {
							user = new User();
							user.setUsername(from);
							newInfo.setUser(user);
						}
						user.setFullPinyin(user.initFullPinyin());
						user.setShortPinyin(user.initShortPinyin());
						user.setSortLetter(user.initSortLetter(user.getShortPinyin()));
						mUserManager.saveOrUpdateNewFriendInfo(newInfo);
						mUserManager.saveOrUpdateFriend(user);
						
						//通知好友列表更新好友
						Intent intent = new Intent(LoadDataBroadcastReceiver.ACTION_USER_ADD);
						intent.putExtra(UserInfoActivity.ARG_USER, user);
						mContext.sendBroadcast(intent);
					} catch (NotConnectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} else {	//只是别人主动添加请求添加我为好友
					newInfo = new NewFriendInfo();
					newInfo.setFriendStatus(FriendStatus.ACCEPT);
					newInfo.setFrom(from);
					newInfo.setTo(to);
					newInfo.setTitle(to);
					newInfo.setContent(mContext.getString(R.string.contact_friend_add_request));
					newInfo.setCreationDate(System.currentTimeMillis());
					if (!isEmpty) {
						String hash = null;
						for (PacketExtension packetExtension : extensions) {
							if (packetExtension instanceof DefaultPacketExtension) {
								DefaultPacketExtension defaultPacketExtension = (DefaultPacketExtension) packetExtension;
								Collection<String> names = defaultPacketExtension.getNames();
								if (names.contains("hash")) {
									hash = defaultPacketExtension.getValue("hash");
								}
								
							}
						}
						if (!TextUtils.isEmpty(hash)) {	//有图像
							//根据对方用户账号查询本地的图片
							File icon = SystemUtil.generateIconFile(from);
							String savePath = icon.getAbsolutePath();
							boolean needSave = false;	//是否需要保存图像
							if (icon.exists()) {	//文件已经存在，则判断hash，看是否需要更新图像
								String oldHash = SystemUtil.getFileHash(icon);
								if (!oldHash.equals(hash)) {	//需要更新图像
									needSave = true;
								}
							} else {	//图像不存在，则存储到储存卡里
								needSave = true;
								savePath = null;
							}
							if (needSave) {
								HeadIcon headIcon = XmppUtil.downloadUserIcon(connection, from);
								if (headIcon != null) {	//图像获取成功
									savePath = headIcon.getFilePath();
									hash = headIcon.getHash();
								} else {
									savePath = null;
									hash = null;
								}
							}
							newInfo.setIconHash(hash);
							newInfo.setIconPath(savePath);
						}
					}
					//查看本地是否有该好友，对方此时没有好友我
					User user = mUserManager.getUserByUsername(from);
					if (user != null) {	//如果存在本地好友
						UserVcard uCrad = user.getUserVcard();
						String fIconHash = newInfo.getIconHash();
						String fIconPath = newInfo.getIconPath();
						String uIconHash = null;
						boolean neddUpdate = false;
						if (uCrad != null) {	//本地好友有名片信息
							uIconHash = uCrad.getIconHash();
							if (!TextUtils.isEmpty(fIconPath)) {	//对方有头像信息
								if (TextUtils.isEmpty(uIconHash) || !fIconPath.equals(uIconHash)) {	//此时本地好友没有头像信息，或者头像没有更细下来
									//本地好友有头像信息，但需要对比一下头像是否已经改变，如果改变，则需要更新
									uIconHash = fIconHash;
									uCrad.setIconHash(uIconHash);
									uCrad.setIconHash(fIconPath);
									neddUpdate = true;
								}
							} else {	//对方没有头像
								if (!TextUtils.isEmpty(uIconHash)) {	//但本地有头像，更新本地头像
									String uIconPath = uCrad.getIconPath();
									uIconHash = null;
									uCrad.setIconHash(null);
									uCrad.setIconPath(null);
									//删除本地图像
									SystemUtil.deleteFile(uIconPath);
									neddUpdate = true;
								}
							}
						} else {	//本地好友没有名片，则新建名片
							if (!TextUtils.isEmpty(fIconHash)) {	//对方有图像
								uCrad = new UserVcard();
								uCrad.setIconHash(fIconHash);
								uCrad.setIconPath(fIconPath);
								user.setUserVcard(uCrad);
								neddUpdate = true;
							}
						}
						if (neddUpdate) {
							user = mUserManager.updateSimpleUser(user);
						}
						newInfo.setUser(user);
						newInfo.setContent(user.getName());
					}
					//如果本地有该好友，则看要不要更新头像
					newInfo = mUserManager.saveOrUpdateNewFriendInfo(newInfo);
				}
				break;
			default:
				break;
			}
		}
		
	}
	
	
}