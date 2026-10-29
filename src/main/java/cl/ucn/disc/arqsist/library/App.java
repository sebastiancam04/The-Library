/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.controller.BookController;
import cl.ucn.disc.arqsist.library.controller.LoanController;
import cl.ucn.disc.arqsist.library.controller.MemberController;
import cl.ucn.disc.arqsist.library.controller.ReservationController;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;

import cl.ucn.disc.arqsist.library.db.Database;

import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanService;
import cl.ucn.disc.arqsist.library.service.MemberService;
import cl.ucn.disc.arqsist.library.service.ReservationService;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Entry point of the library application.
 */
public final class App {

    /**
     * Starts the library application and configures its dependencies.
     *
     * @param args command-line arguments.
     * @throws Exception if the application cannot initialize.
     */
    public static void main(String[] args) throws Exception {

        Database db = new Database("jdbc:sqlite:database.sqlite");
        db.seedIfEmpty();

        BookDao bookDao =
                new BookDao(db.connectionSource());

        MemberDao memberDao =
                new MemberDao(db.connectionSource());

        LoanDao loanDao =
                new LoanDao(db.connectionSource());

        ReservationDao reservationDao =
                new ReservationDao(db.connectionSource());

        BookService bookService =
                new BookService(bookDao);

        MemberService memberService =
                new MemberService(memberDao);

        Clock clock = Clock.system(
                ZoneId.of("America/Santiago")
        );

        LoanService loanService =
                new LoanService(
                        loanDao,
                        memberDao,
                        bookService,
                        clock
                );

        ReservationService reservationService =
                new ReservationService(
                        reservationDao,
                        bookDao,
                        memberDao,
                        loanDao
                );

        BookController bookController =
                new BookController(
                        bookService,
                        bookDao
                );

        MemberController memberController =
                new MemberController(memberService);

        LoanController loanController =
                new LoanController(loanService);

        ReservationController reservationController =
                new ReservationController(reservationService);

        Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });

            bookController.register(config);
            memberController.register(config);
            loanController.register(config);
            reservationController.register(config);

        }).start(7070);
    }
}