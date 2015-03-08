package net.ibaixin.chat.fragment;

import net.ibaixin.chat.R;
import net.ibaixin.chat.util.Constants;
import net.ibaixin.chat.util.WebviewSettings;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
/**
 * 趣图Fragment的界面
 * 
 * @author dudejin
 */
public class JokeFragment extends BaseFragment{
//	private final String url = "http://www.ibaixin.net/ibaixin/jokemobile/listJokesPic" ;
	private final static String websitePrefix = "http://"+Constants.SERVER_HOST ;
//	public static String loginUrl = "http://192.168.42.28:8080/ibaixin/user/login/" ;
	public final static String loginUrl = websitePrefix+"/ibaixin/user/login" ;
	public final static String registerUrl = websitePrefix+"/ibaixin/user/register" ;
	public final static String sessionUrl = websitePrefix+"/ibaixin/" ;
	private static String url = websitePrefix+"/ibaixin/jokemobile/listJokesAll" ;
	private final static String listJokesText = websitePrefix+"/ibaixin/jokemobile/listJokesText" ;
	private final static String listJokesPic = websitePrefix+"/ibaixin/jokemobile/listJokesPic" ;
	private final static String listJokesLife = websitePrefix+"/ibaixin/jokemobile/listJokesLife" ;
//    /** Fragment当前状态是否可见 */
//    protected boolean isVisible ;
//    /** 标志位，标志已经初始化完成 */
//    private boolean isPrepared = false;
//    /** 是否已被加载过一次，第二次就不再去请求数据了 */
//    private boolean mHasLoadedOnce;
//    /** 0、1、2、3分别代表：全部、笑话、趣图、感悟*/
	private int type = 0 ;
	/**
	 * 定义浏览器内核对象
	 */
	private WebView webView;
	/**
	 * 下拉刷新view
	 */
	private SwipeRefreshLayout swipeLayout;
	
	/**
	 * @param type 0、1、2、3分别代表：全部、笑话、趣图、感悟
	 */
	public JokeFragment(int type) {
		this.type = type ;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_joke, container, false);
		webView = (WebView) view.findViewById(R.id.jokeswebView) ;
		swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
		switch (type) {
		case 1:
			url = listJokesText;
			break;
		case 2:
			url = listJokesPic;
			break;
		case 3:
			url = listJokesLife;
			break;
		default:
			break;
		}
		WebviewSettings.initWebView(webView, swipeLayout, url);
//		isPrepared = true ;
//		lazyLoad() ;
		return view;
	}

	/*protected void lazyLoad() {
		if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }
		WebviewSettings.initWebView(webView, swipeLayout, url);
		mHasLoadedOnce = true;
	}
	
	 @Override
     public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            isVisible = true;
            lazyLoad();
        } else {
            isVisible = false;
        }
     }*/
	 
}
