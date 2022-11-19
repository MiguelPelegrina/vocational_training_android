package com.example.proyecto.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.Menu;

import com.example.proyecto.R;

public class SecondActivity extends AppCompatActivity {
    // Declaracion de variables
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        constraintLayout = (ConstraintLayout) findViewById(R.id.root_constraint_layout);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Todo 1.1 Se usa un inflater para construir la vista y se pasa el menu por defecto para
        // que Android se encargue de colocarlo en la vista
        getMenuInflater().inflate(R.menu.simple_menu, menu);

        return true;
    }

}