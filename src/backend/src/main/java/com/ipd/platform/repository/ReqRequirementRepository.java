package com.ipd.platform.repository;

import com.ipd.platform.entity.ReqRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 需求 Repository
 */
@Repository
public interface ReqRequirementRepository extends JpaRepository<ReqRequirement, Long> {

    Optional<ReqRequirement> findByReqNo(String reqNo);

    boolean existsByReqNo(String reqNo);

    Page<ReqRequirement> findByProductId(Long productId, Pageable pageable);

    Page<ReqRequirement> findByProjectId(Long projectId, Pageable pageable);

    Page<ReqRequirement> findByStatus(String status, Pageable pageable);

    Page<ReqRequirement> findByPriority(Integer priority, Pageable pageable);

    @Query("SELECT r FROM ReqRequirement r WHERE " +
           "(:keyword IS NULL OR r.title LIKE %:keyword% OR r.description LIKE %:keyword%) AND " +
           "(:productId IS NULL OR r.productId = :productId) AND " +
           "(:projectId IS NULL OR r.projectId = :projectId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:priority IS NULL OR r.priority = :priority) AND " +
           "(:level IS NULL OR r.level = :level)")
    Page<ReqRequirement> searchRequirements(
            @Param("keyword") String keyword,
            @Param("productId") Long productId,
            @Param("projectId") Long projectId,
            @Param("status") String status,
            @Param("priority") Integer priority,
            @Param("level") Integer level,
            Pageable pageable);

    List<ReqRequirement> findByParentId(Long parentId);

    List<ReqRequirement> findByParentIdOrderByCreatedAtAsc(Long parentId);

    /** 查询项目下指定层级的顶层需求（无父级） */
    @Query("SELECT r FROM ReqRequirement r WHERE r.projectId = :projectId AND r.level = :level AND r.parentId IS NULL ORDER BY r.createdAt ASC")
    List<ReqRequirement> findRootsByProjectIdAndLevel(@Param("projectId") Long projectId, @Param("level") int level);

    /** 查询项目下所有指定层级的需求 */
    List<ReqRequirement> findByProjectIdAndLevelOrderByCreatedAtAsc(Long projectId, Integer level);

    /** 查询项目下所有需求（用于构建树） */
    List<ReqRequirement> findByProjectIdOrderByLevelAscCreatedAtAsc(Long projectId);

    @Query("SELECT COUNT(r) FROM ReqRequirement r WHERE r.projectId = :projectId AND r.status = :status")
    long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") String status);
}
