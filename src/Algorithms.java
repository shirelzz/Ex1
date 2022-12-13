import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

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
    }

    public double runAlgo(int algo){
        double ans = 0;
        if (algo == 1){
            ans = jointProb(this.query);
        }
        if (algo == 2){
            ans = eliminateBySize(this.query);
        }

//        else if (algo == 3){
//            heuristicElimination(query);
//        }
        return ans;
    }

    public double jointProb(String q){
        double ans = 0;

        if (q.equals(null)){
            System.out.println("Null query");
            return -1;
        }

        else {
//            String queryVar = q.substring(2, 5);
            String queryVarName = q.substring(2, 3);
            String queryVarOutcome = q.substring(3, 4);
            int queryVarIndex = network.find(queryVarName);
            CptNode queryVarNode = network.get(queryVarIndex);

            if (q.length() > 5) { //e.g. P(A=F|J=T,M=T)=?
                String givenVars = q.substring(6, q.length() - 1);
                String[] givensArr = givenVars.split(",");
                ArrayList<String> givens = new ArrayList<>();
                ArrayList<String> outcomes = new ArrayList<>();

                String name = "";
                String outcome = "";

                for (int i = 0; i < givensArr.length; i++) {
                    name = givensArr[i].substring(0, 1);
                    givens.add(name);
                    outcome = givensArr[i].substring(2, 3);
                    givens.add(name);


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

                        for (int p = queryVarNode.getParentNodes().size() - 1; p >= 0; p--) {
                            CptNode parent = queryVarNode.getParentNodes().get(p);
                            for (int o = 0; o < parent.getOutcomes().size(); o++) {
                                if (parent.getOutcomes().get(o).equals(queryVarOutcome)) {
                                    outcomeIndex = o;
                                }
                            }

                        }


                        ans = Double.parseDouble(queryVarNode.getProbTable().get(index));
                        ans = formatAnswer(ans);
                        return ans;
                    } else { // we need to calculate the probability

                        String newq = "";
                        for (int i = 0; i < variables.size(); i++) {
                            //newq;
                            calcProb(newq);
                            multiAct++;

                        }
                    }


                }
            } else {  //e.g. P(B=T)=?. We can get that from the CPT
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
        }
        return ans;
    }

    public double eliminateBySize(String q){
        double ans = 0;

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

    public double calcProb(String q){
        double ans =0;
//        String
//        if (this.query.contains())
//        hasParents
//        if related -> evidence, else -> hidden

        return ans;
    }

    public void addToEvidence(String query){
        for (int i = 0; i<variables.size(); i++){
            CptNode currVar = variables.get(i);
            String currVarName = currVar.getName();
            if (query.contains(currVarName)){
                evidence.add(currVar);
            }
        }
    }

    public void addToHidden(String query){
        for (int i = 0; i<this.variables.size(); i++){
            CptNode currVar = this.variables.get(i);
            String currVarName = currVar.getName();
            if (!query.contains(currVarName)){
                hidden.add(currVar);
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

    public double getProbFromCPT(){





        double prob;
        int index = this.network.find(String.valueOf(query.charAt(2)));
        CptNode var = this.variables.get(index);
        var.getProbTable();

//        HashMap<String, >;
        ArrayList<String> varParents;
        if (var.hasParents()){
            varParents = var.getParents();
            for (int i = 0; i<varParents.size(); i++){
                ArrayList<Integer> outcomesNums = new ArrayList<>();
                String parent = varParents.get(i);
                CptNode parentNode = network.get(network.find(parent));
                int numOfOutcomes = parentNode.getOutcomes().size();
                outcomesNums.add(numOfOutcomes);
            }

        }

        prob = Double.parseDouble(var.getProbTable().get(0));
        return prob;
    }

}

