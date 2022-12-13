import java.text.DecimalFormat;
import java.util.*;

public class Algorithms {

    private String query;
    private double answer;
    private int addAct = 0;
    private int multiAct = 0;
    private ArrayList<CptNode> evidence;
    private ArrayList<CptNode> hidden;
    private BayesianNetwork network;
    private ArrayList<CptNode> variables;
//    private ArrayList<Factor> factors;
//    private Hashtable factor;


    Algorithms(String query, BayesianNetwork network) {
        this.query = query;
        this.network = network;
        this.variables = new ArrayList<>();
        for (int i = 0; i < network.size(); i++) {
            CptNode curr = network.get(i);
            this.variables.add(curr);
        }
        this.hidden = new ArrayList<>();
        this.evidence = new ArrayList<>();
        this.answer = 0;
    }

    public double runAlgo(int algo){
        String q = this.query;
        double ans = 0;
        if (getProbFromCPT() > 0) {
            ans = getProbFromCPT();
            ans = formatAnswer(ans);
            return ans;
        }
        else {
            HashMap<String, String> evidenceVars = new HashMap<>();

            //Save evidence variables outcomes
            String numeratorStr = q.replace("|", ",");       //P(B=T,J=T,M=T)
            numeratorStr = numeratorStr.substring(2, numeratorStr.length()-1); //B=T,J=T,M=T
            String[] numerator = numeratorStr.split(",");                //[B=T,J=T,M=T]

            for (int i = 0; i<numerator.length; i++){
                String[] varName_outcome = numerator[i].split("=");       //e.g. [B,T]
                evidenceVars.put(varName_outcome[0], varName_outcome[1]);
            }

            //Hidden variables outcomes (permutations, save)
            int numOfPerm = 1;
            for (int i = 0; i< this.hidden.size(); i++ ){   //get number of permutations on the hidden variables
                numOfPerm *= this.hidden.get(i).getOutcomes().size();
            }




            if (algo == 1){
                ans = jointProb(q);
            }
            if (algo == 2){
                ans = eliminateBySize(q);
            }
//        else if (algo == 3){
//            ans = heuristicElimination(q);
//        }
        }
        return ans;
    }

    public double jointProb(String q){ //P(B=T|J=T,M=T)
        double ans=0;


        String newQ = "";

        if (q.length()>6){
            String numeratorString = q.replace("|", ",");
            String denominator = q.substring(6, q.length()-1);
            newQ = "P(";

            int numOfPerm = 1;
            for (int i = 0; i< this.hidden.size(); i++ ){   //get number of permutations on the hidden variables
                numOfPerm *= this.hidden.get(i).getOutcomes().size();
            }

            double multi = 1;
            double sum = 0;
            for (CptNode evidenceVar: this.evidence)
                newQ += evidenceVar.getName() + "=" ;

            for (int i = 0; i<numOfPerm; i++){
                multi *= calcProb(newQ);
                this.multiAct++;
            }
            sum += multi;
            this.addAct++;


            calcProb(q);
        }
        else {
            ans = getProbFromCPT();
            ans = formatAnswer(ans);
            this.answer = ans;
        }

        return ans;
    }

    public double eliminateBySize(String q){
        double ans = 0;

        return ans;
    }

    public double calcProb(String q){
        double ans =0;
        String numerator = q.replace("|", ",");
        String denominator = q.substring(6, q.length());
        String newQ = "P(";
//        if (){
//
//        }
//        if related -> evidence, else -> hidden

        ans = formatAnswer(ans);
        this.answer = ans;
        return ans;
    }

//    public void heuristicElimination(){
//
//    }

    public double formatAnswer(double ans) {
        double value = ans;
        value = Double.parseDouble(new DecimalFormat("#.#####").format(value));
        return value;
    }

    public double getAnswer(){
        return formatAnswer(this.answer);
    }

    public int getAddActions() {
        return this.addAct;
    }

    public int getMultiplyActions() {
        return this.multiAct;
    }

    public void addToEvidence(String query){
        for (int i = 0; i<variables.size(); i++){
            CptNode currVar = variables.get(i);
            String currVarName = currVar.getName();
            if (query.contains(currVarName) && !this.evidence.contains(currVarName)){
                this.evidence.add(currVar);
            }
        }
    }

