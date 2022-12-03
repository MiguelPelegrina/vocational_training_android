package com.example.proyecto.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Clase encargada de almacenar el método necesario para cargar las preferencias en cada actividad
 */
public class Preferences {
    /**
     * Método que modifica el color de fondo de la actividad actual en función de lo establecido en
     * las preferencias
     * @param context Contexto en el cual se encuentra en ese momento la aplicación
     * @param layout Layout de la actividad
     */
    public static void loadPreferences(Context context, ConstraintLayout layout){
        int numeroColor = Color.WHITE;
        // Obtenemos la instancia del gestor de las preferencias
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // Obtenemos el valor asociado a la clave de la preferencia del color correspondiente al
        // componente del fragment de las preferencias
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
