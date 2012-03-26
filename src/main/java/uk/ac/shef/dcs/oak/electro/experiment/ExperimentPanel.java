package uk.ac.shef.dcs.oak.electro.experiment;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class ExperimentPanel extends JPanel
{
   ExperimentController controller;

   JCheckBox guess;

   public ExperimentPanel(ExperimentController control)
   {
      controller = control;
      initGUI();
   }

   private void initGUI()
   {
      GridBagLayout gbl = new GridBagLayout();
      this.setLayout(gbl);

      final JComboBox device = new JComboBox(controller.getDevices());
      gbl.setConstraints(device, new GridBagConstraints(0, 0, 1, 3, 0.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
      this.add(device);

      final DefaultComboBoxModel comboModel = new DefaultComboBoxModel(
            controller.getDays((String) device.getSelectedItem()));
      final JComboBox day = new JComboBox(comboModel);
      gbl.setConstraints(day, new GridBagConstraints(1, 0, 1, 3, 0.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(day);
      controller.selectDate((String) comboModel.getSelectedItem());

      // Build in the selection action
      device.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            comboModel.removeAllElements();
            for (Object str : controller.getDays((String) device.getSelectedItem()))
               comboModel.addElement(str);
            controller.selectDate((String) comboModel.getSelectedItem());
         }
      });

      day.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            controller.selectDate((String) comboModel.getSelectedItem());
         }
      });

      JButton loadGraph = new JButton("Load");
      gbl.setConstraints(loadGraph, new GridBagConstraints(2, 0, 1, 3, 0.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(loadGraph);
      loadGraph.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            controller.loadGuesses();
         }
      });

      final JCheckBox watts = new JCheckBox("Watts");
      gbl.setConstraints(watts, new GridBagConstraints(3, 0, 1, 1, 0.0, 1.0,
            GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
      this.add(watts);
      watts.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            controller.showWatts(watts.isSelected());
         }

      });

      final JCheckBox temp = new JCheckBox("Temperature");
      gbl.setConstraints(temp, new GridBagConstraints(3, 1, 1, 1, 0.0, 1.0,
            GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
      this.add(temp);
      temp.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            controller.showTemp(temp.isSelected());
         }
      });

      guess = new JCheckBox("Guess");
      gbl.setConstraints(guess, new GridBagConstraints(3, 2, 1, 1, 0.0, 1.0,
            GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
      this.add(guess);
      guess.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            controller.showGuess(guess.isSelected());
         }
      });

   }

   public void showGuess()
   {
      guess.setSelected(true);
      controller.showGuess(true);
   }
}
