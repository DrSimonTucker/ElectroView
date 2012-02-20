package uk.ac.shef.dcs.oak.electro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Model
{
   public static void main(String[] args)
   {
      // Model mine = Model.getModel();
   }

   // This stores all the usage data
   private final Map<Long, Double> useMap = new TreeMap<Long, Double>();

   int addCount = 0;

   private PreparedStatement insert = null;

   private static Connection database;

   long offsetStart;
   long offsetEnd;

   long minVal;

   public Model(long start, long end)
   {
      this.offsetStart = start;
      this.offsetEnd = end;

      // Blocking constructor
      if (database == null)
         connect();
   }

   private final double fixedMax = -1;

   public double getMax()
   {
      System.out.println("SIZE = " + useMap.size());
      Double maxVal = 0.0;
      for (Double val : useMap.values())
         maxVal = Math.max(val, maxVal);
      return maxVal;
   }

   public double getMax(long startTime, long endTime)
   {
      if (fixedMax >= 0)
         return fixedMax;

      double max = 0.0;
      for (Long key : useMap.keySet())
         if (key >= startTime && key <= endTime)
            max = Math.max(useMap.get(key), max);
      return max;
   }

   public double getMean(double percLeft, double percRight)
   {
      double retVal = 0.0;
      double count = 0.0;

      long startTime = (long) (percLeft * (offsetEnd - offsetStart) + minVal);
      long endTime = (long) (percRight * (offsetEnd - offsetStart) + minVal);

      for (Long key : useMap.keySet())
         if (key >= startTime && key <= endTime)
         {
            count++;
            retVal += useMap.get(key);
         }

      if (retVal > 0)
         System.out.println("HERE = " + startTime + "," + endTime);
      else
      {
         System.out.println(range());
         System.out.println(minVal + " => " + (minVal + offsetEnd));
         System.exit(1);
      }

      return retVal / count;
   }

   private String range()
   {
      long lowest = Long.MAX_VALUE;
      long largest = 0;
      for (Long val : useMap.keySet())
      {
         lowest = Math.min(lowest, val);
         largest = Math.max(largest, val);
      }

      return lowest + " => " + largest + " R";
   }

   private void connect()
   {
      try
      {
         File f = new File(System.getProperty("user.home") + File.separator + ".electro.sqlite");
         if (f.exists())
         {
            connect(f);
            sync();
         }
         else
         {
            build(f);
         }
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
      }
      catch (SQLException e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   long oldMaxTime = 0;

   private void sync() throws SQLException, IOException
   {
      addCount = 0;

      // Synchronise the database
      long maxTime = System.currentTimeMillis() / 1000 - offsetEnd;
      System.out.println("MAX = " + maxTime);
      Statement s = database.createStatement();
      ResultSet rs = s.executeQuery("SELECT MAX(dt) from electro");
      if (rs.next())
         maxTime = rs.getTimestamp(1).getTime();
      System.out.println(maxTime);
      sync(new File("/Users/sat/workspace/electricity/data2/"), maxTime);
      rs.close();
      s.close();

      // Synchronise the mapper
      PreparedStatement ps = database
            .prepareStatement("SELECT dt,watts from electro WHERE dt > ? ORDER by dt ASC");
      ps.setTimestamp(1, new Timestamp(oldMaxTime));
      rs = ps.executeQuery();
      Map<Long, Double> addMap = new TreeMap<Long, Double>();
      while (rs.next())
      {
         oldMaxTime = rs.getTimestamp(1).getTime();
         addMap.put(rs.getTimestamp(1).getTime(), rs.getDouble(2));
      }
      rs.close();
      ps.close();

      // Remove the old elements
      minVal = System.currentTimeMillis() / 1000 - offsetEnd;
      for (Entry<Long, Double> entry : useMap.entrySet())
         if (entry.getKey() < minVal)
            useMap.remove(entry.getKey());

      // Crossover
      useMap.putAll(addMap);
   }

   private void connect(File f) throws ClassNotFoundException, SQLException
   {
      Class.forName("org.sqlite.JDBC");
      database = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
   }

   private void build(File f) throws ClassNotFoundException, SQLException, IOException
   {
      connect(f);
      Statement s = database.createStatement();
      s.executeUpdate("DROP TABLE IF EXISTS electro");
      s.executeUpdate("CREATE TABLE electro (dt TIMESTAMP, temp DOUBLE, watts INTEGER)");
      s.executeUpdate("CREATE INDEX dt_index ON electro (dt)");
      sync(new File("/Users/sat/workspace/electricity/data2/"), System.currentTimeMillis() / 1000
            - offsetEnd);
      s.close();
   }

   private void sync(File syncDir, long maxTime) throws IOException, SQLException
   {
      for (File f : syncDir.listFiles())
      {
         if (f.isFile())
            syncFile(f, maxTime);
      }

      System.out.println("Synchronising the database: " + addCount);
      if (insert != null)
         insert.executeBatch();
      System.out.println("Synchronisation complete");
   }

   private void syncFile(File f, long maxTime) throws IOException, SQLException
   {
      BufferedReader reader = new BufferedReader(new FileReader(f));
      for (String line = reader.readLine(); line != null; line = reader.readLine())
      {
         String[] elems = line.trim().split(",");
         if (elems.length == 6)
         {
            long timestamp = Long.parseLong(elems[0]);
            double temp = Double.parseDouble(elems[2]);
            int watts = Integer.parseInt(elems[3]);

            if (maxTime == 0)
               throw new IOException();
            if (timestamp > maxTime)
            {
               addCount++;
               if (insert == null)
                  insert = database
                        .prepareStatement("INSERT into electro (dt,temp,watts) VALUES (?,?,?)");

               insert.setTimestamp(1, new Timestamp(timestamp));
               insert.setDouble(2, temp);
               insert.setInt(3, watts);
               insert.addBatch();
            }
         }
      }
      reader.close();
   }
}
