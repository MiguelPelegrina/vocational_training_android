package com.example.proyecto.io;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.proyecto.model.User;

import java.util.ArrayList;

/**
 * Clase
 */
public class UserDatabaseAccess extends SQLiteOpenHelper {
    // Heredamos de la clase SQLiteOpenHelper para disponer de sus métodos que nos ayudan a gestionar
    // la base de datos local
    // Nombre de la base de datos
    private static final String DB_NAME = "db_project";
    // Nombre de la tabla
    private static final String DB_TABLE_NAME = "db_users";
    // Version de la base de datos
    private static final int DB_VERSION = 2;
    //Columnas de las tabla db_users
    private static final String NAME_COLUMN = "userName";
    private static final String PASSWORD_COLUMN = "userPassword";
    //Contexto de la aplicacion
    private Context context;

    /**
     * Constructor por parámetros de la clase UserDatabaseAccess. Si no existe se crea, sino se
     * conecta.
     * @param context Contexto de la aplicación
     */
    public UserDatabaseAccess(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Nos creamos la instruccion para la creacion de la base de datos
        String CREATE_USER_TABLE = "CREATE TABLE " + DB_TABLE_NAME + "("
                + NAME_COLUMN + " TEXT primary key, " + PASSWORD_COLUMN + " TEXT)";
        // Ejecutamos la instruccion
        sqLiteDatabase.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // En función de la versión de la base de datos del usuario realizan modificaciones de la
        // tabla hasta llegar a la versión actual
        switch (oldVersion){
            case 1:
                sqLiteDatabase.execSQL("ALTER TABLE " + DB_TABLE_NAME  +" ADD COLUMN " + PASSWORD_COLUMN +" TEXT");
        }
    }

    /**
     * Metodo que nos permite introducir nuevos registros en nuestra base de datos
     * @param user Usuario que se desea insertar en la tabla
     * @return ID de la fila insertada
     */
    public long insert(User user) throws SQLiteConstraintException{
        // Obtenemos permiso de escritura sobre la base de datos
        SQLiteDatabase database = this.getWritableDatabase();
        long result = -1;

        // Nos creamos un contenedor con las claves y sus respectivos valores correspondiente al
        // registro que deseamos insertar
        ContentValues values = new ContentValues();
        values.put(NAME_COLUMN, user.getName());
        values.put(PASSWORD_COLUMN, user.getPassword());
        // Introducimos los valores de contenedor y obtenemos el ID del registro introducido
        result = database.insert(DB_TABLE_NAME, null, values);

        // Cerramos la conexion con la base de datos
        database.close();

        return result;
    }

    /**
     * Método que comprueba la existencia de un usuario registrado en la base de datos
     * @param parametrosBusqueda String de parámetros utilizados en la búsqueda del usuario. Si se
     *                           introduce solo un valor, se busca por el nombre del usuario. Si se
     *                           introducen dos parámetros, se busca tanto por el nombre como por
     *                           la constraseña.
     * @return Devuelve verdadero si existe el usuario introducido y falso si no existe
     */
    public boolean getUser(String... parametrosBusqueda){
        // Declaración e inicialización de variables
        boolean existe = false;
        String selection = "";
        String[] columns = new String[]{NAME_COLUMN, PASSWORD_COLUMN};
        String[] filter = null;
        SQLiteDatabase database = getReadableDatabase();

        switch (parametrosBusqueda.length){
            case 1:
                selection = NAME_COLUMN+"=?";
                filter = new String[]{parametrosBusqueda[0]};
                break;
            case 2:
                selection = NAME_COLUMN + " = ? AND "+ PASSWORD_COLUMN + " = ?";
                filter = new String[]{parametrosBusqueda[0], parametrosBusqueda[1]};
                break;
        }

        // Realizamos la consulta y nos creamos un cursor que nos permite recorrer los resultados
        // de la consulta
        Cursor cursor = database.query(DB_TABLE_NAME, columns ,selection, filter,null,null,null);

        // Colocamos el cursor
        if(cursor.moveToFirst()){
            // Si existe el usuario asignamos true a la variable de devolución
            if(cursor.getString(0) != null){
                existe = true;
            }
            // Cerramos el cursor
            cursor.close();
        }
        // Cerrramos la conexión con la base de datos
        database.close();

        return existe;
    }

    /**
     * Método que nos devuelve una lista con todos los usuarios registrados en la
     * base de datos
     * @return Lista de los usuarios
     */
    @Deprecated
    public ArrayList<User> getAllUser(){
        // Obtenemos permiso de lectura sobre la base de datos
        SQLiteDatabase database = this.getReadableDatabase();
        // Nos creamos una lista que guardara posteriormente los usuarios de la tabla
        ArrayList<User> resultUsers = new ArrayList<>();
        // Asignamos las columnas cuyos datos queremos obtener
        String[] columnas = new String[]{ NAME_COLUMN, PASSWORD_COLUMN };
        // Nos creamos un cursor que recorrera los registros de consulta realizada
        Cursor cursor = database.query(DB_TABLE_NAME, columnas, null,null,null,null,null);

        // Colocamos el cursor
        if (cursor.moveToFirst()){
            do{
                // Obtenemos todos los datos
                String name = cursor.getString(0);
                String password = cursor.getString(1);
                resultUsers.add(new User(name, password));
            }while (cursor.moveToNext());
        }

        // Cerramos el cursor
        cursor.close();

        // Cerramos la base de datos
        database.close();

        return resultUsers;
    }
}
