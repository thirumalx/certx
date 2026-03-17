package io.github.thirumalx.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.thirumalx.dao.anchor.ApplicationAnchorDao;
import io.github.thirumalx.dao.attribute.ApplicationNameAttributeDao;
import io.github.thirumalx.dao.attribute.ApplicationUniqueIdAttributeDao;
import io.github.thirumalx.dao.attribute.ApplicationStatusAttributeDao;
import io.github.thirumalx.dao.view.ApplicationViewDao;
import io.github.thirumalx.dto.Application;
import io.github.thirumalx.dto.PageRequest;
import io.github.thirumalx.dto.PageResponse;
import io.github.thirumalx.model.Attribute;
import io.github.thirumalx.model.Knot;

class ApplicationServiceTest {

        @Test
        void testSaveApplicationWithUniqueId() {
                ApplicationAnchorDao anchorDao = mock(ApplicationAnchorDao.class);
                ApplicationNameAttributeDao nameDao = mock(ApplicationNameAttributeDao.class);
                ApplicationUniqueIdAttributeDao uniqueIdDao = mock(ApplicationUniqueIdAttributeDao.class);
                ApplicationViewDao viewDao = mock(ApplicationViewDao.class);
                ApplicationStatusAttributeDao statusDao = mock(ApplicationStatusAttributeDao.class);

                ApplicationService service = new ApplicationService(anchorDao, nameDao, uniqueIdDao, viewDao,
                                statusDao);

                when(anchorDao.insert(any(Long.class))).thenReturn(1L);
                when(nameDao.insert(eq(1L), eq("Test App"), any(Instant.class), eq(Attribute.METADATA_ACTIVE)))
                                .thenReturn(Map.of("ap_nam_ap_id", 1L));
                when(uniqueIdDao.insert(eq(1L), eq("UID123"), eq(Attribute.METADATA_ACTIVE)))
                                .thenReturn(Map.of("ap_uid_ap_id", 1L));

                Application app = Application.builder()
                                .applicationName("Test App")
                                .uniqueId("UID123")
                                .build();

                Application savedApp = service.save(app);

                assertThat(savedApp.getId()).isEqualTo(1L);
                verify(anchorDao).insert(any(Long.class));
                verify(nameDao).insert(eq(1L), eq("Test App"), any(Instant.class), eq(Attribute.METADATA_ACTIVE));
                verify(uniqueIdDao).insert(eq(1L), eq("UID123"), eq(Attribute.METADATA_ACTIVE));
        }

