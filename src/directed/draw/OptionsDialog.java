package directed.draw;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

final class OptionsDialog extends JDialog {

    private final JTextField tfTimeTol = new JTextField(10);
    private final JTextField tfPrecision = new JTextField(10);
    private final JTextField tfEps = new JTextField(10);
    private final JCheckBox cbLog = new JCheckBox("Print to log:");
    private final JTextField tfFile = new JTextField(30);
    private final JComboBox<StatModeEnum> chStatMode = new JComboBox<>(StatModeEnum.values());
    private final JButton btnOk = new JButton("OK");
    private final JButton btnCancel = new JButton("Cancel");

    private boolean ok = false;
    private Options result;

    OptionsDialog(Frame owner, Options options) {
        super(owner, "Options", true);
        this.result = options;

        ToolTipManager.sharedInstance().setDismissDelay(60000);

        JPanel center = new JPanel(new GridBagLayout());
        JLabel lblTimetol = new JLabel("Time tolerance:");
        lblTimetol.setToolTipText(
            "<html>Distance between two packets when they are considered collided.<br>" +
            "Should be >0 because of rounding errors, but sufficiently small</html>"
        );
        center.add(lblTimetol, new GridBagConstraints(
            0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0
        ));
        center.add(tfTimeTol, new GridBagConstraints(
            1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0
        ));
        JLabel lblPrecision = new JLabel("Precision:");
        lblPrecision.setToolTipText(
            "<html>Number of digits used for calculation:<br>" +
            "0 to use native doubles (~14 digits precision),<br>" +
            ">0 to use Apfloat library (much slower, but more precision)</html>"
        );
        center.add(lblPrecision, new GridBagConstraints(
            0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0
        ));
        center.add(tfPrecision, new GridBagConstraints(
            1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0
        ));
        JLabel lblEps = new JLabel("Epsilon:");
        lblEps.setToolTipText(
            "<html>Radius of particles</html>"
        );
        center.add(lblEps, new GridBagConstraints(
            0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0
        ));
        center.add(tfEps, new GridBagConstraints(
            1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0
        ));
        center.add(cbLog, new GridBagConstraints(
            0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0
        ));
        center.add(tfFile, new GridBagConstraints(
            1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0
        ));
        center.add(new JLabel("Log mode:"), new GridBagConstraints(
            0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0
        ));
        center.add(chStatMode, new GridBagConstraints(
            1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0
        ));
        tfTimeTol.setText(String.valueOf(options.timeTol));
        tfPrecision.setText(String.valueOf(options.precision));
        tfEps.setText(String.valueOf(options.eps));
        cbLog.setSelected(options.useLog);
        tfFile.setText(options.logFile.getAbsolutePath());
        chStatMode.setSelectedItem(options.logMode);
        JPanel down = new JPanel();
        down.add(btnOk);
        down.add(btnCancel);
        cbLog.addActionListener(e -> loggingChanged());
        loggingChanged();
        btnOk.addActionListener(e -> {
            result = validateInput();
            if (result != null) {
                ok = true;
                dispose();
            }
        });
        btnCancel.addActionListener(e -> dispose());
        add(center, BorderLayout.CENTER);
        add(down, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnOk);
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void loggingChanged() {
        boolean on = cbLog.isSelected();
        tfFile.setEnabled(on);
        chStatMode.setEnabled(on);
    }

    private Options validateInput() {
        double timeTol;
        try {
            timeTol = Double.parseDouble(tfTimeTol.getText().replace(',', '.'));
        } catch (NumberFormatException nfex) {
            JOptionPane.showMessageDialog(this, "Enter valid time tolerance", "Error", JOptionPane.ERROR_MESSAGE);
            tfTimeTol.requestFocusInWindow();
            return null;
        }
        int precision;
        try {
            precision = Integer.parseInt(tfPrecision.getText());
        } catch (NumberFormatException nfex) {
            JOptionPane.showMessageDialog(this, "Enter valid precision", "Error", JOptionPane.ERROR_MESSAGE);
            tfPrecision.requestFocusInWindow();
            return null;
        }
        double eps;
        try {
            eps = Double.parseDouble(tfEps.getText().replace(',', '.'));
        } catch (NumberFormatException nfex) {
            JOptionPane.showMessageDialog(this, "Enter valid epsilon", "Error", JOptionPane.ERROR_MESSAGE);
            tfEps.requestFocusInWindow();
            return null;
        }
        boolean useLog = cbLog.isSelected();
        File logFile = new File(tfFile.getText());
        if (useLog) {
            try {
                logFile = logFile.getCanonicalFile();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Enter valid file name", "Error", JOptionPane.ERROR_MESSAGE);
                tfFile.requestFocusInWindow();
                return null;
            }
        }
        return new Options(timeTol, precision, eps, useLog, logFile, (StatModeEnum) chStatMode.getSelectedItem());
    }

    boolean isOk() {
        return ok;
    }

    Options getResult() {
        return result;
    }
}
