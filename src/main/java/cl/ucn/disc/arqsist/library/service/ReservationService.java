
/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Manages library reservations and converts them into loans.
 */
public final class ReservationService {

    /** Data access object for reservations and their transactions. */
    private final ReservationDao reservationDao;

    /** Data access object for member lookup. */
    private final MemberDao memberDao;

    /** Data access object for new loans. */
    private final LoanDao loanDao;

    /** Service responsible for book lookup and inventory changes. */
    private final BookService bookService;

    /** Clock used to produce reproducible dates. */
    private final Clock clock;

    /**
     * Creates the reservation service.
     *
     * @param reservationDao DAO for reservation persistence and transactions.
     * @param memberDao DAO for members.
     * @param loanDao DAO for loans.
     * @param bookService service responsible for books and inventory.
     * @param clock clock used to determine current dates.
     */
    public ReservationService(
            ReservationDao reservationDao,
            MemberDao memberDao,
            LoanDao loanDao,
            BookService bookService,
            Clock clock
    ) {
        this.reservationDao = reservationDao;
        this.memberDao = memberDao;
        this.loanDao = loanDao;
        this.bookService = bookService;
        this.clock = clock;
    }

    /**
     * Creates a reservation after validating its book and member.
     * Reserving a book does not decrease available inventory until fulfillment.
     *
     * @param bookId identifier of the requested book.
     * @param memberId identifier of the member.
     * @return newly created reservation.
     * @throws NotFoundException if the book or member does not exist.
     */
    public Reservation reserve(int bookId, int memberId) {
        Book book = bookService.findById(bookId);

        if (book == null) {
            throw new NotFoundException("Book not found: " + bookId);
        }

        Member member = memberDao.findById(memberId);

        if (member == null) {
            throw new NotFoundException("Member not found: " + memberId);
        }

        Reservation reservation = new Reservation(
                member,
                book,
                LocalDate.now(clock)
        );

        reservationDao.create(reservation);

        return reservation;
    }

    /**
     * Lists every stored reservation.
     *
     * @return stored reservations.
     */
    public List<Reservation> findAll() {
        return reservationDao.findAll();
    }

    /**
     * Fulfills a reservation and creates its loan in one transaction.
     * The inventory decrement, loan creation and reservation update
     * are committed together or all rolled back.
     *
     * @param reservationId identifier of the reservation to fulfill.
     * @return loan created from the reservation.
     * @throws NotFoundException if the reservation does not exist.
     * @throws IllegalStateException if it was already fulfilled or no copies remain.
     * @throws SQLException if the database transaction cannot complete.
     */
    public Loan fulfill(int reservationId) throws SQLException {
        return reservationDao.transaction(() -> {

            Reservation reservation = reservationDao.findById(reservationId);

            if (reservation == null) {
                throw new NotFoundException(
                        "Reservation not found: " + reservationId
                );
            }

            if (reservation.isFulfilled()) {
                throw new IllegalStateException(
                        "Reservation already fulfilled: " + reservationId
                );
            }

            LocalDate today = LocalDate.now(clock);
            Book book = reservation.getBook();

            // Decrease inventory through the BookService.
            bookService.borrow(book.getId());

            // Create the loan using the shared lending policy.
            Loan loan = new Loan(
                    reservation.getMember(),
                    book,
                    today,
                    LoanPolicy.dueDate(today)
            );

            loanDao.create(loan);

            // Mark the reservation as fulfilled.
            reservation.setFulfilled(true);
            reservationDao.update(reservation);

            return loan;
        });
    }
}
