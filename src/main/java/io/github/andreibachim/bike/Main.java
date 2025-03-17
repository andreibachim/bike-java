package io.github.andreibachim.bike;

import io.github.andreibachim.bike.components.*;
import io.github.andreibachim.bike.model.Device;
import io.github.jwharm.javagi.gobject.types.Types;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.gio.Resource;

public class Main {

    public static void main(String[] args) throws Exception {
        //Load resources
        Resource resource = Resource.load(
            "src/main/resources/resources.gresource"
        );
        resource.resourcesRegister();

        //Load static type
        TemplateTypes.register(ConnectionButton.class);
        TemplateTypes.register(Window.class);
        TemplateTypes.register(ConnectionDialog.class);
        TemplateTypes.register(DeviceFinder.class);
        TemplateTypes.register(DeviceListing.class);
        TemplateTypes.register(DeviceInfo.class);
        Types.register(Device.class);

        //Create the app
        App.create("io.github.andreibachim.bike").run(args);
    }
}
