package com.ipd.platform.service;

import com.ipd.platform.entity.MdmProduct;
import com.ipd.platform.entity.ReqRequirement;
import com.ipd.platform.repository.MdmProductRepository;
import com.ipd.platform.repository.ReqRequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 需求服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequirementService {

    private final ReqRequirementRepository requirementRepository;
    private final MdmProductRepository productRepository;
    private final AuthService authService;

    /**
     * 分页查询需求
     */
    public Page<ReqRequirement> list(String keyword, Long productId, String status, Integer priority,
                                    Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return requirementRepository.searchRequirements(keyword, productId, status, priority, pageRequest);
    }

    /**
     * 获取需求详情
     */
    public ReqRequirement getById(Long id) {
        return requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
    }

    /**
     * 创建需求
     */
    @Transactional
    public ReqRequirement create(ReqRequirement requirement) {
        // 生成需求编号
        String reqNo = generateReqNo();
        requirement.setReqNo(reqNo);
        requirement.setStatus(ReqRequirement.Status.COLLECTED);
        requirement.setCreatorId(authService.getCurrentUserId());
        return requirementRepository.save(requirement);
    }

    /**
     * 更新需求
     */
    @Transactional
    public ReqRequirement update(Long id, ReqRequirement requirement) {
        ReqRequirement existing = getById(id);
        
        if (requirement.getTitle() != null) existing.setTitle(requirement.getTitle());
        if (requirement.getDescription() != null) existing.setDescription(requirement.getDescription());
        if (requirement.getAcceptance() != null) existing.setAcceptance(requirement.getAcceptance());
        if (requirement.getType() != null) existing.setType(requirement.getType());
        if (requirement.getSource() != null) existing.setSource(requirement.getSource());
        if (requirement.getPriority() != null) existing.setPriority(requirement.getPriority());
        if (requirement.getEstimatePoint() != null) existing.setEstimatePoint(requirement.getEstimatePoint());
        if (requirement.getEstimateHours() != null) existing.setEstimateHours(requirement.getEstimateHours());
        if (requirement.getProductId() != null) existing.setProductId(requirement.getProductId());
        if (requirement.getProjectId() != null) existing.setProjectId(requirement.getProjectId());
        if (requirement.getOwnerId() != null) existing.setOwnerId(requirement.getOwnerId());
        
        return requirementRepository.save(existing);
    }

    /**
     * 更新需求状态
     */
    @Transactional
    public ReqRequirement updateStatus(Long id, String status) {
        ReqRequirement requirement = getById(id);
        requirement.setStatus(status);
        return requirementRepository.save(requirement);
    }

    /**
     * 删除需求
     */
    @Transactional
    public void delete(Long id) {
        if (!requirementRepository.existsById(id)) {
            throw new RuntimeException("需求不存在");
        }
        requirementRepository.deleteById(id);
    }

    /**
     * 按产品线统计需求
     */
    public List<ReqRequirement> getByProductId(Long productId) {
        return requirementRepository.findByProductId(productId, PageRequest.of(0, 100)).getContent();
    }

    /**
     * 按项目统计需求
     */
    public List<ReqRequirement> getByProjectId(Long projectId) {
        return requirementRepository.findByProjectId(projectId, PageRequest.of(0, 100)).getContent();
    }

    /**
     * 生成需求编号
     */
    private String generateReqNo() {
        String prefix = "REQ";
        String dateStr = java.time.LocalDate.now().toString().replace("-", "");
        long count = requirementRepository.count() + 1;
        return String.format("%s-%s-%04d", prefix, dateStr, count);
    }
}
