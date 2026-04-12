package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 产品线实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "mdm_product")
public class MdmProduct extends BaseEntity {

    /**
     * 产品线名称
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 产品线代码（唯一）
     */
    @Column(name = "code", unique = true, length = 50)
    private String code;

    /**
     * 产品线描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 产品负责人ID
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * 产品负责人（不映射数据库）
     */
    @Transient
    private String ownerName;

    /**
     * 产品线状态：1-规划中，2-开发中，3-维护中，4-已下线
     */
    @Column(name = "status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 产品线类型：1-软件，2-硬件，3-混合
     */
    @Column(name = "product_type")
    @Builder.Default
    private Integer productType = 1;

    /**
     * 产品线状态枚举
     */
    public static final class Status {
        public static final int PLANNING = 1;    // 规划中
        public static final int DEVELOPING = 2;  // 开发中
        public static final int MAINTAINING = 3;  // 维护中
        public static final int DEPRECATED = 4;   // 已下线
    }
}
