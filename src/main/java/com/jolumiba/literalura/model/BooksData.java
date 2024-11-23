package com.jolumiba.literalura.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BooksData(
    @JsonAlias("title") String titulo,
    @JsonAlias("authors") List<AuthorData> autor,
    @JsonAlias("languages") List<String> idiomas,
    @JsonAlias("download_count") Double numeroDescargas
) {
}
