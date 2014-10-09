package com.example.chat.activity;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import com.example.chat.model.User;

/**
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月9日 下午9:18:10
 */
public class XmppUtil {
	public static List<User> searchUser(AbstractXMPPConnection connection, String username) {
		List<User> users = null;
		try {
			UserSearchManager searchManager = new UserSearchManager(connection);
			String searchService = "search." + connection.getServiceName();
			Form searchForm = searchManager.getSearchForm(searchService);
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("Username", true);
			answerForm.setAnswer("Name", true);
			answerForm.setAnswer("Username", true);
			answerForm.setAnswer("Email", username);
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
}
