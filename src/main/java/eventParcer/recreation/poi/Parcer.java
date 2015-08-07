package eventParcer.recreation.poi;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by margarita on 07.08.15.
 */
public class Parcer {

    public static void main(String[] args) {
        String path = "/home/illya/Projects/EventParsing/src/recreation/poi/pages.csv";
        parce(path);
    }

    public static String parce(String pathToFile) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pathToFile));
            String line = br.readLine();

            while (line != null) {
                String category = line.split(",")[0];
                String url = line.split(",")[1];
                getHtmlPages(url, category);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
            }
        }
        return "";
    }

    public static String getHtmlPages(String urlStr, String category) throws IOException {
        boolean isSuccessful = true;
        int i = 0;
        while (isSuccessful) {
            if (i != 0) {
                try {
                    URL url = new URL(urlStr + "?page=" + i);
                    URLConnection yc = url.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            yc.getInputStream(), "UTF-8"));
                    String inputLine;
                    StringBuilder a = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        a.append(inputLine);
                        a.append("\r\n");
                    }
                    in.close();
                    getNodesIds(url, category);
                } catch (Exception e) {
                    isSuccessful = false;
                }
            }
            i++;
        }
        return "";
    }


    public static HashMap<Integer, String> getNodesIds(URL url, String category) {

        HashMap<Integer, String> ids = new HashMap<Integer, String>();
        Document doc =
                null;
        try {
            doc = Jsoup.connect(url.toString()).get();
            Elements nodes = doc.select(".node");
            for (Element node : nodes) {
                Elements links = node.select("a");
                Integer id = null;
                String format = "";
                String fileName = "";
                for (Element link : links) {
                    Pattern pattern = Pattern.compile("(/node/)(\\d+)");
                    Matcher m = pattern.matcher(link.attr("href").toString());
                    while (m.find()) {
                        String s = m.group(2);
                        id = Integer.parseInt(s);
                    }
                }
                Elements content = node.select(".content");

                //WTF?!
                Pattern pattern = Pattern.compile("((.*).(gpx))");
                Matcher m = pattern.matcher(content.toString());
                if (m.find()) {
                    fileName = m.group(1);
                    format="csv";
                }
                pattern = Pattern.compile("((.*).(gpx))");
                m = pattern.matcher(content.toString());
                if (m.find()) {
                    fileName = m.group(1);
                    format="gpx";
                }

                if (id != null) ids.put(id, fileName);
            }
            System.out.println(nodes.html());
        } catch (IOException e) {
            e.printStackTrace();
        }



        //parce and get node ids
        Iterator it = ids.entrySet().iterator();
        while (it.hasNext()) {
            String format="";
            Map.Entry pair = (Map.Entry) it.next();
            Pattern pattern = Pattern.compile("((.*).(gpx|csv))");
            Matcher m = pattern.matcher(pair.getValue().toString());
            while (m.find()) {
                format = m.group(3);
            }
            String downloadUrl = "www.poi-factory.com/poifile/download/" + format + "?node=" + pair.getKey();
            downloadFile(category, pair.getKey().toString(), downloadUrl, pair.getValue().toString());
        }
        return ids;
    }

    public static String downloadFile(String category, String nodeId, String urlStr, String filename) {
        String pathToFile = "recreation/poi/" + category + "/" + filename;
        try {
            URL website = new URL(urlStr);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(pathToFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathToFile;
    }

}
