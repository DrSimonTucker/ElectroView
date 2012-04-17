package uk.ac.shef.dcs.oak.electro.simple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Model
{
   private final File dataDir;
   private final DateFormat df = DateFormat.getDateInstance();

   private String fixedDate = "";
   private final List<Reading> fixedReadings = new LinkedList<Reading>();
   private final List<Reading> guessReadings = new LinkedList<Reading>();

   private long maxDate = 0;
   private long minDate = Long.MAX_VALUE;

   boolean scaleGuesses = true;

   public Model(File dir)
   {
      dataDir = dir;
   }

   public void addGuess(File data, String date)
   {
      System.out.println("Adding guess data");
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
      if (date != null)
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
   }

   public long getDateRange()
   {
      if (maxDate == 0)
         getMinMax();
      return maxDate - minDate;
   }

   public Collection<String> getDates()
   {
      List<Long> dateValues = new LinkedList<Long>();
      List<String> dates = new LinkedList<String>();
      for (File f : dataDir.listFiles())
         try
         {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();
            if (line != null)
            {
               String[] elems = line.trim().split(",");
               Long dateValue = Long.parseLong(elems[0]) * 1000;
               dateValues.add(dateValue);
            }
            reader.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }

      Collections.sort(dateValues);
      for (Long dateValue : dateValues)
         if (!dates.contains(df.format(dateValue)))
            dates.add(df.format(dateValue));
      return dates;
   }

   public String getFixedDate()
   {
      return fixedDate;
   }

   public List<Reading> getGuesses()
   {
      if (scaleGuesses)
      {
         List<Reading> scaledReadings = new LinkedList<Reading>();
         double minWatts = Double.MAX_VALUE;
         double maxWatts = 0;
         for (Reading reading : fixedReadings)
         {
            minWatts = Math.min(minWatts, reading.getWattage());
            maxWatts = Math.max(maxWatts, reading.getWattage());
         }

         double minGuess = Double.MAX_VALUE;
         double maxGuess = 0;
         for (Reading reading : guessReadings)
         {
            minGuess = Math.min(minGuess, reading.getWattage());
            maxGuess = Math.max(maxGuess, reading.getWattage());
         }

         for (Reading reading : guessReadings)
         {
            double scaledWatts = (reading.getWattage() - minGuess) / (maxGuess - minGuess);
            double reScaledWatts = scaledWatts * (maxWatts - minWatts) + minWatts;
            scaledReadings.add(new Reading(0.0, reading.getTimestamp(), reScaledWatts));
         }

         return scaledReadings;
      }
      else
         return guessReadings;
   }

   public double getMaxValue()
   {
      double maxValue = 0.0;
      for (Reading r : fixedReadings)
         maxValue = Math.max(r.getWattage(), maxValue);
      return maxValue;
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

   public double getValue(double percStart, double percEnd)
   {
      long range = getDateRange();

      double maxValue = 0.0;
      double valSum = 0.0;
      int count = 0;
      for (Reading reading : getReadings())
         if (reading.getTimestamp() >= range * percStart + minDate
               && reading.getTimestamp() <= range * percEnd + minDate)
         {
            count++;
            valSum += reading.getWattage();
            maxValue = Math.max(maxValue, reading.getWattage());
         }
      return maxValue;
   }
}
