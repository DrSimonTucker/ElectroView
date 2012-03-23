package uk.ac.shef.dcs.oak.electro.simple;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Model factory builds models from a given directory
 * 
 * @author sat
 * 
 */
public class ModelFactory
{
   private final File baseDirectory;

   public ModelFactory(File base)
   {
      baseDirectory = base;
   }

   public Model buildModel(File base, File guess, String date)
   {
      Model m = new Model(base);
      m.addGuess(guess, date);
      return m;
   }

   public Model buildModel(String deviceID)
   {
      return new Model(new File(baseDirectory, deviceID));
   }

   public List<String> getDevices()
   {
      List<String> devices = new LinkedList<String>();
      for (File f : baseDirectory.listFiles())
         if (f.getName().startsWith("0014"))
            devices.add(f.getName());
      return devices;
   }

   public static void main(String[] args)
   {
      ModelFactory f = new ModelFactory(new File("/Users/sat/workspace/electricity/data/"));
      Model mod = f.buildModel("00140b230a80");
      for (String date : mod.getDates())
         System.out.println(date);
   }
}
