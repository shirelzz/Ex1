import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CptNode {

    private String name;
    private ArrayList<String> outcome;
    private ArrayList<String> parents;
    private ArrayList<CptNode> parentNodes;
    private ArrayList<String> children;
    private ArrayList<String> probTable;
    private HashMap<String, Double> CPT;

    /*
    Constructor
     */
    CptNode() throws IOException, SAXException {
        this.outcome = new ArrayList<>(2) ;
        this.parents = new ArrayList<>();
        this.parentNodes = new ArrayList<>();
        this.probTable = new ArrayList<>(1);
        this.children = new ArrayList<>();
        this.CPT = new HashMap<>();
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

    public void addParentNode(CptNode parent) { parentNodes.add(parent);}

    public ArrayList<CptNode> getParentNodes(){
        return this.parentNodes;
    }

    public boolean hasParents(){
        return this.getParents().size() > 0;
    }

    public void addChild (String s) {
        this.children.add(s);
    }

    public ArrayList<String> getChildren(){
        return this.children;
    }

    public boolean hasChildren(){
        return this.getChildren().size() > 0;
    }

    public void addProbTable (String s) {
        probTable.add(s);
    }

    public ArrayList<String> getProbTable(){
        return this.probTable;
    }

    public void addToCPT(String s, double d){

        this.CPT.put(s,d);
    }

    public HashMap<String, Double> getCPT(){
        return this.CPT;
    }

    public String printVariableDetails(){
        return "Name: " + this.name + "\n" +
                "Outcomes: " + this.outcome + "\n" +
                "Parents: " + this.parents + "\n" +
                "Children: " + this.children + "\n" +
                "CPT: " + this.CPT + "\n" +
                "Table: " + this.probTable + "\n" ;
    }

}


