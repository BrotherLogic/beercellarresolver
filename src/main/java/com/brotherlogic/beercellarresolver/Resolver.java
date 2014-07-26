package com.brotherlogic.beercellarresolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Resolver
{
   public static DateFormat df = new SimpleDateFormat("dd/MM/yy");

   private final String baseline = "/Users/simon/Dropbox/beer/";
   Map<String, Integer> drunk;
   List<Beer> bombers;
   List<Beer> smalls;
   String lastDate = "";

   public void organise()
   {
      try
      {
         loadOrg();

         int cube = 1;
         System.out.println("CUBE1");
         System.out.println("-----");
         System.out.println();
         int count = 0;
         for (int i = smalls.size() - 1; i >= 0; i--)
         {
            System.out.println(smalls.get(i));
            count++;
            if (count % 30 == 0)
            {
               System.out.println("CUBE" + (++cube));
               System.out.println("-----");
               System.out.println();
            }
         }

         System.out.println("Bombers");
         System.out.println("CUBE" + (++cube));
         System.out.println("-----");
         System.out.println();

         count = 0;
         for (int i = bombers.size() - 1; i >= 0; i--)
         {
            System.out.println(bombers.get(i));
            count++;
            if (count % 20 == 0)
            {
               System.out.println("CUBE" + (++cube));
               System.out.println("-----");
               System.out.println();
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void run() throws ParseException
   {
      try
      {
         load();

         // Figure out how many times we need to pick - one Sunday ahead
         Calendar nextSunday = Calendar.getInstance();
         for (int i = 0; i < 1; i++)
         {
            while (nextSunday.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
               nextSunday.add(Calendar.DAY_OF_WEEK, 1);
            nextSunday.add(Calendar.DAY_OF_WEEK, 1);
         }

         Calendar lastDone = Calendar.getInstance();
         lastDone.setTime(df.parse(lastDate));

         for (Integer val : new Integer[]
         { Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND })
            lastDone.set(val, nextSunday.get(val));

         DateFormat df2 = DateFormat.getDateTimeInstance();
         if (lastDone.before(nextSunday))
         {
            System.out.println(df2.format(lastDone.getTime()) + " vs "
                  + df2.format(nextSunday.getTime()));
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File(baseline
                  + "todrink.list"), true));
            pw.println(df.format(nextSunday.getTime()));
            for (int i = 0; i < Math.min(2, bombers.size()); i++)
               pw.println(bombers.get(i).name);
            for (int i = 0; i < Math.min(4, smalls.size()); i++)
               pw.println(smalls.get(i));

            int bCount = 2 - bombers.size();
            int sCount = 4 - smalls.size();
            if (bCount < 0)
               bCount = 0;
            if (sCount < 0)
               sCount = 0;
            for (int i = 0; i < bCount + sCount; i++)
               pw.println("EMPTY");
            pw.close();
         }

      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private void load() throws IOException
   {
      drunk = loadDone("todrink.list");
      bombers = loadToGo("bomber.list", false);
      smalls = loadToGo("smaller.list", false);

      Collections.sort(bombers);
      Collections.sort(smalls);
   }

   private Map<String, Integer> loadDone(String name) throws IOException
   {
      Map<String, Integer> counts = new TreeMap<String, Integer>();

      File f = new File(baseline + name);
      BufferedReader reader = new BufferedReader(new FileReader(f));
      int lcount = 0;
      for (String line = reader.readLine(); line != null; line = reader.readLine())
         if (lcount++ % 7 == 0)
            lastDate = line.trim();
         else
         {
            String beerDrunk = line.trim();
            if (counts.containsKey(beerDrunk))
               counts.put(beerDrunk, counts.get(beerDrunk) + 1);
            else
               counts.put(beerDrunk, 1);
         }
      reader.close();

      return counts;
   }

   private void loadOrg() throws IOException
   {
      drunk = loadDone("todrink.list");
      bombers = loadToGo("bomber.list", true);
      smalls = loadToGo("smaller.list", true);

      Collections.sort(bombers);
      Collections.sort(smalls);
   }

   private List<Beer> loadToGo(String name, boolean avail) throws IOException
   {
      List<Beer> beers = new LinkedList<Beer>();
      File f = new File(baseline + name);
      BufferedReader reader = new BufferedReader(new FileReader(f));
      for (String line = reader.readLine(); line != null; line = reader.readLine())
      {
         String[] elems = line.trim().split("~");
         try
         {
            Beer b = new Beer(elems[0], elems[1]);
            if (avail || b.isAvailable())
               if (!drunk.containsKey(b.name) || drunk.get(b.name) < 0)
                  beers.add(b);
               else
                  drunk.put(b.name, drunk.get(b.name) - 1);
         }
         catch (ParseException e)
         {
            System.out.println("Cannot parse: " + elems[1]);
         }
      }
      reader.close();

      return beers;
   }

   public static void main(String[] args) throws Exception
   {
      Resolver r = new Resolver();
      r.organise();
   }
}

class Beer implements Comparable<Beer>
{
   Long outDate;
   String name;

   public Beer(String name, String date) throws ParseException
   {
      this.name = name;
      outDate = Resolver.df.parse(date).getTime();
   }

   @Override
   public int compareTo(Beer o)
   {
      return outDate.compareTo(o.outDate);
   }

   public boolean isAvailable()
   {
      Calendar now = Calendar.getInstance();
      return outDate < now.getTimeInMillis();
   }

   @Override
   public String toString()
   {
      return name;
   }
}
