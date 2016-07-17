package com.lizhixian.lubrowser;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lizhixian.lubrowser.base.BaseActivity;
import com.lizhixian.lubrowser.browser.AlbumController;
import com.lizhixian.lubrowser.browser.BrowserContainer;
import com.lizhixian.lubrowser.browser.BrowserController;
import com.lizhixian.lubrowser.database.RecordAction;
import com.lizhixian.lubrowser.dynamicgrid.DynamicGridView;
import com.lizhixian.lubrowser.service.HolderService;
import com.lizhixian.lubrowser.ui.SettingActivity;
import com.lizhixian.lubrowser.unit.BrowserUnit;
import com.lizhixian.lubrowser.unit.IntentUnit;
import com.lizhixian.lubrowser.unit.ViewUnit;
import com.lizhixian.lubrowser.util.TLog;
import com.lizhixian.lubrowser.view.DialogAdapter;
import com.lizhixian.lubrowser.view.GridAdapter;
import com.lizhixian.lubrowser.view.GridItem;
import com.lizhixian.lubrowser.view.NinjaRelativeLayout;
import com.lizhixian.lubrowser.view.NinjaToast;
import com.lizhixian.lubrowser.view.NinjaWebView;
import com.lizhixian.lubrowser.view.SwipeToBoundListener;
import com.lizhixian.lubrowser.view.SwitcherPanel;

