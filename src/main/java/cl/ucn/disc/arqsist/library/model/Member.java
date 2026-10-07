/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents a member registered in the library.
 */
@DatabaseTable(tableName = "members")
public final class Member {

    /**
     * Unique identifier of the member.
     */
    @DatabaseField(generatedId = true)
    private int id;

    /**
     * Full name of the member.
     */
    @DatabaseField(canBeNull = false)
    private String name;

    /**
     * Email address of the member.
     */
    @DatabaseField(canBeNull = false)
    private String email;

    /**
     * Empty constructor required by ORMLite.
     */
    public Member() {
    }

    /**
     * Creates a new library member.
     *
     * @param name full name of the member.
     * @param email email address of the member.
     */
    public Member(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /**
     * Returns the member identifier.
     *
     * @return the member identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the member identifier.
     *
     * @param id identifier of the member.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the member name.
     *
     * @return the member name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the member name.
     *
     * @param name name of the member.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the member email address.
     *
     * @return the member email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the member email address.
     *
     * @param email email address of the member.
     */
    public void setEmail(String email) {
        this.email = email;
    }
}