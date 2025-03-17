package io.github.andreibachim.bike.components;

import java.lang.foreign.MemorySegment;

import org.gnome.adw.ActionRow;
import org.gnome.glib.Variant;
import org.gnome.gobject.BindingFlags;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Image;

import io.github.andreibachim.bike.model.Device;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@GtkTemplate(
    name = "DeviceListing",
    ui = "/io/github/andreibachim/bike/ui/device-listing.ui"
)
public class DeviceListing extends ActionRow {

    @GtkChild
    public Image icon;

    private Device device;

    public static DeviceListing create(Device device) {
        final DeviceListing deviceListing = GObject.newInstance(
            DeviceListing.class
        );
        deviceListing.device = device;
        device.bindProperty(
            "name",
            deviceListing,
            "title",
            BindingFlags.SYNC_CREATE
        );
        device.bindProperty(
            "status",
            deviceListing,
            "subtitle",
            BindingFlags.SYNC_CREATE
        );
        device.bindProperty(
            "icon-name",
            deviceListing.getIcon(),
            "icon-name",
            BindingFlags.SYNC_CREATE
        );
        deviceListing.setActionTargetValue(Variant.string(device.getPath()));
        return deviceListing;
      
    }

    public DeviceListing(MemorySegment address) {
        super(address);
    }
}
