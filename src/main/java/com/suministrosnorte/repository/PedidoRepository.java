package com.suministrosnorte.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suministrosnorte.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}