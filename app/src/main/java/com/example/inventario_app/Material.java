package com.example.inventario_app;

import org.json.JSONException;
import org.json.JSONObject;

public class Material {
    private int id;
    private String nombre;
    private double precio;
    private int cantidad;
    private int stockMinimo;
    private String rutaImagen;

    public Material(int id, String nombre, double precio, int cantidad, int stockMinimo, String rutaImagen) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
        this.rutaImagen = rutaImagen;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getRutaImagen() { return rutaImagen; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("nombre", nombre);
        obj.put("precio", precio);
        obj.put("cantidad", cantidad);
        obj.put("stockMinimo", stockMinimo);
        obj.put("rutaImagen", rutaImagen != null ? rutaImagen : "");
        return obj;
    }

    public static Material fromJson(JSONObject obj) throws JSONException {
        int id = obj.optInt("id", 0);
        String nombre = obj.optString("nombre", "");
        double precio = obj.optDouble("precio", 0.0);
        int cantidad = obj.optInt("cantidad", 0);
        int stockMinimo = obj.optInt("stockMinimo", 5);
        String rutaImagen = obj.optString("rutaImagen", "");
        return new Material(id, nombre, precio, cantidad, stockMinimo, rutaImagen);
    }
}