package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 文档实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "doc_document")
public class DocDocument extends BaseEntity {

    /**
     * 文档标题
     */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /**
     * 文档内容（富文本/Markdown）
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 文档类型：1- Markdown，2- 富文本
     */
    @Column(name = "doc_type")
    @Builder.Default
    private Integer docType = 1;

    /**
     * 文档分类：1-项目文档，2-需求文档，3-技术文档，4-流程文档，5-模板，6-其他
     */
    @Column(name = "category")
    @Builder.Default
    private Integer category = 1;

    /**
     * 所属项目ID
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 所属需求ID
     */
    @Column(name = "requirement_id")
    private Long requirementId;

    /**
     * 父文档ID（用于目录结构）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 文档路径（用于树形结构展示）
     */
    @Column(name = "path", length = 500)
    private String path;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 是否为目录：0-文件，1-目录
     */
    @Column(name = "is_folder")
    @Builder.Default
    private Boolean isFolder = false;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文件类型
     */
    @Column(name = "file_type", length = 50)
    private String fileType;

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

    /**
     * 文档分类枚举
     */
    public static final class Category {
        public static final int PROJECT = 1;   // 项目文档
        public static final int REQUIREMENT = 2; // 需求文档
        public static final int TECHNICAL = 3;  // 技术文档
        public static final int PROCESS = 4;    // 流程文档
        public static final int TEMPLATE = 5;   // 模板
        public static final int OTHER = 6;      // 其他

        public static String getName(Integer category) {
            if (category == null) return "未知";
            return switch (category) {
                case PROJECT -> "项目文档";
                case REQUIREMENT -> "需求文档";
                case TECHNICAL -> "技术文档";
                case PROCESS -> "流程文档";
                case TEMPLATE -> "模板";
                case OTHER -> "其他";
                default -> "未知";
            };
        }
    }
}
