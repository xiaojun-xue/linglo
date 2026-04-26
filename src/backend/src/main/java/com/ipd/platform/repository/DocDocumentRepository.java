package com.ipd.platform.repository;

import com.ipd.platform.entity.DocDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocDocumentRepository extends JpaRepository<DocDocument, Long> {

    Page<DocDocument> findByProjectId(Long projectId, Pageable pageable);

    Page<DocDocument> findByCategory(Integer category, Pageable pageable);

    @Query("SELECT d FROM DocDocument d WHERE " +
           "(:keyword IS NULL OR d.title LIKE %:keyword%) AND " +
           "(:projectId IS NULL OR d.projectId = :projectId) AND " +
           "(:category IS NULL OR d.category = :category) AND " +
           "(:isFolder IS NULL OR d.isFolder = :isFolder)")
    Page<DocDocument> searchDocs(
            @Param("keyword") String keyword,
            @Param("projectId") Long projectId,
            @Param("category") Integer category,
            @Param("isFolder") Boolean isFolder,
            Pageable pageable);

    List<DocDocument> findByParentId(Long parentId);

    List<DocDocument> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<DocDocument> findByParentIdIsNullOrderBySortOrderAsc();

    long countByProjectId(Long projectId);
}
