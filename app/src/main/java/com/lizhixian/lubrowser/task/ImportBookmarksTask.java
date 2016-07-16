package com.lizhixian.lubrowser.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.lizhixian.lubrowser.R;
import com.lizhixian.lubrowser.fragment.SettingFragment;
import com.lizhixian.lubrowser.unit.BrowserUnit;
import com.lizhixian.lubrowser.view.NinjaToast;

import java.io.File;

public class ImportBookmarksTask extends AsyncTask<Void, Void, Boolean> {
    private SettingFragment fragment;
    private Context context;
    private ProgressDialog dialog;
    private File file;
    private int count;

    public ImportBookmarksTask(SettingFragment fragment, File file) {
        this.fragment = fragment;
        this.context = fragment.getActivity();
        this.dialog = null;
        this.file = file;
        this.count = 0;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage(context.getString(R.string.toast_wait_a_minute));
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        count = BrowserUnit.importBookmarks(context, file);

        if (isCancelled()) {
            return false;
        }
        return count >= 0;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dialog.hide();
        dialog.dismiss();

        if (result) {
            fragment.setDBChange(true);
            NinjaToast.show(context, context.getString(R.string.toast_import_bookmarks_successful) + count);
        } else {
            NinjaToast.show(context, R.string.toast_import_bookmarks_failed);
        }
    }
}
