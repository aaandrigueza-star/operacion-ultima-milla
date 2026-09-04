package com.suministrosnorte.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suministrosnorte.model.Pedido;
import com.suministrosnorte.model.Estado;
import com.suministrosnorte.service.PedidoService;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido crearPedido(@RequestBody Pedido pedido) {
        return pedidoService.crearPedido(pedido);
    }

    @GetMapping
    public List<Pedido> obtenerPedidos() {
        return pedidoService.obtenerPedidos();
    }

    @PutMapping("/{id}/confirmar")
    public Pedido confirmar(@PathVariable Long id) {
        return pedidoService.confirmar(id);
    }

    @PutMapping("/{id}/cancelar")
    public Pedido cancelar(@PathVariable Long id) {
        return pedidoService.cancelar(id);
    }

    @PutMapping("/{id}/despachar")
    public Pedido despachar(@PathVariable Long id) {
        return pedidoService.despachar(id);
    }

    @GetMapping("/pendientes")
    public List<Pedido> pendientes() {
        return pedidoService.porEstado(Estado.PENDIENTE);
    }

    @GetMapping("/urgentes")
    public List<Pedido> urgentes() {
        return pedidoService.urgentes();
    }

    @GetMapping("/estado")
    public List<Pedido> porEstado(@RequestParam Estado estado) {
        return pedidoService.porEstado(estado);
    }

    @GetMapping("/resumen")
    public java.util.Map<String, Long> resumen() {
        return pedidoService.resumen();
    }

    @GetMapping("/siguiente")
    public Pedido siguiente() {
        return pedidoService.siguiente();
    }

    @GetMapping("/en-riesgo")
    public List<Pedido> enRiesgo() {
        return pedidoService.enRiesgo();
    }
}