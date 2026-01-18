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
import java.util.List;

class ApplicationViewDaoTest {

    @Test
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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

    @Test
    @SuppressWarnings("unchecked")
    void testListNow() {
        JdbcClient jdbcClient = mock(JdbcClient.class);
        StatementSpec statementSpec = mock(StatementSpec.class);
        JdbcClient.MappedQuerySpec<Application> mappedQuerySpec = mock(JdbcClient.MappedQuerySpec.class);

        when(jdbcClient.sql(eq("SELECT * FROM certx.nAP_Application ORDER BY AP_ID LIMIT :limit OFFSET :offset")))
                .thenReturn(statementSpec);
        when(statementSpec.param(eq("limit"), any(Integer.class))).thenReturn(statementSpec);
        when(statementSpec.param(eq("offset"), any(Integer.class))).thenReturn(statementSpec);
        when(statementSpec.query(any(RowMapper.class))).thenReturn(mappedQuerySpec);

        List<Application> expectedApps = List.of(Application.builder().id(1L).applicationName("Test App").build());
        when(mappedQuerySpec.list()).thenReturn(expectedApps);

        ApplicationViewDao dao = new ApplicationViewDao(jdbcClient);
        List<Application> result = dao.listNow(0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApplicationName()).isEqualTo("Test App");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCountNow() {
        JdbcClient jdbcClient = mock(JdbcClient.class);
        StatementSpec statementSpec = mock(StatementSpec.class);

        when(jdbcClient.sql(eq("SELECT count(*) FROM certx.nAP_Application"))).thenReturn(statementSpec);

        // Need to mock the single() call.
        JdbcClient.MappedQuerySpec<Long> mappedQuerySpecLong = mock(JdbcClient.MappedQuerySpec.class);
        when(statementSpec.query(Long.class)).thenReturn(mappedQuerySpecLong);
        when(mappedQuerySpecLong.single()).thenReturn(10L);

        ApplicationViewDao dao = new ApplicationViewDao(jdbcClient);
        long result = dao.countNow();

        assertThat(result).isEqualTo(10L);
    }
}