    public void addToHidden(String query){
        for (int i = 0; i<this.variables.size(); i++){
            CptNode currVar = this.variables.get(i);
            String currVarName = currVar.getName();
            if ( !query.contains(currVarName) && !this.hidden.contains(currVarName)){
                    this.hidden.add(currVar);
            }
        }
    }

    public void removeUnnecessaryVars(){

        ArrayList <CptNode> leafNodes = new ArrayList<>();

        for (CptNode currVar : this.variables) {
            if (!currVar.hasChildren()) {
                leafNodes.add(currVar);
            }
        }

        for (int i = 0; i<leafNodes.size(); i++) {
            CptNode currVar = this.variables.get(i);
            if ( !(this.hidden.contains(currVar) && this.evidence.contains(currVar) ) ){
                leafNodes.remove(currVar);
            }
        }

        for (int i = 0; i<this.variables.size(); i++){

        }



    }

    public double getProbFromCPT() {
        double ans = 0;
        String queryVar = this.query.substring(2, 5);
        String queryVarName = this.query.substring(2, 3);
        String queryVarOutcome = this.query.substring(3, 4);
        int queryVarIndex = network.find(queryVarName);
        CptNode queryVarNode = network.get(queryVarIndex);

        if (this.query.length() > 5) { //e.g. P(A=F|E=T,B=T)=?
            String givenVars = this.query.substring(6, this.query.length() - 1);
            String[] givensArr = givenVars.split(",");
            ArrayList<String> givens = new ArrayList<>();
            ArrayList<String> outcomes = new ArrayList<>();

            String name = "";
            String outcome = "";

            for (int i = 0; i < givensArr.length; i++) {
                name = givensArr[i].substring(0, 1);
                givens.add(name);
                outcome = givensArr[i].substring(2, 3);
                outcomes.add(outcome);
            }

            if (queryVarNode.getParents().size() == givens.size()) { //then we might get the probability from the CPT
                boolean flag = true;
                for (int i = 0; i < givens.size(); i++) {
                    if (!queryVarNode.getParents().contains(givens.get(i))) {
                        flag = false;
                    }
                }

                if (flag) { // we can get the probability from the CPT
                    int index = 0;
                    int outcomeIndex = 0;

                    for (int o = 0; o < queryVarNode.getOutcomes().size(); o++) {
                        if (queryVarNode.getOutcomes().get(o).equals(queryVarOutcome)) {
                            outcomeIndex = o;
                        }
                    }
                    index += outcomeIndex;

                    int multiply = queryVarNode.getOutcomes().size();
                    for (int p = queryVarNode.getParentNodes().size() - 1; p >= 0; p--) {
                        CptNode parent = queryVarNode.getParentNodes().get(p);
                        for (int o = 0; o < parent.getOutcomes().size(); o++) {
                            if (parent.getOutcomes().get(o).equals(queryVarOutcome)) {
                                outcomeIndex = o;
                            }
                        }
                        index += outcomeIndex * multiply;
                        multiply *= parent.getOutcomes().size();
                    }

                    ans = Double.parseDouble(queryVarNode.getProbTable().get(index));
                    ans = formatAnswer(ans);
                    return ans;
                }

            }

        }
        else {  //e.g. P(B=T)=?. We can get that from the CPT
            for (int r = 0; r < queryVarNode.getOutcomes().size(); r++) {
                String outcome = queryVarNode.getOutcomes().get(r);
                if (queryVarOutcome.equals(outcome)) {
                    ans = Double.parseDouble(queryVarNode.getProbTable().get(r));
                    ans = formatAnswer(ans);
                    return ans;
                } else {
                    System.out.println("error");
                }
            }

        }

        return ans;
    }

    public String printHidden() {
        String print = "";
        for (int i = 0; i<this.hidden.size(); i++){
            print += this.hidden.get(i).getName() + ", ";
        }
        return print;
    }

    public String printEvidence() {
        String print = "";
        for (int i = 0; i<this.evidence.size(); i++){
            print += this.evidence.get(i).getName() + ", ";
        }
        return print;
    }
}

