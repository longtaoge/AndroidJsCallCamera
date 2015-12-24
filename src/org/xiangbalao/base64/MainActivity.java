package org.xiangbalao.base64;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.xiangbalao.jscallcamera.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.common.utils.FileUtils;
import com.common.utils.PictureUtils;


@SuppressLint("JavascriptInterface")
public class MainActivity extends Activity implements OnClickListener {

	private Button callcamera;
	private ImageView poto_image;
	private String bitmaptoString;
	private Button showbase64;
	private File outDir = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	private String base64 = Environment.getExternalStorageDirectory()
			+ "/base64.txt";
	private WebView mWebView;
	private String picFileFullName;
	private int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_1);

		initView();

		setWebView();
	}

	private void setWebView() {
		mWebView.requestFocus();

		mWebView.setOnKeyListener(new View.OnKeyListener() { // webview can go
																// back
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
					mWebView.goBack();
					return true;
				}
				return false;
			}
		});

		String url = "file:///android_asset/index2.html";
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		mWebView.setBackgroundColor(Color.parseColor("#00000000"));
		mWebView.getSettings().setDefaultTextEncodingName("GBK");
		mWebView.loadUrl(url);
		mWebView.addJavascriptInterface(this, "xiangbalao");
		mWebView.setWebChromeClient(new MyWebChromeClient());
	}

	private void initView() {
		callcamera = (Button) findViewById(R.id.callcamera);
		poto_image = (ImageView) findViewById(R.id.poto_image);
		showbase64 = (Button) findViewById(R.id.showbase64);
		showbase64.setOnClickListener(this);
		callcamera.setOnClickListener(this);
		mWebView = (WebView) findViewById(R.id.webview);
	}

	// js调用android摄像头
	@JavascriptInterface
	public void getCamera() {

		callCamera();

	}

	public void callCamera() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			if (!outDir.exists()) {
				outDir.mkdirs();
			}
			File outFile = new File(outDir, System.currentTimeMillis() + ".jpg");
			picFileFullName = outFile.getAbsolutePath();
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		} else {
			// Log.e(tag, "请确认已经插入SD卡");
			System.out.println("请确认已经插入SD卡");
		}

	}

	// 内容的渲染需要webviewChromClient去实现解决js中alert不弹出的问题和其它内容渲染问题；
	public class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int progress) {
			MainActivity.this.setTitle("Loading...");
			MainActivity.this.setProgress(progress);

			if (progress >= 80) {
				MainActivity.this.setTitle("JsAndroid Test01");
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Bitmap bmp;

				bmp = PictureUtils.reducePicSize(PictureUtils
						.decodeUriAsBitmap(Uri.parse(picFileFullName)), 94, 94);
				poto_image.setImageBitmap(bmp);

				bitmaptoString = bitmaptoString(bmp);

				if (bitmaptoString != null) {

					mWebView.loadUrl("javascript:usePhoto(" + "'"
							+ bitmaptoString + "')");
					mWebView.loadUrl("javascript:usePhoto1(" + "'"
							+ bitmaptoString + "')");

				} else {
					Toast.makeText(MainActivity.this, "bitmaptoString为空",
							Toast.LENGTH_SHORT).show();
				}
				FileUtils.writeFile(base64, bitmaptoString, false);

			}
		}

	}

	// // 将图片转换成字符串
	public String bitmaptoString(Bitmap bitmap) {
		String string = null;
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, bStream);
		byte[] bytes = bStream.toByteArray();
		string = Base64.encodeToString(bytes, Base64.NO_WRAP);

		return string;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.getjs:

			callCamera();

			break;
		case R.id.showbase64:

			// 加载Res 中的Base64图片

			String imageBase64 = getResources().getString(R.string.image);

			/*
			 * mWebView.loadUrl("javascript:usePhoto(" + "'" + bitmaptoString +
			 * "')");
			 */

			mWebView.loadUrl("javascript:usePhoto1(" + "'" + imageBase64 + "')");
			mWebView.loadUrl("javascript:usePhoto(" + "'" + bitmaptoString
					+ "')");

			mWebView.loadUrl("javascript:usePhoto(" + "'" + imageBase64 + "')");

			/*
			 * mWebView.loadUrl("javascript:usePhoto1('" + bitmaptoString +
			 * "')");
			 */

			break;

		default:
			break;
		}

	}

}
