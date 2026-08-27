package com.suministrosnorte.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.suministrosnorte.model.Estado;
import com.suministrosnorte.model.Pedido;
import com.suministrosnorte.model.Prioridad;
import com.suministrosnorte.model.Producto;

@Service
public class PedidoService {

    private final List<Pedido> pedidos = new ArrayList<>();
    private final Map<Long, Producto> productos = new HashMap<>();

    public PedidoService() {
        productos.put(1L, new Producto(1L, "Nike Air Max", 20));
        productos.put(2L, new Producto(2L, "Adidas Ultraboost", 5));
        productos.put(3L, new Producto(3L, "Puma Suede", 0));
        productos.put(4L, new Producto(4L, "Converse Chuck Taylor", 12));
        productos.put(5L, new Producto(5L, "New Balance 574", 8));
    }

    public synchronized Pedido crearPedido(Pedido pedido) {
        if (pedido == null || pedido.getCliente() == null || pedido.getCliente().isBlank()
                || pedido.getProductoId() == null || pedido.getCantidad() == null
                || pedido.getCantidad() <= 0 || pedido.getPrioridad() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "cliente, productoId, cantidad y prioridad son obligatorios; cantidad debe ser mayor que cero");
        }
        if (!productos.containsKey(pedido.getProductoId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no existe");
        }

        pedido.setId((long) (pedidos.size() + 1));
        pedido.setEstado(Estado.PENDIENTE);

        pedidos.add(pedido);

        return pedido;
    }

    public synchronized List<Pedido> obtenerPedidos() {
        return new ArrayList<>(pedidos);
    }

    public synchronized List<Producto> obtenerProductos() {
        return new ArrayList<>(productos.values());
    }

    public synchronized Pedido confirmar(Long id) {
        Pedido pedido = buscar(id);
        exigirEstado(pedido, Estado.PENDIENTE);
        Producto producto = productos.get(pedido.getProductoId());
        if (producto.getStock() < pedido.getCantidad()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente");
        }
        producto.setStock(producto.getStock() - pedido.getCantidad());
        pedido.setEstado(Estado.CONFIRMADO);
        return pedido;
    }

    public synchronized Pedido cancelar(Long id) {
        Pedido pedido = buscar(id);
        if (pedido.getEstado() == Estado.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido ya está cancelado");
        }
        if (pedido.getEstado() == Estado.DESPACHADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cancelar un pedido despachado");
        }
        if (pedido.getEstado() == Estado.CONFIRMADO) {
            Producto producto = productos.get(pedido.getProductoId());
            producto.setStock(producto.getStock() + pedido.getCantidad());
        }
        pedido.setEstado(Estado.CANCELADO);
        return pedido;
    }

    public synchronized Pedido despachar(Long id) {
        Pedido pedido = buscar(id);
        exigirEstado(pedido, Estado.CONFIRMADO);
        pedido.setEstado(Estado.DESPACHADO);
        return pedido;
    }

    public synchronized List<Pedido> porEstado(Estado estado) {
        return pedidos.stream().filter(pedido -> pedido.getEstado() == estado).toList();
    }

    public synchronized List<Pedido> urgentes() {
        return pedidos.stream().filter(pedido -> pedido.getPrioridad() == Prioridad.URGENTE).toList();
    }

    public synchronized Pedido siguiente() {
        return pedidos.stream()
                .filter(pedido -> pedido.getEstado() == Estado.PENDIENTE)
                .min(Comparator.comparing(Pedido::getPrioridad, Comparator.comparingInt(Prioridad::peso))
                        .thenComparing(Pedido::getId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay pedidos pendientes"));
    }

    public synchronized List<Pedido> enRiesgo() {
        return pedidos.stream()
                .filter(pedido -> pedido.getEstado() == Estado.PENDIENTE
                        && productos.get(pedido.getProductoId()).getStock() < pedido.getCantidad())
                .toList();
    }

    public synchronized Map<String, Long> resumen() {
        Map<String, Long> resumen = new HashMap<>();
        for (Estado estado : Estado.values()) {
            resumen.put(estado.name().toLowerCase(), pedidos.stream()
                    .filter(pedido -> pedido.getEstado() == estado).count());
        }
        resumen.put("total", (long) pedidos.size());
        resumen.put("urgentes", pedidos.stream()
                .filter(pedido -> pedido.getPrioridad() == Prioridad.URGENTE).count());
        return resumen;
    }

    private Pedido buscar(Long id) {
        return pedidos.stream().filter(pedido -> pedido.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no existe"));
    }

    private void exigirEstado(Pedido pedido, Estado esperado) {
        if (pedido.getEstado() != esperado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido debe estar " + esperado + " para realizar esta operación");
        }
    }
}