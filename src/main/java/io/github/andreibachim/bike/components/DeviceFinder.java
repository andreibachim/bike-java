package io.github.andreibachim.bike.components;

import io.github.andreibachim.bike.model.Device;
import io.github.andreibachim.bike.services.BluetoothService;
import io.github.andreibachim.bike.services.BluetoothServiceException;
import io.github.andreibachim.bike.services.bluetooth.device.DeviceDiscoveryListener;
import io.github.jwharm.javagi.base.Out;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import lombok.extern.slf4j.Slf4j;
import org.gnome.adw.NavigationPage;
import org.gnome.gio.ListStore;
import org.gnome.glib.MainContext;
import org.gnome.gtk.ListBox;

import java.lang.foreign.MemorySegment;

@Slf4j
@GtkTemplate(name = "DeviceFinder", ui = "/io/github/andreibachim/bike/ui/device-finder.ui")
public class DeviceFinder extends NavigationPage {

    @GtkChild
    public ListBox listBox;

    private final ListStore<Device> devices = new ListStore<>(Device.getType());

    public DeviceFinder(MemorySegment address) {
        super(address);
    }

    @InstanceInit
    public void init() {
        BluetoothService bluetoothService = BluetoothService.INSTANCE;
        listBox.bindModel(devices, (device) -> DeviceListing.create((Device) device));

        onShown(() -> {
            try {
                bluetoothService.startDeviceDiscovery();
            } catch (BluetoothServiceException e) {
                log.error("Could not start device discovery");
                // TODO Handle this properly
                System.exit(1);
            }

            try {
                bluetoothService.registerDeviceDiscoveryListener(
                        new DeviceDiscoveryListener() {
                            @Override
                            public void deviceFound(Device device) {
                                devices.stream()
                                        .filter(knownDevice -> knownDevice.getPath().equalsIgnoreCase(device.getPath()))
                                        .findFirst()
                                        .ifPresentOrElse(
                                                knownDevice -> {
                                                    MainContext.refThreadDefault().invoke(() -> {
                                                        knownDevice.setIconName(device.getIconName());
                                                        return false;
                                                    });
                                                },
                                                () -> devices.append(device));
                            }

                            @Override
                            public void deviceLost(String path) {
                                devices
                                        .stream()
                                        .filter(savedDevice -> savedDevice.getPath().equalsIgnoreCase(path))
                                        .findFirst()
                                        .ifPresent(savedDevice -> {
                                            Out<Integer> position = new Out<>();
                                            if (devices.find(savedDevice, position)) {
                                                devices.removeItem(position.get());
                                            }
                                        });
                            }
                        });
            } catch (BluetoothServiceException e) {
                log.error("Could not register device discovery listener", e);
                // TODO figure out how to gracefully handle this scenario
                System.exit(1);
            }
        });
        onHidden(() -> {
            log.info("Stopping device discovery");
            bluetoothService.stopDeviceDiscovery();
        });
    }
}
