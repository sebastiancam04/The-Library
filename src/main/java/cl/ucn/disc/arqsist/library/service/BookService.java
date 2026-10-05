/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.model.Book;

import java.util.List;

public final class BookService {

    private final BookDao dao;

    public BookService(BookDao dao) {
        this.dao = dao;
    }

    public List<Book> listAll() {
        return dao.findAll();
    }

    public Book findById(int id) {
        return dao.findById(id);
    }

    public Book create(Book book) {
        book.setAvailableCopies(book.getTotalCopies());
        dao.create(book);
        return book;
    }

    public void borrow(int bookId) {
        Book book = dao.findById(bookId);

        if (book == null) {
            throw new NotFoundException("Book not found: " + bookId);
        }

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException(
                    "No available copies of book " + bookId
            );
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        dao.update(book);
    }

    public void returnCopy(int bookId) {
        Book book = dao.findById(bookId);

        if (book == null) {
            throw new NotFoundException("Book not found: " + bookId);
        }

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        dao.update(book);
    }
}