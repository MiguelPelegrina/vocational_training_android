package com.example.proyecto.model;

/**
 * Clase modelo que guarda la información del usuario
 */
public class User {
    //Atributos de la clase
    private String name;
    private String password;

    /**
     * Constructor por parámetros
     * @param name Nombre del usuario
     * @param password Contraseña del usuario
     */
    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    //getter/accessor
    /**
     * Método que devuelve el nombre del usuario
     * @return Nombre del usuario
     */
    public String getName() {
        return name;
    }

    /**
     * Método que devuelve la contraseña del usuario
     * @return Contraseña del usuario
     */
    public String getPassword() {
        return password;
    }

    //setter/mutator
    // TODO Estos métodos pueden ser usados para modificar los datos del usuario si se decide
    // todo -> implementar
    /**
     * Método que modifica el nombre del usuario
     * @param name Nombre del usuario
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Método que modifica la contraseña del usuario
     * @param password Contraseña del usuario
     */
    public void setPassword(String password) {
        this.password = password;
    }


}
