package com.example.proyecto.model;

import android.app.Person;
import android.net.Uri;

import java.io.Serializable;

/**
 * Clase modelo de los elementos que se guardarán en el RecyclerView
 */
public class Personaje implements Serializable{
    // Atributos de la clase
    private String nombre;
    private String actor;
    private Uri imagen;
    private String fechaNacimiento;
    private String estado;

    /**
     * Constructor por parámetros con todos los atributos
     * @param nombre Nombre del personaje
     * @param actor Nombre del actor o actriz
     * @param imagen Imagen del personaje
     */
    public Personaje(String nombre, String actor, Uri imagen, String fechaNacimiento, String estado) {
        this.nombre = nombre;
        this.actor = actor;
        this.imagen = imagen;
        this.fechaNacimiento = fechaNacimiento;
        this.estado = estado;
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

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getEstado() {
        return estado;
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

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
