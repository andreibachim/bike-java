package io.github.andreibachim.bike.components;

import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import lombok.extern.slf4j.Slf4j;
import org.gnome.adw.NavigationPage;

import java.lang.foreign.MemorySegment;

@Slf4j
@GtkTemplate(name = "DeviceInfo", ui = "/io/github/andreibachim/bike/ui/device-info.ui")
public class DeviceInfo extends NavigationPage {
    public DeviceInfo(MemorySegment address) {
        super(address);
    }
}
