package com.brotherlogic.beercellarresolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Resolver
{
   public static DateFormat df = new SimpleDateFormat("dd/MM/yy");

   private final String baseline = "/Users/simon/Dropbox/beer/";
   Set<Beer> drunk;
   List<Beer> bombers;
   List<Beer> smalls;
   Set<Integer> bomberCubes = new TreeSet<Integer>();
   Set<Integer> smallCubes = new TreeSet<Integer>();
   String lastDate = "";

   Map<Integer, List<Beer>> currOrg;

   public void organise()
   {
      try
      {
         loadOrg();

         // System.out.println(drunk);

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

         if (lastDone.before(nextSunday))
         {
            System.out.println(df.format(nextSunday.getTime()));
            for (int i = 0; i < Math.min(2, bombers.size()); i++)
               System.out.println(bombers.get(i));
            for (int i = 0; i < Math.min(4, smalls.size()); i++)
               System.out.println(smalls.get(i));

            int bCount = 2 - bombers.size();
            int sCount = 4 - smalls.size();
            if (bCount < 0)
               bCount = 0;
            if (sCount < 0)
               sCount = 0;
            for (int i = 0; i < bCount + sCount; i++)
               System.out.println("EMPTY");
         }
      }
      catch (Exception e)
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

   private Set<Beer> loadDone(String name) throws IOException
   {
      Set<Beer> counts = new TreeSet<Beer>();

      File f = new File(baseline + name);
      BufferedReader reader = new BufferedReader(new FileReader(f));
      int lcount = 0;
      for (String line = reader.readLine(); line != null; line = reader.readLine())
         if (lcount++ % 7 == 0)
            lastDate = line.trim();
         else
            try
            {
               Beer beerDrunk = new Beer(line.trim());
               counts.add(beerDrunk);
            }
            catch (ParseException e)
            {
               e.printStackTrace();
            }
      reader.close();

      return counts;
   }

   private void loadOrg() throws Exception
   {
      drunk = loadDone("todrink.list");

      bombers = loadToGo("bomber.list", true);
      smalls = loadToGo("smaller.list", true);

      Collections.sort(bombers);
      Collections.sort(smalls);

      currOrg = loadPlaced("organise.list");
   }

   private Map<Integer, List<Beer>> loadPlaced(String filename) throws IOException
   {
      Map<Integer, List<Beer>> placed = new TreeMap<Integer, List<Beer>>();
      for (int i = 1; i <= 8; i++)
         placed.put(i, new LinkedList<Beer>());

      File f = new File(baseline + filename);
      int currentCube = 0;
      BufferedReader reader = new BufferedReader(new FileReader(f));
      for (String line = reader.readLine(); line != null; line = reader.readLine())
         if (line.startsWith("CUBE"))
            currentCube++;
         else if (!line.startsWith("----") && line.trim().length() > 0)
            try
            {
               placed.get(currentCube).add(new Beer(line.trim()));
            }
            catch (ParseException e)
            {
               e.printStackTrace();
            }

      reader.close();

      return placed;
   }

   private List<Beer> loadToGo(String name, boolean avail) throws IOException
   {
      List<Beer> beers = new LinkedList<Beer>();
      File f = new File(baseline + name);
      BufferedReader reader = new BufferedReader(new FileReader(f));
      for (String line = reader.readLine(); line != null; line = reader.readLine())
         if (line.trim().length() > 0)
         {
            String[] elems = line.trim().split("~");
            try
            {
               if (elems.length != 2)
               {
                  System.err.println("ERROR: " + line.trim());
                  System.exit(1);
               }
               Beer b = new Beer(elems[0], elems[1]);
               if (avail || b.isAvailable())
                  if (!drunk.contains(b))
                     beers.add(b);
            }
            catch (ParseException e)
            {
               System.out.println("Cannot parse: " + elems[1]);
            }
         }
      reader.close();

      return beers;
   }

   private void organiseInPlace() throws Exception
   {
      loadOrg();

      for (Entry<Integer, List<Beer>> entry : currOrg.entrySet())
         for (int i = entry.getValue().size() - 1; i >= 0; i--)
         {
            Beer orgd = entry.getValue().get(i);
            boolean found = false;
            for (int j = smalls.size() - 1; j >= 0; j--)
               if (smalls.get(j).equals(orgd))
               {
                  smallCubes.add(entry.getKey());
                  smalls.remove(j);
                  found = true;
                  break;
               }

            if (!found)
               for (int j = bombers.size() - 1; j >= 0; j--)
                  if (bombers.get(j).equals(orgd))
                  {
                     bomberCubes.add(entry.getKey());
                     bombers.remove(j);
                     found = true;
                     break;
                  }
         }

      for (int i = bombers.size() - 1; i >= 0; i--)
         currOrg = place(bombers.get(i), currOrg, true);

      for (int i = smalls.size() - 1; i >= 0; i--)
         currOrg = place(smalls.get(i), currOrg, false);

      for (Entry<Integer, List<Beer>> cube : currOrg.entrySet())
      {
         System.out.println();
         System.out.println("CUBE" + cube.getKey());
         System.out.println("------");

         for (int i = 0; i < cube.getValue().size(); i++)
            System.out.println(cube.getValue().get(i));
      }
   }

   private Map<Integer, List<Beer>> place(Beer beer, Map<Integer, List<Beer>> placed, boolean bomber)
   {

      int[] available = new int[8];
      for (int i = 0; i < available.length; i++)
         available[i] = 31;

      if (bomber)
         for (Integer sc : smallCubes)
            available[sc - 1] = -1;
      else
         for (Integer bc : bomberCubes)
            available[bc - 1] = -1;

      int best = 0;
      for (int i = 0; i < available.length; i++)
         if (available[i] >= 0)
         {
            List<Beer> cubeCurrent = placed.get(i + 1);
            if ((bomber && cubeCurrent.size() < 20) || (!bomber && cubeCurrent.size() < 30))
            {
               // We can place in this cube
               if (cubeCurrent.size() > 0)
                  for (int j = cubeCurrent.size() - 1; j >= 0; j--)
                     if (beer.compareTo(cubeCurrent.get(j)) >= 0)
                        available[i] = cubeCurrent.size() - j;
            }
            else
               available[i] = -1;
         }

      int bestCount = Integer.MAX_VALUE;
      for (int i = 0; i < available.length; i++)
         if (available[i] >= 0 && available[i] < bestCount)
         {
            bestCount = available[i];
            best = i;
         }

      // Place it
      if (bomber)
         bomberCubes.add(best + 1);
      else
         smallCubes.add(best + 1);
      if (available[best] <= 30)
         placed.get(best + 1).add(placed.get(best + 1).size() - available[best], beer);
      else
         placed.get(best + 1).add(beer);
      beer.setNew(true);

      return placed;
   }

   public static void main(String[] args) throws Exception
   {
      Resolver r = new Resolver();
      r.run();
      // r.organise();
      // r.organiseInPlace();
   }
}

class Beer implements Comparable<Beer>
{
   Long outDate;
   String name;
   boolean isNew;

   public Beer(String line) throws ParseException
   {
      this(line.split("~")[0], line.split("~")[1]);
   }

   public Beer(String name, String date) throws ParseException
   {
      this.name = name;
      outDate = Resolver.df.parse(date).getTime();
   }

   @Override
   public int compareTo(Beer o)
   {
      int comp = outDate.compareTo(o.outDate);
      if (comp == 0)
         return name.compareTo(o.name);
      else
         return comp;
   }

   public boolean equals(Beer o)
   {
      return o.name.equals(name) && o.outDate.equals(outDate);
   }

   public boolean isAvailable()
   {
      Calendar now = Calendar.getInstance();
      return outDate < now.getTimeInMillis();
   }

   public boolean isNew()
   {
      return isNew;
   }

   public void setNew(boolean isNew)
   {
      this.isNew = isNew;
   }

   @Override
   public String toString()
   {
      String add = "";
      if (isNew)
         add = " (N)";
      return name + "~" + Resolver.df.format(outDate) + add;
   }
}
