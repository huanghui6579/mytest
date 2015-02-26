package net.ibaixin.chat.activity;

import net.ibaixin.chat.R;
import net.ibaixin.chat.model.User;
import net.ibaixin.chat.model.UserVcard;
import net.ibaixin.chat.view.ClearableEditText;
import android.widget.EditText;

/**
 * 修改好友备注的界面
 * @author huanghui1
 * @version 1.0.0
 * @update 2015年2月26日 下午2:06:59
 */
public class RemarkEditActivity extends BaseActivity {
	private EditText etNickname;
	private EditText etNickDesc;
	
	/**
	 * 传过来的用户实体
	 */
	private User mUser;

	@Override
	protected int getContentView() {
		return R.layout.activity_edit_remark;
	}

	@Override
	protected void initView() {
		etNickname = (EditText) findViewById(R.id.et_nickname);
		etNickDesc = (EditText) findViewById(R.id.et_nick_desc);
	}

	@Override
	protected void initData() {
		mUser = getIntent().getParcelableExtra(UserInfoActivity.ARG_USER);
		
		if (mUser != null) {
			etNickname.setText(mUser.getNickname());
			UserVcard userVcard = mUser.getUserVcard();
			if (userVcard != null) {
				etNickDesc.setText(userVcard.getNickDescription());
			}
		}
	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub

	}

}
