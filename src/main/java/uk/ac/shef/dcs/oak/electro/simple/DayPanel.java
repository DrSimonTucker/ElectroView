package uk.ac.shef.dcs.oak.electro.simple;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class DayPanel extends JPanel
{
   int MARGIN = 20;
   Model mod;
   int TICKSIZE = 5;

   public DayPanel(Model mod)
   {
      this.mod = mod;
   }

   private void drawAxes(Graphics g)
   {
      // Draw the baseline lines
      g.drawLine(MARGIN, this.getHeight() - MARGIN, this.getWidth() - MARGIN, this.getHeight()
            - MARGIN);
      g.drawLine(MARGIN, this.getHeight() - MARGIN, MARGIN, MARGIN);

      // Draw the horiz tick marks
      double tickAdd = (0.0 + this.getWidth() - 2 * MARGIN) / 24;
      for (int i = 0; i < 25; i++)
         g.drawLine(MARGIN + (int) (i * tickAdd), this.getHeight() - MARGIN, MARGIN
               + (int) (i * tickAdd), this.getHeight() - MARGIN + TICKSIZE);
   }

   private void drawGraph(Graphics g)
   {
      double flipValue = mod.getDateRange() / (this.getWidth() - 2 * MARGIN + 0.0);
      double[] graphValues = new double[this.getWidth() - 2 * MARGIN];
      double[] tempValues = new double[this.getWidth() - 2 * MARGIN];
      int[] graphCount = new int[this.getWidth() - 2 * MARGIN];
      double maxWatts = 0;
      double maxTemp = 0;
      double minTemp = 100;
      for (Reading reading : mod.getReadings())
      {
         int bin = (int) ((reading.getTimestamp() - mod.getMinDate()) / flipValue);

         // Adjust the top level values
         bin = Math.min(bin, graphValues.length - 1);

         graphValues[bin] += reading.getWattage();
         tempValues[bin] += reading.getTemperature();
         graphCount[bin]++;
         maxWatts = Math.max(reading.getWattage(), maxWatts);
         maxTemp = Math.max(reading.getTemperature(), maxTemp);
         minTemp = Math.min(reading.getTemperature(), minTemp);
      }

      // Plot the graphs
      for (int i = 0; i < graphValues.length - 1; i++)
      {
         g.setColor(Color.red);
         g.drawLine(
               MARGIN + i,
               this.getHeight()
                     - (MARGIN - (int) (graphValues[i] * (this.getHeight() - 2 * MARGIN) / maxWatts)),
               MARGIN + i + 1, this.getHeight() - (MARGIN - (int) (graphValues[i + 1] / maxWatts)));

         g.setColor(Color.blue);
         int x1 = MARGIN + i;
         int y1 = this.getHeight()
               - (MARGIN + (int) (((tempValues[i] / graphCount[i]) - minTemp)
                     * (this.getHeight() - 2 * MARGIN) / (maxTemp - minTemp)));
         int x2 = MARGIN + i + 1;
         int y2 = (this.getHeight() - (MARGIN + (int) (((tempValues[i + 1] / graphCount[i + 1]) - minTemp)
               * (this.getHeight() - 2 * MARGIN) / (maxTemp - minTemp))));
         System.out.println(x1 + "," + y1 + " => " + x2 + "," + y2);
         g.drawLine(x1, y1, x2, y2);
      }

   }

   @Override
   public void paint(Graphics g)
   {
      super.paint(g);
      drawAxes(g);
      drawGraph(g);
   }

   public static void main(String[] args)
   {
      ModelFactory f = new ModelFactory(new File("/Users/sat/workspace/electricity/data/"));
      Model mod = f.buildModel("00140b23096d");
      mod.fixDate("Mar 14, 2012");
      DayPanel dp = new DayPanel(mod);

      JFrame framer = new JFrame();
      framer.add(dp);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);
      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }

}
