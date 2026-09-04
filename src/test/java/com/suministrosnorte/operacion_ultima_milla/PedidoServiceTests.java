package com.suministrosnorte.operacion_ultima_milla;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.suministrosnorte.model.Estado;
import com.suministrosnorte.model.Pedido;
import com.suministrosnorte.model.Prioridad;
import com.suministrosnorte.service.PedidoService;

class PedidoServiceTests {

    private PedidoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoService();
    }

    @Test
    void creaPedidoPendienteConIdAutomatico() {
        Pedido pedido = service.crearPedido(pedido(1L, 5, Prioridad.ALTA));

        assertEquals(1L, pedido.getId());
        assertEquals(Estado.PENDIENTE, pedido.getEstado());
    }

    @Test
    void noConfirmaSiNoHayStock() {
        Pedido pedido = service.crearPedido(pedido(3L, 1, Prioridad.URGENTE));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.confirmar(pedido.getId()));

        assertEquals(409, error.getStatusCode().value());
        assertEquals(Estado.PENDIENTE, pedido.getEstado());
    }

    @Test
    void noPermiteConfirmarDosVeces() {
        Pedido pedido = service.crearPedido(pedido(1L, 5, Prioridad.MEDIA));
        service.confirmar(pedido.getId());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.confirmar(pedido.getId()));

        assertEquals(409, error.getStatusCode().value());
    }

    @Test
    void cancelarPedidoConfirmadoDevuelveStock() {
        Pedido pedido = service.crearPedido(pedido(1L, 5, Prioridad.MEDIA));
        service.confirmar(pedido.getId());

        service.cancelar(pedido.getId());

        assertEquals(20, service.obtenerProductos().get(0).getStock());
        assertEquals(Estado.CANCELADO, pedido.getEstado());
    }

    @Test
    void noPermiteDespacharPedidoPendiente() {
        Pedido pedido = service.crearPedido(pedido(1L, 1, Prioridad.BAJA));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.despachar(pedido.getId()));

        assertEquals(409, error.getStatusCode().value());
        assertEquals(Estado.PENDIENTE, pedido.getEstado());
    }

    @Test
    void siguientePriorizaYDesempataPorId() {
        Pedido baja = service.crearPedido(pedido(1L, 1, Prioridad.BAJA));
        Pedido urgenteAnterior = service.crearPedido(pedido(2L, 1, Prioridad.URGENTE));
        service.crearPedido(pedido(1L, 1, Prioridad.URGENTE));

        assertEquals(urgenteAnterior.getId(), service.siguiente().getId());
        assertTrue(baja.getId() < urgenteAnterior.getId());
    }

    @Test
    void pedidoSinStockPermanecePendienteYEnRiesgo() {
        Pedido pedido = service.crearPedido(pedido(3L, 1, Prioridad.URGENTE));

        assertEquals(Estado.PENDIENTE, pedido.getEstado());
        assertEquals(1, service.enRiesgo().size());
        assertEquals(pedido.getId(), service.enRiesgo().get(0).getId());
    }

    @Test
    void informaCuandoNoHayPedidosPendientes() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.siguiente());

        assertEquals(404, error.getStatusCode().value());
        assertEquals("No hay pedidos pendientes", error.getReason());
    }

    @Test
    void rechazaDatosInvalidosAlCrear() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.crearPedido(pedido(1L, 0, Prioridad.ALTA)));

        assertEquals(400, error.getStatusCode().value());
    }

    private Pedido pedido(Long productoId, Integer cantidad, Prioridad prioridad) {
        Pedido pedido = new Pedido();
        pedido.setCliente("Cliente de prueba");
        pedido.setProductoId(productoId);
        pedido.setCantidad(cantidad);
        pedido.setPrioridad(prioridad);
        return pedido;
    }
}