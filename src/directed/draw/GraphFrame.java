package directed.draw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public final class GraphFrame extends JFrame {

    private final GraphComponent util;

    public GraphFrame(File file, Readable source) {
        util = new GraphComponent(this, "Graph tool", file, source);
        util.addTo(this);
        JToolBar bar = new JToolBar();
        bar.add(new AbstractAction("New") {
            public void actionPerformed(ActionEvent e) {
                util.newGraph();
            }
        });
        bar.add(new AbstractAction("Open") {
            public void actionPerformed(ActionEvent e) {
                util.saveOrLoad(false, true);
            }
        });
        bar.add(new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) {
                util.saveOrLoad(true, false);
            }
        });
        bar.add(new AbstractAction("Save as") {
            public void actionPerformed(ActionEvent e) {
                util.saveOrLoad(true, true);
            }
        });
        bar.add(new AbstractAction("Export") {
            public void actionPerformed(ActionEvent e) {
                util.export();
            }
        });
        bar.addSeparator();
        JButton btnRun = bar.add(util.runAction);
        util.setRunButton(btnRun);
        bar.add(util.pauseAction);
        bar.addSeparator();
        bar.add(util.optionsAction);
        bar.addSeparator();
        bar.add(util.speed);
        add(bar, BorderLayout.NORTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void dispose() {
        if (util.checkSave()) {
            super.dispose();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        File file = args.length > 0 ? new File(args[0]) : null;
        new GraphFrame(file, null);
    }
}
