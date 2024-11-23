package com.jolumiba.literalura.Principal;

import com.jolumiba.literalura.controller.AuthorC;
import com.jolumiba.literalura.controller.BookC;
import com.jolumiba.literalura.model.*;
import com.jolumiba.literalura.service.AuthorR;
import com.jolumiba.literalura.service.BookR;
import com.jolumiba.literalura.service.ConsumoAPI;
import com.jolumiba.literalura.service.ConvierteDatos;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private final Scanner sc = new Scanner(System.in);
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private static final String URL_BASE = "https://gutendex.com/books/";
    private final ConvierteDatos conversor = new ConvierteDatos();
    private final BookR bookR;
    private final AuthorR authorR;

    public Principal(BookR bookR, AuthorR authorR) {
        this.bookR = bookR;
        this.authorR = authorR;
    }

    // Menú principal
    public void menuPrincipal() {
        int opcion;
        while ((opcion = mostrarMenuPrincipal()) != 0) {
            ejecutarOpcion(opcion);
        }
        System.out.println("\nCerrando la aplicación...");
    }

    private int mostrarMenuPrincipal() {
        String menu = """
                **************************************************
                *                 MENU PRINCIPAL                 *
                **************************************************
                1 - Buscar libro por título
                2 - Listar libros registrados
                3 - Listar autores registrados
                4 - Listar autores vivos en un determinado año
                5 - Listar libros por idioma
                6 - Buscar autor por nombre
                0 - Salir
                """;
        System.out.println(menu);
        System.out.print("Opción: ");
        String opcionMenu = sc.nextLine();
        try {
            return Integer.parseInt(opcionMenu);
        } catch (NumberFormatException e) {
            System.out.println("Por favor ingrese una opción válida: ");
            return -1;
        }
    }

    private void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1 -> buscarLibroPorTitulo();
            case 2 -> listarLibrosRegistrados();
            case 3 -> listarAutoresRegistrados();
            case 4 -> listarAutoresVivosEnAnio();
            case 5 -> listarLibrosPorIdioma();
            case 6 -> buscarAutorPorNombre();
            case 0 -> System.out.println("\nCerrando la aplicación...");
            default -> System.out.println("Opción inválida");
        }
    }

    // Buscar Libro por Título
    private void buscarLibroPorTitulo() {
        BooksData booksData = getBooksData();
        if (booksData == null) {
            System.out.println("\nLibro no Encontrado.");
            pausa();
            return;
        }

        Optional<Book> libroExistente = bookR.findByTitulo(booksData.titulo());
        if (libroExistente.isPresent()) {
            mostrarLibroRegistrado(libroExistente.get());
            return;
        }

        List<Author> autores = crearAutores(booksData);
        Book book = new Book(booksData);
        book.setAutores(autores);
        bookR.save(book);

        mostrarLibroRegistrado(book);
    }

    private BooksData getBooksData() {
        System.out.print("Ingresa el nombre del libro a buscar: ");
        String nombreLibro = sc.nextLine();
        String json = consumoAPI.obtenerDatosLibros(URL_BASE + "?search=" + nombreLibro.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Data.class);

        return datosBusqueda.listaResultados().stream()
                .filter(datosLibros -> datosLibros.titulo().toUpperCase().contains(nombreLibro.toUpperCase()))
                .findFirst()
                .orElse(null);
    }

    private List<Author> crearAutores(BooksData booksData) {
        return booksData.autor().stream()
                .map(authorData -> authorR.findByNombre(authorData.nombre())
                        .orElseGet(() -> {
                            Author nuevoAutor = new Author();
                            nuevoAutor.setNombre(authorData.nombre());
                            nuevoAutor.setFechaNacimiento(authorData.fechaNacimiento());
                            nuevoAutor.setFechaFallecimiento(authorData.fechaFallecimiento());
                            authorR.save(nuevoAutor);
                            return nuevoAutor;
                        })
                ).collect(Collectors.toList());
    }

    private void mostrarLibroRegistrado(Book book) {
        BookC bookC = new BookC(
                book.getId(),
                book.getTitulo(),
                book.getAutores().stream().map(autor -> new AuthorC(autor.getId(), autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento()))
                        .collect(Collectors.toList()),
                String.join(", ", book.getIdiomas()),
                book.getNumeroDeDescargas()
        );

        System.out.printf("""
                **************************************************
                *                      LIBRO                     *
                **************************************************
                Título: %s
                Autor: %s
                Idioma: %s
                N° Descargas: %.2f%n""",
                bookC.titulo(),
                bookC.autores().stream().map(AuthorC::nombre).collect(Collectors.joining(", ")),
                bookC.idiomas(),
                bookC.numeroDeDescargas());
        System.out.println("--------------------------------------------------");
        pausa();
    }

    // Listar Libros Registrados
    private void listarLibrosRegistrados() {
        List<Book> libros = bookR.findAllWithAutores();
        if (libros.isEmpty()) {
            System.out.println("\nNo hay Libros registrados en el sistema.");
            pausa();
            return;
        }
        System.out.printf("\n%d LIBROS REGISTRADOS\n", libros.size());
        mostrarLibros(libros);
    }

    // Listar Autores Registrados
    private void listarAutoresRegistrados() {
        List<Author> autores = authorR.findAllWithLibros();
        if (autores.isEmpty()) {
            System.out.println("\nNo hay Autores registrados en el sistema.");
            pausa();
            return;
        }
        System.out.printf("\n%d AUTORES REGISTRADOS\n", autores.size());
        mostrarAutores(autores);
    }

    // Listar Autores Vivos en un Año Específico
    private void listarAutoresVivosEnAnio() {
        String anio = obtenerAnioValido();
        if (anio == null) return;

        // Convertir el año a entero
        int anioInt = Integer.parseInt(anio);


        // Llamar al repositorio para obtener todos los autores
        List<Author> autores = authorR.findAllWithLibros(); // Asegúrate de obtener todos los autores registrados

        // Filtrar los autores que están vivos en ese año
        List<Author> autoresVivos = autores.stream()
                .filter(autor -> {
                    // Verificar si el autor está vivo en el año dado
                    //int añoNacimiento = Integer.parseInt(autor.getFechaNacimiento());
                    String fechaNacimiento = autor.getFechaNacimiento();
                    int añoNacimiento = (fechaNacimiento != null && !fechaNacimiento.isEmpty()) ? Integer.parseInt(fechaNacimiento) : -1; // -1 o algún valor por defecto


                    Integer añoFallecimiento = autor.getFechaFallecimiento() != null ? Integer.parseInt(autor.getFechaFallecimiento()) : null;

                    // El autor debe haber nacido antes o en el año proporcionado
                    boolean nacidoAntesDelAnio = añoNacimiento <= anioInt;

                    // Si el autor tiene fecha de fallecimiento, debe haber fallecido después del año proporcionado
                    boolean estaVivoEnElAnio = (añoFallecimiento == null || añoFallecimiento > anioInt);

                    // El autor está vivo si nació antes o en el año y no ha fallecido en ese año
                    return nacidoAntesDelAnio && estaVivoEnElAnio;
                })
                .collect(Collectors.toList());

        // Si no se encuentran autores vivos, mostrar mensaje adecuado
        if (autoresVivos.isEmpty()) {
            System.out.println("\nNo se encontraron autores vivos en el año buscado.");
        } else {
            // Mostrar los autores encontrados
            System.out.printf("\n%d AUTORES VIVOS EN %s\n", autoresVivos.size(), anio);
            mostrarAutores(autoresVivos);
        }

        // Pausar la ejecución hasta que el usuario presione Enter
        pausa();
    }

    private String obtenerAnioValido() {
        String anio;
        while (true) {
            System.out.print("Ingresa el año a buscar: ");
            anio = sc.nextLine();
            if (validarAnio4Digitos(anio)) {
                return anio;
            }
            anioNoValido();
        }
    }

    private boolean validarAnio4Digitos(String anio) {
        return anio.matches("\\d{4}");
    }

    private void anioNoValido() {
        System.out.println("\nAño no válido. Por favor, ingresa un año de 4 dígitos.");
    }



    // Listar Libros por Idioma
    private void listarLibrosPorIdioma() {
        String idiomaLibro = obtenerCodigoIdioma();
        if (idiomaLibro == null) return;

        List<Book> librosPorIdioma = bookR.findByIdiomasContaining(idiomaLibro);
        if (librosPorIdioma.isEmpty()) {
            System.out.println("\nNo se encontraron libros en el idioma buscado.");
        } else {
            System.out.printf("\n%d LIBROS EN EL IDIOMA '%s'\n", librosPorIdioma.size(), idiomaLibro.toUpperCase());
            mostrarLibros(librosPorIdioma);
        }
        pausa();
    }

    private String obtenerCodigoIdioma() {
        var menuIdiomas = """
                
                **************************************************
                *               LIBROS POR IDIOMA                *
                **************************************************
                es - Español                it - Italiano
                en - Inglés                 ja - Japonés
                fr - Francés                pt - Portugués
                ru - Ruso                   zh - Chino Mandarín
                de - Alemán                 ar - Árabe
                """;
       // String idiomaLibro;
        System.out.println(menuIdiomas);
        System.out.print("Ingresa el código del idioma para buscar libros: ");
        return sc.nextLine().toLowerCase();
    }

    // Mostrar Libros
    private void mostrarLibros(List<Book> libros) {
        libros.forEach(libro -> System.out.println(libro.getTitulo()));
        pausa();
    }

    // Mostrar Autores
    private void mostrarAutores(List<Author> autores) {
       // autores.forEach(autor -> System.out.println(autor.getNombre()));
        autores.forEach(autor -> {
            String fechaFallecimiento = (autor.getFechaFallecimiento() != null) ? autor.getFechaFallecimiento() : "N/A"; // Mostrar "N/A" si no tiene fecha de fallecimiento
            System.out.printf("Nombre: %s, Año de Nacimiento: %s, Año de Fallecimiento: %s%n",
                    autor.getNombre(), autor.getFechaNacimiento(), fechaFallecimiento);
        });

        pausa();
    }

    private void pausa() {
        boolean inputValido = false;
        while (!inputValido) {
            System.out.print("\nPresiona ENTER para continuar o 'Q' para salir: ");
            String entrada = sc.nextLine().toUpperCase(); // Convertir a mayúsculas para simplificar la comparación
            if (entrada.equals("")) {
                inputValido = true; // El usuario presiona Enter, continuamos
            } else if (entrada.equals("Q")) {
                System.out.println("Saliendo de la aplicación...");
                System.exit(0); // Terminar la aplicación si el usuario ingresa 'Q'
            } else {
                System.out.println("Opción no válida. Por favor, presiona ENTER para continuar o 'Q' para salir.");
            }
        }
    }

    // Buscar Autor por Nombre
    private void buscarAutorPorNombre() {
        System.out.print("Ingresa el nombre del autor: ");
        String nombreAutor = sc.nextLine().toLowerCase();
      //  Optional<Author> autor = authorR.findByNombre(nombreAutor);
        List<Author> autoresBuscados = authorR.findByNombreContainingIgnoreCase(nombreAutor);

        if (autoresBuscados.isEmpty()) {
            System.out.println("\nAutor no encontrado.");
        } else {
            System.out.println("\nAutor encontrado:");
            mostrarAutores(autoresBuscados);
        }
        pausa();
    }
}
