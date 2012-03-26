package uk.ac.shef.dcs.oak.electro.experiment;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import uk.ac.shef.dcs.oak.electro.ElectroConstants;
import uk.ac.shef.dcs.oak.electro.simple.DayPanel;
import uk.ac.shef.dcs.oak.electro.simple.DetectionCallback;
import uk.ac.shef.dcs.oak.electro.simple.GraphDetector;
import uk.ac.shef.dcs.oak.electro.simple.Model;
import uk.ac.shef.dcs.oak.electro.simple.ModelFactory;

public class ExperimentController implements DetectionCallback
{
   private ExperimentPanel controls;
   JFrame detectFrame;
   private ModelFactory factory;
   private DayPanel graph;
   private Model selected;

   @Override
   public void detected()
   {
      detectFrame.setVisible(false);
      selected.addGuess(new File("read.txt"), selected.getFixedDate());
      controls.showGuess();
   }

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

   public void loadGuesses()
   {
      System.out.println("Loading Guesses");
      JFileChooser chooser = new JFileChooser();
      chooser.showOpenDialog(graph);
      File chosen = chooser.getSelectedFile();
      if (chosen != null)
      {
         GraphDetector detector = new GraphDetector(chosen, this);
         detectFrame = new JFrame();
         detectFrame.add(detector);
         detectFrame.setVisible(true);
         detectFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
      }
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
