package com.example.proyecto.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.proyecto.R;
import com.example.proyecto.Utilities.Preferences;
import com.example.proyecto.adapter.RecyclerAdapter;
import com.example.proyecto.io.HttpConnectPersonaje;
import com.example.proyecto.model.Personaje;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity {
    // Declaracion de variables
    private ConstraintLayout constraintLayout;
    private ArrayList<Personaje> listaPersonajes = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private ActionMode actionMode;
    private AlertDialog alertDialog;
    // Variables auxiliares
    private Personaje personaje;
    private RecyclerView.ViewHolder viewHolder;
    private int position;
    private boolean borrar;

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

        new taskConnection().execute("GET", "characters");

        Preferences.loadPreferences(this, constraintLayout);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(listaPersonajes);

        recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                position = viewHolder.getAdapterPosition();
                personaje = listaPersonajes.get(position);
                Toast.makeText(view.getContext(), personaje.getNombre(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(SecondActivity.this, DetailActivity.class);
                i.putExtra("name", personaje.getNombre());
                startActivity(i);
            }
        });

        recyclerAdapter.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                boolean res = false;
                if(actionMode == null){
                    viewHolder = (RecyclerView.ViewHolder) view.getTag();
                    position = viewHolder.getAdapterPosition();
                    personaje = listaPersonajes.get(position);
                    actionMode = startSupportActionMode(actionCallback);
                    res = true;
                }

                return res;
            }
        });

        /*recyclerAdapter.setOnTouchListener(new AdapterView.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
                int position = viewHolder.getAdapterPosition();
                Personaje personaje = listaPersonajes.get(position);
                Toast.makeText(view.getContext(), personaje.getNombre(), Toast.LENGTH_SHORT).show();
                Log.d("Personaje",personaje.getNombre());
                return false;
            }
        });*/

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);
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

    private ActionMode.Callback actionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_menu, menu);
            mode.setTitle("Action Menu");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()){
                case R.id.action_menu_item_borrar:
                    createAlertDialog("Borrar", "¿De verdad quiere borrar el personaje?", item).show();
                    mode.finish();
                    break;
                case R.id.action_menu_item_preferencias:
                    Intent i = new Intent(SecondActivity.this, SettingActivity.class);
                    startActivity(i);
                    mode.finish();
                    break;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    };

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
                try {
                    JSONArray jsonArray = new JSONArray(result);

                    String name = "";
                    String actor = "";
                    Uri img = null;
                    for (int i = 0; i < jsonArray.length(); i++){
                        name = jsonArray.getJSONObject(i).getString("name");
                        actor = jsonArray.getJSONObject(i).getString("portrayed");
                        img = Uri.parse(jsonArray.getJSONObject(i).getString("img"));
                        listaPersonajes.add(new Personaje(name, actor, img));
                    }
                    recyclerAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public AlertDialog createAlertDialog(String titulo, String mensaje, MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);

        builder.setMessage(mensaje).setTitle(titulo);

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                borrarPersonaje(false, item);
            }
        });

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                borrarPersonaje(true, item);
            }
        });

        return builder.create();
    }

    private void borrarPersonaje(boolean borrar, MenuItem item){
        if(borrar){
            listaPersonajes.remove(personaje);
            recyclerAdapter.notifyDataSetChanged();
            Toast.makeText(SecondActivity.this, "Se ha borrado el personaje", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(SecondActivity.this, "Operación cancelada", Toast.LENGTH_SHORT).show();
        }
    }
}