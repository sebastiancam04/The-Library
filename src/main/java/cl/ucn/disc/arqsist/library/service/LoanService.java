/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Provides operations related to library loans.
 */
public final class LoanService {

    /**
     * DAO used to access loans.
     */
    private final LoanDao loanDao;

    /**
     * DAO used to access books.
     */
    private final BookDao bookDao;

    /**
     * Creates the loan service.
     *
     * @param loanDao DAO used to access loans.
     * @param bookDao DAO used to access books.
     */
    public LoanService(
            LoanDao loanDao,
            BookDao bookDao
    ) {
        this.loanDao = loanDao;
        this.bookDao = bookDao;
    }

    /**
     * Returns all loans stored in the library.
     *
     * @return all stored loans.
     * @throws SQLException if the operation cannot be completed.
     */
    public List<Loan> findAll() throws SQLException {
        return loanDao.findAll();
    }

    /**
     * Marks a loan as returned and restores one available
     * copy of the associated book.
     *
     * @param loanId identifier of the loan.
     * @return the updated loan, or null if the loan does not exist
     *         or has already been returned.
     * @throws SQLException if the operation cannot be completed.
     */
    public Loan returnLoan(int loanId) throws SQLException {
        Loan loan = loanDao.findById(loanId);

        if (loan == null || loan.isReturned()) {
            return loan;
        }

        loan.setReturned(true);
        loan.setReturnDate(LocalDate.now());

        LocalDate due = loan.getDueDate();
        LocalDate today = LocalDate.now();

        // Calculate overdue fee if the book is returned after the due date.
        if (today.isAfter(loan.getDueDate())) {
            long daysOverdue =
                    ChronoUnit.DAYS.between(due, today);

            loan.setOverdueFee(daysOverdue * 1.0);
        }

        loanDao.update(loan);

        Book book = loan.getBook();

        book.setAvailableCopies(
                book.getAvailableCopies() + 1
        );

        bookDao.update(book);

        return loan;
    }

    /**
     * Finds all active loans whose due date has passed.
     *
     * @return all overdue loans.
     * @throws SQLException if the operation cannot be completed.
     */
    public List<Loan> overdueLoans() throws SQLException {
        LocalDate today = LocalDate.now();

        return loanDao.findAll()
                .stream()
                .filter(loan -> !loan.isReturned())
                .filter(
                        loan ->
                                loan.getDueDate()
                                        .isBefore(today)
                )
                .toList();
    }
}