package com.octl2.api.entity;

import jdk.jfr.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.repository.Temporal;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEntity {

    @Column(name = "createdate", updatable = false, nullable = false)
    @CreatedDate
    private LocalDateTime createDate;

    @Column(name = "modifydate")
    @LastModifiedDate
    private LocalDateTime modifyDate;

    @PrePersist
    protected void onCreate() {
        this.createDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifyDate = LocalDateTime.now();
    }
}
