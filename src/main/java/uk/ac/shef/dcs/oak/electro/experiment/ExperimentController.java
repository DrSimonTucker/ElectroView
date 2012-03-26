package uk.ac.shef.dcs.oak.electro.experiment;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;

import uk.ac.shef.dcs.oak.electro.ElectroConstants;
import uk.ac.shef.dcs.oak.electro.simple.DayPanel;
import uk.ac.shef.dcs.oak.electro.simple.Model;
import uk.ac.shef.dcs.oak.electro.simple.ModelFactory;

public class ExperimentController
{
   private ExperimentPanel controls;
   private ModelFactory factory;
   private DayPanel graph;
   private Model selected;

   public Object[] getDays(String device)
   {
      selected = factory.buildModel(device);
      return selected.getDates().toArray();
   }

   public Object[] getDevices()
   {
      return factory.getDevices().toArray();
   }

   private void initGUI()
   {
      JFrame framer = new JFrame();
      framer.add(graph, BorderLayout.CENTER);
      framer.add(controls, BorderLayout.SOUTH);
      framer.setVisible(true);
      framer.setExtendedState(JFrame.MAXIMIZED_BOTH);
      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }

   public void runExperiment()
   {
      factory = new ModelFactory(new File(ElectroConstants.DIRECTORY));

      // Prepare the interface elements
      graph = new DayPanel();
      controls = new ExperimentPanel(this);

      // Build up the interface
      initGUI();
   }

   public void selectDate(String date)
   {
      selected.fixDate(date);
      graph.setModel(selected);
   }

   public void showGuess(boolean val)
   {
      graph.showGuess(val);
   }

   public void showTemp(boolean val)
   {
      graph.showTemp(val);
   }

   public void showWatts(boolean val)
   {
      graph.showWatts(val);
   }

   public static void main(String[] args)
   {
      ExperimentController control = new ExperimentController();
      control.runExperiment();
   }
}
