/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;

import cl.ucn.disc.arqsist.library.db.Database;

import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;

import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanService;
import cl.ucn.disc.arqsist.library.service.ReservationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that checkout and reservation fulfillment use the same loan period.
 */
class DueDateDuplicationTest {

    /**
     * Service that creates loans through checkout.
     */
    private LoanService loanService;

    /**
     * Service that fulfills reservations.
     */
    private ReservationService reservationService;

    /**
     * Book used for both flows.
     */
    private Book book;

    /**
     * Member used for both flows.
     */
    private Member member;

    /**
     * Creates an in-memory database with the required services.
     *
     * @throws Exception if initialization fails.
     */
    @BeforeEach
    void setUp() throws Exception {

        Database db = new Database("jdbc:sqlite::memory:");

        BookDao bookDao =
                new BookDao(db.connectionSource());

        MemberDao memberDao =
                new MemberDao(db.connectionSource());

        LoanDao loanDao =
                new LoanDao(db.connectionSource());

        ReservationDao reservationDao =
                new ReservationDao(db.connectionSource());

        loanService = new LoanService(
                loanDao,
                memberDao,
                new BookService(bookDao),
                Clock.systemDefaultZone()
        );

        reservationService = new ReservationService(
                reservationDao,
                bookDao,
                memberDao,
                loanDao
        );

        book = new Book(
                "Design Patterns",
                "Gamma et al.",
                "9780201633610",
                2
        );

        bookDao.create(book);

        member = new Member(
                "Grace Hopper",
                "grace@example.com"
        );

        memberDao.create(member);
    }

    /**
     * Verifies the due-date policy is consistent for both lending paths.
     *
     * @throws Exception if an operation fails.
     */
    @Test
    void checkoutAndFulfillUseTheSameLoanPeriod() throws Exception {

        Loan fromCheckout = loanService.checkout(
                member.getId(),
                book.getId()
        );

        Reservation reservation = reservationService.reserve(
                book.getId(),
                member.getId()
        );

        Loan fromFulfill = reservationService.fulfill(
                reservation.getId()
        );

        assertEquals(
                fromCheckout.getDueDate(),
                fromFulfill.getDueDate(),
                "the same kind of loan should have the same due date"
        );
    }
}