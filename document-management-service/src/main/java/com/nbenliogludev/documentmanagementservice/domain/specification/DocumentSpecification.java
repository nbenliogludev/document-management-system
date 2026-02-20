package com.nbenliogludev.documentmanagementservice.domain.specification;

import com.nbenliogludev.documentmanagementservice.domain.entity.Document;
import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class DocumentSpecification {

    public static Specification<Document> hasStatus(DocumentStatus status) {
        return (root, query, criteriaBuilder) -> status == null ? null
                : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Document> hasAuthorIgnoreCase(String author) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(author)) {
                return null;
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), "%" + author.toLowerCase() + "%");
        };
    }

    public static Specification<Document> hasTitleIgnoreCase(String title) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(title)) {
                return null;
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<Document> hasNumber(String number) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(number)) {
                return null;
            }
            return criteriaBuilder.equal(root.get("number"), number);
        };
    }
}
