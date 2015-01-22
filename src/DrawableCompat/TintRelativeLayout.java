package DrawableCompat;

import android.content.Context;
import android.support.v7.internal.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2015年1月22日 下午5:32:14
 */
public class TintRelativeLayout extends RelativeLayout {
	private static final int[] TINT_ATTRS = {
        android.R.attr.background
	};

	public TintRelativeLayout(Context context) {
		super(context, null);
		// TODO Auto-generated constructor stub
	}

	public TintRelativeLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, TINT_ATTRS,
                defStyleAttr, 0);
        setBackgroundDrawable(a.getDrawable(0));
        a.recycle();
	}

	public TintRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.editTextStyle);
	}

}
