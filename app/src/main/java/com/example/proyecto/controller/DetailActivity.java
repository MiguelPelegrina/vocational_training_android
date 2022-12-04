package com.example.proyecto.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.example.proyecto.R;
import com.example.proyecto.Utilities.Preferences;
import com.example.proyecto.io.APIConnectionBreakingBad;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.dmoral.toasty.Toasty;

public class DetailActivity extends AppCompatActivity {
    // Declaracion de variables
    private ConstraintLayout constraintLayout;
    private ImageView imgPersonajeGrande;
    private EditText txtNombrePersonaje;
    private EditText txtActorPersonaje;
    private EditText txtFechaNacimiento;
    private Spinner sbEstadoPersonaje;
    private Button btnGuardar;
    private CircularProgressDrawable progressDrawable;
    private String accion;
    private String name = "";
    private String actor = "";
    private Uri uri = Uri.parse("");
    private boolean imagenNueva = false;
    private String fecha = "";
    private String estado = "Alive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Asociamos los elemento del layout con el código
        constraintLayout = (ConstraintLayout) findViewById(R.id.activity_detail_constraint);
        imgPersonajeGrande = (ImageView) findViewById(R.id.imagenGrande);
        txtNombrePersonaje = (EditText) findViewById(R.id.editTextPersonNameDetalle);
        txtActorPersonaje = (EditText) findViewById(R.id.editTextPersonajeActorDetalle);
        txtFechaNacimiento = (EditText) findViewById(R.id.editTextPersonajeDetalleNacimiento);
        sbEstadoPersonaje = (Spinner) findViewById(R.id.spEstado);
        btnGuardar = (Button) findViewById(R.id.btnGuardar);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.estados, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sbEstadoPersonaje.setAdapter(adapter);

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
        Intent intent = getIntent();

