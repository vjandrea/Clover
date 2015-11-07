package org.floens.chan.ui.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.floens.chan.ChanBuild;
import org.floens.chan.R;
import org.floens.chan.ui.view.FixedRatioThumbnailView;
import org.floens.chan.utils.AndroidUtils;
import org.floens.chan.utils.IOUtils;

import static org.floens.chan.ui.theme.ThemeHelper.theme;
import static org.floens.chan.utils.AndroidUtils.setRoundItemBackground;

public class LegacyCaptchaLayout extends LinearLayout implements CaptchaLayoutInterface, View.OnClickListener {
    private FixedRatioThumbnailView image;
    private EditText input;
    private ImageView submit;

    private WebView internalWebView;

    private String baseUrl;
    private String siteKey;
    private CaptchaCallback callback;

    private String challenge;


    public LegacyCaptchaLayout(Context context) {
        super(context);
    }

    public LegacyCaptchaLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LegacyCaptchaLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        image = (FixedRatioThumbnailView) findViewById(R.id.image);
        image.setRatio(300f / 57f);
        image.setOnClickListener(this);
        input = (EditText) findViewById(R.id.input);
        submit = (ImageView) findViewById(R.id.submit);
        theme().sendDrawable.apply(submit);
        setRoundItemBackground(submit);
        submit.setOnClickListener(this);

        // This captcha layout uses a webview in the background
        // Because the script changed significantly we can't just load the image straight up from the challenge data anymore.
        // Now we load a skeleton page in the background, and wait until both the image and challenge key are loaded,
        // then the onCaptchaLoaded is called through the javascript interface.

        internalWebView = new WebView(getContext());
        internalWebView.setWebChromeClient(new WebChromeClient());
        internalWebView.setWebViewClient(new WebViewClient());

        WebSettings settings = internalWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        internalWebView.addJavascriptInterface(new CaptchaInterface(this), "CaptchaCallback");

        //noinspection PointlessBooleanExpression
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && ChanBuild.DEVELOPER_MODE) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == submit) {
            callback.captchaEntered(this, challenge, input.getText().toString());
        } else if (v == image) {
            reset();
        }
    }

    @Override
    public void initCaptcha(String baseUrl, String siteKey, boolean lightTheme, CaptchaCallback callback) {
        this.baseUrl = baseUrl;
        this.siteKey = siteKey;
        this.callback = callback;
    }

    @Override
    public void reset() {
        input.setText("");
        String html = IOUtils.assetAsString(getContext(), "captcha/captcha_legacy.html");
        html = html.replace("__site_key__", siteKey);
        internalWebView.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null);
        image.setUrl(null, 0, 0);
    }

    private void onCaptchaLoaded(final String imageUrl, final String challenge) {
        this.challenge = challenge;
        image.setUrl(imageUrl, 300, 57);
    }

    public static class CaptchaInterface {
        private final LegacyCaptchaLayout layout;

        public CaptchaInterface(LegacyCaptchaLayout layout) {
            this.layout = layout;
        }

        @JavascriptInterface
        public void onCaptchaLoaded(final String imageUrl, final String challenge) {
            AndroidUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layout.onCaptchaLoaded(imageUrl, challenge);
                }
            });
        }
    }
}
