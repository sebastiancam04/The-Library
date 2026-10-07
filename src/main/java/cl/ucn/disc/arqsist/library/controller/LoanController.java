/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.controller;

import cl.ucn.disc.arqsist.library.service.LoanService;
import cl.ucn.disc.arqsist.library.service.MemberService;
import io.javalin.config.JavalinConfig;

import java.util.Objects;

/**
 * Registers the HTTP routes related to loans.
 */
public final class LoanController {

    /**
     * Service used for member operations and checkout.
     */
    private final MemberService memberService;

    /**
     * Service used for loan operations.
     */
    private final LoanService loanService;

    /**
     * Creates the loan controller.
     *
     * @param memberService service used for member operations and checkout.
     * @param loanService service used for loan operations.
     */
    public LoanController(
            MemberService memberService,
            LoanService loanService
    ) {
        this.memberService = memberService;
        this.loanService = loanService;
    }

    /**
     * Registers the loan routes in Javalin.
     *
     * @param config Javalin configuration used to register the routes.
     */
    public void register(JavalinConfig config) {
        config.routes.post("/loans", ctx -> {
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
                    memberService.checkout(memberId, bookId)
            );
        });

        config.routes.get(
                "/loans",
                ctx -> ctx.json(loanService.findAll())
        );

        config.routes.post(
                "/loans/{id}/return",
                ctx -> ctx.json(
                        loanService.returnLoan(
                                Integer.parseInt(ctx.pathParam("id"))
                        )
                )
        );

        config.routes.get(
                "/loans/overdue",
                ctx -> ctx.json(loanService.overdueLoans())
        );
    }
}