import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Variable {

    private String name;
    private ArrayList<String> outcome;
    private ArrayList<String> parents;
    private ArrayList<Variable> parentNodes;
    private ArrayList<String> children;
    private ArrayList<String> probTable;
    private int counter;

    /**
    Constructor
     */
    Variable() throws IOException, SAXException {
        this.outcome = new ArrayList<>(2) ;
        this.parents = new ArrayList<>();
        this.parentNodes = new ArrayList<>();
        this.probTable = new ArrayList<>(1);
        this.children = new ArrayList<>();
    }

    public void setCounter(int c){
        this.counter = c;
    }

    public int getCounter() {
        return counter;
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

    /**
     * @return - parents names list
     */
    public ArrayList<String> getParents(){
        return this.parents;
    }

    public void addParentNode(Variable parent) {
        if (!parent.getName().equals(this.name)) {
            parentNodes.add(parent);
        }
    }

    /**
     * @return - parents nodes list
     */
    public ArrayList<Variable> getParentNodes(){
        return this.parentNodes;
    }

    public Variable findParent(String name){
        int index = 0;
        for (int i = 0; i<this.parentNodes.size(); i++){
            Variable p = this.parentNodes.get(i);
            if (p.getName().equals(name)){
                index = i;
                break;
            }
        }
        return this.parentNodes.get(index);
    }

    /**
     * @return true if this variable has parents, else returns false
     */
    public boolean hasParents(){
        return this.getParents().size() > 0;
    }

    public void addChild (String s) {
        this.children.add(s);
    }

    public ArrayList<String> getChildren(){
        return this.children;
    }

    /**
     * @return true if this variable has children, else returns false
     */
    public boolean hasChildren(){
        return this.getChildren().size() > 0;
    }

    /**
     * add the table content as it shows in the xml file
     * @param s the table content as it shows in the xml file
     */
    public void addProbTable (String s) {
        probTable.add(s);
    }

    public ArrayList<String> getProbTable(){
        return this.probTable;
    }

    public String printVariableDetails(){
        return "Name: " + this.name + "\n" +
                "Outcomes: " + this.outcome + "\n" +
                "Parents: " + this.parents + "\n" +
                "Children: " + this.children + "\n" +
                "Table: " + this.probTable + "\n" ;
    }

}


