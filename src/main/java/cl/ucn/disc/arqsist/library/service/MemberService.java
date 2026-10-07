/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Provides operations related to library members.
 */
public final class MemberService {

    /**
     * DAO used to access members.
     */
    private final MemberDao memberDao;

    /**
     * DAO used to access books.
     */
    private final BookDao bookDao;

    /**
     * DAO used to access loans.
     */
    private final LoanDao loanDao;

    /**
     * Creates the member service.
     *
     * @param memberDao DAO used to access members.
     * @param bookDao DAO used to access books.
     * @param loanDao DAO used to access loans.
     */
    public MemberService(
            MemberDao memberDao,
            BookDao bookDao,
            LoanDao loanDao
    ) {
        this.memberDao = memberDao;
        this.bookDao = bookDao;
        this.loanDao = loanDao;
    }

    /**
     * Registers a new member.
     *
     * @param member the member to register.
     * @return the registered member.
     * @throws SQLException if the operation cannot be completed.
     */
    public Member register(Member member) throws SQLException {
        memberDao.create(member);
        return member;
    }

    /**
     * Returns all registered members.
     *
     * @return the registered members.
     * @throws SQLException if the operation cannot be completed.
     */
    public List<Member> findAll() throws SQLException {
        return memberDao.findAll();
    }

    /**
     * Creates a loan for a member and a book.
     *
     * @param memberId identifier of the member.
     * @param bookId identifier of the book.
     * @return the created loan.
     * @throws SQLException if the operation cannot be completed.
     */
    public Loan checkout(int memberId, int bookId) throws SQLException {
        Member member = memberDao.findById(memberId);
        Book book = bookDao.findById(bookId);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookDao.update(book);

        LocalDate today = LocalDate.now();
        LocalDate dueDate = LoanPolicy.dueDate(today);

        Loan loan = new Loan(
                member,
                book,
                today,
                dueDate
        );

        loanDao.create(loan);

        return loan;
    }
}