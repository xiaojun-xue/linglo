package com.ipd.platform.repository;

import com.ipd.platform.entity.RevReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 评审 Repository
 */
@Repository
public interface RevReviewRepository extends JpaRepository<RevReview, Long> {

    Optional<RevReview> findByReviewNo(String reviewNo);

    boolean existsByReviewNo(String reviewNo);

    Page<RevReview> findByProjectId(Long projectId, Pageable pageable);

    Page<RevReview> findByType(Integer type, Pageable pageable);

    Page<RevReview> findByStatus(Integer status, Pageable pageable);

    @Query("SELECT r FROM RevReview r WHERE " +
           "(:keyword IS NULL OR r.title LIKE %:keyword%) AND " +
           "(:projectId IS NULL OR r.projectId = :projectId) AND " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:status IS NULL OR r.status = :status)")
    Page<RevReview> searchReviews(
            @Param("keyword") String keyword,
            @Param("projectId") Long projectId,
            @Param("type") Integer type,
            @Param("status") Integer status,
            Pageable pageable);

    List<RevReview> findByProjectIdAndType(Long projectId, Integer type);
}
