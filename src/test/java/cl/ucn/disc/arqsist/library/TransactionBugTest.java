/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;

import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;

import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanService;
import cl.ucn.disc.arqsist.library.service.NotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks that a failed checkout does not corrupt the book inventory.
 */
class TransactionBugTest {

    /**
     * DAO for creating and reloading books.
     */
    private BookDao bookDao;

    /**
     * Service that performs checkout transactions.
     */
    private LoanService loanService;

    /**
     * Initializes the database and its services.
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

        loanService = new LoanService(
                loanDao,
                memberDao,
                new BookService(bookDao),
                Clock.systemDefaultZone()
        );
    }

    /**
     * Verifies a missing member cannot cause inventory to decrease.
     *
     * @throws Exception if the test operation cannot be completed.
     */
    @Test
    void checkoutLeavesNoPartialStateOnFailure() throws Exception {

        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2
        );

        bookDao.create(book);

        assertThrows(
                NotFoundException.class,
                () -> loanService.checkout(
                        9999,
                        book.getId()
                )
        );

        Book reloaded = bookDao.findById(book.getId());

        assertEquals(
                reloaded.getTotalCopies(),
                reloaded.getAvailableCopies(),
                "availableCopies was decremented even though the loan was never created"
        );
    }
}