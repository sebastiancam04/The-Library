/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Coordinates book loans and their changes to the inventory.
 */
public final class LoanService {

    /**
     * DAO responsible for loans and their transactions.
     */
    private final LoanDao loanDao;

    /**
     * DAO used to look up members.
     */
    private final MemberDao memberDao;

    /**
     * Service that owns inventory updates.
     */
    private final BookService bookService;

    /**
     * Clock injected to make date-dependent operations testable.
     */
    private final Clock clock;

    /**
     * Creates the loan service.
     *
     * @param loanDao DAO for loan records and transactions.
     * @param memberDao DAO for member lookup.
     * @param bookService service responsible for book inventory.
     * @param clock clock used to determine the current date.
     */
    public LoanService(
            LoanDao loanDao,
            MemberDao memberDao,
            BookService bookService,
            Clock clock
    ) {
        this.loanDao = loanDao;
        this.memberDao = memberDao;
        this.bookService = bookService;
        this.clock = clock;
    }

    /**
     * Creates a loan and decreases inventory atomically.
     *
     * @param memberId identifier of the member borrowing the book.
     * @param bookId identifier of the requested book.
     * @return newly created loan.
     * @throws NotFoundException if the member or book does not exist.
     * @throws IllegalStateException if no copies are available.
     * @throws SQLException if the transaction cannot be completed.
     */
    public Loan checkout(int memberId, int bookId) throws SQLException {
        Member member = memberDao.findById(memberId);

        if (member == null) {
            throw new NotFoundException(
                    "Member not found: " + memberId
            );
        }

        Book book = bookService.findById(bookId);

        if (book == null) {
            throw new NotFoundException(
                    "Book not found: " + bookId
            );
        }

        LocalDate today = LocalDate.now(clock);

        Loan loan = new Loan(
                member,
                book,
                today,
                LoanPolicy.dueDate(today)
        );

        return loanDao.transaction(() -> {
            bookService.borrow(bookId);
            loanDao.create(loan);
            return loan;
        });
    }

    /**
     * Returns all stored loans.
     *
     * @return all loan records.
     */
    public List<Loan> findAll() {
        return loanDao.findAll();
    }

    /**
     * Marks a loan as returned, computes its fee, and restores inventory
     * in the same transaction.
     *
     * @param loanId identifier of the loan to return.
     * @return returned loan, or the unchanged result if already returned;
     *         null if the identifier does not exist.
     * @throws SQLException if the transaction cannot be completed.
     */
    public Loan returnLoan(int loanId) throws SQLException {
        Loan loan = loanDao.findById(loanId);

        if (loan == null || loan.isReturned()) {
            return loan;
        }

        LocalDate today = LocalDate.now(clock);

        if (today.isAfter(loan.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(
                    loan.getDueDate(),
                    today
            );

            loan.setOverdueFee(
                    daysOverdue * LoanPolicy.FEE_PER_DAY
            );
        }

        return loanDao.transaction(() -> {
            loan.setReturned(true);
            loan.setReturnDate(today);

            loanDao.update(loan);
            bookService.returnCopy(loan.getBook().getId());

            return loan;
        });
    }

    /**
     * Finds active loans with due dates before today.
     *
     * @return overdue, not-yet-returned loans.
     */
    public List<Loan> overdueLoans() {
        LocalDate today = LocalDate.now(clock);

        return loanDao.findAll()
                .stream()
                .filter(loan -> !loan.isReturned())
                .filter(loan -> loan.getDueDate().isBefore(today))
                .toList();
    }
}