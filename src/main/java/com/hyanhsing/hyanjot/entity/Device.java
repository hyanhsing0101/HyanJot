package com.hyanhsing.hyanjot.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_name", nullable = false, length = 50)
    private String deviceName;

    @Column(name = "device_token", nullable = false, unique = true, length = 64)
    private String deviceToken;

    @Column(name = "mac_address", length = 20)
    private String macAddress;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status;

    @Column(name = "last_online")
    private LocalDateTime lastOnline;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}