/**
 * Sample program that reads tags in the background and prints the
 * tags found.
 */

// Import the API
package samples;
import com.thingmagic.*;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import triangulation.Triangulate;

public class readasync
{
    static SerialPrinter serialPrinter;
  static StringPrinter stringPrinter;
  static TransportListener currentListener;
  
  /******Allee Code*****/
  static File tagFile;
  static File medFile;
  /*********************/
  
  static void usage()
  {
    System.out.printf("Usage: Please provide valid arguments, such as:\n"
                + "  (URI: 'tmr:///COM1 --ant 1,2' or 'tmr://astra-2100d3/ --ant 1,2' "
                + "or 'tmr:///dev/ttyS0 --ant 1,2')\n\n");
    System.exit(1);
  }

   public static void setTrace(Reader r, String args[])
  {
    if (args[0].toLowerCase().equals("on"))
    {
        r.addTransportListener(Reader.simpleTransportListener);
        currentListener = Reader.simpleTransportListener;
    }
    else if (currentListener != null)
    {
        r.removeTransportListener(Reader.simpleTransportListener);
    }
  }

   static class SerialPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      System.out.print(tx ? "Sending: " : "Received:");
      for (int i = 0; i < data.length; i++)
      {
        if (i > 0 && (i & 15) == 0)
          System.out.printf("\n         ");
        System.out.printf(" %02x", data[i]);
      }
      System.out.printf("\n");
    }
  }

  static class StringPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      System.out.println((tx ? "Sending:\n" : "Receiving:\n") +
                         new String(data));
    }
  }
  public static void main(String argv[])
  {
    // Program setup
    Reader r = null;
    int nextarg = 0;
    boolean trace = false;
    int[] antennaList = null;
    
    if (argv.length < 1)
      usage();

    if (argv[nextarg].equals("-v"))
    {
      trace = true;
      nextarg++;
    }

    // Create Reader object, connecting to physical device
    try
    {
        String readerURI = argv[nextarg];
        nextarg++;

        for (; nextarg < argv.length; nextarg++)
        {
            String arg = argv[nextarg];
            if (arg.equalsIgnoreCase("--ant"))
            {
                if (antennaList != null)
                {
                    System.out.println("Duplicate argument: --ant specified more than once");
                    usage();
                }
                antennaList = parseAntennaList(argv, nextarg);
                nextarg++;
            }
            else
            {
                System.out.println("Argument " + argv[nextarg] + " is not recognised");
                usage();
            }
        }

        r = Reader.create(readerURI);
        if (trace)
        {
          setTrace(r, new String[] {"on"});
        }
        r.connect();
        if (Reader.Region.UNSPEC == (Reader.Region)r.paramGet("/reader/region/id"))
        {
            Reader.Region[] supportedRegions = (Reader.Region[])r.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
            if (supportedRegions.length < 1)
            {
                 throw new Exception("Reader doesn't support any regions");
            }
            else
            {
                 r.paramSet("/reader/region/id", supportedRegions[0]);
            }
        }
        
        String model = r.paramGet("/reader/version/model").toString();
        if ((model.equalsIgnoreCase("Sargas") || model.equalsIgnoreCase("M6e Micro") || model.equalsIgnoreCase("M6e Nano")) && antennaList == null)
        {
            System.out.println("Module doesn't has antenna detection support, please provide antenna list");
            r.destroy();
            usage();
        }

        SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000);
        r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);
        ReadExceptionListener exceptionListener = new TagReadExceptionReceiver();
        r.addReadExceptionListener(exceptionListener);
        // Create and add tag listener
        // Tag listener outputs all necessary info to a file
        tagFile = new File("tags.txt");
        medFile = new File("RssVals.txt");
        
        ReadListener rl = new IndividualListener();
        r.addReadListener(rl);
        Map<String, Double> rf = new HashMap<String, Double>();
        Map<String, Double> angle = new HashMap<String, Double>();
        int c = 0;
        int numStops = 12;
        while (c < numStops) {
            System.out.println("Start");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            char l = br.readLine().charAt(0);
            if(l == 'q') break;       
            // search for tags in the background
            BufferedWriter bw = new BufferedWriter(new FileWriter(medFile));
            bw.write("");
            bw.flush();
            //System.out.println("Here");
            r.startReading();
            System.out.println("Counter: " + c++);
            Thread.sleep(2000);
            // Reads all the tag data from file
            Map<String, Double> tagVals = readTagVals();
            System.out.println(tagVals.toString());
            for(String tag : tagVals.keySet()) {
                if(!rf.containsKey(tag)) {
                    rf.put(tag, Double.NEGATIVE_INFINITY);
                }
                if(rf.get(tag) < tagVals.get(tag)) {
                    rf.put(tag, tagVals.get(tag));
                    angle.put(tag, (c-1)*(360.0/numStops));
                }
            }
            //System.out.println("Do other work here");
            //Thread.sleep(500);
            r.stopReading();
            System.out.println("Finish");
        }
        System.out.println(angle.toString());
        System.out.println("Rotation Process Complete!");
        r.removeReadListener(rl);
        r.removeReadExceptionListener(exceptionListener);
        // Shut down reader
        r.destroy();
        
        if(angle.size() < 3) {
          System.err.println("Did not detect 3 tags!");
        }
        HashMap<String, Point2D> point = parseTagPoints();
        System.out.println(point.toString());
        Point2D location = findLocation(point, angle);
        System.out.println("Location: " + location.toString());
        
        
    } 
    catch (ReaderException re)
    {
      System.out.println("ReaderException: " + re.getMessage());
    }
    catch (Exception re)
    {
        System.out.println("Exception: " + re.getMessage());
    }
  }

  static class PrintListener implements ReadListener
  {
    public void tagRead(Reader r, TagReadData tr)
    {
      System.out.println("Background read: " + tr.toString());
    }

  }
  //****************************ALLEE CODE****************//
  static class IndividualListener implements ReadListener
  {
      public void tagRead(Reader r, TagReadData tr) 
      {
          //System.out.println(tr.toString());
          try
          {
              BufferedWriter bw = new BufferedWriter(new FileWriter(medFile,true));
              bw.write(tr.epcString() + "\t" + tr.getRssi() + "\n");
              bw.flush();
          }
          catch (IOException e) {
              e.printStackTrace();
          }
      }
  }
  
  private static Map<String, Double> readTagVals() 
  {
      Map<String, Double> tagVals = new HashMap<String, Double>();
      Map<String, Double> tagRss = new HashMap<String, Double>();
      Map<String, Double> tagCount = new HashMap<String, Double>();
      try {
          BufferedReader br = new BufferedReader(new FileReader(medFile));
          String line = "";
          while((line=br.readLine())!=null) {
              StringTokenizer s = new StringTokenizer(line);
              String epc = s.nextToken();
              double rss = Double.parseDouble(s.nextToken());
              if(tagRss.containsKey(epc)) {
                  tagRss.put(epc, tagRss.get(epc) + rss);
                  tagCount.put(epc, tagCount.get(epc) + 1);
                  tagVals.put(epc, tagRss.get(epc)/tagCount.get(epc));
              }
              else {
                  tagRss.put(epc, rss);
                  tagCount.put(epc, 1.0);
              }
          }
      }
      catch (IOException e) {
          e.printStackTrace();
      }
      return tagVals;
  }
  
  // create map with tags here
  public static HashMap<String, Point2D> parseTagPoints() throws Exception {
      System.out.println("Parsing Points!");
      HashMap<String,Point2D> location = new HashMap<String, Point2D>();
      BufferedReader br = new BufferedReader(new FileReader(tagFile));
      String l = br.readLine();
      while((l=br.readLine())!=null) {
          StringTokenizer s = new StringTokenizer(l);
          location.put(s.nextToken(), new Point2D.Double(Double.parseDouble(s.nextToken()), Double.parseDouble(s.nextToken())));
      }
      return location;
  }
  
  public static Point2D findLocation(Map<String, Point2D> point, Map<String,Double> angle) {
      System.out.println("Finding Location!");
      Triangulate solve = new Triangulate();
      Point2D[] points = new Point2D[3];
      double[] angles = new double[3];
      int c = 0;
      for(String tag : point.keySet()) {
          if(c > 2) break;
          points[c] = point.get(tag);
          angles[c++] = angle.get(tag);
      }
      solve.setAngles(angles[0], angles[1], angles[2]);
      solve.setPoints(points[0], points[1], points[2]);
      return solve.findLocation();
  }
  //*****************************************************//
  
  static class TagReadExceptionReceiver implements ReadExceptionListener
  {
        String strDateFormat = "M/d/yyyy h:m:s a";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        public void tagReadException(com.thingmagic.Reader r, ReaderException re)
        {
            String format = sdf.format(Calendar.getInstance().getTime());
            System.out.println("Reader Exception: " + re.getMessage() + " Occured on :" + format);
            if(re.getMessage().equals("Connection Lost"))
            {
                System.exit(1);
            }
        }
    }
  
  static  int[] parseAntennaList(String[] args,int argPosition)
    {
        int[] antennaList = null;
        try
        {
            String argument = args[argPosition + 1];
            String[] antennas = argument.split(",");
            int i = 0;
            antennaList = new int[antennas.length];
            for (String ant : antennas)
            {
                antennaList[i] = Integer.parseInt(ant);
                i++;
            }
        }
        catch (IndexOutOfBoundsException ex)
        {
            System.out.println("Missing argument after " + args[argPosition]);
            usage();
        }
        catch (Exception ex)
        {
            System.out.println("Invalid argument at position " + (argPosition + 1) + ". " + ex.getMessage());
            usage();
        }
        return antennaList;
    }
  
}
