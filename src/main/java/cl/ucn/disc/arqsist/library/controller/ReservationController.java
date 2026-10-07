/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.controller;

import cl.ucn.disc.arqsist.library.service.ReservationService;
import io.javalin.config.JavalinConfig;

import java.util.Objects;

/**
 * Registers the HTTP routes related to reservations.
 */
public final class ReservationController {

    /**
     * Service used for reservation operations.
     */
    private final ReservationService service;

    /**
     * Creates the reservation controller.
     *
     * @param service service used for reservation operations.
     */
    public ReservationController(
            ReservationService service
    ) {
        this.service = service;
    }

    /**
     * Registers the reservation routes in Javalin.
     *
     * @param config Javalin configuration used to register the routes.
     */
    public void register(JavalinConfig config) {
        config.routes.post("/reservations", ctx -> {
            int memberId = Integer.parseInt(
                    Objects.requireNonNull(
                            ctx.queryParam("memberId")
                    )
            );

            int bookId = Integer.parseInt(
                    Objects.requireNonNull(
                            ctx.queryParam("bookId")
                    )
            );

            ctx.json(
                    service.reserve(bookId, memberId)
            );
        });

        config.routes.get(
                "/reservations",
                ctx -> ctx.json(service.findAll())
        );

        config.routes.post(
                "/reservations/{id}/fulfill",
                ctx -> ctx.json(
                        service.fulfill(
                                Integer.parseInt(ctx.pathParam("id"))
                        )
                )
        );
    }
}