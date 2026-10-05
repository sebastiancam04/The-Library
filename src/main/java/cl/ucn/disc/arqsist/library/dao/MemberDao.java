/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.dao;

import cl.ucn.disc.arqsist.library.model.Member;
import com.j256.ormlite.support.ConnectionSource;

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
}