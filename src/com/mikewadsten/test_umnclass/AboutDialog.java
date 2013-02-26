package com.mikewadsten.test_umnclass;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

// AboutDialog code "inspired" (read - stolen) from Roman Nurik's
// DashClock code.
public class AboutDialog extends DialogFragment {
    private static final String VERSION_UNAVAILABLE = "N/A";

    public AboutDialog() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get app version
        PackageManager pm = getActivity().getPackageManager();
        String packageName = getActivity().getPackageName();
        String versionName;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = VERSION_UNAVAILABLE;
        }

        // Build the about body view
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View rootView = layoutInflater.inflate(R.layout.dialog_about, null);
        TextView nameAndVersion = (TextView) rootView
                .findViewById(R.id.app_name_and_version);
        nameAndVersion.setText(
                Html.fromHtml(
                        String.format("<b>Classroom Finder</b>&nbsp;<font color=\"#888888\">v%s</font>", versionName)));
        ((TextView)rootView.findViewById(R.id.about_body))
        .setText(Html.fromHtml(getString(R.string.about_body)));
        ((TextView)rootView.findViewById(R.id.about_body))
        .setMovementMethod(new LinkMovementMethod());

        return new AlertDialog.Builder(getActivity())
        .setView(rootView)
        .setPositiveButton(R.string.close,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
        })
        .create();
    }
}