package com.redis.om.spring.annotations.hash;

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
import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;
import com.redis.om.spring.fixtures.hash.model.TicketHash;
import com.redis.om.spring.fixtures.hash.repository.AllTicketHashesRepository;
import com.redis.om.spring.fixtures.hash.repository.TeamATicketHashRepository;
import com.redis.om.spring.fixtures.hash.repository.TeamBTicketHashRepository;
import com.redis.om.spring.search.stream.EntityStream;

@DirtiesContext
@SpringBootTest(
    classes = SeparateIndexDefinitionHashTest.Config.class,
    properties = { "spring.main.allow-bean-definition-overriding=true" }
)
public class SeparateIndexDefinitionHashTest extends AbstractBaseOMTest {

  @SpringBootApplication
  @Configuration
  @EnableRedisEnhancedRepositories(
      basePackages = {
          "com.redis.om.spring.fixtures.hash.repository",
          "com.redis.om.spring.fixtures.hash.model"
      }
  )
  static class Config extends TestConfig {
  }

  @Autowired
  AllTicketHashesRepository allTicketHashesRepository;

  @Autowired
  TeamATicketHashRepository teamATicketHashRepository;

  @Autowired
  TeamBTicketHashRepository teamBTicketHashRepository;

  @Autowired
  EntityStream entityStream;

  @BeforeEach
  void setUp() {
    allTicketHashesRepository.deleteAll();

    TicketHash t1 = TicketHash.of("TeamA", "high", "Fix login bug");
    TicketHash t2 = TicketHash.of("TeamA", "low", "Update docs");
    TicketHash t3 = TicketHash.of("TeamB", "high", "Deploy new service");
    TicketHash t4 = TicketHash.of("TeamB", "medium", "Refactor auth module");
    TicketHash t5 = TicketHash.of("TeamC", "low", "Add logging");

    allTicketHashesRepository.saveAll(List.of(t1, t2, t3, t4, t5));
  }

  @Test
  void testAllTicketHashesRepositoryReturnAllTickets() {
    List<TicketHash> all = allTicketHashesRepository.findAll();
    assertThat(all).hasSize(5);
  }

  @Test
  void testTeamAHashRepositoryReturnsOnlyTeamATickets() {
    List<TicketHash> teamATickets = teamATicketHashRepository.findAll();
    assertThat(teamATickets).hasSize(2);
    assertThat(teamATickets).allMatch(t -> "TeamA".equals(t.getTeam()));
  }

  @Test
  void testTeamBHashRepositoryReturnsOnlyTeamBTickets() {
    List<TicketHash> teamBTickets = teamBTicketHashRepository.findAll();
    assertThat(teamBTickets).hasSize(2);
    assertThat(teamBTickets).allMatch(t -> "TeamB".equals(t.getTeam()));
  }

  @Test
  void testCountRespectsRepositoryLevelFilter() {
    assertThat(allTicketHashesRepository.count()).isEqualTo(5);
    assertThat(teamATicketHashRepository.count()).isEqualTo(2);
    assertThat(teamBTicketHashRepository.count()).isEqualTo(2);
  }

  @Test
  void testEntityStreamWithCustomIndexName() {
    List<TicketHash> teamATickets = entityStream.of(TicketHash.class, "ticket_hash_team_a_idx")
        .collect(Collectors.toList());
    assertThat(teamATickets).hasSize(2);
    assertThat(teamATickets).allMatch(t -> "TeamA".equals(t.getTeam()));
  }

  @Test
  void testSaveThroughAllRepoQueryThroughFilteredRepo() {
    TicketHash newTicket = TicketHash.of("TeamA", "critical", "Urgent security patch");
    allTicketHashesRepository.save(newTicket);

    List<TicketHash> teamATickets = teamATicketHashRepository.findAll();
    assertThat(teamATickets).hasSize(3);
    assertThat(teamATickets).anyMatch(t -> "critical".equals(t.getPriority()));
  }

  @Test
  void testFilteredRepoDoesNotReturnOtherTeamTickets() {
    List<TicketHash> teamATickets = teamATicketHashRepository.findAll();
    assertThat(teamATickets).noneMatch(t -> "TeamB".equals(t.getTeam()));
    assertThat(teamATickets).noneMatch(t -> "TeamC".equals(t.getTeam()));
  }
}
