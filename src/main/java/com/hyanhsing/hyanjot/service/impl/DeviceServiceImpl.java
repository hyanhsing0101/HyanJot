package com.hyanhsing.hyanjot.service.impl;

import com.hyanhsing.hyanjot.entity.Device;
import com.hyanhsing.hyanjot.repository.DeviceRepository;
import com.hyanhsing.hyanjot.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public Device registerDevice(Device device) {
        // 设置默认状态
        if (device.getStatus() == null) {
            device.setStatus(1);
        }
        return deviceRepository.save(device);
    }

    @Override
    public Optional<Device> findByDeviceToken(String deviceToken) {
        return deviceRepository.findByDeviceToken(deviceToken);
    }

    @Override
    public List<Device> findByUserId(Long userId) {
        return deviceRepository.findByUserId(userId);
    }

    @Override
    public void updateLastOnline(Long deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setLastOnline(LocalDateTime.now());
            deviceRepository.save(device);
        }
    }

    @Override
    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }
}