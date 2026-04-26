package com.ipd.platform.service;

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

import java.util.*;
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
     * 分页查询需求（支持按层级筛选）
     */
    public Page<ReqRequirement> list(String keyword, Long productId, Long projectId, String status, Integer priority,
                                    Integer level, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return requirementRepository.searchRequirements(keyword, productId, projectId, status, priority, level, pageRequest);
    }

    /**
     * 获取需求详情
     */
    public ReqRequirement getById(Long id) {
        return requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
    }

    /**
     * 获取项目下的需求树（IR→SR→AR层级结构）
     */
    public List<Map<String, Object>> getTree(Long projectId) {
        List<ReqRequirement> all = requirementRepository.findByProjectIdOrderByLevelAscCreatedAtAsc(projectId);

        // 按parentId分组
        Map<Long, List<ReqRequirement>> childrenMap = all.stream()
                .filter(r -> r.getParentId() != null)
                .collect(Collectors.groupingBy(ReqRequirement::getParentId));

        // 顶层IR（parentId为null）
        List<ReqRequirement> roots = all.stream()
                .filter(r -> r.getParentId() == null)
                .collect(Collectors.toList());

        return roots.stream()
                .map(root -> buildTreeNode(root, childrenMap))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildTreeNode(ReqRequirement req, Map<Long, List<ReqRequirement>> childrenMap) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", req.getId());
        node.put("reqNo", req.getReqNo());
        node.put("title", req.getTitle());
        node.put("level", req.getLevel());
        node.put("status", req.getStatus());
        node.put("priority", req.getPriority());
        node.put("type", req.getType());
        node.put("description", req.getDescription());
        node.put("parentId", req.getParentId());
        node.put("projectId", req.getProjectId());
        // 5W2H
        node.put("who", req.getWho());
        node.put("what", req.getWhat());
        node.put("whenDesc", req.getWhenDesc());
        node.put("whereDesc", req.getWhereDesc());
        node.put("whyDesc", req.getWhyDesc());
        node.put("howDesc", req.getHowDesc());
        node.put("howMuch", req.getHowMuch());
        node.put("acceptance", req.getAcceptance());
        node.put("source", req.getSource());
        node.put("createdAt", req.getCreatedAt());

        List<ReqRequirement> children = childrenMap.getOrDefault(req.getId(), Collections.emptyList());
        node.put("children", children.stream()
                .map(child -> buildTreeNode(child, childrenMap))
                .collect(Collectors.toList()));

        return node;
    }

    /**
     * 获取子需求列表
     */
    public List<ReqRequirement> getChildren(Long parentId) {
        return requirementRepository.findByParentIdOrderByCreatedAtAsc(parentId);
    }

    /**
     * 创建需求
     */
    @Transactional
    public ReqRequirement create(ReqRequirement requirement) {
        // 校验层级关系
        if (requirement.getParentId() != null) {
            ReqRequirement parent = getById(requirement.getParentId());
            int expectedLevel = parent.getLevel() + 1;
            if (requirement.getLevel() == null || requirement.getLevel() != expectedLevel) {
                requirement.setLevel(expectedLevel);
            }
            if (expectedLevel > ReqRequirement.Level.AR) {
                throw new RuntimeException("AR是最底层需求，不能继续分解");
            }
            // 继承项目ID
            if (requirement.getProjectId() == null) {
                requirement.setProjectId(parent.getProjectId());
            }
        } else {
            // 顶层默认为IR
            if (requirement.getLevel() == null) {
                requirement.setLevel(ReqRequirement.Level.IR);
            }
        }

        String reqNo = generateReqNo(requirement.getLevel());
        requirement.setReqNo(reqNo);
        requirement.setStatus(ReqRequirement.Status.NEW);
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

        // 5W2H fields（IR层级）
        if (existing.getLevel() == ReqRequirement.Level.IR) {
            if (requirement.getWho() != null) existing.setWho(requirement.getWho());
            if (requirement.getWhat() != null) existing.setWhat(requirement.getWhat());
            if (requirement.getWhenDesc() != null) existing.setWhenDesc(requirement.getWhenDesc());
            if (requirement.getWhereDesc() != null) existing.setWhereDesc(requirement.getWhereDesc());
            if (requirement.getWhyDesc() != null) existing.setWhyDesc(requirement.getWhyDesc());
            if (requirement.getHowDesc() != null) existing.setHowDesc(requirement.getHowDesc());
            if (requirement.getHowMuch() != null) existing.setHowMuch(requirement.getHowMuch());
        }

        return requirementRepository.save(existing);
    }

    /**
     * 推进需求状态到下一阶段
     */
    @Transactional
    public ReqRequirement advanceStatus(Long id) {
        ReqRequirement requirement = getById(id);
        String next = ReqRequirement.Status.getNextStatus(requirement.getStatus());
        if (next == null) {
            throw new RuntimeException("当前状态无法继续推进");
        }
        requirement.setStatus(next);
        return requirementRepository.save(requirement);
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
     * 删除需求（级联删除子需求）
     */
    @Transactional
    public void delete(Long id) {
        if (!requirementRepository.existsById(id)) {
            throw new RuntimeException("需求不存在");
        }
        // 级联删除子需求
        List<ReqRequirement> children = requirementRepository.findByParentIdOrderByCreatedAtAsc(id);
        for (ReqRequirement child : children) {
            delete(child.getId());
        }
        requirementRepository.deleteById(id);
    }

    /**
     * 导出系统需求分析说明书（基于IR列表，包含其下SR）
     */
    public Map<String, Object> exportSystemSpec(List<Long> irIds) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("title", "系统需求分析说明书");
        doc.put("generatedAt", java.time.LocalDateTime.now().toString());

        List<Map<String, Object>> sections = new ArrayList<>();
        for (Long irId : irIds) {
            ReqRequirement ir = getById(irId);
            if (ir.getLevel() != ReqRequirement.Level.IR) continue;

            Map<String, Object> section = new LinkedHashMap<>();
            section.put("ir", ir);
            // 5W2H结构化描述
            Map<String, String> w5h2 = new LinkedHashMap<>();
            w5h2.put("Who（目标用户）", ir.getWho() != null ? ir.getWho() : "-");
            w5h2.put("What（需要什么）", ir.getWhat() != null ? ir.getWhat() : "-");
            w5h2.put("When（时间要求）", ir.getWhenDesc() != null ? ir.getWhenDesc() : "-");
            w5h2.put("Where（使用场景）", ir.getWhereDesc() != null ? ir.getWhereDesc() : "-");
            w5h2.put("Why（业务动机）", ir.getWhyDesc() != null ? ir.getWhyDesc() : "-");
            w5h2.put("How（实现路径）", ir.getHowDesc() != null ? ir.getHowDesc() : "-");
            w5h2.put("How Much（规模范围）", ir.getHowMuch() != null ? ir.getHowMuch() : "-");
            section.put("structured", w5h2);

            // 关联的SR
            List<ReqRequirement> srs = requirementRepository.findByParentIdOrderByCreatedAtAsc(irId);
            section.put("systemRequirements", srs);
            sections.add(section);
        }
        doc.put("sections", sections);
        return doc;
    }

    /**
     * 导出功能需求分析说明书（基于SR列表，包含其下AR）
     */
    public Map<String, Object> exportFuncSpec(List<Long> srIds) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("title", "功能需求分析说明书");
        doc.put("generatedAt", java.time.LocalDateTime.now().toString());

        List<Map<String, Object>> sections = new ArrayList<>();
        for (Long srId : srIds) {
            ReqRequirement sr = getById(srId);
            if (sr.getLevel() != ReqRequirement.Level.SR) continue;

            Map<String, Object> section = new LinkedHashMap<>();
            section.put("sr", sr);

            // 关联的AR
            List<ReqRequirement> ars = requirementRepository.findByParentIdOrderByCreatedAtAsc(srId);
            section.put("allocationRequirements", ars);

            // 追溯到父IR
            if (sr.getParentId() != null) {
                try {
                    section.put("parentIr", getById(sr.getParentId()));
                } catch (RuntimeException ignored) {}
            }
            sections.add(section);
        }
        doc.put("sections", sections);
        return doc;
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
     * 生成需求编号（按层级使用不同前缀）
     */
    private String generateReqNo(Integer level) {
        String prefix = ReqRequirement.Level.getPrefix(level);
        String dateStr = java.time.LocalDate.now().toString().replace("-", "");
        long count = requirementRepository.count() + 1;
        return String.format("%s-%s-%04d", prefix, dateStr, count);
    }
}
