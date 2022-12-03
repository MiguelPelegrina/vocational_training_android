package com.example.proyecto.model;

import java.util.Objects;

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

    /**
     * Método que establece a un usuario cómo único a partir de su nombre
     * @param object Objeto con el cual se compara el objeto que llama a este método
     * @return Devuelve verdadero si se trata de dos objetos idénticos y falso si son dos objetos
     * diferentes
     */
    @Override
    public boolean equals(Object object){
        boolean esIgual = false;
        if(object instanceof User){
            User user = (User) object;
            if(this.name.equals(user.name)){
                esIgual = true;
            }
        }

        return esIgual;
    }

    /**
     * Método hashCode de la clase para identificar a través de un número a un objeto de esta clase
     * @return Devuelve un número identificador
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, password);
    }
}
