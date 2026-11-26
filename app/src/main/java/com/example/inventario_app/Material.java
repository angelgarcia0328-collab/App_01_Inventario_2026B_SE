package com.example.inventario_app;

public class Material {
    private int id;
    private String nombre;
    private double precio;
    private int cantidad;
    private int stockMinimo;

    public Material(int id, String nombre, double precio, int cantidad, int stockMinimo) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public int getStockMinimo() { return stockMinimo; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
}