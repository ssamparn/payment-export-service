package com.payment.export.platform.persistence.repository;

import com.payment.export.platform.persistence.entity.BatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BatchJpaRepository extends JpaRepository<BatchEntity, UUID> {

}

