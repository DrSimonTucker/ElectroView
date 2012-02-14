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

public class Model
{
   public static void main(String[] args)
   {
      Model mine = Model.getModel();
   }

   private static Model singleton;
   private PreparedStatement insert = null;

   Connection database;

   private Model()
   {
      // Blocking constructor
      connect();
   }

   public double getMax(long startTime, long endTime)
   {
      double retVal = 0.0;
      try
      {
         Statement s = database.createStatement();
         ResultSet rs = s.executeQuery("SELECT MAX(temp) from electro WHERE dt >= " + startTime
               + " AND dt <= " + endTime);
         if (rs.next())
            retVal = rs.getDouble(1);
         rs.close();
         s.close();
      }
      catch (SQLException e)
      {
         e.printStackTrace();
      }

      return retVal;
   }

   public double getMean(long startTime, long endTime)
   {
      double retVal = 0.0;
      try
      {
         Statement s = database.createStatement();
         ResultSet rs = s.executeQuery("SELECT SUM(temp)/COUNT(temp) from electro WHERE dt >= "
               + startTime + " AND dt <= " + endTime);
         if (rs.next())
            retVal = rs.getDouble(1);
         rs.close();
         s.close();
      }
      catch (SQLException e)
      {
         e.printStackTrace();
      }

      return retVal;
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
         System.exit(1);
      }
      catch (SQLException e)
      {
         e.printStackTrace();
         System.exit(1);
      }
      catch (IOException e)
      {
         e.printStackTrace();
         System.exit(1);
      }
   }

   private void sync() throws SQLException, IOException
   {
      long maxTime = Long.MIN_VALUE;
      Statement s = database.createStatement();
      ResultSet rs = s.executeQuery("SELECT MAX(dt) from electro");
      if (rs.next())
         maxTime = rs.getLong(1);
      sync(new File("/Users/sat/workspace/electricity/data/00140b23096d/"), maxTime);
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
      sync(new File("/Users/sat/workspace/electricity/data/00140b23096d/"), Long.MIN_VALUE);
   }

   private void sync(File syncDir, long maxTime) throws IOException, SQLException
   {
      for (File f : syncDir.listFiles())
      {
         if (f.isFile())
            syncFile(f, maxTime);
      }

      if (insert != null)
         insert.executeBatch();
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

            if (timestamp > maxTime)
            {
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
   }

   public static Model getModel()
   {
      if (singleton == null)
         singleton = new Model();
      return singleton;
   }

}
