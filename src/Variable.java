import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Variable {

    private String name;
    private ArrayList<String> outcome;
    private ArrayList<String> parents;
    private ArrayList<Variable> ancestors;
    private ArrayList<Variable> parentNodes;
    private ArrayList<String> children;
    private ArrayList<String> probTable;
    private HashMap<String, Double> CPT;

    /*
    Constructor
     */
    Variable() throws IOException, SAXException {
        this.outcome = new ArrayList<>(2) ;
        this.parents = new ArrayList<>();
        this.parentNodes = new ArrayList<>();
        this.probTable = new ArrayList<>(1);
        this.children = new ArrayList<>();
        this.CPT = new HashMap<>();
        this.ancestors = new ArrayList<>();
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

    public void addParentNode(Variable parent) { parentNodes.add(parent);}

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

    public void addAncestors(ArrayList<Variable> variables){

        for (int i = 0; i<variables.size(); i++) {
            Variable var = variables.get(i);
            ArrayList<Variable> varParents = var.getParentNodes();
            if (varParents.size() > 0) {
                for (int j = 0; j < varParents.size(); j++) { //add parents (copy)
                    if (!var.getAncestors().contains(varParents.get(j))) {
                        var.addAncestor(varParents.get(j));
                    }
                }
                for (int j = 0; j < varParents.size(); j++) { //add each parent's parents
                    Variable gParent = varParents.get(j);
                    if (gParent.hasParents()) {
                        ArrayList<Variable> varGParents = gParent.getParentNodes();
                        for (int k = 0; k < varGParents.size(); k++) {
                            if (!var.getAncestors().contains(varGParents.get(k))) {
                                var.addAncestor(varGParents.get(k));
                            }
                        }

                    }
                }
            }
        }
    }

    public void addAncestor(Variable ancestor) { ancestors.add(ancestor);}

    public ArrayList<Variable> getAncestors(){
        return this.ancestors;
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
                "Ancestors: " + this.ancestors + "\n" +
                "Children: " + this.children + "\n" +
                "CPT: " + this.CPT + "\n" +
                "Table: " + this.probTable + "\n" ;
    }

}


