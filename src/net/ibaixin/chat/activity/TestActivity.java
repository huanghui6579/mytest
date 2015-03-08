package net.ibaixin.chat.activity;

import java.util.ArrayList;
import java.util.List;

import net.ibaixin.chat.R;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2015年1月31日 上午11:50:55
 */
public class TestActivity extends BaseActivity {
	private ListView listView;
	private List<String> data;
	private ArrayAdapter<String> adapter;

	@Override
	protected int getContentView() {
		return R.layout.activity_test;
	}

	@Override
	protected void initView() {
		listView = (ListView) findViewById(R.id.lv_data);
	}

	@Override
	protected void initData() {
		data = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			data.add("test" + i);
		}
		adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, data);
		listView.setAdapter(adapter);
	}
	
	@Override
	protected void initWidow() {
//		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
//		requestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub

	}

}
