package com.redisapp.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HttpURLConnectionExample {

    private static final Logger log = Logger.getLogger(HttpURLConnectionExample.class.getName());

    private static final String USER_AGENT = "Mozilla/5.0";

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        byte[] compressed = sendGet("http://www.google.com/search?q=renubatheja");
    }


    /**
     * @param url
     * @return
     * @throws Exception
     * HTTP GET request to Google Search engine to get the query results.
     */
    public static byte[] sendGet(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        //log.info("\nSending 'GET' request to URL : " + url);
        //log.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        byte[] compressed = compress(response.toString());
        //log.info("Compressed Response : " + compressed);
        //log.info("Compressed Response String : " + compressed.toString());
        /*String compressString = compressed.toString();
        String decompressed = decompress(compressed);
		//log.info("DeCompressed Response : " + decompressed);*/
        return compressed;
    }


    /**
     * @param str
     * @return
     * @throws Exception
     * Get the data is Gzipped format
     */
    public static byte[] compress(String str) throws Exception {
        //log.info("Original String length : " + str.length());
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        String outStr = obj.toString("UTF-8");
        //log.info("Compressed String length : " + outStr.length());
        return obj.toByteArray();
    }
    

    /**
     * @param str
     * @return
     * @throws Exception
     * Unzip the GZipped data
     */
    public static String decompress(byte[] str) throws Exception {
        //log.info("Compressed String length : " + str.length);
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        String outStr = "";
        String line;
        while ((line = bf.readLine()) != null) {
            outStr += line;
        }
        //log.info("Decompressed String length : " + outStr.length());
        return outStr;
    }
}