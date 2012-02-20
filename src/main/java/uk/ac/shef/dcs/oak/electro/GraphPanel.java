package uk.ac.shef.dcs.oak.electro;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GraphPanel extends JPanel
{
   Model mod;

   public GraphPanel(Model mod)
   {
      this.mod = mod;
   }

   @Override
   public void paint(Graphics g)
   {
      super.paint(g);

      // Plot out the graph
      int xTicks = this.getWidth();

      double yMax = mod.getMax();
      int oldY = -1;

      for (int i = 0; i < xTicks; i++)
      {
         double yVal = mod.getMean((i + 0.0) / xTicks, (i + 1.0) / xTicks);
         double pixPerc = yVal / yMax;
         int newY = this.getHeight() - (int) (pixPerc * this.getHeight());
         if (oldY == -1)
            oldY = newY;
         g.drawLine(xTicks - i, oldY, xTicks - (i + 1), newY);
         oldY = newY;
      }

      g.setColor(Color.red);
      g.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight());
   }

   public static void main(String[] args) throws Exception
   {
      JFrame framer = new JFrame();
      GraphPanel mine = new GraphPanel(new Model(0, 60 * 60 * 24));
      framer.add(mine);

      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);

   }
}
