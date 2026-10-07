/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that a failed checkout does not leave
 * partial changes in the database.
 *
 * <p>This test exposes the missing transaction problem
 * that is fixed during Stage 2.</p>
 */
class TransactionBugTest {

    /**
     * DAO used to create and reload books during the test.
     */
    private BookDao bookDao;

    /**
     * Service used to perform the checkout operation.
     */
    private MemberService memberService;

    /**
     * Creates an in-memory database and initializes the
     * dependencies required by the test.
     *
     * @throws Exception if the test environment cannot be initialized.
     */
    @BeforeEach
    void setUp() throws Exception {
        Database db =
                new Database("jdbc:sqlite::memory:");

        bookDao =
                new BookDao(db.connectionSource());

        MemberDao memberDao =
                new MemberDao(db.connectionSource());

        LoanDao loanDao =
                new LoanDao(db.connectionSource());

        memberService =
                new MemberService(
                        memberDao,
                        bookDao,
                        loanDao
                );
    }

    /**
     * Verifies that a failed checkout does not leave the
     * available copy count modified.
     *
     * <p>The checkout intentionally uses a member identifier
     * that does not exist.</p>
     *
     * @throws Exception if the test operations cannot be completed.
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
                Exception.class,
                () -> memberService.checkout(
                        9999,
                        book.getId()
                )
        );

        Book reloaded =
                bookDao.findById(book.getId());

        assertEquals(
                reloaded.getTotalCopies(),
                reloaded.getAvailableCopies(),
                "availableCopies was decremented even though the loan was never created"
        );
    }
}