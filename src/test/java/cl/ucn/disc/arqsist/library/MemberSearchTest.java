
/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.grpc.MemberClient;
import cl.ucn.disc.arqsist.library.grpc.MemberGrpcService;
import cl.ucn.disc.arqsist.library.grpc.MemberServiceGrpc;
import cl.ucn.disc.arqsist.library.grpc.SearchMembersRequest;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.MemberService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the member search gRPC round trip.
 */
final class MemberSearchTest {

    /**
     * Verify all seven acceptance cases for member search.
     *
     * @throws Exception if the test server fails
     */
    @Test
    void searchMembersAcceptanceCases() throws Exception {
        // Create an isolated in-memory database.
        Database db = new Database("jdbc:sqlite::memory:");

        MemberService memberService = new MemberService(
                new MemberDao(db.connectionSource()));

        // Insert members in the required order.
        memberService.register(
                new Member("Ada Lovelace", "ada@example.com"));

        memberService.register(
                new Member("Grace Hopper", "grace@example.com"));

        memberService.register(
                new Member("Alan Turing", "alan@example.com"));

        // Create an in-process gRPC server.
        String serverName = InProcessServerBuilder.generateName();

        Server server = InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(new MemberGrpcService(memberService))
                .build()
                .start();

        ManagedChannel channel = InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build();

        try (MemberClient client = new MemberClient(channel)) {

            // Case 1: lowercase partial match.
            assertEquals(
                    List.of("Ada Lovelace"),
                    names(client.search("ada"))
            );

            // Case 2: case-insensitive ASCII match.
            assertEquals(
                    List.of("Ada Lovelace"),
                    names(client.search("ADA"))
            );

            // Case 3: trim whitespace.
            assertEquals(
                    List.of("Ada Lovelace"),
                    names(client.search(" ada "))
            );

            // Case 4: multiple matches in ascending ID order.
            assertEquals(
                    List.of("Ada Lovelace", "Grace Hopper", "Alan Turing"),
                    names(client.search("a"))
            );

            // Case 5: no matching members.
            assertEquals(
                    List.of(),
                    names(client.search("zzz"))
            );

            // Case 6: SQL wildcard characters are literal.
            assertEquals(
                    List.of(),
                    names(client.search("%"))
            );

            assertEquals(
                    List.of(),
                    names(client.search("_"))
            );

            // Case 7: blank queries are invalid.
            assertThrows(
                    IllegalArgumentException.class,
                    () -> client.search("")
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> client.search(" ")
            );

            // Verify the actual gRPC status for a blank query.
            StatusRuntimeException error = assertThrows(
                    StatusRuntimeException.class,
                    () -> MemberServiceGrpc.newBlockingStub(channel)
                            .searchMembers(
                                    SearchMembersRequest.newBuilder()
                                            .setQuery("")
                                            .build())
            );

            assertEquals(
                    Status.Code.INVALID_ARGUMENT,
                    error.getStatus().getCode()
            );

        } finally {
            // Ensure the test server is stopped.
            server.shutdownNow();
        }
    }

    /**
     * Extract member names while preserving their order.
     *
     * @param members the list of members
     * @return the member names
     */
    private static List<String> names(List<Member> members) {
        return members.stream()
                .map(Member::getName)
                .toList();
    }
}
