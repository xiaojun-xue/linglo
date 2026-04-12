package com.ipd.platform.service;

import com.ipd.platform.entity.PrjProject;
import com.ipd.platform.entity.PrjTask;
import com.ipd.platform.entity.ReqRequirement;
import com.ipd.platform.repository.PrjProjectRepository;
import com.ipd.platform.repository.PrjTaskRepository;
import com.ipd.platform.repository.ReqRequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final PrjProjectRepository projectRepository;
    private final PrjTaskRepository taskRepository;
    private final ReqRequirementRepository requirementRepository;
    private final AuthService authService;

    /**
     * 分页查询项目
     */
    public Page<PrjProject> list(String keyword, Long productId, Integer status, Integer stage,
                                 Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return projectRepository.searchProjects(keyword, productId, status, stage, pageRequest);
    }

    /**
     * 获取项目详情
     */
    public PrjProject getById(Long id) {
        PrjProject project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        // 计算进度
        project.setProgress(calculateProgress(id));
        return project;
    }

    /**
     * 创建项目
     */
    @Transactional
    public PrjProject create(PrjProject project) {
        // 生成项目编号
        String projectNo = generateProjectNo();
        project.setProjectNo(projectNo);
        project.setStage(PrjProject.Stage.CONCEPT); // 默认概念阶段
        project.setStatus(PrjProject.Status.NOT_STARTED);
        project.setProgress(0);
        project.setCreatedBy(authService.getCurrentUserId());
        return projectRepository.save(project);
    }

    /**
     * 更新项目
     */
    @Transactional
    public PrjProject update(Long id, PrjProject project) {
        PrjProject existing = getById(id);
        
        if (project.getName() != null) existing.setName(project.getName());
        if (project.getDescription() != null) existing.setDescription(project.getDescription());
        if (project.getType() != null) existing.setType(project.getType());
        if (project.getStage() != null) existing.setStage(project.getStage());
        if (project.getStatus() != null) existing.setStatus(project.getStatus());
        if (project.getStartDate() != null) existing.setStartDate(project.getStartDate());
        if (project.getEndDate() != null) existing.setEndDate(project.getEndDate());
        if (project.getBudget() != null) existing.setBudget(project.getBudget());
        if (project.getManagerId() != null) existing.setManagerId(project.getManagerId());
        if (project.getProductId() != null) existing.setProductId(project.getProductId());
        
        return projectRepository.save(existing);
    }

    /**
     * 更新项目阶段
     */
    @Transactional
    public PrjProject updateStage(Long id, Integer stage) {
        PrjProject project = getById(id);
        project.setStage(stage);
        return projectRepository.save(project);
    }

    /**
     * 删除项目
     */
    @Transactional
    public void delete(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("项目不存在");
        }
        projectRepository.deleteById(id);
    }

    /**
     * 获取项目统计
     */
    public Map<String, Object> getStatistics(Long projectId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 需求统计
        List<ReqRequirement> requirements = requirementRepository.findByProjectId(projectId, PageRequest.of(0, 1)).getContent();
        long totalReqs = requirementRepository.countByProjectIdAndStatus(projectId, ReqRequirement.Status.DEVELOP);
        stats.put("totalRequirements", totalReqs);
        
        // 任务统计
        long totalTasks = taskRepository.countBySprintId(projectId);
        long doneTasks = taskRepository.countBySprintIdAndStatus(projectId, PrjTask.Status.DONE);
        stats.put("totalTasks", totalTasks);
        stats.put("doneTasks", doneTasks);
        stats.put("todoTasks", totalTasks - doneTasks);
        
        // 进度
        stats.put("progress", calculateProgress(projectId));
        
        return stats;
    }

    /**
     * 计算项目进度
     */
    private int calculateProgress(Long projectId) {
        List<PrjTask> tasks = taskRepository.findByProjectId(projectId, PageRequest.of(0, 1000)).getContent();
        if (tasks.isEmpty()) return 0;
        
        long doneCount = tasks.stream()
                .filter(t -> PrjTask.Status.DONE.equals(t.getStatus()) || PrjTask.Status.CLOSED.equals(t.getStatus()))
                .count();
        
        return (int) (doneCount * 100 / tasks.size());
    }

    /**
     * 生成项目编号
     */
    private String generateProjectNo() {
        String prefix = "PRJ";
        String dateStr = java.time.LocalDate.now().toString().replace("-", "");
        long count = projectRepository.count() + 1;
        return String.format("%s-%s-%04d", prefix, dateStr, count);
    }
}
