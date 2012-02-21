package uk.ac.shef.dcs.oak.electro;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SyncRead
{
   public static class MyUserInfo implements UserInfo
   {
      String password = "";

      public MyUserInfo()
      {
         try
         {
            BufferedReader reader = new BufferedReader(new FileReader(".password"));
            password = reader.readLine();
            if (password != null)
               password = password.trim();
            reader.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      @Override
      public String getPassphrase()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public String getPassword()
      {
         return "liraom";
      }

      @Override
      public boolean promptPassphrase(String arg0)
      {
         System.out.println(arg0);
         return true;
      }

      @Override
      public boolean promptPassword(String arg0)
      {
         System.out.println(arg0);
         return true;
      }

      @Override
      public boolean promptYesNo(String arg0)
      {
         System.out.println(arg0);
         return true;
      }

      @Override
      public void showMessage(String arg0)
      {
         System.out.println(arg0);
      }

   }

   public List<String> readLines() throws IOException
   {
      List<String> lines = new LinkedList<String>();
      try
      {

         // Read a file over an ssh connection
         String IP = "143.167.9.252";

         JSch jsch = new JSch();
         Session s = jsch.getSession("viglen", IP);
         UserInfo ui = new MyUserInfo();
         s.setUserInfo(ui);
         s.connect();
         System.out.println("Connected");

         String command = "cat /home/electric/log/monitor*";
         Channel channel = s.openChannel("exec");
         ((ChannelExec) channel).setCommand(command);
         OutputStream out = channel.getOutputStream();
         InputStream is = channel.getInputStream();
         channel.connect();
         byte[] buf = new byte[2048];
         // send '\0'
         buf[0] = 0;
         out.write(buf, 0, 1);
         out.flush();
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         for (String line = reader.readLine(); line != null; line = reader.readLine())
         {
            lines.add(line.trim());
         }

         System.out.println("Read lines");

         reader.close();
         channel.disconnect();
         s.disconnect();
      }
      catch (JSchException e)
      {
         throw new IOException(e);
      }
      return lines;
   }
}
