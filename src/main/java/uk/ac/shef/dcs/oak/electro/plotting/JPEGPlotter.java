package uk.ac.shef.dcs.oak.electro.plotting;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;

import uk.ac.shef.dcs.oak.electro.simple.Model;
import uk.ac.shef.dcs.oak.electro.simple.ModelFactory;

public class JPEGPlotter
{
   boolean drawgrid = true;
   int PSIZE = 5;

   private void paintPixel(BufferedImage image, int x, int y, int colour, int psize)
   {
      image.setRGB(x, y, colour);

      // Also set the surrounding pixels
      for (int i = x - psize; i <= x + psize; i++)
         for (int j = y - psize; j <= y + psize; j++)
            if (i < image.getWidth() && i >= 0 && j < image.getHeight() && j >= 0)
               image.setRGB(i, j, colour);
   }

   private Image produceImage(Model electro, int width, int height, int gridsize)
   {
      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

      int gridColor = Color.lightGray.getRGB();
      int graphColor = Color.black.getRGB();

      // TUrn all the pixels white
      for (int i = 0; i < width; i++)
         for (int j = 0; j < height; j++)
            image.setRGB(i, j, Color.white.getRGB());

      // Paint in the grid - first the vertical lines
      if (drawgrid)
      {
         int gcount = 0;
         for (int i = 0; i < width; i += gridsize)
         {
            for (int j = 0; j < height; j += gridsize)
               for (int counter = 0; counter < gridsize; counter++)
                  if (j + counter < height)
                     if (gcount % 3 == 0)
                        paintPixel(image, i, j + counter, gridColor, PSIZE);
                     else
                        paintPixel(image, i, j + counter, gridColor, PSIZE - 4);
            gcount++;
         }
         // image.setRGB(i, j + counter, gridColor);

         // Paint in the grid - next the horizontal
         for (int i = 0; i < height; i += gridsize)
            for (int j = 0; j < width; j += gridsize)
               for (int counter = 0; counter < gridsize; counter++)
                  if (j + counter < width)
                     paintPixel(image, j + counter, i, gridColor, PSIZE);
         // image.setRGB(j + counter, i, gridColor);
      }

      // Draw in the graph, around the grid lines
      int oldY = -1;
      for (int i = 0; i < width; i += gridsize)
      {
         int pixStart = i;
         int pixEnd = i + gridsize;
         double percStart = (pixStart + 0.0) / width;
         double percEnd = (pixEnd + 0.0) / width;

         double percHeight = electro.getValue(percStart, percEnd) / electro.getMaxValue();
         int pixHeight = height - gridsize
               * (Math.min(height - 1, ((int) (height * percHeight))) / gridsize) - 1;
         if (oldY == -1)
            oldY = pixHeight;

         // Draw the up line of the bar
         if (pixHeight > oldY)
            for (int y = oldY; y <= pixHeight; y++)
               paintPixel(image, i, y, graphColor, PSIZE);
         // image.setRGB(i, y, graphColor);
         else
            for (int y = oldY; y >= pixHeight; y--)
               paintPixel(image, i, y, graphColor, PSIZE);
         // image.setRGB(i, y, graphColor);

         // Draw the horizontal line of the bar
         for (int x = i; x <= i + gridsize; x++)
            if (x < width)
               paintPixel(image, x, pixHeight, graphColor, PSIZE);
         // image.setRGB(x, pixHeight, graphColor);

         oldY = pixHeight;

      }

      return image;
   }

   public void saveJPEG(Model electro, File outFile) throws IOException
   {
      Image img = produceImage(electro, 8641, 6121, 120);
      ImageIO.write(toBufferedImage(img), "gif", outFile);
   }

   public static void main(String[] args) throws Exception
   {
      ModelFactory f = new ModelFactory(new File("/Users/sat/workspace/electricity/data/"));
      for (String id : f.getDevices())
      {
         System.out.println("Building for " + id);
         Model mod = f.buildModel(id);
         Iterator<String> dIterator = mod.getDates().iterator();
         dIterator.next();
         mod.fixDate(dIterator.next());
         JPEGPlotter plotter = new JPEGPlotter();
         plotter.saveJPEG(mod, new File(id + ".gif"));
      }
   }

   private static BufferedImage toBufferedImage(Image src)
   {
      int w = src.getWidth(null);
      int h = src.getHeight(null);
      int type = BufferedImage.TYPE_INT_RGB; // other options
      BufferedImage dest = new BufferedImage(w, h, type);
      Graphics2D g2 = dest.createGraphics();
      g2.drawImage(src, 0, 0, null);
      g2.dispose();
      return dest;
   }
}
