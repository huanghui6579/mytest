package net.ibaixin.joke.chat.activity;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import net.ibaixin.joke.chat.R;
import net.ibaixin.joke.chat.util.UnicodeFormatter;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2015年1月31日 上午11:50:55
 */
public class TestActivity extends BaseActivity {
	private TextView textView;
	private EditText editText;

	@Override
	protected int getContentView() {
		return R.layout.activity_test;
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void initData() {
	}

	@Override
	protected void addListener() {
		// TODO Auto-generated method stub

	}
	
	public static String fromCodePoint(int codePoint) {
        return newString(codePoint);
    }

    public static String fromChar(char ch) {
        return Character.toString(ch);
    }

    public static final String newString(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(Character.toChars(codePoint));
        }
    }
    
    public static void printBytes(byte[] array, String name) {
    }

}
