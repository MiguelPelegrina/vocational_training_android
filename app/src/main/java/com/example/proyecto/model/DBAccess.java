package com.example.proyecto.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAccess extends SQLiteOpenHelper {
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
     * Constructor por parametros de la clase DBAccess. Si no existe se crea, sino se conecta.
     * @param context Contexto de la aplicacion
     */
    public DBAccess(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Nos creamos la instruccion para la creacion de la base de datos
        String CREATE_USER_TABLE = "CREATE TABLE " + DB_TABLE_NAME + "("
                + NAME_COLUMN + " TEXT, " + PASSWORD_COLUMN + " TEXT)";
        // Ejecutamos la instruccion
        sqLiteDatabase.execSQL(CREATE_USER_TABLE);
        Log("Tablas creadas");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log("onUpgrade");
        Log("oldversion -> "+ oldVersion);

        switch (oldVersion){
            case 1:
                sqLiteDatabase.execSQL("ALTER TABLE " + DB_TABLE_NAME  +" ADD COLUMN " + PASSWORD_COLUMN +" TEXT");
                Log.i("DB", "BBDD Actualizada a la versión 2");
        }

    }

    /**
     * Metodo que nos permite introducir nuevos registros en nuestra base de datos
     * @param name Nombre del usuario
     * @param password Contrasena del usuario
     * @return Numero de filas afectadas
     */
    public long insert(String name, String password){
        // Obtenemos permiso de escritura sobre la base de datos
        SQLiteDatabase database = this.getWritableDatabase();
        long result = -1;

        // Nos creamos un contenedor con las claves y sus respectivos valores correspondiente al
        // registro que deseamos insertar
        ContentValues values = new ContentValues();
        values.put(NAME_COLUMN, name);
        values.put(PASSWORD_COLUMN, password);
        // Introducimos los valores de contenedor y obtenemos el ID del registro introducido
        result = database.insert(DB_TABLE_NAME,null, values);

        // Cerramos la conexion con la base de datos
        database.close();

        return result;
    }

    /**
     * Metodo que nos permite sacar por el logcat la version actual de la base de datos
     */
    public void getVersionDB(){
        Log(Integer.toString(this.getReadableDatabase().getVersion()));
    }

    /**
     * Metodo que nos sirve para comprobar el funcionamiento correcto del programa
     * @param msg
     */
    public void Log(String msg){
        Log.d("DB", msg);
    }
}
