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

public class GraphDetector extends JPanel
{
   JProgressBar bar;
   DetectionCallback callback;
   int counter = 0;
   File imageFile;
   BufferedImage img;
   double perc;

   int[] x = new int[4];

   int[] y = new int[4];

   public GraphDetector(File imageFile, DetectionCallback callback)
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
               x[counter] = arg0.getX();
               y[counter] = arg0.getY();
               counter = (counter + 1) % x.length;
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
      int bestX = 0;
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
            bestX = xVal;
         }
      }

      if (counter == 0)
         img.setRGB(bestX, bestIndex, Color.green.getRGB());

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

      double mp = getMidPoint((x[0] + x[3] + 0.0) / (2 * this.getWidth()), (x[1] + x[2] + 0.0)
            / (2 * this.getWidth()), (y[0] + y[3] + 0.0) / (2 * this.getHeight()),
            (y[1] + y[2] + 0.0) / (2 * this.getHeight()), false);

      g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), Color.white, this);

      g.setColor(Color.magenta);
      g.drawLine(0, (int) (mp * this.getHeight()), this.getWidth(), (int) (mp * this.getHeight()));

      g.setColor(Color.red);
      g.drawPolygon(x, y, x.length);

      // Draw the marker line
      g.setColor(Color.magenta);
      // g.drawLine((x[0] + x[3]) / 2, (y[0] + y[3]) / 2, (x[1] + x[2]) / 2,
      // (y[1] + y[2]) / 2);

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
      int[] vals = new int[SIZE];
      for (int i = 0; i < SIZE; i++)
      {
         perc = (i + 0.0) / SIZE;
         if (bar != null)
            bar.setValue((int) (perc * 1000));

         double xStart = (x[0] + (x[3] - x[0]) * perc) / this.getWidth();
         double xEnd = (x[1] + (x[2] - x[1]) * perc) / this.getWidth();
         double yStart = (y[0] + (y[3] - y[0]) * perc) / this.getHeight();
         double yEnd = (y[1] + (y[2] - y[1]) * perc) / this.getHeight();
         vals[i] = this.getHeight()
               - (int) (getMidPoint(xStart, xEnd, yStart, yEnd, false) * this.getHeight());
         ps.println(i + " " + vals[i]);
      }
      ps.close();

      if (callback != null)
         callback.detected();
   }

   public static void main(String[] args)
   {
      GraphDetector detect = new GraphDetector(new File("/Users/sat/Desktop/IMG_2791.JPG"), null);
      JFrame framer = new JFrame();
      framer.add(detect);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);
      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
}
