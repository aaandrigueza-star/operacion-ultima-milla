package com.suministrosnorte.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suministrosnorte.model.Producto;
import com.suministrosnorte.service.PedidoService;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final PedidoService pedidoService;

    public ProductoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Producto> obtenerProductos() {
        return pedidoService.obtenerProductos();
    }
}