        @Test
        void testSaveDuplicateApplicationName() {
                ApplicationAnchorDao anchorDao = mock(ApplicationAnchorDao.class);
                ApplicationNameAttributeDao nameDao = mock(ApplicationNameAttributeDao.class);
                ApplicationUniqueIdAttributeDao uniqueIdDao = mock(ApplicationUniqueIdAttributeDao.class);
                ApplicationViewDao viewDao = mock(ApplicationViewDao.class);
                ApplicationStatusAttributeDao statusDao = mock(ApplicationStatusAttributeDao.class);

                ApplicationService service = new ApplicationService(anchorDao, nameDao, uniqueIdDao, viewDao,
                                statusDao);

                when(anchorDao.insert(any(Long.class))).thenReturn(1L);
                when(nameDao.insert(eq(1L), eq("Test App"), any(Instant.class), eq(Attribute.METADATA_ACTIVE)))
                                .thenThrow(new org.springframework.dao.DuplicateKeyException("Duplicate name"));

                Application app = Application.builder()
                                .applicationName("Test App")
                                .build();

                org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.save(app))
                                .isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
        }

        @Test
        void testSaveDuplicateUniqueId() {
                ApplicationAnchorDao anchorDao = mock(ApplicationAnchorDao.class);
                ApplicationNameAttributeDao nameDao = mock(ApplicationNameAttributeDao.class);
                ApplicationUniqueIdAttributeDao uniqueIdDao = mock(ApplicationUniqueIdAttributeDao.class);
                ApplicationViewDao viewDao = mock(ApplicationViewDao.class);
                ApplicationStatusAttributeDao statusDao = mock(ApplicationStatusAttributeDao.class);

                ApplicationService service = new ApplicationService(anchorDao, nameDao, uniqueIdDao, viewDao,
                                statusDao);

                when(anchorDao.insert(any(Long.class))).thenReturn(1L);
                when(nameDao.insert(eq(1L), eq("Test App"), any(Instant.class), eq(Attribute.METADATA_ACTIVE)))
                                .thenReturn(Map.of("ap_nam_ap_id", 1L));
                when(uniqueIdDao.insert(eq(1L), eq("UID123"), eq(Attribute.METADATA_ACTIVE)))
                                .thenThrow(new org.springframework.dao.DuplicateKeyException("Duplicate unique id"));

                Application app = Application.builder()
                                .applicationName("Test App")
                                .uniqueId("UID123")
                                .build();

                org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.save(app))
                                .isInstanceOf(io.github.thirumalx.exception.DuplicateKeyException.class)
                                .hasMessageContaining("Application ID must be unique...");
        }

        @Test
        void testSaveApplicationWithoutUniqueId() {
                ApplicationAnchorDao anchorDao = mock(ApplicationAnchorDao.class);
                ApplicationNameAttributeDao nameDao = mock(ApplicationNameAttributeDao.class);
                ApplicationUniqueIdAttributeDao uniqueIdDao = mock(ApplicationUniqueIdAttributeDao.class);
                ApplicationViewDao viewDao = mock(ApplicationViewDao.class);
                ApplicationStatusAttributeDao statusDao = mock(ApplicationStatusAttributeDao.class);

                ApplicationService service = new ApplicationService(anchorDao, nameDao, uniqueIdDao, viewDao,
                                statusDao);

                when(anchorDao.insert(any(Long.class))).thenReturn(1L);
                when(nameDao.insert(eq(1L), eq("Test App"), any(Instant.class), eq(Attribute.METADATA_ACTIVE)))
                                .thenReturn(Map.of("ap_nam_ap_id", 1L));

                Application app = Application.builder()
                                .applicationName("Test App")
                                .build();

                Application savedApp = service.save(app);

                assertThat(savedApp.getId()).isEqualTo(1L);
                verify(anchorDao).insert(any(Long.class));
                verify(nameDao).insert(eq(1L), eq("Test App"), any(Instant.class), eq(Attribute.METADATA_ACTIVE));
                // Verify uniqueIdDao.insert was NOT called
                verify(uniqueIdDao, org.mockito.Mockito.never()).insert(any(), any(), any());
        }

        @Test
        void testListApplication() {
                ApplicationAnchorDao anchorDao = mock(ApplicationAnchorDao.class);
                ApplicationNameAttributeDao nameDao = mock(ApplicationNameAttributeDao.class);
                ApplicationUniqueIdAttributeDao uniqueIdDao = mock(ApplicationUniqueIdAttributeDao.class);
                ApplicationViewDao viewDao = mock(ApplicationViewDao.class);
                ApplicationStatusAttributeDao statusDao = mock(ApplicationStatusAttributeDao.class);

                ApplicationService service = new ApplicationService(anchorDao, nameDao, uniqueIdDao, viewDao,
                                statusDao);

                PageRequest pageRequest = new PageRequest(0, 5);
                java.util.List<Application> apps = java.util.List.of(
                                Application.builder().id(1L).applicationName("App 1").build(),
                                Application.builder().id(2L).applicationName("App 2").build());

                when(viewDao.listNow(Knot.ACTIVE, 0, 5)).thenReturn(apps);
                when(viewDao.countNow(Knot.ACTIVE)).thenReturn(10L);

                PageResponse<Application> response = service.listApplication(pageRequest, "ACTIVE");

                assertThat(response.page()).isEqualTo(0);
                assertThat(response.size()).isEqualTo(5);
                assertThat(response.content()).hasSize(2);
                assertThat(response.totalElements()).isEqualTo(10L);
                assertThat(response.totalPages()).isEqualTo(2);
        }

        @Test
        void testDeleteApplication() {
                ApplicationAnchorDao anchorDao = mock(ApplicationAnchorDao.class);
                ApplicationNameAttributeDao nameDao = mock(ApplicationNameAttributeDao.class);
                ApplicationUniqueIdAttributeDao uniqueIdDao = mock(ApplicationUniqueIdAttributeDao.class);
                ApplicationViewDao viewDao = mock(ApplicationViewDao.class);
                ApplicationStatusAttributeDao statusDao = mock(ApplicationStatusAttributeDao.class);

                ApplicationService service = new ApplicationService(anchorDao, nameDao, uniqueIdDao, viewDao,
                                statusDao);

                when(viewDao.findNowById(1L)).thenReturn(java.util.Optional.of(Application.builder().id(1L).build()));

                service.deleteApplication(1L);

                verify(statusDao).insert(eq(1L), eq(io.github.thirumalx.model.Knot.DELETED), any(Instant.class),
                                eq(io.github.thirumalx.model.Attribute.METADATA_ACTIVE));
        }
}
