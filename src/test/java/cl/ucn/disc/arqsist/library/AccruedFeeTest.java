
/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that open overdue loans show their accrued fees without
 * persisting those fees before the book is returned.
 */
class AccruedFeeTest {

    /**
     * Verifies 24 days of accrued fees for an overdue loan and zero
     * fees for an active loan, using a fixed clock.
     *
     * @throws Exception if the test database cannot be accessed.
     */
    @Test
    void findAllComputesAccruedFeesWithoutPersistingThem() throws Exception {

        Database db = new Database("jdbc:sqlite::memory:");

        BookDao bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());

        Book book = new Book(
                "Test Book",
                "Test Author",
                "9780000000001",
                2
        );

        bookDao.create(book);

        Member member = new Member(
                "Test Member",
                "member@example.com"
        );

        memberDao.create(member);

        Loan overdueLoan = new Loan(
                member,
                book,
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 22)
        );

        loanDao.create(overdueLoan);

        Loan activeLoan = new Loan(
                member,
                book,
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 31)
        );

        loanDao.create(activeLoan);

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-01-15T10:00:00Z"),
                ZoneOffset.UTC
        );

        LoanService loanService = new LoanService(
                loanDao,
                memberDao,
                new BookService(bookDao),
                fixedClock
        );

        List<Loan> loans = loanService.findAll();

        assertEquals(2, loans.size());

        Loan overdueResult = loans.stream()
                .filter(loan -> loan.getId() == overdueLoan.getId())
                .findFirst()
                .orElseThrow();

        Loan activeResult = loans.stream()
                .filter(loan -> loan.getId() == activeLoan.getId())
                .findFirst()
                .orElseThrow();

        assertEquals(
                24.0,
                overdueResult.getOverdueFee(),
                0.001,
                "The overdue loan must accrue 24 days of fees"
        );

        assertEquals(
                0.0,
                activeResult.getOverdueFee(),
                0.001,
                "The active loan must not accrue fees"
        );

        assertEquals(
                0.0,
                loanDao.findById(overdueLoan.getId()).getOverdueFee(),
                0.001,
                "Accrued fees must not be persisted before return"
        );
    }
}
