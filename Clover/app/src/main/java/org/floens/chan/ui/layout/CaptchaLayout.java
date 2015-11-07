/*
 * Clover - 4chan browser https://github.com/Floens/Clover/
 * Copyright (C) 2014  Floens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.floens.chan.ui.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.floens.chan.ChanBuild;
import org.floens.chan.utils.AndroidUtils;
import org.floens.chan.utils.IOUtils;

public class CaptchaLayout extends WebView implements CaptchaLayoutInterface {
    private static final String TAG = "CaptchaLayout";

    private CaptchaCallback callback;
    private boolean loaded = false;
    private String baseUrl;
    private String siteKey;
    private boolean lightTheme;

    public CaptchaLayout(Context context) {
        super(context);
    }

    public CaptchaLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CaptchaLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    public void initCaptcha(String baseUrl, String siteKey, boolean lightTheme, CaptchaCallback callback) {
        this.callback = callback;
        this.baseUrl = baseUrl;
        this.siteKey = siteKey;
        this.lightTheme = lightTheme;

        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);

        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(@NonNull ConsoleMessage consoleMessage) {
                Log.i(TAG, consoleMessage.lineNumber() + ":" + consoleMessage.message() + " " + consoleMessage.sourceId());
                return true;
            }
        });

        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getHost().equals(Uri.parse(CaptchaLayout.this.baseUrl).getHost())) {
                    return false;
                } else {
                    AndroidUtils.openLink(url);
                    return true;
                }
            }
        });
        setBackgroundColor(0x00000000);

        addJavascriptInterface(new CaptchaInterface(this), "CaptchaCallback");

        //noinspection PointlessBooleanExpression
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && ChanBuild.DEVELOPER_MODE) {
            setWebContentsDebuggingEnabled(true);
        }
    }

    public void reset() {
        if (loaded) {
            loadUrl("javascript:grecaptcha.reset()");
        } else {
            loaded = true;

            String html = IOUtils.assetAsString(getContext(), "captcha/captcha.html");
            html = html.replace("__site_key__", siteKey);
            html = html.replace("__theme__", lightTheme ? "light" : "dark");

            loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null);
        }
    }

    private void onCaptchaLoaded() {
        callback.captchaLoaded(this);
    }

    private void onCaptchaEntered(String challenge, String response) {
        if (TextUtils.isEmpty(response)) {
            reset();
        } else {
            callback.captchaEntered(this, challenge, response);
        }
    }

    public static class CaptchaInterface {
        private final CaptchaLayout layout;

        public CaptchaInterface(CaptchaLayout layout) {
            this.layout = layout;
        }

        @JavascriptInterface
        public void onCaptchaLoaded() {
            AndroidUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layout.onCaptchaLoaded();
                }
            });
        }

        @JavascriptInterface
        public void onCaptchaEntered(final String response) {
            AndroidUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layout.onCaptchaEntered(null, response);
                }
            });
        }

        @JavascriptInterface
        public void onCaptchaEnteredv1(final String challenge, final String response) {
            AndroidUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layout.onCaptchaEntered(challenge, response);
                }
            });
        }
    }
}
