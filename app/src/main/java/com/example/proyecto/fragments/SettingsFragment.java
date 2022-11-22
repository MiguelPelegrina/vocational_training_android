package com.example.proyecto.fragments;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceFragmentCompat;

import com.example.proyecto.R;

/**
 * Clase que hereda de PreferenceFragmentCompat y que se encarga de 'incrustar' en la ventana el
 * diseño de las preferencias a partir del root_preferences.xml.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    private ConstraintLayout constraintLayout;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    /*
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        constraintLayout = (ConstraintLayout) getView().findViewById(R.id.setting_container);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utilities.loadPreferences(constraintLayout.getContext(), constraintLayout);
    }*/
}
