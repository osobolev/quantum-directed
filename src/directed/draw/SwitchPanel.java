package directed.draw;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

final class SwitchPanel extends JPanel {

    SwitchPanel(GraphPanel panel) {
        StatModeEnum[] values = StatModeEnum.values();
        setLayout(new GridLayout(values.length, 1));
        ButtonGroup group = new ButtonGroup();
        for (StatModeEnum value : values) {
            JRadioButton butt = new JRadioButton(value.toString());
            butt.addActionListener(e -> {
                saveMode(value);
                panel.setMode(value);
            });
            add(butt);
            group.add(butt);
            if (value == panel.getMode()) {
                butt.setSelected(true);
            }
        }
    }

    static StatModeEnum loadMode() {
        return StatModeEnum.valueOf(GraphComponent.getPrefs().get("display.mode", StatModeEnum.NUMBER.name()));
    }

    private static void saveMode(StatModeEnum mode) {
        Preferences prefs = GraphComponent.getPrefs();
        prefs.put("display.mode", mode.name());
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            // ignore
        }
    }
}
