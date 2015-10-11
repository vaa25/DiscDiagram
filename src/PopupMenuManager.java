import javafx.scene.control.ContextMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vaa25 on 02.10.2015.
 */
public class PopupMenuManager {

    private static List<ContextMenu> openedMenus = new ArrayList<>();

    public static void register(ContextMenu menu) {
        openedMenus.add(menu);
    }

    public static void close() {
        for (ContextMenu menu : openedMenus) {
            if (menu.isShowing()) {
                menu.hide();
            }
        }
        openedMenus.clear();
    }
}
