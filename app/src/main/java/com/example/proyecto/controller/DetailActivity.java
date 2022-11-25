package com.example.proyecto.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.proyecto.R;
import com.example.proyecto.Utilities.Preferences;
import com.example.proyecto.io.HttpConnectPersonaje;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {
    // Declaracion de variables
    private ConstraintLayout constraintLayout;
    private ImageView imgPersonajeGrande;
    private EditText txtNombrePersonaje;
    private EditText txtActorPersonaje;
    private CircularProgressDrawable progressDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        constraintLayout = (ConstraintLayout) findViewById(R.id.activity_detail_constraint);
        imgPersonajeGrande = (ImageView) findViewById(R.id.imagenGrande);
        txtNombrePersonaje = (EditText) findViewById(R.id.editTextPersonNameDetalle);
        txtActorPersonaje = (EditText) findViewById(R.id.editTextPersonajeActorDetalle);

        // Activamos el icono de "Volver"(flecha atrás)
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Preferences.loadPreferences(this, constraintLayout);

        progressDrawable = new CircularProgressDrawable(this);
        progressDrawable.setStrokeWidth(15f);
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);
        progressDrawable.setCenterRadius(45f);
        progressDrawable.start();

        // Obtenemos el Intent de la activity que inicio esta activity
        Intent i = getIntent();
        // Obtenemos el mensaje contenido dentro del Intent a través de la clave "INFO"
        String nombre = i.getStringExtra("name");

        new taskConnection().execute("GET", "characters?name="+nombre);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Preferences.loadPreferences(this, constraintLayout);
    }

    // Sobreescribimos el metodo onCreateOptionsMenu para crearnos un menu personalizada
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Usamos un inflater para construir la vista pasandole el menu por defecto como parámetro
        // para colocarlo en la vista
        getMenuInflater().inflate(R.menu.simple_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_preferencias:
                Intent i = new Intent(DetailActivity.this, SettingActivity.class);
                startActivity(i);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class taskConnection extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result = null;

            result = HttpConnectPersonaje.getRequest(strings[1]);

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            if(result != null){
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    // ELEGIR INFORMACIÓN QUE SE DESEA MOSTRAR --> todo?
                    String name = jsonObject.getString("name");
                    txtNombrePersonaje.setText(name);
                    String actor = jsonObject.getString("");
                    //actor = jsonObject.getString("portrayed");
                    Glide.with(DetailActivity.this)
                            .load(jsonObject.getString("img"))
                            .placeholder(progressDrawable)
                            .error(R.mipmap.ic_launcher)
                            .into(imgPersonajeGrande);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}