import java.util.ArrayList;
import java.util.Arrays;
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

        switcherPanel.setStatusListener(new SwitcherPanel.StatusListener() {
            @Override
            public void onFling() {}

            @Override
            public void onExpanded() {}

            @Override
            public void onCollapsed() {
                inputBox.clearFocus();
            }
        });

        initSwitcherView();

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
                TLog.error("canSwipe switcherPanel.isKeyBoardShowing() = " + switcherPanel.isKeyBoardShowing());
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BrowserActivity.this);
                boolean ob = sp.getBoolean(getString(R.string.sp_omnibox_control), true);
                return !switcherPanel.isKeyBoardShowing() && ob;
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
                TLog.error("onBound canSwitch " + canSwitch + "  left= " + left);
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

    private void initSwitcherView() {

        switcherSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BrowserActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        switcherBookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAlbum(BrowserUnit.FLAG_BOOKMARKS);
            }
        });

        switcherHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAlbum(BrowserUnit.FLAG_HISTORY);
            }
        });

        switcherAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAlbum(BrowserUnit.FLAG_HOME);
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

    private void initHomeGrid(final NinjaRelativeLayout layout, boolean update) {
        if (update) {
            updateProgress(BrowserUnit.PROGRESS_MIN);
        }

        RecordAction action = new RecordAction(this);
        action.open(false);
        final List<GridItem> gridList = action.listGrid();
        action.close();

        DynamicGridView gridView = (DynamicGridView) layout.findViewById(R.id.home_grid);
        TextView aboutBlank = (TextView) layout.findViewById(R.id.home_about_blank);
        gridView.setEmptyView(aboutBlank);

        final GridAdapter gridAdapter;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridAdapter = new GridAdapter(this, gridList, 3);
        } else {
            gridAdapter = new GridAdapter(this, gridList, 2);
        }
        gridView.setAdapter(gridAdapter);
        gridAdapter.notifyDataSetChanged();

        /* Wait for gridAdapter.notifyDataSetChanged() */
        if (update) {
            gridView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    layout.setAlbumCover(ViewUnit.capture(layout, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                    updateProgress(BrowserUnit.PROGRESS_MAX);
                }
            }, shortAnimTime);
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateAlbum(gridList.get(position).getURL());
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showGridMenu(gridList.get(position));
                return true;
            }
        });
    }

    private void showGridMenu(final GridItem gridItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_list, null, false);
        builder.setView(layout);

        final String[] array = getResources().getStringArray(R.array.list_menu);
        final List<String> stringList = new ArrayList<>();
        stringList.addAll(Arrays.asList(array));
        stringList.remove(array[1]); // Copy link
        stringList.remove(array[2]); // Share

        ListView listView = (ListView) layout.findViewById(R.id.dialog_list);
        DialogAdapter dialogAdapter = new DialogAdapter(this, R.layout.dialog_text_item, stringList);
        listView.setAdapter(dialogAdapter);
        dialogAdapter.notifyDataSetChanged();

        final AlertDialog dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = stringList.get(position);
                if (s.equals(array[0])) { // New tab
                    addAlbum(getString(R.string.album_untitled), gridItem.getURL(), false, null);
                    NinjaToast.show(BrowserActivity.this, R.string.toast_new_tab_successful);
                } else if (s.equals(array[3])) { // Edit
                    //showEditDialog(gridItem);
                } else if (s.equals(array[4])) { // Delete
                    RecordAction action = new RecordAction(BrowserActivity.this);
                    action.open(true);
                    action.deleteGridItem(gridItem);
                    action.close();
                    BrowserActivity.this.deleteFile(gridItem.getFilename());

                    initHomeGrid((NinjaRelativeLayout) currentAlbumController, true);
                    NinjaToast.show(BrowserActivity.this, R.string.toast_delete_successful);
                }

                dialog.hide();
                dialog.dismiss();
            }
        });
    }

    private synchronized void addAlbum(int flag) {
        TLog.error("addAlbum flag = " + flag);

        final AlbumController controller;

        if (flag == BrowserUnit.FLAG_HOME) {
            NinjaRelativeLayout layout = (NinjaRelativeLayout) getLayoutInflater().inflate(R.layout.home, null, false);
            layout.setBrowserController(this);
            layout.setFlag(BrowserUnit.FLAG_HOME);
            layout.setAlbumCover(ViewUnit.capture(layout, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
            layout.setAlbumTitle(getString(R.string.album_title_home));
            controller = layout;
            initHomeGrid(layout, true);
        } else {
            return;
        }

        final View albumView = controller.getAlbumView();
        albumView.setVisibility(View.INVISIBLE);

        BrowserContainer.add(controller);
        switcherContainer.addView(albumView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.album_slide_in_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                albumView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                TLog.error("onAnimationEnd showAlbum");
                showAlbum(controller, false, true, true);
            }
        });
        albumView.startAnimation(animation);

    }

    private synchronized void addAlbum(String title, final String url, final boolean foreground, final Message resultMsg) {
        final NinjaWebView webView = new NinjaWebView(this);
        webView.setBrowserController(this);
        webView.setFlag(BrowserUnit.FLAG_NINJA);
        webView.setAlbumCover(ViewUnit.capture(webView, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
        webView.setAlbumTitle(title);
        ViewUnit.bound(this, webView);

        final View albumView = webView.getAlbumView();
        if (currentAlbumController != null && (currentAlbumController instanceof NinjaWebView) && resultMsg != null) {
            int index = BrowserContainer.indexOf(currentAlbumController) + 1;
            BrowserContainer.add(webView, index);
            switcherContainer.addView(albumView, index, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        } else {
            BrowserContainer.add(webView);
            switcherContainer.addView(albumView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        if (!foreground) {
            ViewUnit.bound(this, webView);
            webView.loadUrl(url);
            webView.deactivate();

            albumView.setVisibility(View.VISIBLE);
            if (currentAlbumController != null) {
                switcherScroller.smoothScrollTo(currentAlbumController.getAlbumView().getLeft(), 0);
            }
            return;
        }

        albumView.setVisibility(View.INVISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.album_slide_in_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                albumView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showAlbum(webView, false, true, false);

                if (url != null && !url.isEmpty()) {
                    webView.loadUrl(url);
                } else if (resultMsg != null) {
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(webView);
                    resultMsg.sendToTarget();
                }
            }
        });
        albumView.startAnimation(animation);
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
    public void showAlbum(AlbumController controller, boolean anim, final boolean expand, final boolean capture) {
        if (controller == null || controller == currentAlbumController) {
            TLog.error("showAlbum switcherPanel.expanded()");
            switcherPanel.expanded();
            return;
        }

        if (currentAlbumController != null && anim) {
            currentAlbumController.deactivate();
            final View rv = (View) currentAlbumController;
            final View av = (View) controller;

            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.album_fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {}

                @Override
                public void onAnimationStart(Animation animation) {
                    contentFrame.removeAllViews();
                    contentFrame.addView(av);
                }
            });
            rv.startAnimation(fadeOut);
        } else {
            if (currentAlbumController != null) {
                currentAlbumController.deactivate();
            }
            contentFrame.removeAllViews();
            contentFrame.addView((View) controller);
        }

        TLog.error("showAlbum currentAlbumController 970");
        currentAlbumController = controller;
        currentAlbumController.activate();

        updateOmnibox();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (expand) {
                    TLog.error("run showAlbum switcherPanel.expanded()");
                    switcherPanel.expanded();
                }

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
    public void onCreateView(WebView view, final Message resultMsg) {

        if (resultMsg == null) {
            return;
        }
        switcherPanel.collapsed();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addAlbum(getString(R.string.album_untitled), null, true, resultMsg);
            }
        }, shortAnimTime);

    }

    @Override
    public boolean onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback) {
        return onShowCustomView(view, callback);
    }

    @Override
    public boolean onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (view == null) {
            return false;
        }
        /*if (customView != null && callback != null) {
            callback.onCustomViewHidden();
            return false;
        }

        customView = view;
        originalOrientation = getRequestedOrientation();

        fullscreenHolder = new FullscreenHolder(this);
        fullscreenHolder.addView(
                customView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));

        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        decorView.addView(
                fullscreenHolder,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));

        customView.setKeepScreenOn(true);
        ((View) currentAlbumController).setVisibility(View.GONE);
        setCustomFullscreen(true);

        if (view instanceof FrameLayout) {
            if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
                videoView = (VideoView) ((FrameLayout) view).getFocusedChild();
                videoView.setOnErrorListener(new VideoCompletionListener());
                videoView.setOnCompletionListener(new VideoCompletionListener());
            }
        }
        customViewCallback = callback;*/
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Auto landscape when video shows

        return true;
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
            TLog.error("updateAlbum NinjaWebView ");
            ((NinjaWebView) currentAlbumController).loadUrl(url);
            updateOmnibox();

        } else if (currentAlbumController instanceof NinjaRelativeLayout) {
            TLog.error("updateAlbum NinjaRelativeLayout ");

            NinjaWebView webView = new NinjaWebView(this);
            webView.setBrowserController(this);
            webView.setFlag(BrowserUnit.FLAG_NINJA);
            webView.setAlbumCover(ViewUnit.capture(webView, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
            webView.setAlbumTitle(getString(R.string.album_untitled));
            ViewUnit.bound(this, webView);

            int index = switcherContainer.indexOfChild(currentAlbumController.getAlbumView());
            currentAlbumController.deactivate();
            switcherContainer.removeView(currentAlbumController.getAlbumView());
            contentFrame.removeAllViews(); ///

            switcherContainer.addView(webView.getAlbumView(), index, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            contentFrame.addView(webView);
            BrowserContainer.set(webView, index);
            TLog.error("currentAlbumController 795");
            currentAlbumController = webView;
            webView.activate();

            webView.loadUrl(url);
            updateOmnibox();
        } else {
            NinjaToast.show(this, R.string.toast_load_error);
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
