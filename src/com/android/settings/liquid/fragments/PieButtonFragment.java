package com.android.settings.liquid.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PieButtonFragment extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PIE_SECOND_LAYER = "pie_second_layer";

    private CheckBoxPreference mSecondLayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pie_button_fragment);

        PreferenceScreen prefSet = getPreferenceScreen();

        mSecondLayer = (CheckBoxPreference) prefSet.findPreference(PIE_SECOND_LAYER);
        mSecondLayer.setOnPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        final ListView list = (ListView) view.findViewById(android.R.id.list);
        // our container already takes care of the padding
        if (list != null) {
            int paddingTop = list.getPaddingTop();
            int paddingBottom = list.getPaddingBottom();
            list.setPadding(0, paddingTop, 0, paddingBottom);
        }
        return view;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSecondLayer) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SPIE_SECOND_LAYER_ACTIVE, (Boolean) newValue ? 1 : 0);
        }
        return true;
    }
}
