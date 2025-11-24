package io.github.thirumalx.dao.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

import io.github.thirumalx.dto.Application;

class ApplicationViewDaoTest {

    @Test
    void testFindLatestById() {
        // Mock JdbcClient and its fluent API
        JdbcClient jdbcClient = mock(JdbcClient.class);
        StatementSpec statementSpec = mock(StatementSpec.class);
        JdbcClient.MappedQuerySpec<Application> mappedQuerySpec = mock(JdbcClient.MappedQuerySpec.class);

        when(jdbcClient.sql(eq("SELECT * FROM certx.lAP_Application WHERE AP_ID = :id"))).thenReturn(statementSpec);
        when(statementSpec.param(eq("id"), any(Long.class))).thenReturn(statementSpec);
        when(statementSpec.query(any(RowMapper.class))).thenReturn(mappedQuerySpec);

        Application expectedApp = Application.builder().id(1L).applicationName("Test App").build();
        when(mappedQuerySpec.optional()).thenReturn(Optional.of(expectedApp));

        ApplicationViewDao dao = new ApplicationViewDao(jdbcClient);
        Optional<Application> result = dao.findLatestById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getApplicationName()).isEqualTo("Test App");
    }

    @Test
    void testFindNowById() {
        // Mock JdbcClient and its fluent API
        JdbcClient jdbcClient = mock(JdbcClient.class);
        StatementSpec statementSpec = mock(StatementSpec.class);
        JdbcClient.MappedQuerySpec<Application> mappedQuerySpec = mock(JdbcClient.MappedQuerySpec.class);

        when(jdbcClient.sql(eq("SELECT * FROM certx.nAP_Application WHERE AP_ID = :id"))).thenReturn(statementSpec);
        when(statementSpec.param(eq("id"), any(Long.class))).thenReturn(statementSpec);
        when(statementSpec.query(any(RowMapper.class))).thenReturn(mappedQuerySpec);

        Application expectedApp = Application.builder().id(1L).applicationName("Test App").build();
        when(mappedQuerySpec.optional()).thenReturn(Optional.of(expectedApp));

        ApplicationViewDao dao = new ApplicationViewDao(jdbcClient);
        Optional<Application> result = dao.findNowById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getApplicationName()).isEqualTo("Test App");
    }
}
