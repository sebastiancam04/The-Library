
/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.dao;

import cl.ucn.disc.arqsist.library.model.Member;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access object for members.
 */
public final class MemberDao extends BaseDao<Member> {

    /**
     * Creates the member DAO.
     *
     * @param connectionSource the database connection source.
     */
    public MemberDao(ConnectionSource connectionSource) {
        super(connectionSource, Member.class);
    }

    /**
     * Finds members whose names contain a literal text fragment.
     * Matching is case-insensitive for ASCII letters.
     * Results are ordered by member ID in ascending order.
     *
     * @param query the name fragment to search for
     * @return the matching members
     */
    public List<Member> searchByName(String query) {
        try {
            QueryBuilder<Member, Integer> queryBuilder = dao.queryBuilder();

            // instr performs a literal substring match.
            // SelectArg binds the value without concatenating SQL.
            queryBuilder.where().raw(
                    "instr(lower(name), lower(?)) > 0",
                    new SelectArg(SqlType.STRING, query)
            );

            // Return matches ordered by member ID.
            queryBuilder.orderBy("id", true);

            return dao.query(queryBuilder.prepare());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
