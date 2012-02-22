package uk.ac.shef.dcs.oak.electro;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class Model
{
   private static Connection database;

   private static final int TIME = 2 * 1000;

   public static void main(String[] args)
   {
      // Model mine = Model.getModel();
   }

   int addCount = 0;
   private PreparedStatement insert = null;

   // A list of the timestamps stored, used to increase the speed of some
   // calculations
   List<Long> keys = new LinkedList<Long>();
   List<ModelListener> listeners = new LinkedList<ModelListener>();
   long minVal;
   long offsetEnd;
   long offsetStart;
   long oldMaxTime = 0;
   private final SyncRead reader = new SyncRead();

   // Flag indicating that we should pull data remotely
   private boolean synching = true;

   // This stores all the usage data
   private final Map<Long, Double> useMap = new TreeMap<Long, Double>();

   public Model(long start, long end)
   {
      this.offsetStart = start;
      this.offsetEnd = end;

      // Blocking constructor
      if (database == null)
         connect();

      // Start to sync every 10 seconds
      runSync();
   }

   public void addListener(ModelListener list)
   {
      listeners.add(list);
   }

   private void alertListeners()
   {
      for (ModelListener listener : listeners)
         listener.modelUpdated();
   }

   private void build(File f) throws ClassNotFoundException, SQLException, IOException
   {
      connect(f);
      Statement s = database.createStatement();
      s.executeUpdate("DROP TABLE IF EXISTS electro");
      s.executeUpdate("CREATE TABLE electro (dt TIMESTAMP, temp DOUBLE, watts INTEGER)");
      s.executeUpdate("CREATE INDEX dt_index ON electro (dt)");
      s.close();
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
            sync();
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

   private void connect(File f) throws ClassNotFoundException, SQLException
   {
      Class.forName("org.sqlite.JDBC");
      database = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
   }

   public int findIndex(long val)
   {
      for (int i = 0; i < keys.size(); i++)
         if (keys.get(i) > val)
            return i - 1;

      return keys.size() - 1;
   }

   public double getMax()
   {
      Double maxVal = 0.0;
      for (Double val : useMap.values())
         maxVal = Math.max(val, maxVal);
      return maxVal;
   }

   public double getMax(long startTime, long endTime)
   {
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

      for (int i = findIndex(startTime); i <= findIndex(endTime); i++)
      {
         count++;
         retVal += useMap.get(keys.get(i));
      }

      // Deal with the problem of missing data
      if (count == 0)
      {
         // Interpolate over two values
         long closeStart = Long.MAX_VALUE;
         double startVal = 0;
         long closeEnd = Long.MAX_VALUE;
         double endVal = 0;

         for (Long key : useMap.keySet())
         {
            long sDiff = startTime - key;
            long eDiff = key - endTime;
            if (sDiff > 0 && sDiff < closeStart)
            {
               closeStart = sDiff;
               startVal = useMap.get(key);
            }
            if (eDiff > 0 && eDiff < closeEnd)
            {
               closeEnd = eDiff;
               endVal = useMap.get(key);
            }
         }

         return (startVal + endVal) / 2;
      }
      else
         return retVal / count;
   }

   private void runSync()
   {
      if (synching)
         try
         {
            sync();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
         catch (SQLException e)
         {
            e.printStackTrace();
         }

      // Run again in 10s
      Timer t = new Timer();
      t.schedule(new TimerTask()
      {
         @Override
         public void run()
         {
            runSync();
         }
      }, TIME);
   }

   public void setSynching(boolean val)
   {
      synching = val;
   }

   private void sync() throws SQLException, IOException
   {
      addCount = 0;

      // Synchronise the database
      long maxTime = System.currentTimeMillis() / 1000 - offsetEnd;
      Statement s = database.createStatement();
      ResultSet rs = s.executeQuery("SELECT MAX(dt) from electro");
      if (rs.next())
         maxTime = rs.getTimestamp(1).getTime();
      if (useMap.size() > 0)
         syncLines(reader.readLines(), maxTime);
      else
         syncLines(reader.readFullLines(), maxTime);
      rs.close();
      s.close();

      // Synchronise the mapper
      PreparedStatement ps = database
            .prepareStatement("SELECT dt,temp from electro WHERE dt > ? ORDER by dt ASC");
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
      List<Long> toRemove = new LinkedList<Long>();
      for (Entry<Long, Double> entry : useMap.entrySet())
         if (entry.getKey() < minVal)
            toRemove.add(entry.getKey());
      for (Long rem : toRemove)
         useMap.remove(rem);

      // Crossover
      useMap.putAll(addMap);

      // Update listeners
      if (addMap.size() > 0)
      {
         alertListeners();

         // Update the keys list
         keys.clear();
         keys.addAll(useMap.keySet());
         Collections.sort(keys);
      }
   }

   private void syncLines(List<String> lines, long maxTime) throws IOException, SQLException
   {
      addCount = 0;
      for (String line : lines)
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

      if (insert != null)
         insert.executeBatch();
   }
}
