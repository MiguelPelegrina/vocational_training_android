package com.example.proyecto.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.proyecto.R;
import com.example.proyecto.Util.Utilities;
import com.example.proyecto.io.HttpConnectPersonaje;
import com.example.proyecto.model.Personaje;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity {
    // Declaracion de variables
    private ConstraintLayout constraintLayout;
    private ArrayList<Personaje> listaPersonajes = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        constraintLayout = (ConstraintLayout) findViewById(R.id.root_constraint_layout);

        // Activamos el icono de "Volver"(flecha atrás)
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }



        Utilities.loadPreferences(this, constraintLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utilities.loadPreferences(this, constraintLayout);
    }

    // Sobreescribimos el metodo onCreateOptionsMenu para crearnos un menu personalizada
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Usamos un inflater para construir la vista pasandole el menu por defecto como parámetro
        // para colocarlo en la vista
        getMenuInflater().inflate(R.menu.simple_menu, menu);

        return true;
    }

    // Sobrescribimos el metodo onOptionsItemSelected para manejar las diferentes opciones del menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_preferencias:
                Intent i = new Intent(SecondActivity.this, SettingActivity.class);
                startActivity(i);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        //return super.onOptionsItemSelected(item);
        return true;
    }

    private class taskConnection extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            String result = null;

            result = HttpConnectPersonaje.getRequest(strings[1]);

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            if(result != null){
                Log.d("D","DATOS: "+ result);
                try {
                    JSONObject jsonObject = new JSONObject(result);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}