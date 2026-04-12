package com.ipd.platform.service;

import com.ipd.platform.entity.PrjSprint;
import com.ipd.platform.entity.PrjTask;
import com.ipd.platform.repository.PrjSprintRepository;
import com.ipd.platform.repository.PrjTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Sprint 服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SprintService {

    private final PrjSprintRepository sprintRepository;
    private final PrjTaskRepository taskRepository;
    private final AuthService authService;

    /**
     * 获取项目的所有 Sprint
     */
    public List<PrjSprint> getByProjectId(Long projectId) {
        return sprintRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    /**
     * 获取 Sprint 详情
     */
    public PrjSprint getById(Long id) {
        return sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint 不存在"));
    }

    /**
     * 创建 Sprint
     */
    @Transactional
    public PrjSprint create(PrjSprint sprint) {
        String sprintNo = generateSprintNo();
        sprint.setSprintNo(sprintNo);
        sprint.setStatus(PrjSprint.Status.PLANNING);
        sprint.setCreatedBy(authService.getCurrentUserId());
        return sprintRepository.save(sprint);
    }

    /**
     * 更新 Sprint
     */
    @Transactional
    public PrjSprint update(Long id, PrjSprint sprint) {
        PrjSprint existing = getById(id);
        
        if (sprint.getName() != null) existing.setName(sprint.getName());
        if (sprint.getGoal() != null) existing.setGoal(sprint.getGoal());
        if (sprint.getStartDate() != null) existing.setStartDate(sprint.getStartDate());
        if (sprint.getEndDate() != null) existing.setEndDate(sprint.getEndDate());
        
        return sprintRepository.save(existing);
    }

    /**
     * 开始 Sprint
     */
    @Transactional
    public PrjSprint start(Long id) {
        PrjSprint sprint = getById(id);
        if (sprint.getStatus() != PrjSprint.Status.PLANNING) {
            throw new RuntimeException("只有规划中的 Sprint 才能开始");
        }
        
        // 统计该 Sprint 的任务数和故事点
        var tasks = taskRepository.findBySprintId(id, org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();
        sprint.setTotalTasks(tasks.size());
        
        BigDecimal totalPoints = tasks.stream()
                .map(t -> t.getEstimateHours() != null ? t.getEstimateHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        sprint.setTotalPoint(totalPoints);
        
        sprint.setStatus(PrjSprint.Status.IN_PROGRESS);
        return sprintRepository.save(sprint);
    }

    /**
     * 完成 Sprint
     */
    @Transactional
    public PrjSprint complete(Long id) {
        PrjSprint sprint = getById(id);
        if (sprint.getStatus() != PrjSprint.Status.IN_PROGRESS) {
            throw new RuntimeException("只有进行中的 Sprint 才能完成");
        }
        
        // 统计已完成的故事点
        var tasks = taskRepository.findBySprintId(id, org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();
        BigDecimal completedPoints = tasks.stream()
                .filter(t -> "done".equals(t.getStatus()) || "closed".equals(t.getStatus()))
                .map(t -> t.getEstimateHours() != null ? t.getEstimateHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        sprint.setCompletedPoint(completedPoints);
        sprint.setCompletedTasks((int) tasks.stream()
                .filter(t -> "done".equals(t.getStatus()) || "closed".equals(t.getStatus())).count());
        
        sprint.setStatus(PrjSprint.Status.COMPLETED);
        return sprintRepository.save(sprint);
    }

    /**
     * 关闭 Sprint
     */
    @Transactional
    public PrjSprint close(Long id) {
        PrjSprint sprint = getById(id);
        sprint.setStatus(PrjSprint.Status.CLOSED);
        return sprintRepository.save(sprint);
    }

    /**
     * 获取燃尽图数据
     */
    public Map<String, Object> getBurndownData(Long sprintId) {
        PrjSprint sprint = getById(sprintId);
        var tasks = taskRepository.findBySprintId(sprintId, org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();
        
        // 计算总故事点
        BigDecimal totalPoints = tasks.stream()
                .map(t -> t.getEstimateHours() != null ? t.getEstimateHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 按日期统计完成的故事点
        Map<String, BigDecimal> dailyBurndown = new LinkedHashMap<>();
        if (sprint.getStartDate() != null && sprint.getEndDate() != null) {
            LocalDate current = sprint.getStartDate();
            BigDecimal remaining = totalPoints;
            
            while (!current.isAfter(sprint.getEndDate())) {
                dailyBurndown.put(current.toString(), remaining);
                
                // 计算当天完成的故事点
                final LocalDate dateForLambda = current;
                BigDecimal doneToday = tasks.stream()
                        .filter(t -> t.getCompletedAt() != null)
                        .filter(t -> t.getCompletedAt().toLocalDate().equals(dateForLambda))
                        .map(t -> t.getEstimateHours() != null ? t.getEstimateHours() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                remaining = remaining.subtract(doneToday);
                current = current.plusDays(1);
            }
        }
        
        // 当前完成的故事点
        BigDecimal completedPoints = tasks.stream()
                .filter(t -> "done".equals(t.getStatus()) || "closed".equals(t.getStatus()))
                .map(t -> t.getEstimateHours() != null ? t.getEstimateHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> result = new HashMap<>();
        result.put("sprintName", sprint.getName());
        result.put("startDate", sprint.getStartDate());
        result.put("endDate", sprint.getEndDate());
        result.put("totalPoints", totalPoints);
        result.put("completedPoints", completedPoints);
        result.put("remainingPoints", totalPoints.subtract(completedPoints));
        result.put("totalTasks", tasks.size());
        result.put("completedTasks", tasks.stream().filter(t -> "done".equals(t.getStatus()) || "closed".equals(t.getStatus())).count());
        result.put("dailyBurndown", dailyBurndown);
        
        // 计算理想燃尽线
        List<Map<String, Object>> idealLine = new ArrayList<>();
        if (sprint.getStartDate() != null && sprint.getEndDate() != null) {
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate()) + 1;
            BigDecimal dailyBurn = totalPoints.divide(BigDecimal.valueOf(totalDays), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal idealRemaining = totalPoints;
            
            for (long i = 0; i <= totalDays; i++) {
                Map<String, Object> point = new HashMap<>();
                point.put("day", i);
                point.put("remaining", idealRemaining.max(BigDecimal.ZERO));
                idealLine.add(point);
                idealRemaining = idealRemaining.subtract(dailyBurn);
            }
        }
        result.put("idealLine", idealLine);
        
        return result;
    }

    /**
     * 生成 Sprint 编号
     */
    private String generateSprintNo() {
        String prefix = "SPRINT";
        long count = sprintRepository.count() + 1;
        return String.format("%S-%05d", prefix, count);
    }
}
