package com.biblioteca.model;

//Objeto de transferencia de datos para la generación de reportes.
public class ReporteItem {
    private String titulo;
    private String autor;
    private String categoria;
    private int cantidadPrestamos;
    private String estudianteNombre;
    private String estudianteRut;
    private String fechaPrestamoStr;
    private String fechaDevolucionStr;
    private String estado;

    public ReporteItem() {}

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getCantidadPrestamos() {
        return cantidadPrestamos;
    }

    public void setCantidadPrestamos(int cantidadPrestamos) {
        this.cantidadPrestamos = cantidadPrestamos;
    }

    public String getEstudianteNombre() {
        return estudianteNombre;
    }

    public void setEstudianteNombre(String estudianteNombre) {
        this.estudianteNombre = estudianteNombre;
    }

    public String getEstudianteRut() {
        return estudianteRut;
    }

    public void setEstudianteRut(String estudianteRut) {
        this.estudianteRut = estudianteRut;
    }

    public String getFechaPrestamoStr() {
        return fechaPrestamoStr;
    }

    public void setFechaPrestamoStr(String fechaPrestamoStr) {
        this.fechaPrestamoStr = fechaPrestamoStr;
    }

    public String getFechaDevolucionStr() {
        return fechaDevolucionStr;
    }

    public void setFechaDevolucionStr(String fechaDevolucionStr) {
        this.fechaDevolucionStr = fechaDevolucionStr;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
