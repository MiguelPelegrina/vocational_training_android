package com.example.proyecto.model;

public class User {
    //Atributos de la clase
    private String name;
    private String password;

    /**
     *
     * @param name
     * @param password
     */
    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    //getter/accessor
    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    //setter/mutator
    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
