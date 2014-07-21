package com.brotherlogic.beercellarresolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Resolver
{
   private final String baseline = "/Users/simon/Dropbox/beer/";

   Map<String, Integer> drunk;
   List<Beer> bombers;
   List<Beer> smalls;
   String lastDate = "";

   public void run()
   {
      try
      {
         load();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private void load() throws IOException
   {
      drunk = loadDone("todrink.list");
      bombers = loadToGo("bomber.list");
      smalls = loadToGo("smaller.list");

      Collections.sort(bombers);
      Collections.sort(smalls);

      System.out.println(lastDate);
   }

   private Map<String, Integer> loadDone(String name) throws IOException
   {
      Map<String, Integer> counts = new TreeMap<String, Integer>();

      File f = new File(baseline + name);
      BufferedReader reader = new BufferedReader(new FileReader(f));
      int lcount = 0;
      for (String line = reader.readLine(); line != null; line = reader.readLine())
         if (lcount++ % 8 == 0)
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

   private List<Beer> loadToGo(String name) throws IOException
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

   public static void main(String[] args)
   {
      Resolver r = new Resolver();
      r.run();
   }
}

class Beer implements Comparable<Beer>
{
   Long outDate;
   String name;
   DateFormat df = new SimpleDateFormat("dd/MM/yy");

   public Beer(String name, String date) throws ParseException
   {
      this.name = name;
      outDate = df.parse(date).getTime();
   }

   public int compareTo(Beer o)
   {
      return outDate.compareTo(o.outDate);
   }

   @Override
   public String toString()
   {
      return name;
   }
}
