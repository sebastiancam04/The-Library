/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Provides operations related to book reservations.
 */
public final class ReservationService {

    /**
     * DAO used to access reservations.
     */
    private final ReservationDao reservationDao;

    /**
     * DAO used to access books.
     */
    private final BookDao bookDao;

    /**
     * DAO used to access members.
     */
    private final MemberDao memberDao;

    /**
     * DAO used to access loans.
     */
    private final LoanDao loanDao;

    /**
     * Creates the reservation service.
     *
     * @param reservationDao DAO used to access reservations.
     * @param bookDao DAO used to access books.
     * @param memberDao DAO used to access members.
     * @param loanDao DAO used to access loans.
     */
    public ReservationService(
            ReservationDao reservationDao,
            BookDao bookDao,
            MemberDao memberDao,
            LoanDao loanDao
    ) {
        this.reservationDao = reservationDao;
        this.bookDao = bookDao;
        this.memberDao = memberDao;
        this.loanDao = loanDao;
    }

    /**
     * Creates a reservation for a book and a member.
     *
     * @param bookId identifier of the book.
     * @param memberId identifier of the member.
     * @return the created reservation.
     * @throws SQLException if the operation cannot be completed.
     */
    public Reservation reserve(
            int bookId,
            int memberId
    ) throws SQLException {

        Book book = bookDao.findById(bookId);
        Member member = memberDao.findById(memberId);

        Reservation reservation = new Reservation(
                member,
                book,
                LocalDate.now()
        );

        reservationDao.create(reservation);

        return reservation;
    }

    /**
     * Returns all reservations.
     *
     * @return all stored reservations.
     * @throws SQLException if the operation cannot be completed.
     */
    public List<Reservation> findAll() throws SQLException {
        return reservationDao.findAll();
    }

    /**
     * Fulfills an existing reservation and creates its loan.
     *
     * @param reservationId identifier of the reservation.
     * @return the loan created from the reservation.
     * @throws SQLException if the operation cannot be completed.
     * @throws IllegalStateException if the reservation does not exist
     *                               or has already been fulfilled.
     */
    public Loan fulfill(int reservationId) throws SQLException {

        Reservation reservation =
                reservationDao.findById(reservationId);

        if (reservation == null || reservation.isFulfilled()) {
            throw new IllegalStateException(
                    "Reservation not available"
            );
        }

        reservation.setFulfilled(true);
        reservationDao.update(reservation);

        LocalDate today = LocalDate.now();
        LocalDate dueDate = LoanPolicy.dueDate(today);

        Loan loan = new Loan(
                reservation.getMember(),
                reservation.getBook(),
                today,
                dueDate
        );

        loanDao.create(loan);

        return loan;
    }
}