package com.ipd.platform.repository;

import com.ipd.platform.entity.PrjTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务 Repository
 */
@Repository
public interface PrjTaskRepository extends JpaRepository<PrjTask, Long> {

    Optional<PrjTask> findByTaskNo(String taskNo);

    boolean existsByTaskNo(String taskNo);

    Page<PrjTask> findByProjectId(Long projectId, Pageable pageable);

    Page<PrjTask> findBySprintId(Long sprintId, Pageable pageable);

    Page<PrjTask> findByAssigneeId(Long assigneeId, Pageable pageable);

    Page<PrjTask> findByStatus(String status, Pageable pageable);

    @Query("SELECT t FROM PrjTask t WHERE " +
           "(:keyword IS NULL OR t.title LIKE %:keyword%) AND " +
           "(:projectId IS NULL OR t.projectId = :projectId) AND " +
           "(:sprintId IS NULL OR t.sprintId = :sprintId) AND " +
           "(:assigneeId IS NULL OR t.assigneeId = :assigneeId) AND " +
           "(:status IS NULL OR t.status = :status)")
    Page<PrjTask> searchTasks(
            @Param("keyword") String keyword,
            @Param("projectId") Long projectId,
            @Param("sprintId") Long sprintId,
            @Param("assigneeId") Long assigneeId,
            @Param("status") String status,
            Pageable pageable);

    List<PrjTask> findByParentId(Long parentId);

    @Query("SELECT COUNT(t) FROM PrjTask t WHERE t.sprintId = :sprintId")
    long countBySprintId(@Param("sprintId") Long sprintId);

    @Query("SELECT COUNT(t) FROM PrjTask t WHERE t.sprintId = :sprintId AND t.status = :status")
    long countBySprintIdAndStatus(@Param("sprintId") Long sprintId, @Param("status") String status);
}