        accion = intent.getStringExtra("info");
        // Obtenemos el mensaje contenido dentro del Intent a través de la clave "info"
        String nombre = intent.getStringExtra("name");

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                update();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                update();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                update();
            }
        };
        txtNombrePersonaje.addTextChangedListener(textWatcher);
        txtActorPersonaje.addTextChangedListener(textWatcher);
        txtFechaNacimiento.addTextChangedListener(textWatcher);
        sbEstadoPersonaje.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                update();
            }
        });
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(accion){
                    case "mod":
                        if(comprobarCamposDiferentes()){
                            if(comprobarCampoFecha()){
                                createAlertDialog("Modificar", "¿De verdad quiere modificar los datos del personaje?").show();
                            }
                        }
                        break;
                    case "add":
                        if(comprobarCampoFecha()){
                            volver();
                        }

                        break;
                }
            }
        });

        //
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        File sdExterna = new File(Environment.getExternalStorageDirectory().getPath());
        properties.root = sdExterna;
        properties.error_dir = sdExterna;
        properties.offset = sdExterna;
        properties.extensions = new String[]{"jpg","png"};
        FilePickerDialog dialog = new FilePickerDialog(DetailActivity.this, properties);
        dialog.setTitle("Eliga una imagen");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                uri = Uri.fromFile(new File(files[0]));
                imgPersonajeGrande.setImageURI(uri);
                imagenNueva = true;
                update();
            }
        });
        imgPersonajeGrande.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                dialog.show();

                return false;
            }
        });

        if(Preferences.notificationPreference(this)) {
            Toasty.info(DetailActivity.this, "Para poder guardar los cambios los campos" +
                    " no deben estar vacios").show();
        }

        switch (accion){
            case "mod":
                new taskConnection().execute("GET", "characters?name="+nombre);
                break;
            case "add":
                imgPersonajeGrande.setImageResource(R.drawable.image_not_found);
                break;
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
        protected void onPreExecute() {
            super.onPreExecute();
            // Antes de solicitar la información a la API deshabilitamos todos los campos con los
            // que podría interactuar el usuario
            txtNombrePersonaje.setEnabled(false);
            txtActorPersonaje.setEnabled(false);
            txtFechaNacimiento.setEnabled(false);
            sbEstadoPersonaje.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;

            result = APIConnectionBreakingBad.getRequest(strings[1]);

            return result;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            txtNombrePersonaje.setEnabled(false);
            txtActorPersonaje.setEnabled(false);
            txtFechaNacimiento.setEnabled(false);
            sbEstadoPersonaje.setEnabled(false);
        }

        @Override
        protected void onPostExecute(String result){
            if(result != null){
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    // ELEGIR INFORMACIÓN QUE SE DESEA MOSTRAR --> todo?
                    name = jsonObject.getString("name");
                    txtNombrePersonaje.setText(name);
                    actor = jsonObject.getString("portrayed");
                    txtActorPersonaje.setText(actor);
                    uri = Uri.parse(jsonObject.getString("img"));
                    Glide.with(DetailActivity.this)
                            .load(uri)
                            .placeholder(progressDrawable)
                            .error(R.drawable.image_not_found)
                            .into(imgPersonajeGrande);
                    fecha = jsonObject.getString("birthday");
                    txtFechaNacimiento.setText(fecha);
                    estado = jsonObject.getString("status");
                    switch (estado){
                        case "Alive":
                            sbEstadoPersonaje.setSelection(0);
                            break;
                        case "Presumed dead":
                            sbEstadoPersonaje.setSelection(1);
                            break;
                        case "Deceased":
                            sbEstadoPersonaje.setSelection(2);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            txtNombrePersonaje.setEnabled(true);
            txtActorPersonaje.setEnabled(true);
            txtFechaNacimiento.setEnabled(true);
            sbEstadoPersonaje.setEnabled(true);
        }
    }

    //Métodos auxiliares
    private boolean comprobarCamposVacios() {
        boolean vacios = true;

        if(!txtNombrePersonaje.getText().toString().trim().equals("") &&
                !txtActorPersonaje.getText().toString().trim().equals("") &&
                !txtFechaNacimiento.getText().toString().trim().equals("")){
            vacios = false;
        }
        return vacios;
    }

    private boolean comprobarCamposDiferentes(){
        boolean diferentes = true;
        if(accion.equals("mod")){
            if(name.equals(txtNombrePersonaje.getText().toString()) &&
                    actor.equals(txtActorPersonaje.getText().toString()) &&
                    fecha.equals(txtFechaNacimiento.getText().toString()) &&
                    estado.equals(sbEstadoPersonaje.getSelectedItem().toString()) &&
                    !imagenNueva){
                diferentes = false;
            }
        }

        return diferentes;
    }

    @NonNull
    private String comprobarFecha(String stringFecha) throws ParseException {
        Date fecha = null;
        SimpleDateFormat formato = new SimpleDateFormat("MM-dd-yyyy");

        fecha = formato.parse(stringFecha);

        stringFecha = formato.format(fecha);

        return stringFecha;
    }

    private boolean comprobarCampoFecha(){
        boolean valid = false;

        if(txtFechaNacimiento.getText().toString().trim().equalsIgnoreCase("Unknown")){
            valid = true;
        }else{
            try {
                comprobarFecha(txtFechaNacimiento.getText().toString().trim());
                valid = true;
            } catch (ParseException e) {
                Toasty.error(DetailActivity.this,"Introducza una fecha válida según el " +
                        "formato MM-dd-yyyy").show();
            }
        }

        return valid;
    }



    @NonNull
    private AlertDialog createAlertDialog(String titulo, String mensaje){
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);

        builder.setMessage(mensaje).setTitle(titulo);

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toasty.error(DetailActivity.this, "Modificación cancelada",
                        Toasty.LENGTH_SHORT,true).show();
            }
        });

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toasty.success(DetailActivity.this, "Modificación realizada",
                        Toasty.LENGTH_SHORT, true).show();
                volver();
            }
        });

        return builder.create();
    }

    private void volver(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("name", txtNombrePersonaje.getText() + "");
        returnIntent.putExtra("actor", txtActorPersonaje.getText() + "");
        if(uri != null){
            returnIntent.putExtra("uri", uri.toString());
        }
        setResult(DetailActivity.RESULT_OK, returnIntent);

        finish();
    }

    private void update(){
        if(!comprobarCamposVacios()){
            if(comprobarCamposDiferentes()){
                btnGuardar.setEnabled(true);
            }else{
                btnGuardar.setEnabled(false);
            }
        }else{
            btnGuardar.setEnabled(false);
        }
    }
}