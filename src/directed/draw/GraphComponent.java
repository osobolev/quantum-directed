package directed.draw;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.prefs.Preferences;

final class GraphComponent {

    private final GraphPanel panel;
    private final JFrame frame;
    private final String title;
    private JComponent btnRun;

    private Options options = Options.load(getPrefs());

    private Running running = null;
    final JSlider speed = new JSlider(0, 1000, 500);
    private final Timer timer = new Timer(200, e -> showRunning());
    final AbstractAction runAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            runModel();
        }
    };
    final AbstractAction pauseAction = new AbstractAction("Pause") {
        public void actionPerformed(ActionEvent e) {
            pauseModel();
        }
    };
    final AbstractAction optionsAction = new AbstractAction("Options") {
        public void actionPerformed(ActionEvent e) {
            OptionsDialog dlg = new OptionsDialog(frame, options);
            if (dlg.isOk()) {
                options = dlg.getResult();
                options.save(getPrefs());
            }
        }
    };

    GraphComponent(JFrame frame, String title, File file, Readable source) {
        this.frame = frame;
        this.title = title;
        StatModeEnum displayMode = SwitchPanel.loadMode();
        this.panel = new GraphPanel(displayMode);

        stopModel();
        speed.addChangeListener(e -> setSpeed());

        if (file != null || source != null) {
            try {
                panel.load(file, source);
            } catch (FileNotFoundException ex) {
                handleException(ex);
            }
        }
        setTitle();
    }

    public void setRunButton(JComponent btnRun) {
        this.btnRun = btnRun;
    }

    private void setSpeed() {
        int value = speed.getValue();
        if (running != null) {
            running.setSpeed(value / 1000.0);
        }
    }

    public static void handleException(JFrame frame, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(frame, ex, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void handleException(Exception ex) {
        handleException(frame, ex);
    }

    private void runModel() {
        if (running == null) {
            if (btnRun == null) {
                doRun(false);
            } else {
                JPopupMenu popup = new JPopupMenu();
                JMenuItem miRun = new JMenuItem(new AbstractAction("Normal") {
                    public void actionPerformed(ActionEvent e) {
                        doRun(false);
                    }
                });
                JMenuItem miShow = new JMenuItem(new AbstractAction("Show packets") {
                    public void actionPerformed(ActionEvent e) {
                        doRun(true);
                    }
                });
                popup.add(miRun);
                popup.add(miShow);
                popup.show(btnRun, 0, btnRun.getHeight());
            }
        } else {
            stopModel();
        }
    }

    private void doRun(boolean showPhotons) {
        running = new Running(panel, options, showPhotons);
        runAction.putValue(AbstractAction.NAME, "Stop");
        pauseAction.setEnabled(true);
        optionsAction.setEnabled(false);
        speed.setEnabled(showPhotons);
        setSpeed();
        timer.setDelay(showPhotons ? 20 : 200);
        timer.setInitialDelay(0);
        timer.start();
        running.start();
    }

    private void stopModel() {
        runAction.putValue(AbstractAction.NAME, "Run");
        pauseAction.putValue(AbstractAction.NAME, "Pause");
        pauseAction.setEnabled(false);
        optionsAction.setEnabled(true);
        speed.setEnabled(true);
        timer.stop();
        if (running != null) {
            running.stop();
        }
        panel.clearStat();
        running = null;
    }

    private void pauseModel() {
        if (timer.isRunning()) {
            pauseAction.putValue(AbstractAction.NAME, "Resume");
            timer.stop();
        } else {
            pauseAction.putValue(AbstractAction.NAME, "Pause");
            timer.start();
        }
    }

    private void showRunning() {
        if (running != null) {
            if (!running.refresh()) {
                stopModel();
            }
        } else {
            timer.stop();
        }
    }

    public boolean checkSave() {
        if (panel.isChanged()) {
            int ans = JOptionPane.showConfirmDialog(
                frame, "Save changes?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION
            );
            if (ans == JOptionPane.YES_OPTION) {
                return saveOrLoad(true, false);
            } else if (ans == JOptionPane.NO_OPTION) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean saveOrLoad(boolean save, boolean force) {
        if (!save) {
            if (!checkSave())
                return false;
        }
        File saved = panel.getSaved();
        File file;
        if (saved != null && !force) {
            file = saved;
        } else {
            JFileChooser chooser = new JFileChooser();
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Graph files", "graph"));
            chooser.setDialogType(save ? JFileChooser.SAVE_DIALOG : JFileChooser.OPEN_DIALOG);
            if (saved != null) {
                chooser.setCurrentDirectory(saved.getParentFile());
            } else {
                String dirName = getPrefs().get("default.dir", null);
                if (dirName != null) {
                    File dir = new File(dirName);
                    if (dir.isDirectory()) {
                        chooser.setCurrentDirectory(dir);
                    }
                }
            }
            int res = chooser.showDialog(frame, null);
            if (res == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
            } else {
                file = null;
            }
        }
        if (file != null) {
            getPrefs().put("default.dir", file.getParent());
            try {
                if (save) {
                    if (saved == null && file.exists()) {
                        int ans = JOptionPane.showConfirmDialog(
                            frame, "Overwrite " + file.getName() + "?", "Confirm", JOptionPane.YES_NO_OPTION
                        );
                        if (ans != JOptionPane.YES_OPTION)
                            return false;
                    }
                    panel.save(file);
                } else {
                    stopModel();
                    panel.load(file);
                }
                setTitle();
                return true;
            } catch (FileNotFoundException ex) {
                handleException(ex);
            }
        }
        return false;
    }

    public static Preferences getPrefs() {
        return Preferences.userRoot().node("graph");
    }

    public void newGraph() {
        if (!checkSave())
            return;
        stopModel();
        panel.createNew();
        setTitle();
    }

    static void setTitle(JFrame frame, GraphPanel panel, String title) {
        frame.setTitle(title + (panel.getSaved() == null ? "" : " - " + panel.getSaved().getName()));
    }

    private void setTitle() {
        setTitle(frame, panel, title);
    }

    public void export() {
        BufferedImage image = new BufferedImage(panel.getWidth() * 2, panel.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.scale(2.0, 2.0);
        g.setColor(Color.white);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        panel.draw(g, true);
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG files", "png"));
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            String dirName = getPrefs().get("export.dir", null);
            boolean dirSet = false;
            if (dirName != null) {
                File dir = new File(dirName);
                if (dir.isDirectory()) {
                    chooser.setCurrentDirectory(dir);
                    dirSet = true;
                }
            }
            File saved = panel.getSaved();
            if (saved != null) {
                String fileName = saved.getName();
                int p = fileName.lastIndexOf('.');
                if (p >= 0) {
                    String exportName = fileName.substring(0, p) + ".png";
                    File exportFile;
                    if (!dirSet) {
                        exportFile = new File(saved.getParentFile(), exportName);
                    } else {
                        exportFile = new File(chooser.getCurrentDirectory(), exportName);
                    }
                    chooser.setSelectedFile(exportFile.getAbsoluteFile());
                }
            }
            int res = chooser.showDialog(frame, null);
            File file;
            if (res == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
            } else {
                file = null;
            }
            if (file != null) {
                getPrefs().put("export.dir", file.getParent());
                if (file.exists()) {
                    int ans = JOptionPane.showConfirmDialog(
                        frame, "Overwrite " + file.getName() + "?", "Confirm", JOptionPane.YES_NO_OPTION
                    );
                    if (ans != JOptionPane.YES_OPTION)
                        return;
                }
                ImageIO.write(image, "PNG", file);
            }
        } catch (IOException ex) {
            handleException(ex);
        }
    }

    public void addTo(Container main) {
        main.add(panel, BorderLayout.CENTER);
        JTextPane help = new JTextPane();
        help.setText(
            "Click - create vertex or select existing vertex/edge\n" +
            "Left-drag - create edge or move edge; Right-drag - move vertex\n" +
            "Double-click - change edge length or set initial edge\n" +
            "Delete button - delete selected object"
        );
        help.setFont(help.getFont().deriveFont(Font.BOLD));
        help.setEditable(false);
        help.setBorder(BorderFactory.createEtchedBorder());
        JPanel down = new JPanel(new BorderLayout());
        down.add(help, BorderLayout.CENTER);
        SwitchPanel statMode = new SwitchPanel(panel);
        down.add(statMode, BorderLayout.EAST);
        main.add(down, BorderLayout.SOUTH);
    }
}
