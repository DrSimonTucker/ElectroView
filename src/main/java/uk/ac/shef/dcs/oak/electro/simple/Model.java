package uk.ac.shef.dcs.oak.electro.simple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Model
{
   private final File dataDir;
   private final DateFormat df = DateFormat.getDateInstance();

   private String fixedDate = "";
   private final List<Reading> fixedReadings = new LinkedList<Reading>();
   private final List<Reading> guessReadings = new LinkedList<Reading>();

   private long maxDate = 0;
   private long minDate = Long.MAX_VALUE;

   public Model(File dir)
   {
      dataDir = dir;
   }

   public void addGuess(File data, String date)
   {
      fixDate(date);
      try
      {
         long offset = df.parse(date).getTime();

         BufferedReader reader = new BufferedReader(new FileReader(data));
         for (String line = reader.readLine(); line != null; line = reader.readLine())
         {
            String[] elems = line.trim().split("\\s+");
            long time = offset + Long.parseLong(elems[0]) * 1000;

            double wattage = Double.parseDouble(elems[1]);
            guessReadings.add(new Reading(0, time, wattage));
         }
         reader.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (ParseException e)
      {
         e.printStackTrace();
      }
   }

   public void fixDate(String date)
   {
      fixedReadings.clear();
      fixedDate = date;

      try
      {
         minDate = df.parse(date).getTime();
         maxDate = minDate + 24 * 60 * 60 * 1000;
      }
      catch (ParseException e)
      {
         e.printStackTrace();
      }
   }

   public long getDateRange()
   {
      if (maxDate == 0)
         getMinMax();
      return maxDate - minDate;
   }

   public Collection<String> getDates()
   {

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

   public List<Reading> getGuesses()
   {
      return guessReadings;
   }

   public long getMinDate()
   {
      if (maxDate == 0)
         getMinMax();
      return minDate;
   }

   private void getMinMax()
   {
      minDate = Long.MAX_VALUE;
      maxDate = 0;
      for (Reading r : getReadings())
      {
         minDate = Math.min(minDate, r.getTimestamp());
         maxDate = Math.max(maxDate, r.getTimestamp());
      }
   }

   public List<Reading> getReadings()
   {
      if (fixedReadings.size() == 0)
         fixedReadings.addAll(getReadings(fixedDate));

      return fixedReadings;
   }

   public List<Reading> getReadings(String date)
   {
      List<Reading> readings = new LinkedList<Reading>();
      for (File f : dataDir.listFiles())
         try
         {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
               Reading r = Reading.parseLine(line);

               if (r != null && df.format(r.getTimestamp()).equals(date))
                  readings.add(r);
            }
            reader.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }

      return readings;
   }
}
