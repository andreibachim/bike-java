package io.github.andreibachim.bike.components;

import java.lang.foreign.MemorySegment;

import org.gnome.adw.Application;
import org.gnome.gobject.GObject;

import io.github.jwharm.javagi.gobject.types.Types;

public class App extends Application {
  static {
    Types.register(App.class);
  }

  public App(MemorySegment address) {
    super(address);
  }

  public static App create(String app_id) {
    final App app = GObject.newInstance(App.class);
    app.setApplicationId(app_id);
    app.onActivate(() -> app.build_ui());
    return app;
  }

  private void build_ui() {
    final Window window = Window.create(this);
    window.present();
  }
}
