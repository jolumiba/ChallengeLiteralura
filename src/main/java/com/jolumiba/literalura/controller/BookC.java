package com.jolumiba.literalura.controller;


import java.util.List;

public record BookC(
        Long id,
        String titulo,
        List<AuthorC> autores,
        String idiomas,
        Double numeroDeDescargas
) {

}
