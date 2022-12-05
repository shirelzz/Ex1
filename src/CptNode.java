import com.sun.beans.editors.ColorEditor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CptNode {

    private static String name;
    private static ArrayList<String> outcome;
    private static ArrayList<String> given;
    private static ArrayList<CptNode> parents;
    private static ArrayList<CptNode> children;
    private static ArrayList<String> probTable;


    /*
    Constructor
     */
    CptNode() throws IOException, SAXException {
        CptNode.name = name;
        CptNode.outcome = new ArrayList<String>(2) ;
        CptNode.given = new ArrayList<String>() ;
        CptNode.parents = new ArrayList<CptNode>();
        CptNode.children = new ArrayList<CptNode>();
        CptNode.probTable = new ArrayList<String>(1);

    }

    public void addName(String name){
        CptNode.name = name;
    }

    public void addOutcome (String s) {
        outcome.add(s);
    }

    public void addGiven (String s) {
        given.add(s);
    }

    public void addParent(Node cptParentNode) {
        CptNode.parents.add((CptNode) cptParentNode);
    }

    public void addChild (Node cptNodeChild) {
        CptNode.children.add((CptNode) cptNodeChild);
    }

    public void addProbTable (String s) {
        CptNode.probTable.add(s);
    }

    public String printVariableDetails(){
        return "Name: " + CptNode.name + "\n" +
                "Outcomes: " + CptNode.outcome + "\n";
    }

    public String printDefinitionDetails(){
        return "For: " + CptNode.name + "\n" +
                "Given: " + CptNode.given + "\n" +
                "Table: " + CptNode.probTable;
    }

    public String toString(){
        String parentsStr = "";
        String childrenStr = "";

        for (CptNode parent: parents){
            parentsStr += parent;
        }
        for (CptNode child: children){
            childrenStr += child;
        }

        return "Parents are: " + parentsStr +
                "Children are: " + childrenStr;
    }

}


