package com.suministrosnorte.operacion_ultima_milla;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.suministrosnorte.model.Estado;
import com.suministrosnorte.model.Pedido;
import com.suministrosnorte.model.Prioridad;
import com.suministrosnorte.model.Producto;
import com.suministrosnorte.repository.ProductoRepository;
import com.suministrosnorte.service.PedidoService;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTests {

    @Mock
    private ProductoRepository productoRepository;

    private PedidoService service;

    private Producto producto1;
    private Producto producto2;
    private Producto producto3;

    @BeforeEach
    void setUp() {

        producto1 = new Producto(1L, "Nike Air Max 20", 20);
        producto2 = new Producto(2L, "Adidas Ultraboost 5", 5);
        producto3 = new Producto(3L, "Puma Suede", 0);

        service = new PedidoService(productoRepository);
    }

    @Test
    void creaPedidoPendienteConIdAutomatico() {

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto1));

        Pedido pedido = service.crearPedido(
                pedido(1L, 5, Prioridad.ALTA)
        );

        assertEquals(1L, pedido.getId());
        assertEquals(Estado.PENDIENTE, pedido.getEstado());
    }

    @Test
    void noConfirmaSiNoHayStock() {

        when(productoRepository.findById(3L))
                .thenReturn(java.util.Optional.of(producto3));

        Pedido pedido = service.crearPedido(
                pedido(3L, 1, Prioridad.URGENTE)
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.confirmar(pedido.getId())
        );

        assertEquals(409, error.getStatusCode().value());
        assertEquals(Estado.PENDIENTE, pedido.getEstado());
    }

    @Test
    void noPermiteConfirmarDosVeces() {

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto1));

        Pedido pedido = service.crearPedido(
                pedido(1L, 5, Prioridad.MEDIA)
        );

        service.confirmar(pedido.getId());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.confirmar(pedido.getId())
        );

        assertEquals(409, error.getStatusCode().value());
    }

    @Test
    void cancelarPedidoConfirmadoDevuelveStock() {

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto1));

        Pedido pedido = service.crearPedido(
                pedido(1L, 5, Prioridad.MEDIA)
        );

        service.confirmar(pedido.getId());

        service.cancelar(pedido.getId());

        assertEquals(20, producto1.getStock());
        assertEquals(Estado.CANCELADO, pedido.getEstado());
    }

    @Test
    void noPermiteDespacharPedidoPendiente() {

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto1));

        Pedido pedido = service.crearPedido(
                pedido(1L, 1, Prioridad.BAJA)
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.despachar(pedido.getId())
        );

        assertEquals(409, error.getStatusCode().value());
        assertEquals(Estado.PENDIENTE, pedido.getEstado());
    }

    @Test
    void siguientePriorizaYDesempataPorId() {

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto1));
        when(productoRepository.findById(2L))
                .thenReturn(java.util.Optional.of(producto2));

        Pedido baja = service.crearPedido(
                pedido(1L, 1, Prioridad.BAJA)
        );

        Pedido urgenteAnterior = service.crearPedido(
                pedido(2L, 1, Prioridad.URGENTE)
        );

        service.crearPedido(
                pedido(1L, 1, Prioridad.URGENTE)
        );

        assertEquals(
                urgenteAnterior.getId(),
                service.siguiente().getId()
        );

        assertTrue(
                baja.getId() < urgenteAnterior.getId()
        );
    }

    @Test
    void pedidoSinStockPermanecePendienteYEnRiesgo() {

        when(productoRepository.findById(3L))
                .thenReturn(java.util.Optional.of(producto3));

        Pedido pedido = service.crearPedido(
                pedido(3L, 1, Prioridad.URGENTE)
        );

        assertEquals(
                Estado.PENDIENTE,
                pedido.getEstado()
        );

        assertEquals(
                1,
                service.enRiesgo().size()
        );

        assertEquals(
                pedido.getId(),
                service.enRiesgo().get(0).getId()
        );
    }

    @Test
    void informaCuandoNoHayPedidosPendientes() {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.siguiente()
        );

        assertEquals(
                404,
                error.getStatusCode().value()
        );

        assertEquals(
                "No hay pedidos pendientes",
                error.getReason()
        );
    }

    @Test
    void rechazaDatosInvalidosAlCrear() {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.crearPedido(
                        pedido(1L, 0, Prioridad.ALTA)
                )
        );

        assertEquals(
                400,
                error.getStatusCode().value()
        );
    }

    @Test
    void rechazaProductoInexistente() {

        when(productoRepository.findById(99L))
                .thenReturn(java.util.Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.crearPedido(
                        pedido(99L, 1, Prioridad.ALTA)
                )
        );

        assertEquals(
                404,
                error.getStatusCode().value()
        );
    }

    @Test
    void confirmaPedidoYDescuentaStock() {

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto1));

        Pedido pedido = service.crearPedido(
                pedido(1L, 5, Prioridad.ALTA)
        );

        service.confirmar(pedido.getId());

        assertEquals(
                15,
                producto1.getStock()
        );

        assertEquals(
                Estado.CONFIRMADO,
                pedido.getEstado()
        );
    }

    @Test
    void despachaPedidoConfirmado() {

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto1));

        Pedido pedido = service.crearPedido(
                pedido(1L, 2, Prioridad.MEDIA)
        );

        service.confirmar(pedido.getId());
        service.despachar(pedido.getId());

        assertEquals(
                Estado.DESPACHADO,
                pedido.getEstado()
        );
    }

    @Test
    void noPermiteCancelarPedidoDespachado() {

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto1));

        Pedido pedido = service.crearPedido(
                pedido(1L, 2, Prioridad.MEDIA)
        );

        service.confirmar(pedido.getId());
        service.despachar(pedido.getId());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.cancelar(pedido.getId())
        );

        assertEquals(
                409,
                error.getStatusCode().value()
        );
    }

    private Pedido pedido(
            Long productoId,
            Integer cantidad,
            Prioridad prioridad) {

        Pedido pedido = new Pedido();

        pedido.setCliente("Cliente de prueba");
        pedido.setProductoId(productoId);
        pedido.setCantidad(cantidad);
        pedido.setPrioridad(prioridad);

        return pedido;
    }
}