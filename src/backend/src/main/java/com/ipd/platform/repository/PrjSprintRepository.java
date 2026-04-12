package com.ipd.platform.repository;

import com.ipd.platform.entity.PrjSprint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Sprint Repository
 */
@Repository
public interface PrjSprintRepository extends JpaRepository<PrjSprint, Long> {

    List<PrjSprint> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    Page<PrjSprint> findByProjectId(Long projectId, Pageable pageable);

    @Query("SELECT s FROM PrjSprint s WHERE s.projectId = :projectId AND s.status = :status")
    List<PrjSprint> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") Integer status);

    @Query("SELECT s FROM PrjSprint s WHERE s.projectId = :projectId AND s.status IN (1, 2) ORDER BY s.createdAt DESC")
    List<PrjSprint> findActiveSprints(@Param("projectId") Long projectId);

    @Query("SELECT s FROM PrjSprint s WHERE s.status = 2") // 进行中的Sprint
    List<PrjSprint> findInProgressSprints();
}
