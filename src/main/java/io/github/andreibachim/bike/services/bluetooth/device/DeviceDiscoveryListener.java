package io.github.andreibachim.bike.services.bluetooth.device;

import io.github.andreibachim.bike.model.Device;

public interface DeviceDiscoveryListener {
    void deviceFound(Device device);
    void deviceLost(String path);
}
