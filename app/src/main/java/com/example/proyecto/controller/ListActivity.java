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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.proyecto.R;
import com.example.proyecto.uilities.Preferences;
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
    private String eleccion;
    private String endpoint;
    private Personaje personaje;
    private RecyclerView.ViewHolder viewHolder;
    private int position;
    private Connection connection;
    private Thread hilo;
    private ProgressDialog progressDialog;

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

        // Asignamos al botón flotante un oyente.
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Al pulsar el botón nos vamos a la actividad detalle que permite al usuario
                // añadir un nuevo elemento al recyclerView
                Intent anadir = new Intent(ListActivity.this, DetailActivity.class);
                // Pasamos a la actividad la información de que se trata de añadir un nuevo elemento
                anadir.putExtra("info", "add");
                // Mandamos la imagen por defecto
                anadir.putExtra("uri", "android.resource://" + getPackageName() + "/" + R.drawable.image_not_found);
                startActivityForResult(anadir, RESULTCODE_ADD_ACT);
            }
        });

        // Cargamos la preferencias
        Preferences.loadPreferences(this, constraintLayout);
        // Obtenemos el recyclerView y le asignamos la lista de personajes
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(listaPersonajes);
        // Asignamos un onClickListener al recyclerAdapter para que nos lleve a la vista detalle del
        // elemento elegido
        recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtenemos el viewHolder, su posicion y el personaje elegido
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                position = viewHolder.getAdapterPosition();
                personaje = listaPersonajes.get(position);
                // Configuramos el intent que nos lleva a la actividad que se encarga de mostrar
                // los detalles de la vista a través de la información necesaria del objeto
                Intent modificar = new Intent(ListActivity.this, DetailActivity.class);
                modificar.putExtra("info", "mod");
                modificar.putExtra("name", personaje.getNombre());
                modificar.putExtra("actor", personaje.getActor());
                modificar.putExtra("birthday", personaje.getFechaNacimiento());
                modificar.putExtra("uri", personaje.getImagenUri().toString());
                modificar.putExtra("status", personaje.getEstado());
                //modificar.putExtra("posicion", position);
                startActivityForResult(modificar, RESULTCODE_MOD_ACT);
            }
        });

        // Asignamos el onLongClickListener encargado de llamar el menú de acción
        recyclerAdapter.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                boolean res = false;
                // Cuando no está creado aún el menú de acción
                if(actionMode == null){
                    // Obtenemos el viewHolder, su posicion y el personaje elegido
                    viewHolder = (RecyclerView.ViewHolder) view.getTag();
                    position = viewHolder.getAdapterPosition();
                    personaje = listaPersonajes.get(position);
                    // Nos creamos el menú de acción
                    actionMode = startSupportActionMode(actionCallback);
                    res = true;
                }

                return res;
            }
        });

        // Nos creamos un LayoutManager, en este caso linear
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Configuramos el recyclerView asignandole el adapter y el layoutManager
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        // En función de las preferencias informamos al usuario de las opciones que tiene
        if(Preferences.notificationPreference(this)) {
            Toasty.info(this, "Para ver detalles pulse sobre un personaje, podrá " +
                    "modificarlo posteriormente", Toasty.LENGTH_LONG, true).show();
            Toasty.info(this, "Para borrar un personaje mantenga el dedo " +
                    "pulsado y elija la opción de borrar", Toasty.LENGTH_LONG, true).show();
        }

        // Obtenemos el Intent de la activity que inicio esta activity
        Intent intent = getIntent();
        // Averiguamos si el usuario quiere ver la información de Breakind Bad y de Harry Potter
        eleccion = intent.getStringExtra("selection");
        // Configuramos a un ProgressDialog de tal forma que avisa al usuario de que se están
        // cargando los datos
        progressDialog = new ProgressDialog(ListActivity.this);
        progressDialog.setMessage("Cargando los datos de los personajes.");
        progressDialog.setCancelable(true);
        // Asignamos un oyente al dialogo para poder cancelar el AsyncTask
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                connection.cancel(true);
            }
        });

        // En función de la API elegida lanzamos la petición a la API
        switch(eleccion){
            // Si se solicita información a la API de Breaking Bad lanzamos un hilo
            case "bb":
                // Instanciamos el hilo
                hilo = new Thread(new Runnable() {
                    // Sobrecargamos el método run()
                    @Override
                    public void run() {
                        // Realizamos la petición
                        String result = APIConnection.getRequest("characters","bb");
                        // Si obtenemos un resulato válido
                        if(result != null){
                            try {
                                // Declaramos e inicializamos las variables
                                JSONArray jsonArray = new JSONArray(result);
                                // Recorremos el JSONArray que nos guarda los resultados de la petición
                                for (int i = 0; i < jsonArray.length(); i++){
                                    // Asignamos la información obtenida a través de la API
                                    String name = jsonArray.getJSONObject(i).getString("name");
                                    String actor = jsonArray.getJSONObject(i).getString("portrayed");
                                    Uri image = Uri.parse(jsonArray.getJSONObject(i).getString("img"));
                                    String fecha = jsonArray.getJSONObject(i).getString("birthday");
                                    String estado = jsonArray.getJSONObject(i).getString("status");
                                    // Añadimos un personaje nuevo a la lista
                                    listaPersonajes.add(new Personaje(name, actor, image, fecha, estado));
                                }
                                // Notificamos al adapter que se han producido cambios
                                recyclerAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            // Si no se ha podido conectar con el servidor se informa al usuario a
                            // través de un elemento de la vista
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.error(ListActivity.this, "Error por " +
                                            "parte del servidor de la API de Breaking Bad. " +
                                            "Vuelva a intentarlo más tarde").show();
                                }
                            });
                        }
                    }
                });
                // Lanzamos el hilo
                hilo.start();
                break;
            // Si se solicita información a la API de Harry Potter ejecutamos un AsyncTask
            case "hp":
                connection = new Connection();
                connection.execute("GET", "");
                break;
        }
    }

    /**
     * Método que se ejecuta cuando finaliza una activida lanzada anteriormente
     * @param requestCode Código que identifica qué actividad envía el mensaje.
     * @param resultCode Código que identifica si el mensaje que ha recibido es correcto o no.
     * @param data Contiene el mensaje.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Declaración de variables
        String name;
        String actor;
        Uri uri;
        String fecha;
        String estado;
        // Si hay datos
        if(data != null){
            // Comprobamos el código utilizado al lanzar la actividad
            switch(requestCode){
                // Si queriamos AÑADIR un elemento
                case RESULTCODE_ADD_ACT:
                    // Si el resultado es correcto
                    if(resultCode == RESULT_OK){
                        // Obtenemos los datos y los asignamos a las variables
                        name = data.getStringExtra("name");
                        actor = data.getStringExtra("actor");
                        uri = Uri.parse(data.getStringExtra("uri"));
                        // Comprobamos si se ha modificada la imagen. Si no es así, asignamos la
                        // imagen por defecto
                        if(uri.toString().equals("")){
                            uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.image_not_found);
                        }
                        fecha = data.getStringExtra("birthday");
                        estado = data.getStringExtra("status");
                        // Con los datos devueltos nos creamos un personaje nuevo que insertamos en
                        // el recyclerView y notificamos al recyclerAdapter
                        listaPersonajes.add(0, new Personaje(name, actor, uri, fecha, estado));
                        recyclerAdapter.notifyDataSetChanged();
                        Toasty.success(ListActivity.this, "Personaje añadido").show();
                    }
                    break;
                    // Si queriamos MODIFICAR un elemento
                case RESULTCODE_MOD_ACT:
                    // Si el resultado es correcto
                    if(resultCode == RESULT_OK){
                        // Obtenemos los datos y los asignamos a las variables
                        name = data.getStringExtra("name");
                        actor = data.getStringExtra("actor");
                        uri = Uri.parse(data.getStringExtra("uri"));
                        fecha = data.getStringExtra("birthday");
                        estado = data.getStringExtra("status");
                        // Modificamos los datos del personaje
                        personaje.setNombre(name);
                        personaje.setActor(actor);
                        personaje.setImagen(uri);
                        personaje.setFechaNacimiento(fecha);
                        personaje.setEstado(estado);
                        // Notificamos al recyclerAdapter
                        recyclerAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cargamos las preferencias
        Preferences.loadPreferences(this, constraintLayout);
    }

    // Sobreescribimos el metodo onCreateOptionsMenu para crearnos un menu personalizada
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Usamos un inflater para construir la vista pasandole el menu por defecto como parámetro
        // para colocarlo en la vista
        getMenuInflater().inflate(R.menu.simple, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // Sobrescribimos el metodo onOptionsItemSelected para manejar las diferentes opciones del menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            // Si queremos modificar las preferencias
            case R.id.item_preferencias:
                Intent preferencias = new Intent(ListActivity.this, SettingActivity.class);
                startActivity(preferencias);
                break;
                // Si queremos volver a la actividad anterior
            case android.R.id.home:
                // Si el hilo que carga la petición está instanciado
                if(hilo != null){
                    // Lo interrupimos
                    hilo.interrupt();
                }
                onBackPressed();
                break;
        }

        return true;
    }

    // Configuramos el menú de acción
    private ActionMode.Callback actionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflamos el menú
            mode.getMenuInflater().inflate(R.menu.action, menu);
            mode.setTitle("Gestión de personajes");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // En función del botón del menú elegido
            switch(item.getItemId()){
                case R.id.action_menu_item_borrar:
                    // Pedimos la confirmación del usuario al querer borrar un personaje
                    createAlertDialog("¿De verdad quiere borrar el " +
                            "personaje " + personaje.getNombre() +"?").show();
                    mode.finish();
                    break;
                case R.id.action_menu_item_preferencias:
                    // Lanzamos un intent a la actividad de las preferencias
                    Intent preferencias = new Intent(ListActivity.this, SettingActivity.class);
                    startActivity(preferencias);
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

    /**
     * Clase que hereda de AsyncTask y que se encarga de ejecutar la petición de información a la
     * API a través de un hilo secundario
     */
    private class Connection extends AsyncTask<String, Void, String>{
        /**
         * Método a ejecutar antes de lanzar la petición
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Mostramos el progressDialog de tal forma que el usuario que se están cargando los
            // datos de los personajes
            progressDialog.show();
        }

        /**
         * Método ejecutado en el hilo secundariio
         * @param strings
         * @return
         */
        @Override
        protected String doInBackground(String... strings) {
            String result = null;

            // Lanzamos la petición a la API de Harry Potter
            result = APIConnection.getRequest(strings[1], "hp");

            return result;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Avisamos al usuario que se ha cancelado el hilo
            Toasty.error(ListActivity.this, "Petición cancelada", Toasty.LENGTH_LONG, true).show();
        }

        /**
         * Método encargado de gestionar los datos devueltos posteriormente a la petición
         * @param result
         */
        @Override
        protected void onPostExecute(String result){
            // Si la API devuelve un respuesta válida
            if(result != null){
                try {
                    // Declaramos e inicializamos las variables
                    JSONArray jsonArray = new JSONArray(result);
                    // Recorremos el JSONArray que nos guarda los resultados de la petición
                    for (int i = 0; i < jsonArray.length(); i++){
                        // Obtenemos el nombre, que es común en ambas APIs
                        String name = jsonArray.getJSONObject(i).getString("name");
                        // En función de la API elegida
                        // Asignamos la información de la API
                        String actor = jsonArray.getJSONObject(i).getString("actor");
                        Uri image = Uri.parse(jsonArray.getJSONObject(i).getString("image"));
                        String fecha = jsonArray.getJSONObject(i).getString("dateOfBirth");
                        String estado = String.valueOf(jsonArray.getJSONObject(i).getBoolean("alive"));
                        // Añadimos un personaje nuevo a la lista
                        listaPersonajes.add(new Personaje(name, actor, image, fecha, estado));
                    }
                    // Cerramos el progressDialog que indicaba el progreso
                    progressDialog.dismiss();
                    // Notificamos al adapter que se han producido cambios
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

    /**
     * Método encargado de crear un AlertDialog para comprobar la eliminación de un personaje
     * @param mensaje Mensaje del AlertDialog
     * @return Devuelve un objeto de la clase AlertDialog
     */
    private AlertDialog createAlertDialog(String mensaje){
        // Obtenemos una instancia de un AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
        // Configuramos el mensaje y el título
        builder.setMessage(mensaje).setTitle("Borrar");
        // Configuramos el botón de No
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Se cancela la operación de eliminar el personaje
                borrarPersonaje(false);
            }
        });
        // Configuramos el botón de Si
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Se finaliza la operación de eliminar el personaje
                borrarPersonaje(true);
            }
        });
        // Devolvemos el objeto creado por el builder
        return builder.create();
    }

    /**
     * Método encargado de borrar el personaje elegido del recyclerView
     * @param borrar Boleano que indica si se quiere borrar el personaje o no
     */
    private void borrarPersonaje(boolean borrar){
        if(borrar){
            listaPersonajes.remove(personaje);
            recyclerAdapter.notifyDataSetChanged();
            Toasty.success(ListActivity.this, "Se ha borrado el personaje", Toasty.LENGTH_LONG, true).show();
        }else{
            Toasty.info(ListActivity.this, "Operación cancelada", Toasty.LENGTH_LONG, true).show();
        }
    }
}