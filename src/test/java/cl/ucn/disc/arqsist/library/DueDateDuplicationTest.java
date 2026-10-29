
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that checkout and reservation fulfillment
 * use the same loan period.
 */
class DueDateDuplicationTest {

    /** Service that creates loans through checkout. */
    private LoanService loanService;

    /** Service that creates and fulfills reservations. */
    private ReservationService reservationService;

    /** DAO used to verify inventory changes. */
    private BookDao bookDao;

    /** DAO used to verify reservation state. */
    private ReservationDao reservationDao;

    /** Book used for both lending operations. */
    private Book book;

    /** Member used for both lending operations. */
    private Member member;

    /**
     * Creates an in-memory database and initializes the services.
     *
     * @throws Exception if initialization fails.
     */
    @BeforeEach
    void setUp() throws Exception {

        Database db = new Database("jdbc:sqlite::memory:");

        bookDao = new BookDao(db.connectionSource());

        MemberDao memberDao =
                new MemberDao(db.connectionSource());

        LoanDao loanDao =
                new LoanDao(db.connectionSource());

        reservationDao =
                new ReservationDao(db.connectionSource());

        BookService bookService =
                new BookService(bookDao);

        Clock clock = Clock.fixed(
                Instant.parse("2026-01-15T10:00:00Z"),
                ZoneOffset.UTC
        );

        loanService = new LoanService(
                loanDao,
                memberDao,
                bookService,
                clock
        );

        reservationService = new ReservationService(
                reservationDao,
                memberDao,
                loanDao,
                bookService,
                clock
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
     * Verifies that checkout and fulfillment use the same due date
     * and each decrease the inventory by one copy.
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
                "Checkout and fulfillment must use the same due date"
        );

        assertEquals(
                LocalDate.of(2026, 2, 5),
                fromFulfill.getDueDate(),
                "A loan created on January 15 must be due after 21 days"
        );

        Book updatedBook = bookDao.findById(book.getId());

        assertEquals(
                0,
                updatedBook.getAvailableCopies(),
                "Both lending operations must decrease inventory"
        );

        Reservation updatedReservation =
                reservationDao.findById(reservation.getId());

        assertTrue(
                updatedReservation.isFulfilled(),
                "The reservation must be marked as fulfilled"
        );
    }
}
