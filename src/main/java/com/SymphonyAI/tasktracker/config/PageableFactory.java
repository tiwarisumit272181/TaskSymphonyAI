package com.SymphonyAI.tasktracker.config;
import com.SymphonyAI.tasktracker.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PageableFactory {

    static final int MAX_PAGE_SIZE = 100;

    private final AppProperties appProperties;

    public PageableFactory(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public Pageable create(Integer page, Integer size, String sortBy, String direction,
                           List<String> allowedSortFields, String defaultSortField) throws BadRequestException {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : appProperties.getDefaultPageSize();

        if (pageNumber < 0) {
            throw new BadRequestException("page must be 0 or greater");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("size must be between 1 and " + MAX_PAGE_SIZE);
        }

        String sortField = (sortBy == null || sortBy.isBlank()) ? defaultSortField : sortBy;
        if (!allowedSortFields.contains(sortField)) {
            throw new BadRequestException(
                    "Invalid sortBy '" + sortField + "'. Allowed values: " + allowedSortFields);
        }

        Sort.Direction sortDirection = Sort.Direction.ASC;
        if (direction != null && !direction.isBlank()) {
            try {
                sortDirection = Sort.Direction.fromString(direction.trim());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("direction must be 'asc' or 'desc'");
            }
        }

        return PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortField));
    }
}
