package com.jolumiba.literalura.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Entity
@Table(name = "libros")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String titulo;

    @Column(name = "idiomas")
    private String idiomas;// Ejemplo: "en,es,fr"

    private Double numeroDeDescargas;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "libro_autor",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private List<Author> autores = new ArrayList<>();

    public Book() {};

    public Book(BooksData booksData) {
        this.titulo = booksData.titulo();
        this.idiomas = String.join(",", booksData.idiomas()); // Convierte la lista a un String
        this.numeroDeDescargas = booksData.numeroDescargas();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<String> getIdiomas() {
        return Arrays.asList(idiomas.split(","));
    }

    public void setIdiomas(List<String> idiomas) {
        this.idiomas = String.join(",", idiomas);
    }

    public Double getNumeroDeDescargas() {
        return numeroDeDescargas;
    }

    public void setNumeroDeDescargas(Double numeroDeDescargas) {
        this.numeroDeDescargas = numeroDeDescargas;
    }

    public List<Author> getAutores() {
        return autores;
    }

    public void setAutores(List<Author> autores) {
        this.autores = autores;
    }

    @Override
    public String toString() {
        return """
                **************************************************
                *                      LIBRO                     *
                **************************************************
                Título: %s
                Autor: %s
                Idioma: %s
                N° Descargas: %f""".formatted(titulo, autores, idiomas, numeroDeDescargas);// + "\n";
    }
}
