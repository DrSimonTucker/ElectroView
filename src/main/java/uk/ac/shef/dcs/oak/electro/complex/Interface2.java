package uk.ac.shef.dcs.oak.electro.complex;

import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

public class Interface2 extends JFrame
{
   Model mod;

   public Interface2(Model model)
   {
      mod = model;
      init();
   }

   private void init()
   {
      this.setLayout(new GridLayout(2, 1));
      GraphPanel graph = new GraphPanel(mod);
      this.add(graph);

      GraphPanel graph2 = new GraphPanel(mod);
      graph2.setAsFollower();
      this.add(graph2);

      this.setSize(500, 500);
      this.setLocationRelativeTo(null);
   }

   public static void main(String[] args) throws IOException
   {
      Model mod = new Model();
      mod.loadData(new File("/Users/sat/oneday.txt"));
      mod.setFixed(60 * 60 * 1000);

      Interface2 display = new Interface2(mod);
      display.setVisible(true);
      display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
}
