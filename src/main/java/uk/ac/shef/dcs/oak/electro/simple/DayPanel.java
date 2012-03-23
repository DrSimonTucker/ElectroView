package uk.ac.shef.dcs.oak.electro.simple;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class DayPanel extends JPanel
{
   boolean drawGuess = false;
   boolean drawTemp = true;
   boolean drawWatts = true;
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

      // Paint the times in
      for (int i = 0; i < 25; i++)
      {
         double pos = MARGIN + (i * tickAdd);
         g.drawString(i + "", (int) pos - 5, this.getHeight() - MARGIN + TICKSIZE + 13);
      }
   }

   private void drawGraph(Graphics g)
   {
      double flipValue = mod.getDateRange() / (this.getWidth() - 2 * MARGIN + 0.0);
      double[] graphValues = new double[this.getWidth() - 2 * MARGIN];
      double[] guessValues = new double[this.getWidth() - 2 * MARGIN];
      double[] tempValues = new double[this.getWidth() - 2 * MARGIN];
      int[] graphCount = new int[this.getWidth() - 2 * MARGIN];
      int[] guessCount = new int[this.getWidth() - 2 * MARGIN];
      double maxWatts = 0;
      double maxGuess = 0;
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

      for (Reading reading : mod.getGuesses())
      {
         int bin = (int) ((reading.getTimestamp() - mod.getMinDate()) / flipValue);

         // Adjust the top level values
         bin = Math.min(bin, graphValues.length - 1);

         guessValues[bin] += reading.getWattage();
         guessCount[bin]++;
         maxGuess = Math.max(reading.getWattage(), maxGuess);

      }

      // Plot the graphs
      for (int i = 0; i < graphValues.length - 1; i++)
      {
         if (drawWatts)
         {
            g.setColor(Color.red);
            g.drawLine(
                  MARGIN + i,
                  this.getHeight()
                        - (MARGIN - (int) (graphValues[i] * (this.getHeight() - 2 * MARGIN) / maxWatts)),
                  MARGIN + i + 1, this.getHeight()
                        - (MARGIN - (int) (graphValues[i + 1] / maxWatts)));
         }
         if (drawGuess)
         {
            g.setColor(Color.magenta);
            int gx1 = MARGIN + i;
            int gy1 = this.getHeight()
                  - (MARGIN + (int) ((guessValues[i] / guessCount[i])
                        * (this.getHeight() - 2 * MARGIN) / maxGuess));
            int gx2 = MARGIN + i + 1;
            int gy2 = this.getHeight()
                  - (MARGIN + (int) ((guessValues[i + 1] / guessCount[i + 1])
                        * (this.getHeight() - 2 * MARGIN) / maxGuess));
            g.drawLine(gx1, gy1, gx2, gy2);
         }

         if (drawTemp)
         {
            g.setColor(Color.blue);
            int x1 = MARGIN + i;
            int y1 = this.getHeight()
                  - (MARGIN + (int) (((tempValues[i] / graphCount[i]) - minTemp)
                        * (this.getHeight() - 2 * MARGIN) / (maxTemp - minTemp)));
            int x2 = MARGIN + i + 1;
            int y2 = (this.getHeight() - (MARGIN + (int) (((tempValues[i + 1] / graphCount[i + 1]) - minTemp)
                  * (this.getHeight() - 2 * MARGIN) / (maxTemp - minTemp))));
            // System.out.println(x1 + "," + y1 + " => " + x2 + "," + y2);
            g.drawLine(x1, y1, x2, y2);
         }

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
      Model mod = f.buildModel("00140b230a80");
      mod.addGuess(new File("read.txt"), "Mar 22, 2012");
      // mod.fixDate("Mar 14, 2012");
      DayPanel dp = new DayPanel(mod);

      JFrame framer = new JFrame();
      framer.add(dp);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);
      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
}
