package com.example.catalogue;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.catalogue.entity.Product;
import com.example.catalogue.repository.ProductRepository;

@SpringBootApplication
public class CatalogueApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogueApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(ProductRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                // Crear los 3 productos únicos con su respectivo stock
                repository.save(new Product(null, "Empanada", "Pâte de maïs croustillante farcie de viande effilochée et de pommes de terre, accompagnée d’ají maison.", 3.00, 15));
                repository.save(new Product(null, "Arepa", "Arepa de maïs avec una généreuse couche de fromage paysan fondu.", 4.90, 20));
                repository.save(new Product(null, "Bandeja Paisa", "Le plat emblématique : haricots, riz, chicharrón, œuf, viande, avocado et bien plus encore.", 12.45, 10));
                
                System.out.println("Base de datos de Catálogo inicializada con 3 productos únicos y stock específico.");
            }
        };
    }
}
