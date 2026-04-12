package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 测试用例实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "qa_test_case")
public class QaTestCase extends BaseEntity {

    /**
     * 用例编号
     */
    @Column(name = "case_no", nullable = false, unique = true, length = 50)
    private String caseNo;

    /**
     * 用例标题
     */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /**
     * 前置条件
     */
    @Column(name = "precondition", columnDefinition = "TEXT")
    private String precondition;

    /**
     * 测试步骤
     */
    @Column(name = "steps", columnDefinition = "TEXT")
    private String steps;

    /**
     * 预期结果
     */
    @Column(name = "expected_result", columnDefinition = "TEXT")
    private String expectedResult;

    /**
     * 所属模块
     */
    @Column(name = "module", length = 100)
    private String module;

    /**
     * 用例类型：1-功能测试，2-接口测试，3-性能测试，4-安全测试
     */
    @Column(name = "type")
    @Builder.Default
    private Integer type = 1;

    /**
     * 优先级：1-高，2-中，3-低
     */
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 2;

    /**
     * 状态：1-设计，2-待评审，3-评审通过，4-禁用
     */
    @Column(name = "status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 关联项目ID
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 关联需求ID
     */
    @Column(name = "requirement_id")
    private Long requirementId;

    /**
     * 创建人
     */
    @Column(name = "creator_id")
    private Long creatorId;

    /**
     * 创建人姓名
     */
    @Transient
    private String creatorName;
}
