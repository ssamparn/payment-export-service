package com.payment.export.platform.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "batch",
        uniqueConstraints = {
            @UniqueConstraint(
                    columnNames = {"job_id", "encrypted_batch_id"}
            )
})
public class BatchEntity {

    @Id
    private Long id;
}
