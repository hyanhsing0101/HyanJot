package com.hyanhsing.hyanjot.controller;

import com.hyanhsing.hyanjot.entity.Device;
import com.hyanhsing.hyanjot.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device")
@CrossOrigin(origins = "*")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    /**
     * 注册设备
     */
    @PostMapping("/register")
    public ResponseEntity<Device> registerDevice(@RequestBody Device device) {
        Device registered = deviceService.registerDevice(device);
        return ResponseEntity.ok(registered);
    }

    /**
     * 根据Token验证设备（硬件端调用）
     */
    @GetMapping("/auth/{token}")
    public ResponseEntity<Map<String, Object>> authDevice(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        return deviceService.findByDeviceToken(token)
                .map(device -> {
                    // 更新最后在线时间
                    deviceService.updateLastOnline(device.getId());
                    response.put("success", true);
                    response.put("device", device);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "设备未授权");
                    return ResponseEntity.status(401).body(response);
                });
    }

    /**
     * 获取用户的所有设备
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Device>> getDevicesByUserId(@PathVariable Long userId) {
        List<Device> devices = deviceService.findByUserId(userId);
        return ResponseEntity.ok(devices);
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDevice(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            deviceService.deleteDevice(id);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}