
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
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the initial library data, book inventory, and seed idempotency.
 */
class DatabaseSeedTest {

    /**
     * Confirms that the seed creates the expected records, accounts for
     * outstanding loans in inventory, and does not duplicate records.
     *
     * @throws Exception if seed creation or database access fails.
     */
    @Test
    void seedCreatesConsistentDataOnlyOnce() throws Exception {

        Database db = new Database("jdbc:sqlite::memory:");

        db.seedIfEmpty();

        BookDao bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());
        ReservationDao reservationDao =
                new ReservationDao(db.connectionSource());

        List<Loan> loans = loanDao.findAll();

        assertEquals(
                3,
                memberDao.findAll().size(),
                "The seed must create three members"
        );

        assertEquals(
                3,
                loans.size(),
                "The seed must create three loans"
        );

        assertEquals(
                1,
                reservationDao.findAll().size(),
                "The seed must create one reservation"
        );

        LocalDate today = LocalDate.now();

        long overdueCount = loans.stream()
                .filter(loan -> !loan.isReturned())
                .filter(loan -> loan.getDueDate().isBefore(today))
                .count();

        assertEquals(
                1L,
                overdueCount,
                "Exactly one seeded loan must be overdue"
        );

        long returnedCount = loans.stream()
                .filter(Loan::isReturned)
                .count();

        assertEquals(
                1L,
                returnedCount,
                "Exactly one loan must already be returned"
        );

        Map<Integer, Long> openLoansByBook = loans.stream()
                .filter(loan -> !loan.isReturned())
                .collect(Collectors.groupingBy(
                        loan -> loan.getBook().getId(),
                        Collectors.counting()
                ));

        for (Book book : bookDao.findAll()) {

            long openLoans = openLoansByBook.getOrDefault(
                    book.getId(),
                    0L
            );

            assertEquals(
                    book.getTotalCopies() - openLoans,
                    (long) book.getAvailableCopies(),
                    "Available copies must reflect open loans for book "
                            + book.getId()
            );

            if (openLoans > 0) {
                assertTrue(
                        book.getAvailableCopies() < book.getTotalCopies(),
                        "A book with an open loan must have fewer available copies"
                );
            }
        }

        int originalLoanCount = loans.size();
        int originalReservationCount =
                reservationDao.findAll().size();

        db.seedIfEmpty();

        assertEquals(
                originalLoanCount,
                loanDao.findAll().size(),
                "Calling seed twice must not duplicate loans"
        );

        assertEquals(
                originalReservationCount,
                reservationDao.findAll().size(),
                "Calling seed twice must not duplicate reservations"
        );
    }
}
