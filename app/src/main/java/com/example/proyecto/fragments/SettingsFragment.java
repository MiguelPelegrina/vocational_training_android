package com.example.proyecto.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.proyecto.R;

/**
 * Clase que hereda de PreferenceFragmentCompat y que se encarga de 'incrustar' en la ventana el
 * diseño de las preferencias a partir del root_preferences.xml.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
