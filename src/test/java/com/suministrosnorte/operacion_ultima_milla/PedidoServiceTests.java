package com.suministrosnorte.operacion_ultima_milla;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    private Pedido pedido(Long productoId, Integer cantidad, Prioridad prioridad) {
        Pedido pedido = new Pedido();
        pedido.setCliente("Cliente de prueba");
        pedido.setProductoId(productoId);
        pedido.setCantidad(cantidad);
        pedido.setPrioridad(prioridad);
        return pedido;
    }
}