package com.brotherlogic.beercellarresolver;

import java.io.BufferedReader;
import java.io.File;
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

class Beer implements Comparable<Beer> {
    Long outDate;
    String name;

    public Beer(String name, String date) throws ParseException {
        this.name = name;
        outDate = Resolver.df.parse(date).getTime();
    }

    @Override
    public int compareTo(Beer o) {
        return outDate.compareTo(o.outDate);
    }

    public boolean isAvailable() {
        Calendar now = Calendar.getInstance();
        return outDate < now.getTimeInMillis();
    }

    @Override
    public String toString() {
        return name;
    }
}

public class Resolver {
    public static void main(String[] args) throws Exception {
        Resolver r = new Resolver();
        r.run();
    }

    public static DateFormat df = new SimpleDateFormat("dd/MM/yy");
    private final String baseline = "/Users/simon/Dropbox/beer/";
    Map<String, Integer> drunk;
    List<Beer> bombers;
    List<Beer> smalls;

    String lastDate = "";

    private void load() throws IOException {
        drunk = loadDone("todrink.list");
        bombers = loadToGo("bomber.list");
        smalls = loadToGo("smaller.list");

        Collections.sort(bombers);
        Collections.sort(smalls);
    }

    private Map<String, Integer> loadDone(String name) throws IOException {
        Map<String, Integer> counts = new TreeMap<String, Integer>();

        File f = new File(baseline + name);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        int lcount = 0;
        for (String line = reader.readLine(); line != null; line = reader
                .readLine())
            if (lcount++ % 8 == 0)
                lastDate = line.trim();
            else {
                String beerDrunk = line.trim();
                if (counts.containsKey(beerDrunk))
                    counts.put(beerDrunk, counts.get(beerDrunk) + 1);
                else
                    counts.put(beerDrunk, 1);
            }
        reader.close();

        return counts;
    }

    private List<Beer> loadToGo(String name) throws IOException {
        List<Beer> beers = new LinkedList<Beer>();
        File f = new File(baseline + name);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            String[] elems = line.trim().split("~");
            try {
                Beer b = new Beer(elems[0], elems[1]);
                if (b.isAvailable())
                    beers.add(b);
            } catch (ParseException e) {
                System.out.println("Cannot parse: " + elems[1]);
            }
        }
        reader.close();

        return beers;
    }

    public void run() throws ParseException {
        try {
            load();

            // Figure out how many times we need to pick - one Sunday ahead
            Calendar nextSunday = Calendar.getInstance();
            for (int i = 0; i < 1; i++) {
                while (nextSunday.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
                    nextSunday.add(Calendar.DAY_OF_WEEK, 1);
                nextSunday.add(Calendar.DAY_OF_WEEK, 1);
            }

            Calendar lastDone = Calendar.getInstance();
            lastDone.setTime(df.parse(lastDate));

            if (lastDone.before(nextSunday)) {
                PrintWriter pw = new PrintWriter(baseline + "todrink.list");
                pw.println(df.format(nextSunday.getTime()));
                for (int i = 0; i < Math.min(2, bombers.size()); i++)
                    pw.print(bombers.get(i).name);
                for (int i = 0; i < Math.min(4, smalls.size()); i++)
                    pw.println(smalls.get(i));
                pw.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
