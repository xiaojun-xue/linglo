package com.ipd.platform.repository;

import com.ipd.platform.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限 Repository
 */
@Repository
public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {

    /**
     * 根据权限代码查找权限
     */
    Optional<SysPermission> findByCode(String code);

    /**
     * 查询所有菜单权限（按排序号排序）
     */
    List<SysPermission> findByTypeOrderBySortOrderAsc(Integer type);

    /**
     * 查询所有启用的菜单
     */
    List<SysPermission> findByTypeAndStatusOrderBySortOrderAsc(Integer type, Integer status);
}
