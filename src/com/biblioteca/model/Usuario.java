package com.biblioteca.model;

//Modelo de Usuario del sistema (Bibliotecario o Estudiante).
public class Usuario {
    private int id;
    private String nombre;
    private String rut;
    private String correo;
    private String contraseña;
    private String rol; // "bibliotecario" o "estudiante"

    public Usuario() {}

    public Usuario(int id, String nombre, String rut, String correo, String contraseña, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
        this.contraseña = contraseña;
        this.rol = rol;
    }

    public Usuario(String nombre, String rut, String correo, String contraseña, String rol) {
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
        this.contraseña = contraseña;
        this.rol = rol;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean esBibliotecario() {
        return "bibliotecario".equalsIgnoreCase(this.rol);
    }

    public boolean esEstudiante() {
        return "estudiante".equalsIgnoreCase(this.rol);
    }

    @Override
    public String toString() {
        return nombre + " (" + rol + ")";
    }
}
