/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Member;

import java.util.List;

/**
 * Handles member registration and retrieval.
 * Lending operations belong to {@link LoanService}.
 */
public final class MemberService {

    /**
     * DAO responsible for member persistence.
     */
    private final MemberDao memberDao;

    /**
     * Creates the member service.
     *
     * @param memberDao DAO used for member operations.
     */
    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    /**
     * Registers a new member.
     *
     * @param member member to register.
     * @return the registered member.
     */
    public Member register(Member member) {
        memberDao.create(member);
        return member;
    }

    /**
     * Returns all registered members.
     *
     * @return list of registered members.
     */
    public List<Member> findAll() {
        return memberDao.findAll();
    }
}