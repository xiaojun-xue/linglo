package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 系统用户实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseEntity {

    /**
     * 用户名（唯一）
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码（加密存储）
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 昵称
     */
    @Column(name = "nickname", length = 100)
    private String nickname;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 头像URL
     */
    @Column(name = "avatar", length = 500)
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Column(name = "gender")
    private Integer gender;

    /**
     * 状态：0-禁用，1-正常
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 用户类型：0-普通用户，1-管理员
     */
    @Column(name = "user_type")
    @Builder.Default
    private Integer userType = 0;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    /**
     * 用户角色关联（多对多）
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "sys_user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<SysRole> roles = new HashSet<>();

    /**
     * 是否超级管理员
     */
    /**
     * 是否超级管理员
     */
    @Transient
    public boolean isAdmin() {
        return this.userType != null && this.userType == 1;
    }

    /**
     * 是否禁用
     */
    @Transient
    public boolean isDisabled() {
        return this.status == null || this.status == 0;
    }

    /**
     * 角色代码列表（用于接收前端参数，非数据库字段）
     */
    @Transient
    private Set<String> roleCodes;
}
