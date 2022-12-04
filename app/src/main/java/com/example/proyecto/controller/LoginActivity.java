package com.example.proyecto.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.proyecto.R;
import com.example.proyecto.Utilities.Preferences;
import com.example.proyecto.io.UserDatabaseAccess;
import com.example.proyecto.model.User;

import es.dmoral.toasty.Toasty;

/**
 * Actividad que gestiona el login y el registro del usuario
 */
public class LoginActivity extends AppCompatActivity {
    //Declaracion de variables
    private Button btnLogin;
    private Button btnRegistro;
    private EditText txtUsuario;
    private EditText txtContrasena;
    private UserDatabaseAccess controladorDB;
    // Variables encargadas de guardar los datos de login del usuario que ha realizado un login
    // de forma exitosa
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences notificationPreferences;
    private SharedPreferences.Editor loginPreferencesEditor;
    private Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializacion de variables
        // Asociamos los elemento del layout con el código
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegistro = (Button) findViewById(R.id.btnRegistro);
        txtUsuario = (EditText) findViewById(R.id.txtUsuario);
        txtContrasena = (EditText) findViewById(R.id.txtContrasena);
        saveLoginCheckBox = (CheckBox) findViewById(R.id.cbPreferencias);
        // Instanciamos el controlador de la base de datos
        controladorDB = new UserDatabaseAccess(this);

        // Obtenemos las preferencias encargadas de guardar los datos del login
        loginPreferences = getSharedPreferences("loginPreferences", MODE_PRIVATE);
        // Inicializamos el editor
        loginPreferencesEditor = loginPreferences.edit();
        // Si anteriormente se han guardado los datos
        saveLogin = loginPreferences.getBoolean("saveLogin",false);
        if(saveLogin){
            // Rellenamos los campos de texto y checkeamos el checkBox
            txtUsuario.setText(loginPreferences.getString("username", ""));
            txtContrasena.setText(loginPreferences.getString("password",""));
            saveLoginCheckBox.setChecked(true);
        }

        // La biblioteca Toasty (diferente a la vista en clase) permite modificar los atributos por
        // defecto. En este caso aumentamos su tamaño
        Toasty.Config.getInstance()
                .setTextSize(20)
                .apply();
        // Informamos al usuario
        if(Preferences.notificationPreference(this)){
            Toasty.info(this,"Para poder hacer login debe registrarse primero",
                    Toasty.LENGTH_LONG, true).show();
        }

        // Se ha decidido incorporar el login y el registro en la misma actividad
        // Oyente que gestiona el evento OnClick sobre el botón de login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ListActivity.class);
                // Se realizarán todas las comprobaciones necesarias
                // Comprobamos que los campos de texto no estén vacios
                if(comprobarCampos()) {
                    // 1. Consulta --> comprobamos que el nombre del usuario este en la base datos
                    if (controladorDB.getUser(txtUsuario.getText().toString())) {
                        // 2. Consulta --> Comprobamos que la contraseña introducida corresponda
                        // al nombre del usuario
                        if (controladorDB.getUser(txtUsuario.getText().toString(), txtContrasena.getText().toString())) {
                            // Informamos sobre el éxito del login
                            Toasty.success(LoginActivity.this,"Login realizado",
                                    Toasty.LENGTH_SHORT,true).show();
                                    // Si el checkBox está checkeado, se guardan los datos del
                                    // usuario, sino se vacían las preferencias
                                    if(saveLoginCheckBox.isChecked()){
                                        loginPreferencesEditor.putBoolean("saveLogin", true);
                                        loginPreferencesEditor.putString("username", txtUsuario.getText().toString());
                                        loginPreferencesEditor.putString("password", txtContrasena.getText().toString());
                                        loginPreferencesEditor.commit();
                                    }else{
                                        loginPreferencesEditor.clear();
                                        loginPreferencesEditor.commit();
                                    }
                                    startActivity(intent);
                        // Informamos al usuario de todos los posibles errores: campos vacios,
                            // no estar registrado, contraseña no corresponde al nombre
                        } else {
                            Toasty.error(LoginActivity.this,"No se ha podido logear, " +
                                            "compruebe el nombre y/o la contraseña",
                                    Toasty.LENGTH_LONG,true).show();
                        }
                    } else {
                        Toasty.error(LoginActivity.this,
                                "No se ha podido logear, debe registrarse primero",
                                Toasty.LENGTH_LONG,true).show();
                    }
                }else{
                    Toasty.error(LoginActivity.this,
                            "Debe introducidr datos válidos", Toasty.LENGTH_LONG,
                            true).show();
                }
            }
        });

        // Oyente que gestiona el evento OnClick sobre el botón de registro
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Comprobamos que los campos no estén vacios, si no lo están informamos al usuario
                if(comprobarCampos()){
                    User user = new User(txtUsuario.getText().toString(), txtContrasena.getText().toString());
                    try{
                        // Insertamos al usuario en la base de datos
                        long result = controladorDB.insert(user);
                        // Se le comunica el resultado de la operación
                        // Si ha sido exitoso
                        if (result != -1){
                            Toasty.success(LoginActivity.this,
                                    "Se ha registrado exitosamente, ya puede hacer Login",
                                    Toasty.LENGTH_SHORT,true).show();
                        }
                        // Si ha surgido un error, que en este caso lo gestiona la base de datos
                        // internamente al disponer de una clave primaria y ser más rápida que
                        // nosotros recorriendo un bucle, lo más probable es que se trata de un
                        // usuario que ya esté registrado, ya que es la única restricción establecida
                    }catch(SQLiteConstraintException e){
                        Toasty.error(LoginActivity.this,
                                "No se ha podido registrar, probablemente ya esté " +
                                        "registrado", Toasty.LENGTH_LONG,true).show();
                    }
                }else{
                    Toasty.error(LoginActivity.this,
                            "Debe introducidr datos válidos", Toasty.LENGTH_LONG,
                            true).show();
                }
            }
        });
    }

    /**
     * Método que comprueba que los campos de texto no estén vacios
     * @return Devuelve true si la longitud del texto de los campos de texto es mayor que 0 y sino
     * devuelve false
     */
    public boolean comprobarCampos(){
        boolean camposValidos = false;

        if(txtUsuario.getText().length() > 0 && txtContrasena.getText().length() > 0){
            camposValidos = true;
        }

        return camposValidos;
    }
}