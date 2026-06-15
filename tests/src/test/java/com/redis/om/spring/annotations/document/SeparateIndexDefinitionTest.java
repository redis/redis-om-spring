package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import com.redis.om.spring.AbstractBaseOMTest;
import com.redis.om.spring.TestConfig;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.fixtures.document.model.Ticket;
import com.redis.om.spring.fixtures.document.repository.AllTicketsRepository;
import com.redis.om.spring.fixtures.document.repository.TeamATicketRepository;
import com.redis.om.spring.fixtures.document.repository.TeamBTicketRepository;
import com.redis.om.spring.search.stream.EntityStream;

@DirtiesContext
@SpringBootTest(
    classes = SeparateIndexDefinitionTest.Config.class,
    properties = { "spring.main.allow-bean-definition-overriding=true" }
)
public class SeparateIndexDefinitionTest extends AbstractBaseOMTest {

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(
      basePackages = {
          "com.redis.om.spring.fixtures.document.repository",
          "com.redis.om.spring.fixtures.document.model"
      }
  )
  static class Config extends TestConfig {
  }

  @Autowired
  AllTicketsRepository allTicketsRepository;

  @Autowired
  TeamATicketRepository teamATicketRepository;

  @Autowired
  TeamBTicketRepository teamBTicketRepository;

  @Autowired
  EntityStream entityStream;

  @BeforeEach
  void setUp() {
    allTicketsRepository.deleteAll();

    Ticket t1 = Ticket.of("TeamA", "high", "Fix login bug");
    Ticket t2 = Ticket.of("TeamA", "low", "Update docs");
    Ticket t3 = Ticket.of("TeamB", "high", "Deploy new service");
    Ticket t4 = Ticket.of("TeamB", "medium", "Refactor auth module");
    Ticket t5 = Ticket.of("TeamC", "low", "Add logging");

    allTicketsRepository.saveAll(List.of(t1, t2, t3, t4, t5));
  }

  @Test
  void testAllTicketsRepositoryReturnAllTickets() {
    List<Ticket> all = allTicketsRepository.findAll();
    assertThat(all).hasSize(5);
  }

  @Test
  void testTeamARepositoryReturnsOnlyTeamATickets() {
    List<Ticket> teamATickets = teamATicketRepository.findAll();
    assertThat(teamATickets).hasSize(2);
    assertThat(teamATickets).allMatch(t -> "TeamA".equals(t.getTeam()));
  }

  @Test
  void testTeamBRepositoryReturnsOnlyTeamBTickets() {
    List<Ticket> teamBTickets = teamBTicketRepository.findAll();
    assertThat(teamBTickets).hasSize(2);
    assertThat(teamBTickets).allMatch(t -> "TeamB".equals(t.getTeam()));
  }

  @Test
  void testEntityStreamWithCustomIndexName() {
    List<Ticket> teamATickets = entityStream.of(Ticket.class, "ticket_team_a_idx")
        .collect(Collectors.toList());
    assertThat(teamATickets).hasSize(2);
    assertThat(teamATickets).allMatch(t -> "TeamA".equals(t.getTeam()));
  }

  @Test
  void testEntityLevelIndexingOptionsStillWorks() {
    List<Ticket> allTickets = entityStream.of(Ticket.class)
        .collect(Collectors.toList());
    assertThat(allTickets).hasSize(5);
  }

  @Test
  void testSaveThroughAllRepoQueryThroughFilteredRepo() {
    Ticket newTicket = Ticket.of("TeamA", "critical", "Urgent security patch");
    allTicketsRepository.save(newTicket);

    List<Ticket> teamATickets = teamATicketRepository.findAll();
    assertThat(teamATickets).hasSize(3);
    assertThat(teamATickets).anyMatch(t -> "critical".equals(t.getPriority()));
  }

  @Test
  void testFilteredRepoDoesNotReturnOtherTeamTickets() {
    List<Ticket> teamATickets = teamATicketRepository.findAll();
    assertThat(teamATickets).noneMatch(t -> "TeamB".equals(t.getTeam()));
    assertThat(teamATickets).noneMatch(t -> "TeamC".equals(t.getTeam()));
  }
}
