package com.SymphonyAI.tasktracker.config;

import com.SymphonyAI.tasktracker.entity.Priority;
import com.SymphonyAI.tasktracker.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class PageableFactoryTest {

    private static final List<String> ALLOWED = List.of("id", "name");

    private final PageableFactory factory =
            new PageableFactory(new AppProperties("App", "1.0", Priority.MEDIUM, 10));

    @Test
    void create_usesDefaults_whenNothingSupplied() throws Exception {
        Pageable pageable = factory.create(null, null, null, null, ALLOWED, "id");

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(pageable.getSort().getOrderFor("id")).getDirection());
    }

    @Test
    void create_usesSuppliedValues() throws Exception {
        Pageable pageable = factory.create(2, 5, "name", "desc", ALLOWED, "id");

        assertEquals(2, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("name").getDirection());
    }

    @Test
    void create_throwsBadRequest_whenSortFieldNotAllowed() {
        assertThrows(BadRequestException.class,
                () -> factory.create(0, 10, "password", "asc", ALLOWED, "id"));
    }

    @Test
    void create_throwsBadRequest_whenDirectionInvalid() {
        assertThrows(BadRequestException.class,
                () -> factory.create(0, 10, "id", "sideways", ALLOWED, "id"));
    }

    @Test
    void create_throwsBadRequest_whenPageNegative() {
        assertThrows(BadRequestException.class,
                () -> factory.create(-1, 10, "id", "asc", ALLOWED, "id"));
    }

    @Test
    void create_throwsBadRequest_whenSizeTooSmall() {
        assertThrows(BadRequestException.class,
                () -> factory.create(0, 0, "id", "asc", ALLOWED, "id"));
    }

    @Test
    void create_throwsBadRequest_whenSizeTooLarge() {
        assertThrows(BadRequestException.class,
                () -> factory.create(0, 101, "id", "asc", ALLOWED, "id"));
    }
}