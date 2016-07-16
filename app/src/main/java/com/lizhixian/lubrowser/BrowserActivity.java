package com.lizhixian.lubrowser;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lizhixian.lubrowser.browser.AlbumController;
import com.lizhixian.lubrowser.browser.BrowserContainer;
import com.lizhixian.lubrowser.browser.BrowserController;
import com.lizhixian.lubrowser.service.HolderService;
import com.lizhixian.lubrowser.ui.SettingActivity;
import com.lizhixian.lubrowser.unit.BrowserUnit;
import com.lizhixian.lubrowser.unit.IntentUnit;
import com.lizhixian.lubrowser.unit.ViewUnit;
import com.lizhixian.lubrowser.util.TLog;
import com.lizhixian.lubrowser.view.NinjaRelativeLayout;
import com.lizhixian.lubrowser.view.NinjaToast;
import com.lizhixian.lubrowser.view.NinjaWebView;
import com.lizhixian.lubrowser.view.SwipeToBoundListener;
import com.lizhixian.lubrowser.view.SwitcherPanel;

import java.util.List;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrowserActivity extends BaseActivity implements View.OnClickListener,BrowserController{

    @BindView(R.id.switcher_panel)
    SwitcherPanel switcherPanel;
    @BindView(R.id.switcher_scroller)
    HorizontalScrollView switcherScroller;
    @BindView(R.id.switcher_container)
    LinearLayout switcherContainer;
    @BindView(R.id.switcher_setting)
    ImageButton switcherSetting;
    @BindView(R.id.switcher_bookmarks)
    ImageButton switcherBookmarks;
    @BindView(R.id.switcher_history)
    ImageButton switcherHistory;
    @BindView(R.id.switcher_add)
    ImageButton switcherAdd;

    @BindView(R.id.main_omnibox)
    RelativeLayout omnibox;
    @BindView(R.id.main_omnibox_input)
    AutoCompleteTextView inputBox;
    @BindView(R.id.main_omnibox_bookmark)
    ImageButton omniboxBookmark;
    @BindView(R.id.main_omnibox_refresh)
    ImageButton omniboxRefresh;
    @BindView(R.id.main_omnibox_overflow)
    ImageButton omniboxOverflow;
    @BindView(R.id.main_progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.main_search_panel)
    RelativeLayout searchPanel;
    @BindView(R.id.main_search_box)
    EditText searchBox;
    @BindView(R.id.main_search_up)
    ImageButton searchUp;
    @BindView(R.id.main_search_down)
    ImageButton searchDown;
    @BindView(R.id.main_search_cancel)
    ImageButton searchCancel;

    @BindView(R.id.main_relayout_ok)
    Button relayoutOK;
    @BindView(R.id.main_content)
    FrameLayout contentFrame;

    private static final int DOUBLE_TAPS_QUIT_DEFAULT = 2000;

    private int anchor;
    @BindDimen(R.dimen.layout_width_156dp)
    float dimen156dp;
    @BindDimen(R.dimen.layout_width_144dp)
    float dimen144dp;
    @BindDimen(R.dimen.layout_height_117dp)
    float dimen117dp;
    @BindDimen(R.dimen.layout_height_108dp)
    float dimen108dp;
    @BindDimen(R.dimen.layout_height_48dp)
    float dimen48dp;

    private static boolean quit = false;
    private boolean create = true;
    private int shortAnimTime = 0;
    private int mediumAnimTime = 0;
    private int longAnimTime = 0;
    private AlbumController currentAlbumController = null;

    private ValueCallback<Uri[]> filePathCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        create = true;
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentUnit.setContext(this);
        if (create) {
            return;
        }
        dispatchIntent(getIntent());

    }

    private void initView() {
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId != EditorInfo.IME_ACTION_DONE) {
                    return false;
                }
                if (searchBox.getText().toString().isEmpty()) {
                    NinjaToast.show(BrowserActivity.this, R.string.toast_input_empty);
                    return true;
                }
                return false;
            }
        });

        inputBox.setOnTouchListener(new SwipeToBoundListener(omnibox,new SwipeToBoundListener.BoundCallback(){
            private KeyListener keyListener = inputBox.getKeyListener();

            @Override
            public boolean canSwipe() {
                return false;
            }

            @Override
            public void onSwipe() {
                inputBox.setKeyListener(null);
                inputBox.setFocusable(false);
                inputBox.setFocusableInTouchMode(false);
                inputBox.clearFocus();
            }

            @Override
            public void onBound(boolean canSwitch, boolean left) {
                TLog.error("canSwitch " + canSwitch + "  left= " + left);
                inputBox.setKeyListener(keyListener);
                inputBox.setFocusable(true);
                inputBox.setFocusableInTouchMode(true);
                inputBox.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                inputBox.clearFocus();

                if (canSwitch) {
                    AlbumController controller = nextAlbumController(left);
                    showAlbum(controller, false, false, true);
                    NinjaToast.show(BrowserActivity.this, controller.getAlbumTitle());
                }

            }


        }));

        inputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                TLog.error("inputBox onEditorAction " + (currentAlbumController == null)) ;
                if (currentAlbumController == null) {
                    return false;
                }

                String query = inputBox.getText().toString().trim();
                if (query.isEmpty()) {
                    NinjaToast.show(BrowserActivity.this, R.string.toast_input_empty);
                    return true;
                }
                updateAlbum(query);
                hideSoftInput(inputBox);


                return false;
            }
        });

    }

    private void initData() {
        dispatchIntent(getIntent());
    }

    private void dispatchIntent(Intent intent) {
        TLog.error("dispatchIntent " + (null==intent));
        Intent holderService = new Intent(this, HolderService.class);
        IntentUnit.setClear(false);
        stopService(holderService);

        if(null != intent) {

            if(intent.hasExtra(IntentUnit.OPEN)) {// From HolderActivity's menu
                TLog.error("dispatchIntent OPEN");
                pinAlbums(intent.getStringExtra(IntentUnit.OPEN));
            }else if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_WEB_SEARCH)) {// From ActionMode and some others
                TLog.error("dispatchIntent QUERY");
                pinAlbums(intent.getStringExtra(SearchManager.QUERY));
            }else if (filePathCallback != null) {
                TLog.error("dispatchIntent filePathCallback");
                filePathCallback = null;
            } else {
                TLog.error("dispatchIntent 999");
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                if (sp.getBoolean(getString(R.string.sp_first), true)) {
                    String lang;
                    if (getResources().getConfiguration().locale.getLanguage().equals("zh")) {
                        lang = BrowserUnit.INTRODUCTION_ZH;
                    } else {
                        lang = BrowserUnit.INTRODUCTION_EN;
                    }
                    pinAlbums(BrowserUnit.BASE_URL + lang);
                    sp.edit().putBoolean(getString(R.string.sp_first), false).commit();
                } else {
                    pinAlbums(null);
                }
            }


        } else {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            if (sp.getBoolean(getString(R.string.sp_first), true)) {
                String lang;
                if (getResources().getConfiguration().locale.getLanguage().equals("zh")) {
                    lang = BrowserUnit.INTRODUCTION_ZH;
                } else {
                    lang = BrowserUnit.INTRODUCTION_EN;
                }
                pinAlbums(BrowserUnit.BASE_URL + lang);
                sp.edit().putBoolean(getString(R.string.sp_first), false).commit();
            } else {
                pinAlbums(null);
            }
        }

    }


    @Override
    @OnClick({ R.id.switcher_setting,R.id.main_omnibox_refresh })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switcher_setting:
                gotoActivity(SettingActivity.class);
                break;
            case R.id.switcher_add:
                addAlbum(BrowserUnit.FLAG_HOME);
                break;
            case R.id.main_omnibox_refresh:
                refreshPage();
                break;
            default:
                break;
        }
    }

    private void refreshPage() {
        if (currentAlbumController == null) {
            NinjaToast.show(BrowserActivity.this, R.string.toast_refresh_failed);
            return;
        }

        if (currentAlbumController instanceof NinjaWebView) {
            NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
            if (ninjaWebView.isLoadFinish()) {
                ninjaWebView.reload();
            } else {
                ninjaWebView.stopLoading();
            }
        } else if (currentAlbumController instanceof NinjaRelativeLayout) {
            /*final NinjaRelativeLayout layout = (NinjaRelativeLayout) currentAlbumController;
            if (layout.getFlag() == BrowserUnit.FLAG_HOME) {
                initHomeGrid(layout, true);
                return;
            }
            initBHList(layout, true);*/
        } else {
            NinjaToast.show(BrowserActivity.this, R.string.toast_refresh_failed);
        }
    }

    private synchronized void addAlbum(int flag) {

    }

    private AlbumController nextAlbumController(boolean next) {
        if (BrowserContainer.size() <= 1) {
            return currentAlbumController;
        }

        List<AlbumController> list = BrowserContainer.list();
        int index = list.indexOf(currentAlbumController);
        if (next) {
            index++;
            if (index >= list.size()) {
                index = 0;
            }
        } else {
            index--;
            if (index < 0) {
                index = list.size() - 1;
            }
        }

        return list.get(index);
    }

    @Override
    public void updateAutoComplete() {

    }

    @Override
    public void updateBookmarks() {

    }

    @Override
    public void updateInputBox(String query) {

    }

    @Override
    public void updateProgress(int progress) {

    }

    @Override
    public void showAlbum(AlbumController controller, boolean anim, boolean expand, final boolean capture) {
        if (controller == null || controller == currentAlbumController) {
            switcherPanel.expanded();
            return;
        }

        currentAlbumController = controller;
        currentAlbumController.activate();

        updateOmnibox();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (capture) {
                    currentAlbumController.setAlbumCover(ViewUnit.capture((View)currentAlbumController,dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                }
            }
        }, shortAnimTime);
    }

    @Override
    public void removeAlbum(AlbumController albumController) {

    }

    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {

    }

    @Override
    public void showFileChooser(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {

    }

    @Override
    public void onCreateView(WebView view, Message resultMsg) {

    }

    @Override
    public boolean onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback) {
        return false;
    }

    @Override
    public boolean onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        return false;
    }

    @Override
    public boolean onHideCustomView() {
        return false;
    }

    @Override
    public void onLongPress(String url) {

    }


    private void updateOmnibox() {
        TLog.error("updateOmnibox");
        if (currentAlbumController == null) {
            return;
        }

        if (currentAlbumController instanceof NinjaWebView) {
            NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
            updateProgress(ninjaWebView.getProgress());

            if (ninjaWebView.getUrl() == null && ninjaWebView.getOriginalUrl() == null) {
                updateInputBox(null);
            } else if (ninjaWebView.getUrl() != null) {
                updateInputBox(ninjaWebView.getUrl());
            } else {
                updateInputBox(ninjaWebView.getOriginalUrl());
            }

        }


    }

    private synchronized void updateAlbum() {

    }

    private synchronized void updateAlbum(String url) {
        TLog.error("updateAlbum url " + url);
        if (currentAlbumController == null) {
            return;
        }

        if (currentAlbumController instanceof NinjaWebView) {
            ((NinjaWebView) currentAlbumController).loadUrl(url);
            updateOmnibox();

        }

    }

    private synchronized void pinAlbums(String url) {
        TLog.error("pinAlbums ur = " + url);
        hideSoftInput(inputBox);

        switcherContainer.removeAllViews();

        for (AlbumController controller : BrowserContainer.list()) {
            if (controller instanceof NinjaWebView) {
                ((NinjaWebView) controller).setBrowserController(this);
            }
            switcherContainer.addView(controller.getAlbumView(), LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            controller.getAlbumView().setVisibility(View.VISIBLE);
            controller.deactivate();
        }

        if(null == url) {
            if (BrowserContainer.size() < 1) {
                addAlbum(BrowserUnit.FLAG_HOME);
            } else {
                if (currentAlbumController != null) {
                    currentAlbumController.activate();
                    return;
                }

                int index = BrowserContainer.size() - 1;
                currentAlbumController = BrowserContainer.get(index);
                contentFrame.removeAllViews();
                contentFrame.addView((View) currentAlbumController);
                currentAlbumController.activate();

                updateOmnibox();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switcherScroller.smoothScrollTo(currentAlbumController.getAlbumView().getLeft(), 0);
                        currentAlbumController.setAlbumCover(ViewUnit.capture(((View) currentAlbumController), dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                    }
                }, shortAnimTime);
            }
        } else {// When url != null

            NinjaWebView webView = new NinjaWebView(this);
            webView.setBrowserController(this);
            webView.setFlag(BrowserUnit.FLAG_NINJA);
            webView.setAlbumCover(ViewUnit.capture(webView, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
            webView.setAlbumTitle(getString(R.string.album_untitled));
            ViewUnit.bound(this, webView);
            webView.loadUrl(url);

            BrowserContainer.add(webView);
            final View albumView = webView.getAlbumView();
            albumView.setVisibility(View.VISIBLE);
            switcherContainer.addView(albumView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            contentFrame.removeAllViews();
            contentFrame.addView(webView);

            if (currentAlbumController != null) {
                currentAlbumController.deactivate();
            }
            currentAlbumController = webView;
            currentAlbumController.activate();

            updateOmnibox();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    switcherScroller.smoothScrollTo(currentAlbumController.getAlbumView().getLeft(), 0);
                    currentAlbumController.setAlbumCover(ViewUnit.capture(((View) currentAlbumController), dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                }
            }, shortAnimTime);

        }

    }
}