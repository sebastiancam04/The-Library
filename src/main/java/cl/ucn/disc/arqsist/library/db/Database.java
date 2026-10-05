/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.db;

import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Manages the database connection, table creation and initial seed data.
 */
public final class Database {

    /**
     * Logger for database operations.
     */
    private static final Logger log = LoggerFactory.getLogger(Database.class);

    /**
     * Connection source used by ORMLite.
     */
    private final ConnectionSource connectionSource;

    /**
     * Creates the database connection and required tables.
     *
     * @param jdbcUrl JDBC URL used to connect to the database.
     * @throws SQLException if the connection or table creation fails.
     */
    public Database(String jdbcUrl) throws SQLException {
        this.connectionSource = new JdbcConnectionSource(jdbcUrl);

        TableUtils.createTableIfNotExists(connectionSource, Book.class);
        TableUtils.createTableIfNotExists(connectionSource, Member.class);
        TableUtils.createTableIfNotExists(connectionSource, Loan.class);
        TableUtils.createTableIfNotExists(connectionSource, Reservation.class);
    }

    /**
     * Returns the database connection source.
     *
     * @return the current connection source.
     */
    public ConnectionSource connectionSource() {
        return connectionSource;
    }

    /**
     * Seeds the database with initial data when the corresponding tables
     * are empty.
     *
     * @throws SQLException if a database operation fails.
     */
    public void seedIfEmpty() throws SQLException {

        Dao<Book, Integer> bookDao =
                DaoManager.createDao(connectionSource, Book.class);

        Dao<Member, Integer> memberDao =
                DaoManager.createDao(connectionSource, Member.class);

        Dao<Loan, Integer> loanDao =
                DaoManager.createDao(connectionSource, Loan.class);

        Dao<Reservation, Integer> reservationDao =
                DaoManager.createDao(connectionSource, Reservation.class);

        /*
         * Books
         */
        if (bookDao.queryForAll().isEmpty()) {
            log.debug("Seeding books");

            bookDao.create(
                    new Book(
                            "Clean Code",
                            "Robert C. Martin",
                            "9780132350884",
                            3
                    )
            );

            bookDao.create(
                    new Book(
                            "The Pragmatic Programmer",
                            "Hunt & Thomas",
                            "9780201616224",
                            2
                    )
            );

            bookDao.create(
                    new Book(
                            "Design Patterns",
                            "Gamma et al.",
                            "9780201633610",
                            4
                    )
            );
        }

        /*
         * Members
         */
        if (memberDao.queryForAll().isEmpty()) {
            log.debug("Seeding members");

            memberDao.create(
                    new Member("Ada Lovelace", "ada@example.com")
            );

            memberDao.create(
                    new Member("Grace Hopper", "grace@example.com")
            );

            memberDao.create(
                    new Member("Alan Turing", "alan@example.com")
            );
        }

        /*
         * Loans
         */
        if (loanDao.queryForAll().isEmpty()) {
            log.debug("Seeding loans");

            List<Book> books = bookDao.queryForAll();
            List<Member> members = memberDao.queryForAll();

            LocalDate today = LocalDate.now();

            /*
             * Returned loan.
             *
             * It does NOT decrease availableCopies because the book
             * has already been returned.
             */
            Loan returned = new Loan(
                    members.getFirst(),
                    books.getFirst(),
                    today.minusDays(30),
                    today.minusDays(9)
            );

            returned.setReturned(true);
            returned.setReturnDate(today.minusDays(10));

            loanDao.create(returned);

            /*
             * Active loan.
             *
             * The due date is still in the future, therefore the loan
             * is active and one available copy must be removed.
             */
            Loan active = new Loan(
                    members.get(1),
                    books.get(2),
                    today.minusDays(5),
                    today.plusDays(16)
            );

            loanDao.create(active);

            Book activeBook = books.get(2);
            activeBook.setAvailableCopies(
                    activeBook.getAvailableCopies() - 1
            );
            bookDao.update(activeBook);

            /*
             * Overdue loan.
             *
             * The due date is in the past and the book has not been
             * returned, so one available copy must also be removed.
             */
            Loan overdue = new Loan(
                    members.get(2),
                    books.get(1),
                    today.minusDays(30),
                    today.minusDays(9)
            );

            loanDao.create(overdue);

            Book overdueBook = books.get(1);
            overdueBook.setAvailableCopies(
                    overdueBook.getAvailableCopies() - 1
            );
            bookDao.update(overdueBook);
        }

        /*
         * Reservations
         */
        if (reservationDao.queryForAll().isEmpty()) {
            log.debug("Seeding reservations");

            List<Book> books = bookDao.queryForAll();
            List<Member> members = memberDao.queryForAll();

            LocalDate today = LocalDate.now();

            reservationDao.create(
                    new Reservation(
                            members.getFirst(),
                            books.get(1),
                            today.minusDays(1)
                    )
            );
        }

        log.debug("Database seed completed");
    }
}