package com.kii.sample.chat.ui.util;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SimpleProgressDialogFragment extends DialogFragment {

    public static final String TAG = "ProgressDialogFragment";
    public static SimpleProgressDialogFragment newInstance() {
        return new SimpleProgressDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog d = new ProgressDialog(getActivity());
        d.setIndeterminate(true);
        return d;
    }

}
