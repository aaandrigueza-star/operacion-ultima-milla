package com.suministrosnorte.model;

public class Producto {

    private Long id;
    private String nombre;
    private Integer stock;

    public Producto(Long id, String nombre, Integer stock) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}