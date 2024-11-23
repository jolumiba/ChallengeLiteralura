package com.jolumiba.literalura.controller;

public record AuthorC(
        Long id,
        String nombre,
        String fechaNacimiento,
        String fechaFallecimiento
) {
}
