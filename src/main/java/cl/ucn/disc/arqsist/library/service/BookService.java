/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.model.Book;

import java.util.List;

/**
 * Provides operations related to books and their inventory.
 */
public final class BookService {

    /**
     * DAO used to access books.
     */
    private final BookDao dao;

    /**
     * Creates the book service.
     *
     * @param dao DAO used to access books.
     */
    public BookService(BookDao dao) {
        this.dao = dao;
    }

    /**
     * Returns all books stored in the library.
     *
     * @return all stored books.
     */
    public List<Book> listAll() {
        return dao.findAll();
    }

    /**
     * Finds a book by its identifier.
     *
     * @param id identifier of the book.
     * @return the book, or null if it does not exist.
     */
    public Book findById(int id) {
        return dao.findById(id);
    }

    /**
     * Creates a new book.
     *
     * <p>The number of available copies is initialized with the
     * total number of copies of the book.</p>
     *
     * @param book book to create.
     * @return the created book.
     */
    public Book create(Book book) {
        book.setAvailableCopies(book.getTotalCopies());
        dao.create(book);
        return book;
    }

    /**
     * Borrows one available copy of a book.
     *
     * @param bookId identifier of the book.
     * @throws NotFoundException if the book does not exist.
     * @throws IllegalStateException if the book has no available copies.
     */
    public void borrow(int bookId) {
        Book book = dao.findById(bookId);

        if (book == null) {
            throw new NotFoundException(
                    "Book not found: " + bookId
            );
        }

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException(
                    "No available copies of book " + bookId
            );
        }

        book.setAvailableCopies(
                book.getAvailableCopies() - 1
        );

        dao.update(book);
    }

    /**
     * Returns one copy of a book to the inventory.
     *
     * @param bookId identifier of the book.
     * @throws NotFoundException if the book does not exist.
     */
    public void returnCopy(int bookId) {
        Book book = dao.findById(bookId);

        if (book == null) {
            throw new NotFoundException(
                    "Book not found: " + bookId
            );
        }

        book.setAvailableCopies(
                book.getAvailableCopies() + 1
        );

        dao.update(book);
    }
}