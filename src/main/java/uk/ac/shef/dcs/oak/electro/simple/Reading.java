package uk.ac.shef.dcs.oak.electro.simple;

/**
 * A single electricity meter reading
 * 
 * @author sat
 * 
 */
public class Reading
{
   private final double temperature;
   private final long timestamp;
   private final double wattage;

   public Reading(double temp, long time, double watts)
   {
      temperature = temp;
      timestamp = time;
      wattage = watts;
   }

   public double getTemperature()
   {
      return temperature;
   }

   public long getTimestamp()
   {
      return timestamp;
   }

   public double getWattage()
   {
      return wattage;
   }

   public static Reading parseLine(String line)
   {
      String[] elems = line.trim().split(",");
      if (elems.length == 6)
      {
         long timestamp = Long.parseLong(elems[0]) * 1000;
         double temp = Double.parseDouble(elems[2]);
         double wattage = Double.parseDouble(elems[3]);

         return new Reading(temp, timestamp, wattage);
      }
      else if (elems.length == 4)
      {
         long timestamp = Long.parseLong(elems[0]) * 1000;
         double temp = Double.parseDouble(elems[2]);
         double wattage = Double.parseDouble(elems[3]);

         return new Reading(temp, timestamp, wattage);
      }
      else if (elems.length == 5 && elems[4].equals("0"))
      {
         long timestamp = Long.parseLong(elems[0]) * 1000;
         double temp = Double.parseDouble(elems[2]);
         double wattage = Double.parseDouble(elems[3]);

         return new Reading(temp, timestamp, wattage);
      }
      // else
      // System.err.println(line.trim() + " => " + elems.length);

      return null;
   }
}
