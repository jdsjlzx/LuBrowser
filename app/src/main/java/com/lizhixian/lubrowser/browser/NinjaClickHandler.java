package com.lizhixian.lubrowser.browser;

import android.os.Handler;
import android.os.Message;

import com.lizhixian.lubrowser.view.NinjaWebView;


public class NinjaClickHandler extends Handler {
    private NinjaWebView webView;

    public NinjaClickHandler(NinjaWebView webView) {
        super();
        this.webView = webView;
    }

    @Override
    public void handleMessage(Message message) {
        super.handleMessage(message);
        webView.getBrowserController().onLongPress(message.getData().getString("url"));
    }
}
