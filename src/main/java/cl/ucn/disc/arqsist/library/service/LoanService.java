
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
 * Manages book loans, returns, overdue fees and inventory transactions.
 */
public final class LoanService {

    /** DAO used to store loans and execute transactions. */
    private final LoanDao loanDao;

    /** DAO used to find library members. */
    private final MemberDao memberDao;

    /** Service responsible for book inventory changes. */
    private final BookService bookService;

    /** Clock used to determine today's date consistently. */
    private final Clock clock;

    /**
     * Creates the loan service.
     *
     * @param loanDao DAO used for loan persistence and transactions.
     * @param memberDao DAO used for member lookup.
     * @param bookService service that owns book inventory.
     * @param clock clock used for loan and return dates.
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
     * Checks out a book and creates a loan in one transaction.
     *
     * @param memberId identifier of the borrowing member.
     * @param bookId identifier of the requested book.
     * @return newly created loan.
     * @throws NotFoundException if the member or book does not exist.
     * @throws IllegalStateException if the book has no available copies.
     * @throws SQLException if the transaction cannot be completed.
     */
    public Loan checkout(int memberId, int bookId) throws SQLException {
        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new NotFoundException("Member not found: " + memberId);
        }

        Book book = bookService.findById(bookId);
        if (book == null) {
            throw new NotFoundException("Book not found: " + bookId);
        }

        LocalDate today = LocalDate.now(clock);
        Loan loan = new Loan(member, book, today, LoanPolicy.dueDate(today));

        return loanDao.transaction(() -> {
            bookService.borrow(bookId);
            loanDao.create(loan);
            return loan;
        });
    }

    /**
     * Lists all loans, computing accrued fees for open overdue loans.
     * The computed fees are not persisted until return.
     *
     * @return loans with their current accrued fees.
     */
    public List<Loan> findAll() {
        return withAccruedFees(loanDao.findAll());
    }

    /**
     * Marks a loan as returned and restores the book inventory atomically.
     * Persists an overdue fee only when the return occurs after the due date.
     *
     * @param loanId identifier of the loan to return.
     * @return returned loan with its final overdue fee.
     * @throws NotFoundException if the loan does not exist.
     * @throws IllegalStateException if the loan was already returned.
     * @throws SQLException if the transaction cannot be completed.
     */
    public Loan returnLoan(int loanId) throws SQLException {
        return loanDao.transaction(() -> {
            Loan loan = loanDao.findById(loanId);

            if (loan == null) {
                throw new NotFoundException("Loan not found: " + loanId);
            }

            if (loan.isReturned()) {
                throw new IllegalStateException("Loan already returned: " + loanId);
            }

            LocalDate today = LocalDate.now(clock);

            if (today.isAfter(loan.getDueDate())) {
                loan.setOverdueFee(accruedFee(loan, today));
            }

            loan.setReturned(true);
            loan.setReturnDate(today);

            loanDao.update(loan);
            bookService.returnCopy(loan.getBook().getId());

            return loan;
        });
    }

    /**
     * Lists loans that have not been returned and whose due dates are past.
     * Includes their calculated, non-persisted accrued fees.
     *
     * @return overdue, open loans with accrued fees.
     */
    public List<Loan> overdueLoans() {
        LocalDate today = LocalDate.now(clock);

        return findAll().stream()
                .filter(loan -> !loan.isReturned())
                .filter(loan -> loan.getDueDate().isBefore(today))
                .toList();
    }

    /**
     * Computes the fee owed for an overdue loan as of the given date.
     * Call only when the due date is earlier than today.
     *
     * @param loan loan whose fee is calculated.
     * @param today reference date.
     * @return accrued fee at the policy's daily rate.
     */
    private double accruedFee(Loan loan, LocalDate today) {
        return ChronoUnit.DAYS.between(loan.getDueDate(), today)
                * LoanPolicy.FEE_PER_DAY;
    }

    /**
     * Adds current fees to the in-memory representation of open overdue loans.
     * Does not update stored loan records.
     *
     * @param loans loans to inspect.
     * @return the loans with calculated fees for overdue, open loans.
     */
    private List<Loan> withAccruedFees(List<Loan> loans) {
        LocalDate today = LocalDate.now(clock);

        for (Loan loan : loans) {
            if (!loan.isReturned() && loan.getDueDate().isBefore(today)) {
                loan.setOverdueFee(accruedFee(loan, today));
            }
        }

        return loans;
    }
}
