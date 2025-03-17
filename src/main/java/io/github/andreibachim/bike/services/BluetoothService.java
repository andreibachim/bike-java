package io.github.andreibachim.bike.services;

import com.github.hypfvieh.bluetooth.DeviceManager;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;
import io.github.andreibachim.bike.model.Device;
import io.github.andreibachim.bike.services.bluetooth.adapter.AdapterStateListener;
import io.github.andreibachim.bike.services.bluetooth.device.DeviceDiscoveryListener;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.handlers.AbstractInterfacesAddedHandler;
import org.freedesktop.dbus.handlers.AbstractInterfacesRemovedHandler;
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler;
import org.freedesktop.dbus.interfaces.ObjectManager;
import org.freedesktop.dbus.interfaces.Properties.PropertiesChanged;
import org.freedesktop.dbus.types.Variant;

@Slf4j
public enum BluetoothService {
    INSTANCE;

    private Optional<DeviceManager> deviceManager;
    private Optional<BluetoothAdapter> bluetoothAdapter;

    private BluetoothService() {
        try {
            final DeviceManager deviceManager = DeviceManager.createInstance(
                false
            );
            this.deviceManager = Optional.of(deviceManager);
            bluetoothAdapter = Optional.ofNullable(deviceManager.getAdapter());
        } catch (final Exception e) {
            this.deviceManager = Optional.empty();
        }
    }

    public boolean isAdapterOn() throws BluetoothServiceException {
        return getAdapter().isPowered();
    }

    public void registerAdapterStateListener(AdapterStateListener listener)
        throws BluetoothServiceException {
        final DeviceManager manager = deviceManager.orElseThrow(() ->
            new BluetoothServiceException("No device manager found")
        );
        try {
            manager.registerPropertyHandler(
                new AbstractPropertiesChangedHandler() {
                    @Override
                    public void handle(final PropertiesChanged signal) {
                        final Variant<?> poweredVariant = signal
                            .getPropertiesChanged()
                            .get("Powered");
                        if (Objects.nonNull(poweredVariant)) {
                            boolean powered =
                                (boolean) poweredVariant.getValue();
                            if (powered) {
                                listener.powerOn();
                            } else {
                                listener.powerOff();
                            }
                        }
                    }
                }
            );
        } catch (final Exception e) {
            throw new BluetoothServiceException(
                "Could not register adapter state listener"
            );
        }
    }

    public void startDeviceDiscovery() throws BluetoothServiceException {
        log.debug("Starting device discovery...");
        final BluetoothAdapter adapter = getAdapter();
        if (adapter.startDiscovery()) log.debug("Device discovery started");
    }

    public void stopDeviceDiscovery() {
        try {
            log.debug("Stopping device discovery...");
            final BluetoothAdapter adapter = getAdapter();
            if (adapter.stopDiscovery()) log.debug("Device discovery stopped");
        } catch (BluetoothServiceException e) {
            log.error(
                "Could not stop device discovery. Shutting down the app",
                e
            );
            System.exit(1);
        }
    }

    public void registerDeviceDiscoveryListener(
        DeviceDiscoveryListener listener
    ) throws BluetoothServiceException {
        final DeviceManager manager = deviceManager.orElseThrow(() ->
            new BluetoothServiceException("No device manager found")
        );

        List.copyOf(manager.getDevices(true))
            .stream()
            .filter(this::filterDevicesWithNoName)
            .map(Device::create)
            .forEach(listener::deviceFound);

        try {
            manager.registerSignalHandler(
                new AbstractInterfacesAddedHandler() {
                    @Override
                    public void handle(ObjectManager.InterfacesAdded signal) {
                        DBusPath signalSource = signal.getSignalSource();
                        manager
                            .getDevices(true)
                            .stream()
                            .filter(device ->
                                device
                                    .getDbusPath()
                                    .equals(signalSource.toString())
                            )
                            .filter(
                                BluetoothService.this::filterDevicesWithNoName
                            )
                            .findFirst()
                            .map(Device::create)
                            .ifPresent(listener::deviceFound);
                    }
                }
            );
            manager.registerSignalHandler(
                new AbstractInterfacesRemovedHandler() {
                    @Override
                    public void handle(ObjectManager.InterfacesRemoved signal) {
                        listener.deviceLost(
                            signal.getSignalSource().toString()
                        );
                    }
                }
            );

            manager.registerSignalHandler(
                new AbstractPropertiesChangedHandler() {
                    @Override
                    public void handle(PropertiesChanged signal) {
                        if (signal.getPropertiesChanged().containsKey("RSSI")) {
                            manager
                                .getDevices(true)
                                .stream()
                                .filter(device ->
                                    device
                                        .getDbusPath()
                                        .equals(signal.getPath())
                                )
                                .filter(
                                    BluetoothService.this::filterDevicesWithNoName
                                )
                                .findFirst()
                                .map(Device::create)
                                .ifPresent(listener::deviceFound);
                        }
                    }
                }
            );
        } catch (DBusException e) {
            throw new BluetoothServiceException(
                "Could not register device discovery listener"
            );
        }
    }

    public void connect(Device device) {
        final BluetoothDevice bluetoothDevice = device.getRawDevice();
        try {
            if (!bluetoothDevice.isPaired()) bluetoothDevice.pair();
            if (!bluetoothDevice.isConnected()) bluetoothDevice.connect();
            if (bluetoothDevice.isConnected()) {
                log.info("\n\n\nCONNECTED\n\n\n");
            }
        } catch (Exception e) {
            log.error("Could not connect to device", e);
        }
    }

    private BluetoothAdapter getAdapter() throws BluetoothServiceException {
        if (
            Objects.isNull(bluetoothAdapter) || bluetoothAdapter.isEmpty()
        ) throw new BluetoothServiceException("No adapter found");
        return bluetoothAdapter.get();
    }

    private boolean filterDevicesWithNoName(BluetoothDevice device) {
        return (
            Objects.nonNull(device) &&
            !device.getName().isBlank() &&
            !device
                .getName()
                .equalsIgnoreCase(device.getAddress().replace(":", "-"))
        );
    }
}
