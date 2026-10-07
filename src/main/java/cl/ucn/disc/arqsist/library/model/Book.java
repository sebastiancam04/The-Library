/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents a book in the library catalog.
 */
@DatabaseTable(tableName = "books")
public final class Book {

    /**
     * Unique identifier of the book.
     */
    @DatabaseField(generatedId = true)
    private int id;

    /**
     * Title of the book.
     */
    @DatabaseField(canBeNull = false)
    private String title;

    /**
     * Author of the book.
     */
    @DatabaseField(canBeNull = false)
    private String author;

    /**
     * ISBN of the book.
     */
    @DatabaseField(canBeNull = false)
    private String isbn;

    /**
     * Total number of copies owned by the library.
     */
    @DatabaseField(canBeNull = false)
    private int totalCopies;

    /**
     * Number of copies currently available for borrowing.
     */
    @DatabaseField(canBeNull = false)
    private int availableCopies;

    /**
     * Empty constructor required by ORMLite.
     */
    public Book() {
    }

    /**
     * Creates a new book.
     *
     * @param title title of the book.
     * @param author author of the book.
     * @param isbn ISBN of the book.
     * @param totalCopies total number of copies.
     */
    public Book(
            String title,
            String author,
            String isbn,
            int totalCopies
    ) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    /**
     * Returns the book identifier.
     *
     * @return the book identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the book identifier.
     *
     * @param id identifier of the book.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the title of the book.
     *
     * @return the book title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the book.
     *
     * @param title title of the book.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the author of the book.
     *
     * @return the book author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of the book.
     *
     * @param author author of the book.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Returns the ISBN of the book.
     *
     * @return the book ISBN.
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Sets the ISBN of the book.
     *
     * @param isbn ISBN of the book.
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * Returns the total number of copies.
     *
     * @return total number of copies.
     */
    public int getTotalCopies() {
        return totalCopies;
    }

    /**
     * Sets the total number of copies.
     *
     * @param totalCopies total number of copies.
     */
    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    /**
     * Returns the number of currently available copies.
     *
     * @return number of available copies.
     */
    public int getAvailableCopies() {
        return availableCopies;
    }

    /**
     * Sets the number of currently available copies.
     *
     * @param availableCopies number of available copies.
     */
    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }
}