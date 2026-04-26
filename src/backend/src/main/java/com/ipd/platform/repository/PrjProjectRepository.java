package com.ipd.platform.repository;

import com.ipd.platform.entity.PrjProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目 Repository
 */
@Repository
public interface PrjProjectRepository extends JpaRepository<PrjProject, Long> {

    Optional<PrjProject> findByProjectNo(String projectNo);

    boolean existsByProjectNo(String projectNo);

    Page<PrjProject> findByStatus(Integer status, Pageable pageable);

    Page<PrjProject> findByProductId(Long productId, Pageable pageable);

    Page<PrjProject> findByStage(Integer stage, Pageable pageable);

    @Query("SELECT p FROM PrjProject p WHERE " +
           "(:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%) AND " +
           "(:productId IS NULL OR p.productId = :productId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:stage IS NULL OR p.stage = :stage)")
    Page<PrjProject> searchProjects(
            @Param("keyword") String keyword,
            @Param("productId") Long productId,
            @Param("status") Integer status,
            @Param("stage") Integer stage,
            Pageable pageable);

    @Query("SELECT p FROM PrjProject p WHERE p.managerId = :managerId ORDER BY p.createdAt DESC")
    List<PrjProject> findByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT COUNT(p) FROM PrjProject p WHERE p.status = :status")
    long countByStatus(@Param("status") Integer status);

    @Query("SELECT COALESCE(MAX(p.projectNo), '') FROM PrjProject p WHERE p.projectNo LIKE :pattern")
    String findMaxProjectNoByPattern(@Param("pattern") String pattern);
}
