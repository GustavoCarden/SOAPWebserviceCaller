/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package soapwebservicecaller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author Gustavo Cardenas Alba
 */
public class SOAPCaller {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String wsdlUrl = "";
        String SOAPAction = "";
        String Operation = "";

        String xmlTemplate = "";
        String[] paramNames = new String[]{};
        String[] paramValues = new String[]{};

        Document doc = getDocFromWSCall(wsdlUrl, xmlTemplate, paramNames, paramValues, SOAPAction, Operation, null);
    }

    public static Document getDocFromWSCall(String wsdlUrl, String xmlTemplate, String paramsNames[], String paramsValues[], String SOAPAction, String Operation, HeaderWSCall... headers) throws Exception {
        //Code to make a webservice HTTP request
        String responseString = "";
        String outputString = "";

        InputStreamReader isr = null;
        BufferedReader in = null;
        ByteArrayOutputStream bout = null;
        OutputStream out = null;
        Document document = null;
        try {
            //Escribimos el template en bytes para posteriormente escribirlos en la peticion.
            bout = new ByteArrayOutputStream();
            String xmlInput = xmlInputContentBuilder(xmlTemplate, paramsNames, paramsValues);
            byte[] buffer = new byte[xmlInput.length()];
            buffer = xmlInput.getBytes();
            bout.write(buffer);
            byte[] b = bout.toByteArray();

            //Preparamos la conexion al servicio.
            URL url = new URL(wsdlUrl);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) connection;
            httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
            httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            httpConn.setRequestProperty("SOAPAction", SOAPAction);
            httpConn.setRequestProperty("Operation", Operation);
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);

            if (headers != null) {
                for (HeaderWSCall hwc : headers) {
                    httpConn.setRequestProperty(hwc.getName(), hwc.getValue());
                }
            }
            
            //Escribimos el contenido de la peticion en el objeto HttpURLConnection
            out = httpConn.getOutputStream();
            out.write(b);

            //Ejecutamos la llamada al servicio.
            isr = new InputStreamReader(httpConn.getInputStream());
            
            //Si el servicio nos responde guardamos la respuesta del servicio.
            if (isr != null) {
                in = new BufferedReader(isr);
                //Write the SOAP message response to a String.
                while ((responseString = in.readLine()) != null) {
                    outputString = outputString + responseString;
                }
            }

            //Hacemos una conversion de la respuesta del servicio en String a un objeto org.w3c.dom.Document para su procesamiento.
            document = parseXmlFile(outputString);
        } catch (Exception e) {
            Logger.getLogger("ERROR").log(Level.SEVERE, e.getMessage());
        } finally {
            if (isr != null) {
                isr.close();
            }
            if (in != null) {
                in.close();
            }
            if (bout != null) {
                bout.flush();
                bout.close();
            }
            if (out != null) {
                out.flush();
                out.close();
            }
        }
        return document;
    }

    private static Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (org.xml.sax.SAXException ex) {
            Logger.getLogger(SOAPCaller.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static String xmlInputContentBuilder(String xmlTemplate, String paramNames[], String paramValues[]) {
        if (xmlTemplate != null) {
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    xmlTemplate = xmlTemplate.replace(paramNames[i], paramValues[i] == null ? "" : paramValues[i]);
                }
            }
        }
        return xmlTemplate;
    }

}
