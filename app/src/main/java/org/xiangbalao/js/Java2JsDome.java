package org.xiangbalao.js;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.common.utils.LogUtil;

import org.xiangbalao.jscallcamera.R;

public class Java2JsDome extends Activity implements OnClickListener {
    private static final String LOG_TAG = "WebViewDemo";
    private WebView mWebView;
    private Button mButton;
    private boolean flag = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        initView();
        setWebView();
    }

    private void setWebView() {
        // WebSettings 几乎浏览器的所有设置都在该类中进行
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true); // 设置允许访问文件数据
        webSettings.setSupportZoom(false);
        mWebView.setWebViewClient(new DemoWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        /*
		 * DemoJavaScriptInterface类为js调用android服务器端提供接口 android
		 * 作为DemoJavaScriptInterface类的客户端接口被js调用
		 * 调用的具体方法在DemoJavaScriptInterface中定义： 例如该实例中的clickOnAndroid
		 */
        mWebView.addJavascriptInterface(new DemoJavaScriptInterface(), "demo"); // demo
        // 是前缀，可以理解为空间

        //mWebView.addJavascriptInterface(new DemoJavaScriptInterface(), null);

        mWebView.loadUrl("file:///android_asset/index.html"); // 对应当前project的asserts目录
    }

    private void initView() {
        // 获得浏览器组件
        mWebView = (WebView) findViewById(R.id.webview);
        mButton = (Button) findViewById(R.id.getjs);
        mButton.setOnClickListener(this);
    }

    class DemoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    /**
     * java 接口 实现方法被 webView调用
     *
     * @author longtaoge
     */
    final class DemoJavaScriptInterface {
        DemoJavaScriptInterface() {

        }

        /**
         * 该方法被浏览器端调用
         */
        public void clickOnAndroid() {

            Toast.makeText(Java2JsDome.this, "js 调用了 JAVA代码",
                    Toast.LENGTH_SHORT).show();

            LogUtil.i("JAVA2jsDome", "clickOnAndroid");


        }
    }

    /**
     * 继承WebChromeClient类
     * 在这个类的3个方法中，分别使用Android的内置控件重写了Js中对应的对话框，就是说对js中的对话框做处理了，就是重写了。
     */
    final class MyWebChromeClient extends WebChromeClient {
        /**
         * 处理confirm弹出框
         */
        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   JsResult result) {
            Log.d(LOG_TAG, "onJsConfirm:" + message);
            // 对confirm的简单封装
            new AlertDialog.Builder(Java2JsDome.this)
                    .setTitle("Confirm")
                    .setMessage(message)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {

                                }
                            }).create().show();

            // result.confirm();
            return true;

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getjs:
                // mWebView调用 js 代码的 wave()方法
                if (flag) {

                    mWebView.loadUrl("javascript:wave(" + "'android_normal.png')");
                } else {
                    mWebView.loadUrl("javascript:wave(" + "'android_waving.png')");

                }
                flag = !flag;

                break;

            default:
                break;
        }

    }
}
