package com.example.gary.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * This is the code for the Help Dialogs in all pages.
 * Created by gary on 27/03/2016.
 */
public class HelpDialogFragment extends DialogFragment {

    public static final String HELP_TAG = "help_tag";
    public static final String HELP_MSG = "help_msg";

    public Dialog onCreateDialog(Bundle savedInstanceState){
        String msg = this.getArguments().getString(HELP_MSG);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.help_dialog_title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(getString(R.string.debug_tag),"User clicks OK");
                    }
                });
        return builder.create();
    }
}
