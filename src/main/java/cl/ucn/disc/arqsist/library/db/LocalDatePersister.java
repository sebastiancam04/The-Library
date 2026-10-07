/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.db;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Persists a {@link LocalDate} as an ISO-8601 string.
 */
public final class LocalDatePersister extends BaseDataType {

    /**
     * Singleton instance of the persister.
     */
    private static final LocalDatePersister SINGLETON =
            new LocalDatePersister();

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance.
     */
    public static LocalDatePersister getSingleton() {
        return SINGLETON;
    }

    /**
     * Creates the LocalDate persister.
     */
    private LocalDatePersister() {
        super(SqlType.STRING, new Class<?>[]{LocalDate.class});
    }

    /**
     * Converts the default database value to its SQL argument representation.
     *
     * @param fieldType information about the mapped field.
     * @param defaultStr default value represented as a string.
     * @return the default string value.
     */
    @Override
    public Object parseDefaultString(
            FieldType fieldType,
            String defaultStr
    ) {
        return defaultStr;
    }

    /**
     * Reads the SQL value from the database result.
     *
     * @param fieldType information about the mapped field.
     * @param results database query results.
     * @param columnPos position of the column in the result.
     * @return the SQL argument as a string.
     * @throws SQLException if the database value cannot be read.
     */
    @Override
    public Object resultToSqlArg(
            FieldType fieldType,
            DatabaseResults results,
            int columnPos
    ) throws SQLException {
        return results.getString(columnPos);
    }

    /**
     * Converts a stored SQL argument into a {@link LocalDate}.
     *
     * @param fieldType information about the mapped field.
     * @param sqlArg SQL argument containing the date string.
     * @param columnPos position of the column in the result.
     * @return the parsed LocalDate.
     */
    @Override
    public Object sqlArgToJava(
            FieldType fieldType,
            Object sqlArg,
            int columnPos
    ) {
        return LocalDate.parse((String) sqlArg);
    }

    /**
     * Converts a {@link LocalDate} into its SQL string representation.
     *
     * @param fieldType information about the mapped field.
     * @param javaObject LocalDate value to persist.
     * @return the ISO-8601 string representation of the date.
     */
    @Override
    public Object javaToSqlArg(
            FieldType fieldType,
            Object javaObject
    ) {
        return javaObject.toString();
    }
}