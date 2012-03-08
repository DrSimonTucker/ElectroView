package uk.ac.shef.dcs.oak.electro;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DatePanel extends JPanel implements ModelListener
{
   DateFormat df = DateFormat.getTimeInstance();
   Model mod;
   JLabel startDateLabel, endDateLabel;

   public DatePanel(Model model)
   {
      this.mod = model;
      model.addListener(this);
      init();
   }

   @Override
   public void dateUpdated()
   {
      System.out.println("Updating date");
      startDateLabel.setText(df.format(mod.getCurrStartTime()));
      endDateLabel.setText(df.format(mod.getCurrEndTime()));
   }

   private void init()
   {
      GridBagLayout gbl = new GridBagLayout();
      this.setLayout(gbl);

      JLabel startLabel = new JLabel("Start:");
      gbl.setConstraints(startLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 2, 2));
      add(startLabel);

      startDateLabel = new JLabel("12:21");
      gbl.setConstraints(startDateLabel, new GridBagConstraints(1, 0, 1, 1, 0, 0,
            GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 2, 2));
      add(startDateLabel);

      JLabel endLabel = new JLabel("End:");
      gbl.setConstraints(endLabel, new GridBagConstraints(2, 0, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 2, 2));
      add(endLabel);

      endDateLabel = new JLabel("14:21");
      gbl.setConstraints(endDateLabel, new GridBagConstraints(3, 0, 1, 1, 0, 0,
            GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 2, 2));
      add(endDateLabel);

      JButton zoomButton = new JButton("Zoom");
      gbl.setConstraints(zoomButton, new GridBagConstraints(4, 0, 1, 1, 0, 0,
            GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 2, 2));
      add(zoomButton);
   }

   @Override
   public void modelUpdated()
   {
      // TODO Auto-generated method stub

   }
}
