package com.example.chat.activity;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import android.widget.EditText;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.util.UnicodeFormatter;

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
		textView = (TextView) findViewById(R.id.textview);
		editText = (EditText) findViewById(R.id.edittext);
	}

	@Override
	protected void initData() {
		try {
			String unicode = "\u1f3b5";
			byte[] utf8Bytes = unicode.getBytes("UTF-8");
			byte[] defaultBytes = unicode.getBytes();
			textView.setText(fromCodePoint(0x1f3b5));
			editText.setText(fromCodePoint(0x1f3b5));
			String roundTrip = new String(utf8Bytes, "UTF-8");
			System.out.println("unicode = " + unicode);
			System.out.println("roundTrip = " + roundTrip);
			System.out.println();
			printBytes(utf8Bytes, "utf8Bytes");
			System.out.println();
			printBytes(defaultBytes, "defaultBytes");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
        for (int k = 0; k < array.length; k++) {
            System.out.println(name + "[" + k + "] = " + "0x" +
                UnicodeFormatter.byteToHex(array[k]));
        }
    }

}
