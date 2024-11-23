package com.jolumiba.literalura.service;

import com.jolumiba.literalura.model.Author;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorR extends JpaRepository<Author, Long> {

    Optional<Author> findByNombre(String nombre);

    // Consulta para obtener todos los autores con sus libros relacionados (usando LEFT JOIN FETCH)
    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.librosDelAutor")
    List<Author> findAllWithLibros();

    // Método con EntityGraph para cargar los libros del autor de forma inmediata
    @EntityGraph(attributePaths = "librosDelAutor")
    List<Author> findByFechaNacimientoBeforeAndFechaFallecimientoAfterOrFechaFallecimientoIsNullAndFechaNacimientoIsNotNull(String fechaNacimiento, String fechaFallecimiento);

    // Método para buscar por nombre ignorando mayúsculas y minúsculas
    @EntityGraph(attributePaths = "librosDelAutor")
    List<Author> findByNombreContainingIgnoreCase(String nombre);

    // Método para buscar autores nacidos entre dos fechas
    @EntityGraph(attributePaths = "librosDelAutor")
    List<Author> findByFechaNacimientoBetween(String inicio, String fin);

    // Nueva consulta para obtener autores vivos
    @Query("SELECT a FROM Author a WHERE (a.fechaNacimiento < :anio AND a.fechaFallecimiento > :anio) OR (a.fechaFallecimiento IS NULL AND a.fechaNacimiento IS NOT NULL)")
    List<Author> findAutorsVivosByAnio(@Param("anio") int anio);
}
