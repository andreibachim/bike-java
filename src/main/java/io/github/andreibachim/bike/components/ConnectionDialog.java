package io.github.andreibachim.bike.components;

import java.lang.foreign.MemorySegment;

import org.gnome.adw.NavigationView;
import org.gnome.gio.SimpleAction;
import org.gnome.gio.SimpleActionGroup;

import io.github.jwharm.javagi.glib.types.VariantTypes;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@GtkTemplate(name = "ConnectionDialog", ui = "/io/github/andreibachim/bike/ui/connection-dialog.ui")
public class ConnectionDialog extends org.gnome.adw.Dialog {
  @GtkChild(name = "navigationView")
  public NavigationView navigationView;

  @InstanceInit
  public void init() {
    SimpleAction loadInfo = SimpleAction.builder()
        .setName("load-info")
        .setParameterType(VariantTypes.STRING)
        .onActivate(devicePath -> {
          log.info("Should connect to device: {}", devicePath);
          navigationView.pushByTag("device-info");
        })
        .build();
    SimpleAction loadFinder = SimpleAction.builder()
        .setName("load-find")
        .onActivate(_ -> {
          navigationView.popToTag("device-finder");
        })
        .build();
    SimpleActionGroup actionGroup = new SimpleActionGroup();
    actionGroup.addAction(loadInfo);
    actionGroup.addAction(loadFinder);
    insertActionGroup("connect-dialog", actionGroup);
  }

  public ConnectionDialog(MemorySegment segment) {
    super(segment);
  }
}
