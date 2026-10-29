
/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.controller;

import cl.ucn.disc.arqsist.library.service.LoanService;
import io.javalin.config.JavalinConfig;

/**
 * Registers HTTP routes for lending and returns through the loan service.
 */
public final class LoanController {

    /** Service responsible for loan operations. */
    private final LoanService loanService;

    /**
     * Creates the loan controller.
     *
     * @param loanService service responsible for loan operations.
     */
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /**
     * Registers loan routes in Javalin.
     *
     * @param config Javalin configuration used to register routes.
     */
    public void register(JavalinConfig config) {
        config.routes.post("/loans", ctx -> {
            int memberId = Integer.parseInt(ctx.queryParam("memberId"));
            int bookId = Integer.parseInt(ctx.queryParam("bookId"));

            ctx.json(loanService.checkout(memberId, bookId));
        });

        config.routes.get("/loans", ctx -> ctx.json(loanService.findAll()));

        config.routes.post("/loans/{id}/return", ctx -> ctx.json(
                loanService.returnLoan(Integer.parseInt(ctx.pathParam("id")))
        ));

        config.routes.get("/loans/overdue", ctx -> ctx.json(loanService.overdueLoans()));
    }
}
