package com.ipd.platform.service;

import com.ipd.platform.entity.RevReview;
import com.ipd.platform.repository.RevReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评审服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final RevReviewRepository reviewRepository;
    private final AuthService authService;

    /**
     * 分页查询评审
     */
    public Page<RevReview> list(String keyword, Long projectId, Integer type, Integer status,
                                Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.searchReviews(keyword, projectId, type, status, pageRequest);
    }

    /**
     * 获取评审详情
     */
    public RevReview getById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评审不存在"));
    }

    /**
     * 创建评审
     */
    @Transactional
    public RevReview create(RevReview review) {
        String reviewNo = generateReviewNo();
        review.setReviewNo(reviewNo);
        review.setStatus(RevReview.Status.DRAFT);
        review.setCreatedBy(authService.getCurrentUserId());
        review.setHostId(authService.getCurrentUserId());
        return reviewRepository.save(review);
    }

    /**
     * 更新评审
     */
    @Transactional
    public RevReview update(Long id, RevReview review) {
        RevReview existing = getById(id);
        
        if (review.getTitle() != null) existing.setTitle(review.getTitle());
        if (review.getType() != null) existing.setType(review.getType());
        if (review.getStage() != null) existing.setStage(review.getStage());
        if (review.getProjectId() != null) existing.setProjectId(review.getProjectId());
        if (review.getScheduledAt() != null) existing.setScheduledAt(review.getScheduledAt());
        if (review.getConclusion() != null) existing.setConclusion(review.getConclusion());
        
        return reviewRepository.save(existing);
    }

    /**
     * 提交评审
     */
    @Transactional
    public RevReview submit(Long id) {
        RevReview review = getById(id);
        if (review.getStatus() != RevReview.Status.DRAFT) {
            throw new RuntimeException("只有草稿状态才能提交");
        }
        review.setStatus(RevReview.Status.PENDING);
        return reviewRepository.save(review);
    }

    /**
     * 开始评审
     */
    @Transactional
    public RevReview startReview(Long id) {
        RevReview review = getById(id);
        if (review.getStatus() != RevReview.Status.PENDING) {
            throw new RuntimeException("只有待评审状态才能开始");
        }
        review.setStatus(RevReview.Status.IN_REVIEW);
        review.setHeldAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    /**
     * 评审决策
     */
    @Transactional
    public RevReview decide(Long id, Integer decision, String conclusion) {
        RevReview review = getById(id);
        if (review.getStatus() != RevReview.Status.IN_REVIEW) {
            throw new RuntimeException("只有评审中状态才能决策");
        }
        
        review.setDecision(decision);
        review.setConclusion(conclusion);
        
        // 根据决策设置状态
        if (decision == RevReview.Decision.PASS) {
            review.setStatus(RevReview.Status.PASSED);
        } else if (decision == RevReview.Decision.CONDITIONAL) {
            review.setStatus(RevReview.Status.CONDITIONAL);
        } else {
            review.setStatus(RevReview.Status.REJECTED);
        }
        
        return reviewRepository.save(review);
    }

    /**
     * 撤回评审
     */
    @Transactional
    public RevReview withdraw(Long id) {
        RevReview review = getById(id);
        if (review.getStatus() == RevReview.Status.PASSED || 
            review.getStatus() == RevReview.Status.REJECTED) {
            throw new RuntimeException("已完成的评审不能撤回");
        }
        review.setStatus(RevReview.Status.WITHDRAWN);
        return reviewRepository.save(review);
    }

    /**
     * 获取待我评审的列表
     */
    public Page<RevReview> getMyPendingReviews(Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "scheduledAt"));
        return reviewRepository.findByStatus(RevReview.Status.PENDING, pageRequest);
    }

    /**
     * 按项目获取评审
     */
    public List<RevReview> getByProjectId(Long projectId) {
        return reviewRepository.findByProjectId(projectId, PageRequest.of(0, 100)).getContent();
    }

    /**
     * 按类型获取评审
     */
    public List<RevReview> getByType(Long projectId, Integer type) {
        return reviewRepository.findByProjectIdAndType(projectId, type);
    }

    /**
     * 获取评审统计
     */
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", reviewRepository.count());
        stats.put("draft", reviewRepository.findByStatus(RevReview.Status.DRAFT, PageRequest.of(0, 1)).getTotalElements());
        stats.put("pending", reviewRepository.findByStatus(RevReview.Status.PENDING, PageRequest.of(0, 1)).getTotalElements());
        stats.put("inReview", reviewRepository.findByStatus(RevReview.Status.IN_REVIEW, PageRequest.of(0, 1)).getTotalElements());
        stats.put("passed", reviewRepository.findByStatus(RevReview.Status.PASSED, PageRequest.of(0, 1)).getTotalElements());
        return stats;
    }

    /**
     * 生成评审编号
     */
    private String generateReviewNo() {
        String prefix = "REV";
        String dateStr = java.time.LocalDate.now().toString().replace("-", "");
        long count = reviewRepository.count() + 1;
        return String.format("%s-%s-%04d", prefix, dateStr, count);
    }
}
