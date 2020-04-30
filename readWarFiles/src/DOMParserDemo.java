import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;

public class DOMParserDemo {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        //Get Document Builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        WebXmlParser webXmlParser
        DocumentBuilder builder = factory.newDocumentBuilder();

        //Build Document
        Document document = builder.parse(new File("web.xml"));

        //Normalize the XML Structure; It's just too important !!
        document.getDocumentElement().normalize();

        //Here comes the root node
        Element root = document.getDocumentElement();
        System.out.println(root.getNodeName());

        //Get all servlets
        NodeList nList = document.getElementsByTagName("servlet");
        System.out.println("============================");

        for (int temp = 0; temp < nList.getLength(); temp++)
        {
            Node node = nList.item(temp);
            System.out.println("");    //Just a separator
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                //Print servlet details
                Element eElement = (Element) node;
                System.out.println("servlet id : "    + eElement.getAttribute("id"));
                System.out.println("servlet-name : "  + eElement.getElementsByTagName("servlet-name").item(0).getTextContent());
                System.out.println("servlet-class : "   + eElement.getElementsByTagName("servlet-class").item(0).getTextContent());
                System.out.println("init-param : "    + eElement.getElementsByTagName("init-param").item(0).getTextContent());
            }
        }
    }
}
