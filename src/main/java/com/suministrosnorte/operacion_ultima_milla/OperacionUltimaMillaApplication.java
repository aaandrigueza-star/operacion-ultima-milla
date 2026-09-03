package com.suministrosnorte.operacion_ultima_milla;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.suministrosnorte.model.Producto;
import com.suministrosnorte.repository.ProductoRepository;

@SpringBootApplication(scanBasePackages = "com.suministrosnorte")
@EntityScan(basePackages = "com.suministrosnorte.model")
@EnableJpaRepositories(basePackages = "com.suministrosnorte.repository")
public class OperacionUltimaMillaApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                OperacionUltimaMillaApplication.class,
                args
        );
    }

    @Bean
    CommandLineRunner cargarProductosIniciales(ProductoRepository productoRepository) {
        return args -> {
            if (productoRepository.count() == 0) {
                productoRepository.saveAll(List.of(
                        new Producto(null, "Nike Air Max", 20),
                        new Producto(null, "Adidas Ultraboost", 5),
                        new Producto(null, "Puma Suede", 0),
                        new Producto(null, "Converse Chuck Taylor", 12),
                        new Producto(null, "New Balance 574", 8)
                ));
            }
        };
    }
}