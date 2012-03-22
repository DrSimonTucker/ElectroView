package uk.ac.shef.dcs.oak.electro.complex;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GraphPanel extends JPanel implements ModelListener
{
   boolean fixed = false;
   boolean follower = false;
   Model mod;

   public GraphPanel(Model model)
   {
      this.mod = model;
      fixed = model.isFixed();
      System.out.println("FIXED = " + fixed);
      mod.addListener(this);

      this.addMouseListener(new MouseAdapter()
      {

         @Override
         public void mouseClicked(MouseEvent e)
         {
            super.mouseClicked(e);

            if (!fixed)
               if (e.getButton() == MouseEvent.BUTTON1)
                  mod.setLeftSelection(e.getX() / (getWidth() + 0.0));
               else
                  mod.setRightSelection(e.getX() / (getWidth() + 0.0));
         }
      });

      this.addMouseMotionListener(new MouseMotionAdapter()
      {
         @Override
         public void mouseDragged(MouseEvent e)
         {
            if (fixed)
            {
               double percValue = (e.getX() / (getWidth() + 0.0));
               mod.setBounds(percValue);
            }
         }
      });
   }

   @Override
   public void dateUpdated()
   {
      // Ignore
   }

   @Override
   public void modelUpdated()
   {
      repaint();
   }

   @Override
   public void paint(Graphics g)
   {
      super.paint(g);

      long sTime = System.currentTimeMillis();

      // Plot out the graph
      int xTicks = this.getWidth();

      double yMax = mod.getMax();
      int oldY = -1;

      for (int i = 0; i < xTicks; i++)
      {
         double yVal;
         if (!follower)
            yVal = mod.getMean((i + 0.0) / xTicks, (i + 1.0) / xTicks);
         else
            yVal = mod.getCurrMean((i + 0.0) / xTicks, (i + 1.0) / xTicks);

         double pixPerc = yVal / yMax;
         int newY = this.getHeight() - (int) (pixPerc * this.getHeight());
         if (oldY == -1)
            oldY = newY;
         g.drawLine(i, oldY, (i + 1), newY);
         oldY = newY;
      }

      // g.setColor(Color.red);
      // g.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1,
      // this.getHeight());

      // Plot the date selection lines
      if (!follower)
      {
         g.setColor(Color.blue);
         int pixValueLeft = (int) (mod.getLeftPerc() * this.getWidth());
         int pixValueRight = (int) (mod.getRightPerc() * this.getWidth());
         System.out.println(pixValueLeft + " and " + pixValueRight);
         g.drawLine(pixValueLeft, 0, pixValueLeft, this.getHeight());
         g.drawLine(pixValueRight, 0, pixValueRight, this.getHeight());
      }
   }

   public void setAsFollower()
   {
      follower = true;
   }

   public static void main(String[] args) throws Exception
   {
      JFrame framer = new JFrame();
      GraphPanel mine = new GraphPanel(new Model());
      framer.add(mine);

      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);

   }
}
