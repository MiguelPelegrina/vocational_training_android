package com.example.proyecto.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.example.proyecto.R;
import com.example.proyecto.Utilities.Preferences;
import com.example.proyecto.adapter.RecyclerAdapter;
import com.example.proyecto.io.APIConnection;
import com.example.proyecto.model.Personaje;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class ListActivity extends AppCompatActivity {
    // Declaracion de variables
    public static final int RESULTCODE_ADD_ACT = 1;
    public static final int RESULTCODE_MOD_ACT = 2;
    private ConstraintLayout constraintLayout;
    private ArrayList<Personaje> listaPersonajes = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private ActionMode actionMode;
    private FloatingActionButton floatingActionButton;
    private String accion;
    private String endpoint;
    private Personaje personaje;
    private RecyclerView.ViewHolder viewHolder;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraint_layout_list);

        // Activamos el icono de "Volver"(flecha atrás)
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent anadir = new Intent(ListActivity.this, DetailActivity.class);
                anadir.putExtra("info", "add");
                anadir.putExtra("uri", "android.resource://" + getPackageName() + "/" + R.drawable.image_not_found);
                startActivityForResult(anadir, RESULTCODE_ADD_ACT);
            }
        });

        Preferences.loadPreferences(this, constraintLayout);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(listaPersonajes);

        recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                position = viewHolder.getAdapterPosition();
                personaje = listaPersonajes.get(position);
                Intent i = new Intent(ListActivity.this, DetailActivity.class);
                i.putExtra("info", "mod");
                i.putExtra("name", personaje.getNombre());
                i.putExtra("actor", personaje.getActor());
                i.putExtra("birthday", personaje.getFechaNacimiento());
                i.putExtra("uri", personaje.getImagenUri().toString());
                i.putExtra("status", personaje.getEstado());
                i.putExtra("posicion", position);
                startActivityForResult(i, RESULTCODE_MOD_ACT);
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);
        if(Preferences.notificationPreference(this)) {
            Toasty.info(this, "Para ver detalles pulse sobre un personaje, podrá " +
                    "modificarlo posteriormente", Toasty.LENGTH_LONG, true).show();
            Toasty.info(this, "Para borrar un personaje mantenga el dedo " +
                    "pulsado y elija la opción de borrar", Toasty.LENGTH_LONG, true).show();
        }

        // Obtenemos el Intent de la activity que inicio esta activity
        Intent intent = getIntent();

        accion = intent.getStringExtra("selection");

        switch(accion){
            case "bb":
                endpoint = "characters";
                break;
            case "hp":
                endpoint = "";
                break;
        }

        new Connection().execute("GET", endpoint);
    }

    /**
     * Método que recibe información de una actividad lanzada anteriormente
     * @param requestCode Codigo que identifica qué actividad envía el mensaje.
     * @param resultCode Código que identifica si el mensaje que ha recibido es correcto o no.
     * @param data Contiene el mensaje.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String name;
        String actor;
        Uri uri;
        String fecha;
        String estado;
        if(data != null){
            switch(requestCode){
                case RESULTCODE_ADD_ACT:
                    if(resultCode == RESULT_OK){
                        name = data.getStringExtra("name");
                        actor = data.getStringExtra("actor");
                        if(!(uri = Uri.parse(data.getStringExtra("uri"))).toString().equals("")){

                        }else{
                            uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.image_not_found);
                        }
                        fecha = data.getStringExtra("birthday");
                        estado = data.getStringExtra("status");
                        listaPersonajes.add(0, new Personaje(name, actor, uri, fecha, estado));
                        recyclerAdapter.notifyDataSetChanged();
                    }
                    break;
                case RESULTCODE_MOD_ACT:
                    if(resultCode == RESULT_OK){
                        name = data.getStringExtra("name");
                        actor = data.getStringExtra("actor");
                        uri = Uri.parse(data.getStringExtra("uri"));
                        fecha = data.getStringExtra("birthday");
                        estado = data.getStringExtra("status");
                        personaje.setNombre(name);
                        personaje.setActor(actor);
                        personaje.setImagen(uri);
                        personaje.setFechaNacimiento(fecha);
                        personaje.setEstado(estado);
                        recyclerAdapter.notifyDataSetChanged();;
                    }
                    break;

            }
        }
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
        getMenuInflater().inflate(R.menu.simple, menu);
        /*
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchBar)
                .getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    StringBuilder sb = new StringBuilder(s.trim());
                    if(s.contains(" ")){
                        int auxPos = s.indexOf(" ");
                        sb.replace(auxPos,auxPos+1,"+");
                    }
                    String aux = sb.toString();
                    new Connection().execute("GET", "characters?=name="+aux);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });
        }*/

        return super.onCreateOptionsMenu(menu);
    }

    // Sobrescribimos el metodo onOptionsItemSelected para manejar las diferentes opciones del menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_preferencias:
                Intent ver = new Intent(ListActivity.this, SettingActivity.class);
                startActivity(ver);
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
            mode.getMenuInflater().inflate(R.menu.action, menu);
            mode.setTitle("Gestión de elementos");
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
                    Intent i = new Intent(ListActivity.this, SettingActivity.class);
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

    private class Connection extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            String result = null;

            switch(accion){
                case "bb":
                    result = APIConnection.getRequest(strings[1],"bb");
                    break;
                case "hp":
                    result = APIConnection.getRequest(strings[1], "hp");
                    break;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            if(result != null){
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    String name = "";
                    String actor = "";
                    Uri image = null;
                    String fecha = "";
                    String estado = "";

                    for (int i = 0; i < jsonArray.length(); i++){
                        name = jsonArray.getJSONObject(i).getString("name");
                        switch(accion){
                            case "bb":
                                actor = jsonArray.getJSONObject(i).getString("portrayed");
                                image = Uri.parse(jsonArray.getJSONObject(i).getString("img"));
                                fecha = jsonArray.getJSONObject(i).getString("birthday");
                                estado = jsonArray.getJSONObject(i).getString("status");
                                break;
                            case "hp":
                                actor = jsonArray.getJSONObject(i).getString("actor");
                                image = Uri.parse(jsonArray.getJSONObject(i).getString("image"));
                                fecha = jsonArray.getJSONObject(i).getString("dateOfBirth");
                                estado = String.valueOf(jsonArray.getJSONObject(i).getBoolean("alive"));
                                break;
                        }
                        listaPersonajes.add(new Personaje(name, actor, image, fecha, estado));
                    }
                    recyclerAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                // Si no se ha podido conectar con el servidor se informa al usuario
                Toasty.error(ListActivity.this, "Error por parte del servidor. " +
                        "Vuelva a intentarlo más tarde").show();
            }
        }
    }

    public AlertDialog createAlertDialog(String titulo, String mensaje, MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);

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
            Toasty.success(ListActivity.this, "Se ha borrado el personaje", Toasty.LENGTH_LONG, true).show();
        }else{
            Toasty.info(ListActivity.this, "Operación cancelada", Toasty.LENGTH_LONG, true).show();
        }
    }
}