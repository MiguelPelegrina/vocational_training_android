package com.example.proyecto.model;

import android.net.Uri;

public class Personaje {
    // Atributos de la clase
    private String nombre;
    private String actor;
    private Uri imagen;

    /**
     * Constructor por parámetros
     * @param nombre Nombre del personaje
     * @param actor Nombre del actor o actriz
     * @param imagen Imagen del personaje
     */
    public Personaje(String nombre, String actor, Uri imagen) {
        this.nombre = nombre;
        this.actor = actor;
        this.imagen = imagen;
    }

    //getter
    public String getNombre() {
        return nombre;
    }

    public String getActor() {
        return actor;
    }

    public Uri getImagenUri() {
        return imagen;
    }

    //setter
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setImagen(Uri imagen) {
        this.imagen = imagen;
    }
}
