package uk.ac.shef.dcs.oak.electro.simple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class Model
{
   private final File dataDir;

   public Model(File dir)
   {
      dataDir = dir;
   }

   public Collection<String> getDates()
   {
      DateFormat df = DateFormat.getDateInstance();
      Set<String> dates = new TreeSet<String>();
      for (File f : dataDir.listFiles())
         try
         {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();
            if (line != null)
            {
               String[] elems = line.trim().split(",");
               Long dateValue = Long.parseLong(elems[0]) * 1000;
               dates.add(df.format(dateValue));
            }
            reader.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }

      return dates;
   }
}
