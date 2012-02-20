package uk.ac.shef.dcs.oak.electro;

import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SuperGraphPanel extends JPanel
{
   GraphPanel panelWeek, panelDay, panelHour;

   Model mod;

   public SuperGraphPanel(Model mod)
   {
      super();

      this.mod = mod;

      mod.fixMax(System.currentTimeMillis() / 1000 - 0, System.currentTimeMillis() / 1000 - 60 * 60
            * 24 * 7);

      // Build the display
      panelHour = new GraphPanel(0, 60 * 60, 60 * 60 * 24 * 7, mod);
      panelDay = new GraphPanel(60 * 60, 60 * 60 * 24, 60 * 60 * 24 * 7, mod);
      panelWeek = new GraphPanel(60 * 60 * 24, 60 * 60 * 24 * 7, 60 * 60 * 24 * 7, mod);

      this.setLayout(new GridLayout(1, 3));

      this.add(panelWeek);
      this.add(panelDay);
      this.add(panelHour);
   }

   @Override
   public void paint(Graphics g)
   {
      super.paint(g);
      mod.printStats();
   }

   public static void main(String[] args) throws Exception
   {
      JFrame framer = new JFrame();
      SuperGraphPanel mine = new SuperGraphPanel(Model.getModel());
      framer.add(mine);

      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);

   }
}
