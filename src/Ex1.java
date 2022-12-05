//import javax.swing.text.Document;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Ex1 {
    public static void main(String[] args) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        BayesianNetwork network = new BayesianNetwork();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File("/Users/syrlzkryh/Documents/GitHub/Ex1/src/alarm_net.xml"));
            document.getDocumentElement().normalize();

            NodeList variableList = document.getElementsByTagName("VARIABLE");
            NodeList definitionList = document.getElementsByTagName("DEFINITION");
            String variableData = "";
            String definitionData = "";
            String varName = "";
            String defFor = "";
            String outcomes ="";
            String givens = "";
            String probs = "";


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
                            variableData += outcomeElement.getTextContent() + " ";
                            varNode.addOutcome(outcomeElement.getTextContent());
                        }
                    }
                }
                System.out.println(varNode.printVariableDetails());
                network.add(varNode);
            }

//            System.out.println(network.toString());
//            System.out.println(variableData);
//            String varSubstrings[] = variableData.split(" ");
//            String varSubstring;
//            for (int i = 0; i<varSubstrings.length; i++){
//
//            }



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
                                probs += givenElement.getTextContent() + " ";
                                defNode.addProbTable(givenElement.getTextContent());
                            }
                            else {
                                givens += givenElement.getTextContent() + " " ;
                                defNode.addGiven(givenElement.getTextContent());
                            }
                        }
                    }
                    System.out.println(defNode.printDefinitionDetails());
                    network.add(defNode);
                }
            }
//            System.out.println(probs);
//            System.out.println(givens);
//            System.out.println(definitionData);
//            String defSubstrings[] = definitionData.split(" ");
//            System.out.println(Arrays.toString(defSubstrings));
//            String defSubstring = defSubstrings[0];
//            System.out.println(defSubstring);


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

    }





}
