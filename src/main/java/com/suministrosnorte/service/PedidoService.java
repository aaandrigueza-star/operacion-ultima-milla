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

    /**
     * Inicializa la lista de productos disponibles para la operación de última milla.
     * Cada producto incluye un identificador y el stock actual disponible.
     */
    public PedidoService() {
        productos.put(1L, new Producto(1L, "Nike Air Max", 20));
        productos.put(2L, new Producto(2L, "Adidas Ultraboost", 5));
        productos.put(3L, new Producto(3L, "Puma Suede", 0));
        productos.put(4L, new Producto(4L, "Converse Chuck Taylor", 12));
        productos.put(5L, new Producto(5L, "New Balance 574", 8));
    }

    /**
     * Crea un nuevo pedido a partir de la información recibida por la API.
     * Valida que el cliente, el producto, la cantidad y la prioridad sean correctos.
     *
     * @param pedido datos del pedido que se desea registrar.
     * @return pedido creado con su identificador inicial y estado pendiente.
     * @throws ResponseStatusException si faltan datos, la cantidad es inválida, la prioridad es nula o el producto no existe.
     */
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

    /**
     * Obtiene la lista completa de pedidos registrados en memoria.
     *
     * @return copia de la colección de pedidos para evitar modificaciones externas.
     */
    public synchronized List<Pedido> obtenerPedidos() {
        return new ArrayList<>(pedidos);
    }

    /**
     * Obtiene todos los productos disponibles en inventario.
     *
     * @return copia de la colección de productos actuales.
     */
    public synchronized List<Producto> obtenerProductos() {
        return new ArrayList<>(productos.values());
    }

    /**
     * Confirma un pedido pendiente y descuenta la cantidad solicitada del stock del producto.
     *
     * @param id identificador del pedido a confirmar.
     * @return pedido actualizado con estado confirmado.
     * @throws ResponseStatusException si el pedido no existe, no está pendiente o no hay stock suficiente.
     */
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

    /**
     * Cancela un pedido que aún no ha sido despachado.
     * Si estaba confirmado, devuelve la cantidad al stock disponible.
     *
     * @param id identificador del pedido a cancelar.
     * @return pedido actualizado con estado cancelado.
     * @throws ResponseStatusException si el pedido no existe, ya fue cancelado o ya fue despachado.
     */
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

    /**
     * Marca un pedido como despachado solo cuando se encuentra confirmado.
     *
     * @param id identificador del pedido a despachar.
     * @return pedido actualizado con estado despachado.
     * @throws ResponseStatusException si el pedido no existe o no está confirmado.
     */
    public synchronized Pedido despachar(Long id) {
        Pedido pedido = buscar(id);
        exigirEstado(pedido, Estado.CONFIRMADO);
        pedido.setEstado(Estado.DESPACHADO);
        return pedido;
    }

    /**
     * Filtra los pedidos según el estado indicado.
     *
     * @param estado valor de filtro a buscar.
     * @return lista de pedidos que coinciden con el estado recibido.
     */
    public synchronized List<Pedido> porEstado(Estado estado) {
        return pedidos.stream().filter(pedido -> pedido.getEstado() == estado).toList();
    }

    /**
     * Obtiene los pedidos marcados como urgentes.
     *
     * @return colección de pedidos con prioridad urgente.
     */
    public synchronized List<Pedido> urgentes() {
        return pedidos.stream().filter(pedido -> pedido.getPrioridad() == Prioridad.URGENTE).toList();
    }

    /**
     * Selecciona el siguiente pedido pendiente con la prioridad más alta y el menor identificador.
     *
     * @return el pedido pendiente con mayor urgencia disponible.
     * @throws ResponseStatusException si no existen pedidos pendientes.
     */
    public synchronized Pedido siguiente() {
        return pedidos.stream()
                .filter(pedido -> pedido.getEstado() == Estado.PENDIENTE)
                .min(Comparator.comparing(Pedido::getPrioridad, Comparator.comparingInt(Prioridad::peso))
                        .thenComparing(Pedido::getId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay pedidos pendientes"));
    }

    /**
     * Identifica los pedidos en riesgo, es decir, pendientes cuya cantidad supera el stock disponible.
     *
     * @return lista de pedidos pendientes con inventario insuficiente.
     */
    public synchronized List<Pedido> enRiesgo() {
        return pedidos.stream()
                .filter(pedido -> pedido.getEstado() == Estado.PENDIENTE
                        && productos.get(pedido.getProductoId()).getStock() < pedido.getCantidad())
                .toList();
    }

    /**
     * Genera un resumen cuantitativo de los pedidos por estado y total general.
     *
     * @return mapa con contadores por estado, total de pedidos y cantidad de urgentes.
     */
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

    /**
     * Busca un pedido por su identificador dentro de la colección principal.
     *
     * @param id identificador del pedido a buscar.
     * @return pedido encontrado.
     * @throws ResponseStatusException si no existe un pedido con ese identificador.
     */
    private Pedido buscar(Long id) {
        return pedidos.stream().filter(pedido -> pedido.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no existe"));
    }

    /**
     * Valida que un pedido esté en un estado concreto antes de ejecutar una acción.
     *
     * @param pedido pedido que se quiere evaluar.
     * @param esperado estado requerido para continuar.
     * @throws ResponseStatusException si el pedido no se encuentra en el estado esperado.
     */
    private void exigirEstado(Pedido pedido, Estado esperado) {
        if (pedido.getEstado() != esperado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido debe estar " + esperado + " para realizar esta operación");
        }
    }
}