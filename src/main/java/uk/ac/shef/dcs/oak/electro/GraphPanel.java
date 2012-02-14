package uk.ac.shef.dcs.oak.electro;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GraphPanel extends JPanel
{
   Model mod;

   long offsetStart;
   long offsetEnd;

   public GraphPanel(long offsetStart, long offsetEnd, Model mod)
   {
      this.offsetStart = offsetStart;
      this.offsetEnd = offsetEnd;
      this.mod = mod;
   }

   @Override
   public void paint(Graphics g)
   {
      super.paint(g);

      g.setColor(Color.RED);
      // Plot out the graph
      long startTime = System.currentTimeMillis() / 1000 - offsetStart;
      long endTime = System.currentTimeMillis() / 1000 - offsetEnd;
      int xTicks = this.getWidth();

      double yMax = mod.getMax(endTime, startTime);
      int oldY = 0;

      for (int i = 0; i < xTicks; i++)
      {
         double yVal = mod.getMean(((endTime - startTime) * (i + 1)) / xTicks + startTime,
               ((endTime - startTime) * i) / xTicks + startTime);
         double pixPerc = yVal / yMax;
         int newY = (int) (pixPerc * this.getHeight());
         g.drawLine(xTicks - i, oldY, xTicks - (i + 1), newY);
         oldY = newY;
      }
   }

   public static void main(String[] args) throws Exception
   {
      JFrame framer = new JFrame();
      GraphPanel mine = new GraphPanel(0, 60 * 60 * 24, Model.getModel());
      framer.add(mine);

      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);

   }
}
