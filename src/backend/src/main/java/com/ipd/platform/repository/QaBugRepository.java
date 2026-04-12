package com.ipd.platform.repository;

import com.ipd.platform.entity.QaBug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QaBugRepository extends JpaRepository<QaBug, Long> {

    Page<QaBug> findByProjectId(Long projectId, Pageable pageable);

    Page<QaBug> findByAssigneeId(Long assigneeId, Pageable pageable);

    @Query("SELECT b FROM QaBug b WHERE " +
           "(:keyword IS NULL OR b.title LIKE %:keyword% OR b.bugNo LIKE %:keyword%) AND " +
           "(:projectId IS NULL OR b.projectId = :projectId) AND " +
           "(:severity IS NULL OR b.severity = :severity) AND " +
           "(:status IS NULL OR b.status = :status)")
    Page<QaBug> searchBugs(
            @Param("keyword") String keyword,
            @Param("projectId") Long projectId,
            @Param("severity") Integer severity,
            @Param("status") Integer status,
            Pageable pageable);

    long countByProjectIdAndStatus(Long projectId, Integer status);

    long countBySeverity(Integer severity);

    List<QaBug> findByStatus(Integer status);

    List<QaBug> findByProjectIdAndStatus(Long projectId, Integer status);

    @Query("SELECT COUNT(b) FROM QaBug b WHERE b.projectId = :projectId")
    long countByProject(@Param("projectId") Long projectId);
}
