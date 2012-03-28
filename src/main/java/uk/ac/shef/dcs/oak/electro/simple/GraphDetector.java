package uk.ac.shef.dcs.oak.electro.simple;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class GraphDetector extends JPanel
{
   JProgressBar bar;
   DetectionCallback callback;
   int counter = 0;
   boolean fixed = false;
   File imageFile;
   BufferedImage img;
   int minY = -1;
   double perc;
   Map<Integer, Integer> posMap = new TreeMap<Integer, Integer>();
   boolean runFix = false;
   int[] vals;
   int[] x = new int[4];
   double xStart, xEnd, yStart, yEnd;
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
            if (arg0.getButton() == 1 && arg0.getModifiers() == 16)
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

      this.addMouseMotionListener(new MouseMotionAdapter()
      {

         @Override
         public void mouseDragged(MouseEvent e)
         {
            if (fixed)
            {
               if (minY == -1)
                  minY = e.getY();
               posMap.put(e.getX(), e.getY());
               repaint();
            }
         }

      });
   }

   private void fixPosMap()
   {
      if (posMap.size() > 0)
      {
         int prev = minY;
         for (int i = 0; i < this.getWidth(); i++)
            if (posMap.containsKey(i))
               prev = posMap.get(i);
            else
               posMap.put(i, prev);
      }
      runFix = true;
   }

   private double getMidPoint(double x1, double x2, double y1, double y2, boolean draw)
   {
      int imgHeight = img.getHeight(this);
      int imgWidth = img.getWidth(this);

      double bestValue = 0;
      int bestIndex = 0;
      int bestX = 0;
      for (int i = ((int) (y1 * imgHeight)); i < ((int) (y2 * imgHeight)); i++)
      {
         double xperc = (x1 + ((x2 - x1) + 0.0) * (i - (y1 * imgHeight))
               / (y2 * imgHeight - y1 * imgHeight));
         int xVal = Math.max(0, (int) (xperc * imgWidth));
         int rgb = img.getRGB(xVal, i);
         if (draw)
            img.setRGB(xVal, i, Color.blue.getRGB());

         Color c = new Color(rgb);

         double sumv = 255 * 3 - (c.getBlue() + c.getGreen() + c.getRed());
         // System.out.println(i + ": " + sumv + " => " + c.getBlue() + " and "
         // + c.getGreen()
         // + " and " + c.getRed());
         double weight = getWeight((xVal + 0.0) / imgWidth, (i + 0.0) / imgHeight);
         if (sumv * weight > bestValue)
         {
            bestValue = sumv * weight;
            bestIndex = i;
            bestX = xVal;
         }
      }

      System.out.println(bestValue);

      // System.exit(1);

      /*
       * if (counter == 0) img.setRGB(bestX, bestIndex, Color.magenta.getRGB());
       */
      repaint();

      // System.out.println(1 - ((bestIndex - y1 * imgHeight) / (y2 * imgHeight
      // - y1 * imgHeight)));
      return 1 - (bestIndex - y1 * imgHeight) / (y2 * imgHeight - y1 * imgHeight);
   }

   private double getWeight(double x, double y)
   {
      if (posMap.size() == 0)
         return 1.0;

      int xCoord = (int) (x * this.getWidth());
      int yCoord = (int) (y * this.getHeight());

      double weight = Math.abs(yCoord - posMap.get(xCoord));
      if (weight < this.getHeight() * 0.05)
         return 1.0;
      else
         return weight / this.getHeight();
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

      g.setColor(Color.magenta);
      g.drawLine((int) (xStart * this.getWidth()), (int) (yStart * this.getHeight()),
            (int) (xEnd * this.getWidth()), (int) (yEnd * this.getHeight()));

      // Paint the electricity curve
      g.setColor(Color.green);
      int oldX = 0;
      int oldY = 0;
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

      if (!runFix)
      {
         g.setColor(Color.blue);
         for (Entry<Integer, Integer> entry : posMap.entrySet())
            g.drawLine(entry.getKey(), entry.getValue(), entry.getKey() + 1, entry.getValue() + 1);
      }
   }

   private void produceGraph()
   {
      fixPosMap();

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
         }
      });

      graphThread.start();
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

         xStart = (x[0] + (x[3] - x[0]) * perc) / this.getWidth();
         xEnd = (x[1] + (x[2] - x[1]) * perc) / this.getWidth();
         yStart = (y[0] + (y[3] - y[0]) * perc) / this.getHeight();
         yEnd = (y[1] + (y[2] - y[1]) * perc) / this.getHeight();
         vals[i] = (int) (1000 * (getMidPoint(xStart, xEnd, yStart, yEnd, false)));
         ps.println(i + " " + vals[i]);
      }
      ps.close();

      if (callback != null)
         callback.detected();
   }

   public static void main(String[] args) throws Exception
   {
      GraphDetector detect = new GraphDetector(new File("/Users/sat/Desktop/IMG_2876.JPG"), null);
      JFrame framer = new JFrame();
      framer.add(detect);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);
      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
}
