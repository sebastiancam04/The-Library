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
import cl.ucn.disc.arqsist.library.service.MemberService;
import cl.ucn.disc.arqsist.library.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that checkout and reservation fulfillment use
 * the same loan period.
 */
class DueDateDuplicationTest {

    /**
     * Service used to create loans through checkout.
     */
    private MemberService memberService;

    /**
     * Service used to create and fulfill reservations.
     */
    private ReservationService reservationService;

    /**
     * Book used by the test.
     */
    private Book book;

    /**
     * Member used by the test.
     */
    private Member member;

    /**
     * Creates an in-memory database and initializes the
     * services and entities required by the test.
     *
     * @throws Exception if the test environment cannot be initialized.
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

        memberService =
                new MemberService(
                        memberDao,
                        bookDao,
                        loanDao
                );

        reservationService =
                new ReservationService(
                        reservationDao,
                        bookDao,
                        memberDao,
                        loanDao
                );

        book = new Book(
                "Design Patterns",
                "Gamma et al.",
                "9780201633610",
                1
        );

        bookDao.create(book);

        member = new Member(
                "Grace Hopper",
                "grace@example.com"
        );

        memberDao.create(member);
    }

    /**
     * Verifies that a loan created through checkout and a loan
     * created by fulfilling a reservation have the same due date.
     *
     * @throws Exception if the test operations cannot be completed.
     */
    @Test
    void checkoutAndFulfillUseTheSameLoanPeriod() throws Exception {
        Loan fromCheckout =
                memberService.checkout(
                        member.getId(),
                        book.getId()
                );

        Reservation reservation =
                reservationService.reserve(
                        book.getId(),
                        member.getId()
                );

        Loan fromFulfill =
                reservationService.fulfill(
                        reservation.getId()
                );

        assertEquals(
                fromCheckout.getDueDate(),
                fromFulfill.getDueDate(),
                "the same kind of loan should have the same due date"
        );
    }
}