import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.FileReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XmlReader {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    BayesianNetwork network = new BayesianNetwork();


    public XmlReader(String XmlFilePath){
        File xml_file = new File(XmlFilePath);
//        this.network = parse(XmlFile);
    }

    {
        try {
            builder = factory.newDocumentBuilder();
            File XmlFile = new File("/Users/syrlzkryh/Documents/GitHub/Ex1/src/alarm_net.xml");
            Document document = builder.parse(XmlFile);
            document.getDocumentElement().normalize();

            NodeList variableList = document.getElementsByTagName("VARIABLE");
            NodeList definitionList = document.getElementsByTagName("DEFINITION");
            String variableData = "";
            String definitionData = "";
            String varName = "";
            String defFor = "";

            for (int i = 0; i<variableList.getLength(); i++){
                Node variable = variableList.item(i);
                CptNode varNode = new CptNode();

                if (variable.getNodeType() == Node.ELEMENT_NODE){
                    Element variableElement = (Element) variable;
                    varName = variableElement.getElementsByTagName("NAME").item(0).getTextContent();
                    varNode.addName(varName);
                }

                NodeList outcomeList = variable.getChildNodes();
                for (int j=0; j<outcomeList.getLength() ;j++){
                    Node outcome = outcomeList.item(j);
                    if (outcome.getNodeType() == Node.ELEMENT_NODE){
                        Element outcomeElement = (Element) outcome;
                        if (outcomeElement.getTextContent() != varName ){
                            varNode.addOutcome(outcomeElement.getTextContent());
                        }
                    }
                }
                network.add(varNode);
            }

            for (int i = 0; i<definitionList.getLength(); i++){
                Node definition = definitionList.item(i);
                CptNode defNode = new CptNode();


                if (definition.getNodeType() == Node.ELEMENT_NODE){
                    Element definitionElement = (Element) definition;
                    defFor = definitionElement.getElementsByTagName("FOR").item(0).getTextContent();
                    defNode.addName(defFor);
                }

                NodeList givenList = definition.getChildNodes();
                for (int j=0; j<givenList.getLength() ;j++){
                    Node given = givenList.item(j);
                    if (given.getNodeType() == Node.ELEMENT_NODE){
                        Element givenElement = (Element) given;
                        if (givenElement.getTextContent() != defFor){
                            Boolean flag = Character.isDigit(givenElement.getTextContent().charAt(0));
                            if (flag){
                                defNode.addProbTable(givenElement.getTextContent());
                            }
                            else {
                                defNode.addGiven(givenElement.getTextContent());
                            }
                        }
                    }
                    network.add(defNode);
                }
            }





        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }


}
