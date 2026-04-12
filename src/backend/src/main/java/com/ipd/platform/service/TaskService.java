package com.ipd.platform.service;

import com.ipd.platform.entity.PrjTask;
import com.ipd.platform.repository.PrjTaskRepository;
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
 * 任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final PrjTaskRepository taskRepository;
    private final AuthService authService;

    /**
     * 分页查询任务
     */
    public Page<PrjTask> list(String keyword, Long projectId, Long sprintId, Long assigneeId,
                              String status, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return taskRepository.searchTasks(keyword, projectId, sprintId, assigneeId, status, pageRequest);
    }

    /**
     * 获取任务详情
     */
    public PrjTask getById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
    }

    /**
     * 创建任务
     */
    @Transactional
    public PrjTask create(PrjTask task) {
        String taskNo = generateTaskNo();
        task.setTaskNo(taskNo);
        task.setStatus(PrjTask.Status.TODO);
        task.setCreatedBy(authService.getCurrentUserId());
        return taskRepository.save(task);
    }

    /**
     * 更新任务
     */
    @Transactional
    public PrjTask update(Long id, PrjTask task) {
        PrjTask existing = getById(id);
        
        if (task.getTitle() != null) existing.setTitle(task.getTitle());
        if (task.getDescription() != null) existing.setDescription(task.getDescription());
        if (task.getType() != null) existing.setType(task.getType());
        if (task.getPriority() != null) existing.setPriority(task.getPriority());
        if (task.getProjectId() != null) existing.setProjectId(task.getProjectId());
        if (task.getSprintId() != null) existing.setSprintId(task.getSprintId());
        if (task.getRequirementId() != null) existing.setRequirementId(task.getRequirementId());
        if (task.getParentId() != null) existing.setParentId(task.getParentId());
        if (task.getAssigneeId() != null) existing.setAssigneeId(task.getAssigneeId());
        if (task.getEstimateHours() != null) existing.setEstimateHours(task.getEstimateHours());
        if (task.getActualHours() != null) existing.setActualHours(task.getActualHours());
        if (task.getStartDate() != null) existing.setStartDate(task.getStartDate());
        if (task.getDueDate() != null) existing.setDueDate(task.getDueDate());
        if (task.getSortOrder() != null) existing.setSortOrder(task.getSortOrder());
        
        return taskRepository.save(existing);
    }

    /**
     * 更新任务状态
     */
    @Transactional
    public PrjTask updateStatus(Long id, String status) {
        PrjTask task = getById(id);
        String oldStatus = task.getStatus();
        task.setStatus(status);
        
        // 完成时记录完成时间
        if (PrjTask.Status.DONE.equals(status) || PrjTask.Status.CLOSED.equals(status)) {
            task.setCompletedAt(LocalDateTime.now());
        }
        
        PrjTask saved = taskRepository.save(task);
        log.info("任务 {} 状态从 {} 更新为 {}", task.getTaskNo(), oldStatus, status);
        return saved;
    }

    /**
     * 分配任务
     */
    @Transactional
    public PrjTask assign(Long id, Long assigneeId) {
        PrjTask task = getById(id);
        task.setAssigneeId(assigneeId);
        return taskRepository.save(task);
    }

    /**
     * 删除任务
     */
    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("任务不存在");
        }
        taskRepository.deleteById(id);
    }

    /**
     * 获取看板视图数据
     */
    public Map<String, List<PrjTask>> getBoardView(Long projectId, Long sprintId) {
        List<PrjTask> tasks = taskRepository.searchTasks(
                null, projectId, sprintId, null, null, PageRequest.of(0, 1000)
        ).getContent();

        Map<String, List<PrjTask>> board = new HashMap<>();
        board.put("todo", tasks.stream().filter(t -> PrjTask.Status.TODO.equals(t.getStatus())).toList());
        board.put("in_progress", tasks.stream().filter(t -> PrjTask.Status.IN_PROGRESS.equals(t.getStatus())).toList());
        board.put("in_test", tasks.stream().filter(t -> PrjTask.Status.IN_TEST.equals(t.getStatus())).toList());
        board.put("done", tasks.stream().filter(t -> PrjTask.Status.DONE.equals(t.getStatus()) || PrjTask.Status.CLOSED.equals(t.getStatus())).toList());
        
        return board;
    }

    /**
     * 获取燃尽图数据
     */
    public Map<String, Object> getBurndownData(Long sprintId) {
        List<PrjTask> tasks = taskRepository.findBySprintId(sprintId, PageRequest.of(0, 1000)).getContent();
        
        int totalPoints = tasks.stream()
                .mapToInt(t -> t.getEstimateHours() != null ? t.getEstimateHours().intValue() : 0)
                .sum();
        
        int donePoints = tasks.stream()
                .filter(t -> PrjTask.Status.DONE.equals(t.getStatus()) || PrjTask.Status.CLOSED.equals(t.getStatus()))
                .mapToInt(t -> t.getEstimateHours() != null ? t.getEstimateHours().intValue() : 0)
                .sum();
        
        Map<String, Object> data = new HashMap<>();
        data.put("totalPoints", totalPoints);
        data.put("donePoints", donePoints);
        data.put("remainingPoints", totalPoints - donePoints);
        data.put("completionRate", totalPoints > 0 ? (donePoints * 100 / totalPoints) : 0);
        
        return data;
    }

    /**
     * 生成任务编号
     */
    private String generateTaskNo() {
        String prefix = "TASK";
        long count = taskRepository.count() + 1;
        return String.format("%s-%05d", prefix, count);
    }
}
