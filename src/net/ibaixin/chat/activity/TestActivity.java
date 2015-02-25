package net.ibaixin.chat.activity;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import com.afollestad.materialdialogs.MaterialDialog;

import net.ibaixin.chat.R;
import net.ibaixin.chat.util.UnicodeFormatter;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2015年1月31日 上午11:50:55
 */
public class TestActivity extends BaseActivity {

	@Override
	protected int getContentView() {
		return R.layout.activity_test;
	}

	@Override
	protected void initView() {
		Button basicNoTitle = (Button) findViewById(R.id.basicNoTitle);
		basicNoTitle.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new MaterialDialog.Builder(TestActivity.this)
                .title("提示")
                .titleColorAttr(R.attr.colorPrimary)
                .content("文本内容")
                .positiveText("确定")
                .negativeText("取消")
                .show();
			}
		});
	}

	@Override
	protected void initData() {
	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub

	}

}
