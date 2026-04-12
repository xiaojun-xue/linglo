package com.ipd.platform.repository;

import com.ipd.platform.entity.QaTestCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QaTestCaseRepository extends JpaRepository<QaTestCase, Long> {

    Page<QaTestCase> findByProjectId(Long projectId, Pageable pageable);

    Page<QaTestCase> findByRequirementId(Long requirementId, Pageable pageable);

    @Query("SELECT c FROM QaTestCase c WHERE " +
           "(:keyword IS NULL OR c.title LIKE %:keyword% OR c.caseNo LIKE %:keyword%) AND " +
           "(:projectId IS NULL OR c.projectId = :projectId) AND " +
           "(:module IS NULL OR c.module = :module) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<QaTestCase> searchCases(
            @Param("keyword") String keyword,
            @Param("projectId") Long projectId,
            @Param("module") String module,
            @Param("status") Integer status,
            Pageable pageable);

    long countByProjectId(Long projectId);

    long countByStatus(Integer status);

    List<QaTestCase> findByStatus(Integer status);
}
