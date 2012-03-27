package uk.ac.shef.dcs.oak.electro.simple;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class OldGraphDetector extends JPanel
{
   JProgressBar bar;
   DetectionCallback callback;
   int counter = 0;
   boolean fixed = false;
   File imageFile;
   BufferedImage img;
   double perc;
   int[] vals;
   int[] x = new int[4];
   int[] y = new int[4];

   public OldGraphDetector(File imageFile, DetectionCallback callback)
   {
      this.callback = callback;
      this.imageFile = imageFile;

      this.addMouseListener(new MouseAdapter()
      {

         @Override
         public void mouseClicked(MouseEvent arg0)
         {
            if (arg0.getButton() == 1)
            {
               if (!fixed || counter != 0)
               {
                  x[counter] = arg0.getX();
                  y[counter] = arg0.getY();
                  counter = (counter + 1) % x.length;
                  fixed = true;
               }
            }
            else
               produceGraph();

            repaint();
         }

      });
   }

   private double getMidPoint(double x1, double x2, double y1, double y2, boolean draw)
   {
      int imgHeight = img.getHeight(this);
      int imgWidth = img.getWidth(this);

      int bestValue = Integer.MAX_VALUE;
      int bestIndex = 0;
      for (int i = ((int) (y1 * imgHeight)); i < ((int) (y2 * imgHeight)); i++)
      {
         double xperc = (x1 + ((x2 - x1) + 0.0) * (i - (y1 * imgHeight))
               / (y2 * imgHeight - y1 * imgHeight));
         int xVal = Math.max(0, (int) (xperc * imgWidth));
         if (draw)
            img.setRGB(xVal, i, Color.magenta.getRGB());
         int rgb = img.getRGB(xVal, i);
         Color c = new Color(rgb);

         int sumv = c.getBlue() + c.getGreen() + c.getRed();
         if (sumv < bestValue)
         {
            bestValue = sumv;
            bestIndex = i;
         }
      }

      // if (counter == 0)
      // img.setRGB(bestX, bestIndex, Color.green.getRGB());

      return (bestIndex + 0.0) / imgHeight;
   }

   @Override
   public void paint(Graphics g)
   {
      if (img == null)
         try
         {
            img = ImageIO.read(imageFile);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }

      g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), Color.white, this);

      g.setColor(Color.red);
      g.drawPolygon(x, y, x.length);

      g.setColor(Color.green);

      // Paint the electricity min-max if we're done
      int oldX = 0;
      int oldY = 0;
      int imgHeight = img.getHeight();
      int imgWidth = img.getWidth();
      if (vals != null)
         for (int i = 0; i < vals.length; i++)
         {
            double perc = (i + 0.0) / vals.length;
            double percValue = vals[i] / 1000.0;
            double xPointBot = (perc * (x[2] - x[1]) + x[1]);
            double xPointTop = (perc * (x[3] - x[0]) + x[0]);
            double yPointBot = (perc * (y[2] - y[1]) + y[1]);
            double yPointTop = (perc * (y[3] - y[0]) + y[0]);

            int plotX = (int) (percValue * (xPointTop - xPointBot) + xPointBot);
            int plotY = (int) (percValue * (yPointTop - yPointBot) + yPointBot);
            g.drawLine(oldX, oldY, plotX, plotY);
            oldX = plotX;
            oldY = plotY;
         }
   }

   private void produceGraph()
   {
      final JDialog popupFrame = new JDialog();
      popupFrame.setModal(true);
      bar = new JProgressBar(0, 1000);
      popupFrame.add(bar);
      popupFrame.pack();
      popupFrame.setLocationRelativeTo(this);
      Thread graphThread = new Thread(new Runnable()
      {
         @Override
         public void run()
         {
            try
            {
               produceGraph(new File("read.txt"));
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
            perc = 1;
            popupFrame.setVisible(false);
         }
      });

      graphThread.start();
      popupFrame.setVisible(true);
   }

   private void produceGraph(File outFile) throws IOException
   {
      PrintStream ps = new PrintStream(outFile);
      int SIZE = 60 * 60 * 24;
      vals = new int[SIZE];
      for (int i = 0; i < SIZE; i++)
      {
         perc = (i + 0.0) / SIZE;
         if (bar != null)
            bar.setValue((int) (perc * 1000));

         double xStart = (x[0] + (x[3] - x[0]) * perc) / this.getWidth();
         double xEnd = (x[1] + (x[2] - x[1]) * perc) / this.getWidth();
         double yStart = (y[0] + (y[3] - y[0]) * perc) / this.getHeight();
         double yEnd = (y[1] + (y[2] - y[1]) * perc) / this.getHeight();
         System.out.println("HERE = " + xStart + "," + yStart + " and " + xEnd + "," + yEnd);
         vals[i] = (int) (1000 * (getMidPoint(xStart, xEnd, yStart, yEnd, false)));
         ps.println(i + " " + vals[i]);
      }
      ps.close();

      if (callback != null)
         callback.detected();
   }

   public static void main(String[] args)
   {
      OldGraphDetector detect = new OldGraphDetector(new File("/Users/sat/Desktop/IMG_2791.JPG"),
            null);
      JFrame framer = new JFrame();
      framer.add(detect);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);
      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
}
