import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

public class CptNode {

    private String name;
    private ArrayList<String> outcome;
    private ArrayList<String> parents;
//    private ArrayList<CptNode> children;
    private  ArrayList<String> probTable;

    /*
    Constructor
     */
    CptNode() throws IOException, SAXException {
        this.outcome = new ArrayList<String>(2) ;
        this.parents = new ArrayList<String>() ;
        this.probTable = new ArrayList<String>(1);

    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void addOutcome (String s) {
        outcome.add(s);
    }

    public ArrayList<String> getOutcomes(){
        return this.outcome;
    }

    public void addParent(String s) { parents.add(s);}

    public ArrayList<String> getParents(){
        return this.parents;
    }

//    public void addChild (Node cptNodeChild) {
//        CptNode.children.add((CptNode) cptNodeChild);
//    }

    public void addProbTable (String s) {
        probTable.add(s);
    }

    public ArrayList<String> getProbTable(){
        return probTable;
    }

    public String printVariableDetails(){
        return "Name: " + this.name + "\n" +
                "Outcomes: " + this.outcome + "\n" +
                "Parents: " + this.parents + "\n" +
                "Table: " + this.probTable + "\n" ;
    }

}


