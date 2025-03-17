package io.github.andreibachim.bike.components;

import java.lang.foreign.MemorySegment;

import org.gnome.adw.ApplicationWindow;
import org.gnome.gobject.GObject;

import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;

@GtkTemplate(name = "Window", ui = "/io/github/andreibachim/bike/ui/window.ui")
public class Window extends ApplicationWindow {
    public Window(MemorySegment address) {
        super(address);
    }

    public static Window create(App app) {
        return GObject.newInstance(Window.class, "application", app);
    }
}
