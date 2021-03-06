/*
 * Copyright (C) 2013 SlimRoms
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.beanstalk;

import android.app.AlertDialog;
import android.preference.CheckBoxPreference;
import android.os.Bundle;
import android.content.ContentResolver;
import android.provider.Settings;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.Preference;
import android.content.DialogInterface;
import android.view.WindowManagerGlobal;
import android.content.Intent;
import android.preference.PreferenceScreen;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceCategory;
import android.text.Spannable;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.widget.EditText;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.List;

public class MiscSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "MiscSettings";
    private static final String PREF_MEDIA_SCANNER_ON_BOOT = "media_scanner_on_boot";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String STATUS_BAR_CARRIER = "status_bar_carrier";
    private static final String PREF_VIBRATE_NOTIF_EXPAND = "vibrate_notif_expand";
    private static final String PREF_DISABLE_FC_NOTIFICATIONS = "disable_fc_notifications";

    private ListPreference mMsob;
    private Preference mCustomLabel;
    private String mCustomLabelText = null;
    CheckBoxPreference mVibrateOnExpand;
	CheckBoxPreference mDisableFC;
    private CheckBoxPreference mStatusBarCarrier;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.misc_settings);
	PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

	mStatusBarCarrier = (CheckBoxPreference) findPreference(STATUS_BAR_CARRIER);
	mStatusBarCarrier.setChecked((Settings.System.getInt(resolver, Settings.System.STATUS_BAR_CARRIER, 0) == 1));
	mStatusBarCarrier.setOnPreferenceChangeListener(this);

	mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        mMsob = (ListPreference) findPreference(PREF_MEDIA_SCANNER_ON_BOOT);
        mMsob.setValue(String.valueOf(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.MEDIA_SCANNER_ON_BOOT, 0)));
        mMsob.setSummary(mMsob.getEntry());
        mMsob.setOnPreferenceChangeListener(this);

	updateCustomLabelTextSummary();

	mVibrateOnExpand = (CheckBoxPreference) findPreference(PREF_VIBRATE_NOTIF_EXPAND);
        mVibrateOnExpand.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.VIBRATE_NOTIF_EXPAND, true));

	mDisableFC = (CheckBoxPreference) findPreference(PREF_DISABLE_FC_NOTIFICATIONS);
        mDisableFC.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.DISABLE_FC_NOTIFICATIONS, true));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
//        String value = (String) newValue;
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mMsob) {
            String value = (String) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MEDIA_SCANNER_ON_BOOT,
                    Integer.valueOf(value));

            mMsob.setValue(String.valueOf(value));
            mMsob.setSummary(mMsob.getEntry());
            return true;
        } else if (preference == mStatusBarCarrier) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_CARRIER, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	if (preference == mVibrateOnExpand) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.VIBRATE_NOTIF_EXPAND,
                    ((CheckBoxPreference) preference).isChecked());
         //   Helpers.restartSystemUI();
            return true;
	} else if (preference == mDisableFC) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.DISABLE_FC_NOTIFICATIONS,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);
            alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString();
                    Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                    Intent i = new Intent();
                    i.setAction("com.android.settings.LABEL_CHANGED");
                    mContext.sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            alert.show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null || mCustomLabelText.length() == 0) {
            mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }
     }
}
