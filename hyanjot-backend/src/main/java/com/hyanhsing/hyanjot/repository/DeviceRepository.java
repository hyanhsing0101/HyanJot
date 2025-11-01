package com.hyanhsing.hyanjot.repository;

import com.hyanhsing.hyanjot.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    // 根据用户ID查找设备列表
    List<Device> findByUserId(Long userId);

    // 根据设备Token查找（硬件认证用）
    Optional<Device> findByDeviceToken(String deviceToken);
}