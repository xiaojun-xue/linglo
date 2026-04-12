package com.ipd.platform.repository;

import com.ipd.platform.entity.MdmProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 产品线 Repository
 */
@Repository
public interface MdmProductRepository extends JpaRepository<MdmProduct, Long> {

    Optional<MdmProduct> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT p FROM MdmProduct p WHERE p.status != 4 ORDER BY p.createdAt DESC")
    List<MdmProduct> findAllActive();

    List<MdmProduct> findByOwnerId(Long ownerId);
}
