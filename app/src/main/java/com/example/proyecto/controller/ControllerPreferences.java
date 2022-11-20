package com.example.proyecto.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ControllerPreferences {
    public static void loadPreferences(Context context, ConstraintLayout layout){
        int numeroColor = Color.WHITE;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String color = sharedPreferences.getString("colorPreference","Blanco");
        switch (color){
            case "Verde":
                numeroColor = Color.GREEN;
                break;
            case "Azul":
                numeroColor = Color.BLUE;
                break;
            case "Rojo":
                numeroColor = Color.RED;
                break;
        }
       layout.setBackgroundColor(numeroColor);
    }
}
