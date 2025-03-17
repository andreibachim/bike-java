package io.github.andreibachim.bike.components;

import io.github.andreibachim.bike.services.BluetoothService;
import io.github.andreibachim.bike.services.BluetoothServiceException;
import io.github.andreibachim.bike.services.bluetooth.adapter.AdapterStateListener;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import java.lang.foreign.MemorySegment;
import lombok.extern.slf4j.Slf4j;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.Dialog;
import org.gnome.gtk.Button;

@Slf4j
@GtkTemplate(
    name = "ConnectionButton",
    ui = "/io/github/andreibachim/bike/ui/connection-button.ui"
)
public class ConnectionButton extends Button {

    private Status status = Status.NoHardware;

    public ConnectionButton(MemorySegment address) {
        super(address);
    }

    @InstanceInit
    public void init() {
        BluetoothService bluetoothService = BluetoothService.INSTANCE;
        try {
            if (bluetoothService.isAdapterOn()) {
                setDisconnected();
            } else {
                setPoweredOff();
            }
        } catch (BluetoothServiceException e) {
            setNoAdapter();
            log.error("Could not get the power state of the adapter", e);
            return;
        }

        try {
            bluetoothService.registerAdapterStateListener(
                new AdapterStateListener(
                    this::setDisconnected,
                    this::setPoweredOff
                )
            );
        } catch (BluetoothServiceException e) {
            setNoAdapter();
            log.error("Could not register adapter state listener", e);
            return;
        }

        onClicked(() -> {
            switch (status) {
                case Disconnected -> {
                    Dialog dialog = ConnectionDialog.newInstance(
                        ConnectionDialog.class
                    );
                    dialog.onClosed(bluetoothService::stopDeviceDiscovery);
                    dialog.present(getAncestor(ApplicationWindow.getType()));
                }
                case Connected -> {
                    log.info("Device is connected");
                }
                default -> {
                    log.info("Hello, world");
                }
            }
        });
    }

    private void setNoAdapter() {
        status = Status.NoHardware;
        setIconName("bluetooth-hardware-disabled");
        setCssClasses(
            new String[] { "destructive-action", "flat", "no-hover" }
        );
        setTooltipText("This device does not support bluetooth");
    }

    private void setPoweredOff() {
        status = Status.TurnedOff;
        setIconName("bluetooth-disabled-symbolic");
        setCssClasses(new String[] { "error", "flat" });
        setTooltipText("Bluetooth is turned off");
    }

    private void setDisconnected() {
        status = Status.Disconnected;
        setIconName("bluetooth-disconnected-symbolic");
        setCssClasses(new String[] { "suggested-action" });
        setTooltipText("");
    }

    // private void setConnected() {
    //     status = Status.Connected;
    //     setIconName("bluetooth-active-symbolic");
    //     setCssClasses(new String[] { "success", "flat" });
    //     setTooltipText("");
    // }

    private enum Status {
        NoHardware,
        TurnedOff,
        Disconnected,
        Connected,
    }
}
