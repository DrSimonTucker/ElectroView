package uk.ac.shef.dcs.oak.electro.complex;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

public class Interface1 extends JFrame
{
   Model mod;

   public Interface1(Model model)
   {
      mod = model;
      init();
   }

   private void init()
   {
      GraphPanel graph = new GraphPanel(mod);
      this.add(graph, BorderLayout.CENTER);

      DatePanel date = new DatePanel(mod);
      this.add(date, BorderLayout.SOUTH);

      this.setSize(500, 500);
      this.setLocationRelativeTo(null);
   }

   public static void main(String[] args) throws IOException
   {
      Model mod = new Model();
      mod.loadData(new File("/Users/sat/oneday.txt"));

      Interface1 display = new Interface1(mod);
      display.setVisible(true);
      display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
}
