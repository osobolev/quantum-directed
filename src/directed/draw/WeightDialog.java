package directed.draw;

import directed.util.Arithmetic;
import directed.util.JSRunner;

import javax.swing.*;
import java.awt.*;

final class WeightDialog extends JDialog {

    private final JSRunner runner = new JSRunner(Arithmetic.createArithmetic(0, 0));

    private final JTextField tfWeight;
    private final JCheckBox cbStart = new JCheckBox("Start edge");
    private final JButton btnOk = new JButton("OK");
    private final JButton btnCancel = new JButton("Cancel");

    private boolean ok = false;
    private String result;

    WeightDialog(Frame owner, String value, Boolean direction) {
        super(owner, "Enter weight", true);
        result = value;

        cbStart.setSelected(direction != null);

        tfWeight = new JTextField(value, 10);

        JPanel center1 = new JPanel(new GridBagLayout());
        center1.add(new JLabel("Weight:"), new GridBagConstraints(
            0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0
        ));
        center1.add(tfWeight, new GridBagConstraints(
            1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 5), 0, 0
        ));

        center1.add(cbStart, new GridBagConstraints(
            1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0
        ));

        JTabbedPane tab = new JTabbedPane();
        tab.addTab("Edge", center1);

        JPanel down = new JPanel();
        down.add(btnOk);
        down.add(btnCancel);
        btnOk.addActionListener(e -> {
            String s = tfWeight.getText().replace(',', '.');
            if (validate(s)) {
                result = s;
                ok = true;
                dispose();
            }
        });
        btnCancel.addActionListener(e -> dispose());
        add(tab, BorderLayout.CENTER);
        add(down, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnOk);

        SwingUtilities.invokeLater(tfWeight::requestFocusInWindow);

        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private boolean validate(String text) {
        Number number = runner.evaluate(text);
        if (number != null)
            return true;
        JOptionPane.showMessageDialog(this, "Enter valid expression", "Error", JOptionPane.ERROR_MESSAGE);
        tfWeight.requestFocusInWindow();
        return false;
    }

    boolean isOk() {
        return ok;
    }

    String getResult() {
        return result;
    }

    Boolean getStartDirection() {
        return cbStart.isSelected() ? true : null;
    }
}
