package io.github.andreibachim.bike.model;

import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;
import io.github.jwharm.javagi.gobject.annotations.Property;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.gnome.glib.MainContext;
import org.gnome.gobject.GObject;

@Slf4j
public class Device extends GObject {

    @Getter
    private BluetoothDevice rawDevice;

    private String iconName;

    public static Device create(BluetoothDevice rawDevice) {
        Device device = Device.newInstance(Device.class);
        device.rawDevice = rawDevice;
        MainContext.refThreadDefault()
            .invoke(() -> {
                device.setIconName(
                    device.getIconNameFromRssi(rawDevice.getRssi())
                );
                return false;
            });
        return device;
    }

    @Property(name = "name")
    public String getName() {
        return rawDevice.getName();
    }

    public Short getRssi() {
        return rawDevice.getRssi();
    }

    public String getPath() {
        return rawDevice.getDbusPath();
    }

    public boolean isPaired() {
        return rawDevice.isPaired();
    }

    public boolean isConnected() {
        return rawDevice.isConnected();
    }

    @Property(name = "status")
    public String getStatus() {
        if (isConnected()) return "Connected";
        if (isPaired()) return "Disconnected";
        return "Not Set Up";
    }

    @Property
    public String getIconName() {
        return iconName;
    }

    @Property
    public void setIconName(String iconName) {
        this.iconName = iconName;
        notify_("icon-name");
    }

    private String getIconNameFromRssi(Short rssi) {
        if (Objects.isNull(rssi)) return "network-cellular-offline-symbolic";
        return switch (rssi) {
            case Short value when (
                value >= -119 && value <= -90
            ) -> "network-cellular-signal-weak-symbolic";
            case Short value when (
                value <= -60
            ) -> "network-cellular-signal-ok-symbolic";
            case Short value when (
                value <= -30
            ) -> "network-cellular-signal-good-symbolic";
            case Short value when (
                value <= 0
            ) -> "network-cellular-signal-excellent-symbolic";
            default -> "network-cellular-offline-symbolic";
        };
    }

    public Device(MemorySegment address) {
        super(address);
    }
}
