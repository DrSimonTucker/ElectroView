package uk.ac.shef.dcs.oak.electro.plotting;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import uk.ac.shef.dcs.oak.electro.simple.Model;
import uk.ac.shef.dcs.oak.electro.simple.ModelFactory;

public class JPEGPlotter
{
   private Image produceImage(Model electro)
   {
      BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY);
      return image;
   }

   public void saveJPEG(Model electro, File outFile) throws IOException
   {
      Image img = produceImage(electro);
      ImageIO.write(toBufferedImage(img), "jpg", outFile);
   }

   public static void main(String[] args) throws Exception
   {
      ModelFactory f = new ModelFactory(new File("/Users/sat/workspace/electricity/data/"));
      Model mod = f.buildModel("00140b230a80");
      mod.fixDate("Mar 25, 2012");
      JPEGPlotter plotter = new JPEGPlotter();
      plotter.saveJPEG(mod, new File("test.jpg"));
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
