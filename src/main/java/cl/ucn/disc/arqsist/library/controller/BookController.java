
/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.controller;

import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.service.BookService;
import io.javalin.config.JavalinConfig;

/**
 * Registers HTTP routes for the book catalogue through the service layer.
 */
public final class BookController {

    /** Service responsible for book operations. */
    private final BookService service;

    /**
     * Creates the book controller.
     *
     * @param service service responsible for book operations.
     */
    public BookController(BookService service) {
        this.service = service;
    }

    /**
     * Registers the book routes.
     *
     * @param config Javalin configuration used to register routes.
     */
    public void register(JavalinConfig config) {
        config.routes.get("/books", ctx -> ctx.json(service.listAll()));

        config.routes.get("/books/{id}", ctx -> ctx.json(
                service.findById(Integer.parseInt(ctx.pathParam("id")))
        ));

        config.routes.post("/books", ctx -> ctx.json(
                service.create(ctx.bodyAsClass(Book.class))
        ));
    }
}
