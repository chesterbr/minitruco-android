package me.chester.minitruco.android;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import me.chester.minitruco.R;

public class OpcoesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.opcoes, rootKey);
    }

}
