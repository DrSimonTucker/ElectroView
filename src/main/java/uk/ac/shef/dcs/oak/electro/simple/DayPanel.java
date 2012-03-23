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
   int JUMP = 100;
   int MARGIN = 40;
   double maxTemp = -1;
   double maxWatts = -1;
   double minTemp = -1;
   Model mod;
   int TICKSIZE = 5;

   public DayPanel(Model mod)
   {
      this.mod = mod;
   }

   private void drawAxes(Graphics g)
   {
      g.setColor(Color.black);

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

      // Draw in the axes for temperature
      g.drawLine(this.getWidth() - MARGIN, this.getHeight() - MARGIN, this.getWidth() - MARGIN,
            MARGIN);

      // Draw the vertical tick marks
      for (int i = (int) Math.ceil(minTemp); i < (int) Math.ceil(maxTemp); i++)
      {
         double pos = (i - minTemp) / (maxTemp - minTemp);
         int yPos = this.getHeight() - (int) (pos * (this.getHeight() - 2 * MARGIN)) - MARGIN;
         g.drawLine(this.getWidth() - MARGIN, yPos, this.getWidth() - MARGIN + TICKSIZE, yPos);

         g.drawString(i + "", this.getWidth() - MARGIN + TICKSIZE + 1, yPos + 4);
      }

      // Draw the watts tick marks
      for (int i = 0; i < (int) (Math.ceil(maxWatts)); i += JUMP)
      {
         double pos = (i) / maxWatts;
         int yPos = this.getHeight() - (int) (pos * (this.getHeight() - 2 * MARGIN)) - MARGIN;
         g.drawLine(MARGIN, yPos, MARGIN - TICKSIZE, yPos);

         g.drawString(i + "", 0, yPos + 4);
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
      maxWatts = 0;
      double maxGuess = 0;
      maxTemp = 0;
      minTemp = 100;
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

      System.out.println(maxWatts);

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
            int x1 = MARGIN + i;
            int y1 = this.getHeight()
                  - (MARGIN + (int) ((graphValues[i] / graphCount[i])
                        * (this.getHeight() - 2 * MARGIN) / maxWatts));
            int x2 = MARGIN + i + 1;
            int y2 = this.getHeight()
                  - (MARGIN + (int) ((graphValues[i + 1] / graphCount[i + 1])
                        * (this.getHeight() - 2 * MARGIN) / maxWatts));
            g.drawLine(x1, y1, x2, y2);
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
      drawGraph(g);
      drawAxes(g);
   }

   public static void main(String[] args)
   {
      ModelFactory f = new ModelFactory(new File("/Users/sat/workspace/electricity/data/"));
      Model mod = f.buildModel("00140b230a80");
      mod.addGuess(new File("read.txt"), "Mar 23, 2012");
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
