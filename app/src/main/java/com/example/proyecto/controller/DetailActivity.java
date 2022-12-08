package com.example.proyecto.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextMenu;
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
    private boolean imagenNueva = false;
    private Uri uri;
    private String name;
    private String actor;
    private String fecha;
    private String estado;
    private Intent intent;
    private FilePickerDialog dialog;
    private String enlaceWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Asociamos los elementos del layout con el código
        constraintLayout = (ConstraintLayout) findViewById(R.id.activity_detail_constraint);
        imgPersonajeGrande = (ImageView) findViewById(R.id.imagenGrande);
        txtNombrePersonaje = (EditText) findViewById(R.id.editTextPersonNameDetalle);
        txtActorPersonaje = (EditText) findViewById(R.id.editTextPersonajeActorDetalle);
        txtFechaNacimiento = (EditText) findViewById(R.id.editTextPersonajeDetalleNacimiento);
        sbEstadoPersonaje = (Spinner) findViewById(R.id.spEstado);
        btnGuardar = (Button) findViewById(R.id.btnGuardar);

        // Registramos la imagen como menú de contexto
        // De tal forma tendremos la opción de modificar o añadir una imagen a partir de un fichero
        // almacenado previamente o un imagen elegida a través de una URI
        registerForContextMenu(imgPersonajeGrande);

        // Rellenamos el spinner que guarda los posible estados de los personajes
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.estados, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sbEstadoPersonaje.setAdapter(adapter);

        // Activamos el icono de "Volver"(flecha atrás)
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Cargamos las preferencias
        Preferences.loadPreferences(this, constraintLayout);

        // Configuramos el CircularProgressDrawable que indicará que se está cargando una imagen
        progressDrawable = new CircularProgressDrawable(this);
        progressDrawable.setStrokeWidth(15f);
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);
        progressDrawable.setCenterRadius(45f);
        progressDrawable.start();

        // Obtenemos el Intent de la activity que inicio esta activity
        intent = getIntent();
        // Obtenemos toda la información pasada a través del intent
        accion = intent.getStringExtra("info");
        name = intent.getStringExtra("name");
        actor = intent.getStringExtra("actor");
        uri = Uri.parse(intent.getStringExtra("uri"));
        fecha = intent.getStringExtra("birthday");
        estado = intent.getStringExtra("status");
        // Esta parte es necesaria para adaptar el contenido del spinner a ambas APIs ya que una lo
        // guarda como variable boleana y la otro como "enumerado" de tres opciones
        if(accion.equals("mod")){
            if(estado.equals("true")){
                estado = "Alive";
            }else{
                if(estado.equals("false")){
                    estado = "Dead";
                }
            }
        }

        // El textWatcher está implementado como habilitador del botón de guardar:
        // La idea es que mientras que el botón solo se habilite cuando
        // 1. Los campos NO estén vacios
        // 2. La información se haya modificado de alguna manera en alguno de los campos, incluyendo
        // la imagen
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
        // Asignamos el oyente configurado a todos los componentes de texto y al spinner
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

        //
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(accion){
                    case "mod":
                        if(comprobarCampoFecha()){
                            createAlertDialog().show();
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

        // Esta parte del código corresponde a la biblioteca FilePicker. Esta nos permite elegir
        // ficheros. En este caso en concreto nos permite añadir o modificar la imagen del personaje
        // Nos creamos un objeto de la clase DialogProperties
        DialogProperties properties = new DialogProperties();
        // Configuramos las variables de dicho objeto
        // El modo de selección será de un único fichero
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        // Solo se podrán elegir ficheros
        properties.selection_type = DialogConfigs.FILE_SELECT;
        // Obtenemos el directorio de la sdExterna que guarda los datos del usuario
        File sdExterna = new File(Environment.getExternalStorageDirectory().getPath());
        // Establecemos como directorios la ruta de la sdExterna
        properties.root = sdExterna;
        properties.error_dir = sdExterna;
        properties.offset = sdExterna;
        // Establecemos las extensiones permitidas
        properties.extensions = new String[]{"jpg","jpeg","png"};
        // Nos creamos un objeto de la ventana de dialogo
        dialog = new FilePickerDialog(DetailActivity.this, properties);
        // Modificamos su título
        dialog.setTitle("Eliga una imagen");
        // Asignamos un oyente al dialogo
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                // Cuando se elige un fichero obtenemos su uri local
                // NO HE PODIDO COMPROBAR AÚN LO SUFICIENTE LA POSIBILIDAD DE EXCEPCIONES
                //try {
                    uri = Uri.fromFile(new File(files[0]));
                    // Asignamos la uri al imageView
                    imgPersonajeGrande.setImageURI(uri);
                    // Controlamos las posibles excepciones
                /*}catch(Exception e){
                    Toasty.error(DetailActivity.this,"Solo eliga ficheros con la " +
                            "extensión jpg o png").show();
                }*/
                // Modificamos nuestra variable booleana que registra cambios en las imagenes
                imagenNueva = true;
                // Comprobamos si se han modificado los datos para habilitar el boton de guardar al
                // cambiar la imagen
                update();
            }
        });
        // Le asignamos al imageView que mostrará el dialog configurado previamente cuando se realice
        // un onLongClick
        /*imgPersonajeGrande.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                dialog.show();

                return false;
            }
        });*/

        // En función de las preferencias se mostrarán notificaciones informativas
        if(Preferences.notificationPreference(this)) {
            Toasty.info(DetailActivity.this, "Para poder guardar los cambios los campos" +
                    " no deben estar vacios", Toasty.LENGTH_LONG, true).show();
            Toasty.info(DetailActivity.this, "Puede modificar la imagen manteniendo " +
                    " el dedo pulsado sobre ella", Toasty.LENGTH_LONG, true).show();
        }

        switch (accion){
            case "mod":
                txtNombrePersonaje.setText(name);
                txtActorPersonaje.setText(actor);
                imgPersonajeGrande.setImageURI(uri);
                Glide.with(DetailActivity.this)
                        .load(uri)
                        .placeholder(progressDrawable)
                        .error(R.drawable.image_not_found)
                        .into(imgPersonajeGrande);
                txtFechaNacimiento.setText(fecha);
                switch (estado){
                    case "Alive":
                    case "true":
                        sbEstadoPersonaje.setSelection(0);
                        break;
                    case "Presumed dead":
                        sbEstadoPersonaje.setSelection(1);
                        break;
                    case "Deceased":
                    case "false":
                        sbEstadoPersonaje.setSelection(2);
                        break;
                }
                break;
            case "add":
                imgPersonajeGrande.setImageResource(R.drawable.image_not_found);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.flotante, menu);
        menu.setHeaderTitle("Elección de imagen");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.elegirFichero:
                dialog.show();
                break;
            case R.id.elegirUri:
                createInputDialog().show();
                break;
        }

        return true;
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
            if(name.equals(txtNombrePersonaje.getText().toString().trim()) &&
                    actor.equals(txtActorPersonaje.getText().toString().trim()) &&
                    fecha.equals(txtFechaNacimiento.getText().toString().trim()) &&
                    estado.equals(sbEstadoPersonaje.getSelectedItem().toString().trim()) &&
                    !imagenNueva){
                diferentes = false;
            }
        }

        return diferentes;
    }

    /**
     * Método encargado de comprobar que la fecha introducida sea correcta
     * @param stringFecha
     * @return Devuelva un String con la fecha comprobado
     * @throws ParseException Excepcion que se lanza cuando el formato de la fecha
     * introducido no corresponde al deseado
     */
    @NonNull
    private String comprobarFecha(String stringFecha) throws ParseException {
        Date fecha = null;
        //TODO si vuelve a funcionar la API de BB
        //SimpleDateFormat formato = new SimpleDateFormat("MM-dd-yyyy");
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");

        fecha = formato.parse(stringFecha);

        stringFecha = formato.format(fecha);

        return stringFecha;
    }

    /**
     * Método encargado de comprobar que el campo de texto de la fecha sea válido y en el caso de
     * desconocer la fecha que se tenga que escribir "unknown". Ya que el formato de la fecha
     * puede llegar a ser poco intuitivo se informa al usuario sobre la correcta edición del campo
     * @return
     */
    private boolean comprobarCampoFecha(){
        boolean valid = false;
        // Aceptamos el String unknown, independientemente de que esté en mayúscula o minúscula
        if(txtFechaNacimiento.getText().toString().equalsIgnoreCase("unknown")){
            valid = true;
        }else{
            try {
                // Comprobamos la fecha indicada
                comprobarFecha(txtFechaNacimiento.getText().toString());
                valid = true;
            } catch (ParseException e) {
                //TODO si vuelve a funcionar la API de BB
                //Toasty.error(DetailActivity.this,"Introducza una fecha válida según el " +
                        //"formato MM-dd-yyyy").show();
                Toasty.error(DetailActivity.this,"Introducza una fecha válida según el " +
                        "formato dd-MM-yyyy o escribe unknown si desconoce la fecha").show();
            }
        }

        return valid;
    }

    /**
     *
     * @return
     */
    @NonNull
    private AlertDialog createAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);

        builder.setMessage("¿De verdad quiere modificar los datos del personaje?").setTitle("Modificar");

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


    private AlertDialog createInputDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Introduzca la ruta de una imagen");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enlaceWeb = input.getText().toString();
                Glide.with(DetailActivity.this)
                        .load(enlaceWeb)
                        .placeholder(progressDrawable)
                        .error(R.drawable.image_not_found)
                        .into(imgPersonajeGrande);
                imagenNueva = true;
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    /**
     * Método encargado de preparar el intent devuelta hacia la ListActivity desde la cual se
     * inicio esta actividad
     */
    private void volver(){
        // Configuramos el intent
        Intent returnIntent = new Intent();
        returnIntent.putExtra("name", txtNombrePersonaje.getText() + "");
        returnIntent.putExtra("actor", txtActorPersonaje.getText() + "");
        returnIntent.putExtra("uri", uri.toString());
        returnIntent.putExtra("birthday", txtFechaNacimiento.getText() + "");
        returnIntent.putExtra("status",sbEstadoPersonaje.getSelectedItem().toString());
        // Damos la actividad como finalizada de forma correcta
        setResult(DetailActivity.RESULT_OK, returnIntent);
        // Terminamos esta actividad
        finish();
    }

    /**
     * Método encargado de comprobar que los campos de vista no estén vacios y que hayan modificado
     * con respecto a la información cargada inicialmente
     */
    private void update(){
        // Una vez que hayamos comprobado que los campos no estén vacios
        if(!comprobarCamposVacios()){
            // Comprobamos que los campos sean diferentes
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