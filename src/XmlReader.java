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
import java.util.ArrayList;

public class XmlReader {

    private BayesianNetwork network;

    public XmlReader(){
        this.network = new BayesianNetwork();
    }

    public BayesianNetwork buildNet(String path) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        {
            try {
                builder = factory.newDocumentBuilder();
                File xmlFile = new File(path);
                Document document = builder.parse(xmlFile);
                document.getDocumentElement().normalize();

                NodeList variableList = document.getElementsByTagName("VARIABLE");
                NodeList definitionList = document.getElementsByTagName("DEFINITION");
                String varName = "";
                String defName = "";

                for (int i = 0; i < variableList.getLength(); i++) {
                    Node variable = variableList.item(i);
                    CptNode varNode = new CptNode();

                    if (variable.getNodeType() == Node.ELEMENT_NODE) {
                        Element variableElement = (Element) variable;
                        varName = variableElement.getElementsByTagName("NAME").item(0).getTextContent();
                        varNode.setName(varName);
                    }

                    NodeList outcomeList = variable.getChildNodes();
                    for (int j = 0; j < outcomeList.getLength(); j++) {
                        Node outcome = outcomeList.item(j);
                        if (outcome.getNodeType() == Node.ELEMENT_NODE) {
                            Element outcomeElement = (Element) outcome;
                            if (outcomeElement.getTextContent() != varName) {
                                varNode.addOutcome(outcomeElement.getTextContent());
                            }
                        }
                    }
                    network.add(varNode);
                }

                for (int i = 0; i < definitionList.getLength(); i++) {
                    Node definition = definitionList.item(i);

                    if (definition.getNodeType() == Node.ELEMENT_NODE) {
                        Element definitionElement = (Element) definition;
                        defName = definitionElement.getElementsByTagName("FOR").item(0).getTextContent();
                    }

                    for (int k = 0; k < network.size(); k++) {
                        CptNode currNode = network.get(k);

                        NodeList givenList = definition.getChildNodes();
                        for (int j = 0; j < givenList.getLength(); j++) {
                            Node given = givenList.item(j);
                            if (given.getNodeType() == Node.ELEMENT_NODE) {
                                Element givenElement = (Element) given;

                                if (currNode.getName().equals(defName)) {
                                    if (givenElement.getTextContent() != defName) {
                                        Boolean flag = Character.isDigit(givenElement.getTextContent().charAt(0));
                                        if (flag) {
                                            currNode.addProbTable(givenElement.getTextContent());
                                        } else {
                                            currNode.addParent(givenElement.getTextContent());
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

                for (int i = 0; i<network.size(); i++){
                    CptNode curr = network.get(i);
                    if (curr.hasParents()){
                        for (int j =0; j<curr.getParents().size(); j++){
                            CptNode parent;
                            String parentName = curr.getParents().get(j);
                            String childName = curr.getName();
                            int index = network.find(parentName);
                            network.get(index).addChild(childName);
                        }
                    }
                }

                //add parents nodes
                for (int i = 0; i<network.size(); i++) {
                    CptNode currChild = network.get(i);
                    String currChildName = currChild.getName();
                    ArrayList parentsNames = currChild.getParents();

                    if (parentsNames.size()>0){
                        for (int k=0; k<network.size(); k++){
                            CptNode currParentNode = network.get(k);
                            if (currParentNode.getChildren().contains(currChildName)) {
                                currChild.addParentNode(currParentNode);
                            }
                        }
                    }

                }

                //add to cpt
//                for (int i = 0; i<network.size(); i++) {
//                    CptNode curr = network.get(i);
//                    ArrayList<CptNode> currParentsNodes = curr.getParentNodes();
//                    ArrayList<String> currParentsNames = curr.getParents();
//                    ArrayList<String> currOutcomes = curr.getOutcomes();
//                    ArrayList<String> currTable = curr.getProbTable();
//
//                    if (currParentsNodes.size()>0){
//                        for (int j = 0; j<currOutcomes.size(); j++){
//                            int numOfOutcomes = currOutcomes.size();
//                            int index = 0;
//                            while (index<numOfOutcomes){
//                                index++;
//                            }
//
//                                String s = "P(" + curr.getName() + "=" + currOutcomes.get(j) + " | ";
//                            for (int k = currParentsNodes.size()-1 ; k>=0; k--){
//                                s += currParentsNames.get(k) + "=" + currParentsNodes.get(k)  + "="
//                            }
//
//                            int tableIndex =
//
//                            curr.addToCPT();
//                        }
//                    }
//                    else {
//                        for (String currOutcome : currOutcomes) {
//                            for (String currProb : currTable) {
//                                String s = "P(" + curr.getName() + "=" + currOutcome;
//                                double prob = Double.parseDouble(currProb);
//                                curr.addToCPT(s, prob);
//                            }
//                        }
//                    }
//                }



                } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
        return network;
    }
}
