package com.suministrosnorte.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suministrosnorte.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}