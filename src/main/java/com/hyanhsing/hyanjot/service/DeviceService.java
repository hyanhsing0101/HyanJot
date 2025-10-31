package com.hyanhsing.hyanjot.service;

import com.hyanhsing.hyanjot.entity.Device;
import java.util.List;
import java.util.Optional;

public interface DeviceService {
    // 注册设备
    Device registerDevice(Device device);

    // 根据Token查找设备（硬件认证用）
    Optional<Device> findByDeviceToken(String deviceToken);

    // 获取用户的所有设备
    List<Device> findByUserId(Long userId);

    // 更新设备最后在线时间
    void updateLastOnline(Long deviceId);

    // 删除设备
    void deleteDevice(Long id);
}