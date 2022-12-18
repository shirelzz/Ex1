import java.text.DecimalFormat;
import java.util.*;

public class Algorithms {

    private String query;
    private double answer;
    private int addAct1 = 0;
    private int multiAct1 = 0;
    private int addAct2 = 0;
    private int multiAct2 = 0;
    private ArrayList<Variable> evidence;
    private ArrayList<Variable> hidden;
    private BayesianNetwork network;
    private ArrayList<Variable> variables;
    private double alpha = 0;
//    private ArrayList<Factor> factors;
//    private Hashtable factor;


    Algorithms(String query, BayesianNetwork network) {
        this.query = query;
        this.network = network;
        this.variables = new ArrayList<>();
        for (int i = 0; i < network.size(); i++) {
            Variable curr = network.get(i);
            this.variables.add(curr);
        }
        this.hidden = new ArrayList<>();
        this.evidence = new ArrayList<>();
        this.answer = 0;

    }

    public void runAlgo(int algo) {
        String q = query;
        double ans = 0;
        if (q.contains("|")) {                //e.g. "P(B=T|J=T,M=T)"
            String numeratorStr = q.replace("|", ",");       //"P(B=T,J=T,M=T)"
            numeratorStr = numeratorStr.substring(2, numeratorStr.length() - 1); //"B=T,J=T,M=T"
            String[] numerator = numeratorStr.split(",");                //["B=T","J=T","M=T"]
            String[] queryName_Outcome = numerator[0].split("=");       //e.g. [B,T]
            String queryVarName = queryName_Outcome[0];                      //e.g. "B"
            String queryRequestedOutcome = queryName_Outcome[1];             //e.g. "T"
            //Find query variable
            int index = network.find(queryVarName);
            Variable queryVar = network.get(index);

            //Save evidence variables outcomes
            HashMap<String, String> evidenceVars = new HashMap<>();         //at the end, it would look like {B=T, J=T, M=T}
            for (String s : numerator) {
                String[] varName_outcome = s.split("=");       //e.g. [B,T] ...
                evidenceVars.put(varName_outcome[0], varName_outcome[1]);       //e.g. {B=T} ...
            }

            if (checkForCPT(evidenceVars, queryVar)) {
                ans = getProbFromCPT(queryVar, queryRequestedOutcome, evidenceVars);
                ans = formatAnswer(ans);
                answer = ans;
            } else {
                if (algo == 1) {
                    ans = jointProb(queryVar, evidenceVars);
                    ans = formatAnswer(ans);
                    answer = ans;
                }
                if (algo == 2) {
                    ans = varElm(queryVar, evidenceVars, 2);
                    ans = formatAnswer(ans);
                    answer = ans;
                }
//                if (algo == 3) {
//                    ans = varElm(queryVar, evidenceVars, 3);
//                    ans = formatAnswer(ans);
//                    answer = ans;
//                }
            }
        } else { //e.g. "P(B=T)"
            String newQ = q.substring(2, q.length() - 1); //e.g. "B=T"
            String[] qName_Outcome = newQ.split("=");
            String name = qName_Outcome[0];
            String outcome = qName_Outcome[1];

            HashMap<String, String> query = new HashMap<>();
            query.put(name, outcome);

            int index = network.find(name);
            Variable queryVar = network.get(index);
            if (!queryVar.hasParents()) {
                ans = getProbFromCPT(queryVar, outcome, query);
            } else {
                ans = jointProb(queryVar, query);
            }
            ans = formatAnswer(ans);
            answer = ans;
        }
//        else if (algo == 3){
//            ans = heuristicElimination(q);
//        }
        ans = formatAnswer(ans);
        answer = ans;
    }

    public double varElm(Variable queryVar, HashMap<String,String> evidenceVars, int eliminationOrder){
        double ans = 0;

        //define factors
        ArrayList<Factor> factors = new ArrayList<>();
        for (int i = 0; i<variables.size(); i++){
            Variable variable = variables.get(i);
            Factor factor = new Factor(hidden, evidence, variable.getName());
            if (variable.hasParents()){
                ArrayList<Variable> parents = variable.getParentNodes();
                parents.add(variable); //add the child
                ArrayList<HashMap<String,String>> perms = getPermsG(parents);
                ArrayList<String> values = new ArrayList<>();

                for (int p = 0; i < perms.size(); p++) {
                    HashMap<String, String> currRow = perms.get(p);
                    for (int k = 0; k<parents.size(); k++){

                    }
                    String val = String.valueOf(getProbFromCPT(variable, currRow.get(variable.getName()), currRow));
                    values.add(val);
                }

                factor.defFactor(perms,values);
            }
            else {
                ArrayList<HashMap<String,String>> perm = new ArrayList<>();
                for (int j = 0; j<variable.getOutcomes().size(); j++){
                    HashMap<String,String> row = new HashMap<>();
                    String outcome = variable.getOutcomes().get(i);
                    double value = getProbFromCPT(variable, outcome, evidenceVars);
                    String val = String.valueOf(value);
                    row.put(variable.getName(), outcome);
                    row.put("val", val);
                    factor.addRow(row);
                }
            }
            factors.add(factor);
        }

        //restrict factors
        for (int i = 0; i<evidence.size(); i++){
            Variable evi = evidence.get(i);
            String outcome = evidenceVars.get(evi.getName());
            ArrayList<Factor> f_evi = getFactorsConVar(factors, evi);
            for (int j = 0; j<f_evi.size(); j++){
                Factor factor = f_evi.get(j);
                factor.restrictFactor(evi, outcome);
            }
        }

        //eliminate hidden variables
        ArrayList<Factor> hiddenFsAfterMulti = new ArrayList<>();
        ArrayList<Factor> sumOutHiddenFs = new ArrayList<>();

        for (int i = 0; i<hidden.size(); i++){
            Variable hid = hidden.get(i);
            String newName = hid.getName();
            String outcome = evidenceVars.get(hid.getName());
            ArrayList<Factor> f_hid = getFactorsConVar(factors, hid); //find factors
            f_hid = order(f_hid, eliminationOrder);

            //multiply factors
            Factor f0 = f_hid.get(0);
            Factor f1 = f_hid.get(1);
            Factor f2 = f0.multiplyFactors(f1);
            f2.setName(newName);
            hiddenFsAfterMulti.add(f2);
            multiAct2++;

            for (int j = 2; j<f_hid.size()-1; j++){
                f1 = f_hid.get(j);
                f2 = f2.multiplyFactors(f1);
                hiddenFsAfterMulti.add(f2);
                multiAct2++;
            }

            //sum out
            f2.sumOut(hid);
            sumOutHiddenFs.add(f2);
        }

        //multiply all remaining factors


        //normalize






        return ans;
    }

    public ArrayList<Factor> getFactorsConVar(ArrayList<Factor> factors, Variable variable) {
        ArrayList<Factor> factorsContVar = new ArrayList<>();

        for (int i = 0; i < factors.size(); i++) {
            Factor currFactor = factors.get(i);
            if (currFactor.contains(variable.getName())) {
                factorsContVar.add(currFactor);
            }
        }
        return factorsContVar;
    }

    public ArrayList<Factor> order(ArrayList<Factor> factors, int eliminationOrder) {
        ArrayList<Factor> ordered = new ArrayList<>();

        if (eliminationOrder == 2){ //by size

        }

        if (eliminationOrder == 3){ //heuristic

        }

        return ordered;
    }


    //    public void heuristicElimination(){
    //
    //    }




    public double jointProb(Variable queryVar, HashMap<String, String> evidenceVars) { //e.g. evidenceVars = {B=T,J=T,M=T}
        double ans;

        //get number of permutations for the hidden variables
        int numOfPerms = 1;
        for (Variable variable : hidden) {
            numOfPerms *= variable.getOutcomes().size();
        }

        double numeratorAns = calcProb(numOfPerms, evidenceVars, queryVar);
        String orgQueryOutcome = evidenceVars.get(queryVar.getName());
        double denominatorAns = 0;


        /*
            For the denominator we will sum all possible outcomes of the query variable
        */
        for (int i = 0; i < queryVar.getOutcomes().size(); i++) {
            HashMap<String, String> queryVarOutcome = new HashMap<>();
            queryVarOutcome.putAll(evidenceVars);            //copy all evidence variables including the query variable
            queryVarOutcome.put(queryVar.getName(), queryVar.getOutcomes().get(i)); //set different outcome to the query variable
            if (orgQueryOutcome.equals(queryVar.getOutcomes().get(i))){
                denominatorAns += numeratorAns;
            }
            else{
                denominatorAns += calcProb(numOfPerms, queryVarOutcome, queryVar);
            }
            addAct1++;
        }
        addAct1--; //first addition does not count
        double alpha = normalize(denominatorAns);
        this.alpha = numeratorAns;
        ans = alpha * numeratorAns;
        ans = formatAnswer(ans);
        return ans;
    }

    public double normalize(double res) { //see later how to normalize
        double alpha = 0;
        if (res > 0) {
            alpha = 1 / res;
        }
        return alpha;
    }

    public ArrayList<HashMap<String, String>> getPermsHid(int numOfPerms) {

        ArrayList<HashMap<String, String>> permutations = new ArrayList<>();
        int[] outcomesSizes = new int[hidden.size()];
        int hiddenSize = hidden.size();

        for (int i = 0; i < hiddenSize; i++) {
            Variable curr = hidden.get(i);
            outcomesSizes[i] = curr.getOutcomes().size();
        }

        int m = outcomesSizes[0];
        int temp = outcomesSizes[1];
        outcomesSizes[1] = m;
        m = temp;
        outcomesSizes[0] = 0;

        for (int i = 2; i < outcomesSizes.length; i++) {
            m *= outcomesSizes[i];
            outcomesSizes[i] = m;
        }

        String name;
        String outcome;
        int[] outcomes = new int[hiddenSize];

        for (int i = 0; i < numOfPerms; i++) {
            HashMap<String, String> perm = new HashMap<>();
            for (int j = 0; j < hiddenSize; j++) {
                Variable currHidden = hidden.get(j);
                name = currHidden.getName();
                int numOfOutcomes = currHidden.getOutcomes().size();
                if (outcomes[j] >= numOfOutcomes) {
                    outcomes[j] = 0;
                }
                outcome = currHidden.getOutcomes().get(outcomes[j]);
                if (j == 0) {
                    outcomes[j]++;
                } else {
                    if ((i % outcomesSizes[j] == 0) && (i != 0)) {
                        if (outcomes[j] + 1 >= numOfOutcomes) {
                            outcomes[j] = 0;
                        } else {
                            outcomes[j]++;
                        }
                        outcome = currHidden.getOutcomes().get(outcomes[j]);
                    }
                }
                perm.put(name, outcome);
            }
            if (!permutations.contains(perm)) {
                permutations.add(perm);
            }
        }
        return permutations;
    }

    public ArrayList<HashMap<String, String>> getPermsG(ArrayList<Variable> variables) {

        int numOfPerms = getNumOfPerms(variables);

        ArrayList<HashMap<String, String>> permutations = new ArrayList<>();
        int[] outcomesSizes = new int[variables.size()];

        for (int i = 0; i < variables.size(); i++) {
            Variable curr = variables.get(i);
            outcomesSizes[i] = curr.getOutcomes().size();
        }

        int m = outcomesSizes[0];
        int temp = outcomesSizes[1];
        outcomesSizes[1] = m;
        m = temp;
        outcomesSizes[0] = 0;

        for (int i = 2; i < outcomesSizes.length; i++) {
            m *= outcomesSizes[i];
            outcomesSizes[i] = m;
        }

        String name;
        String outcome;
        int[] outcomes = new int[variables.size()];

        for (int i = 0; i < numOfPerms; i++) {
            HashMap<String, String> perm = new HashMap<>();
            for (int j = 0; j < variables.size(); j++) {
                Variable currVar = variables.get(j);
                name = currVar.getName();
                int numOfOutcomes = currVar.getOutcomes().size();
                if (outcomes[j] >= numOfOutcomes) {
                    outcomes[j] = 0;
                }
                outcome = currVar.getOutcomes().get(outcomes[j]);
                if (j == 0) {
                    outcomes[j]++;
                } else {
                    if ((i % outcomesSizes[j] == 0) && (i != 0)) {
                        if (outcomes[j] + 1 >= numOfOutcomes) {
                            outcomes[j] = 0;
                        } else {
                            outcomes[j]++;
                        }
                        outcome = currVar.getOutcomes().get(outcomes[j]);
                    }
                }
                perm.put(name, outcome);
            }
            if (!permutations.contains(perm)) {
                permutations.add(perm);
            }
        }
        return permutations;
    }

    public double calcProb(int numOfPerms, HashMap<String, String> evidenceVars, Variable queryVar) {
        double res = 0;

        //Create an array list that contains all permutations on the *hidden* variables
        ArrayList<HashMap<String, String>> perms;
        perms = getPermsHid(numOfPerms);

        //loop over all hidden permutations
        for (int i = 0; i < numOfPerms; i++) {
            HashMap<String, String> currQuery = new HashMap<>();
            currQuery.putAll(evidenceVars);
            currQuery.putAll(perms.get(i));
            res += calcEachPerm(currQuery);
            addAct1++;
        }

        addAct1--; //first addition does not count
        return res;
    }

    public double calcEachPerm(HashMap<String, String> currQuery) { //e.g. currQuery = {J=F, A=T, M=t, B=F, E=T}}
        double res = 1;

        for (int i = 0; i < network.size(); i++) {
            Variable curr = network.get(i);
            String currName = curr.getName();  //e.g J
            String currOutcome = currQuery.get(currName);  //e.g F
            HashMap<String,String> newQuery = new HashMap<>();
            newQuery.put(currName, currOutcome);   //e.g {J=F}
            if (curr.hasParents()){
                ArrayList<String> currParents = curr.getParents();   //e.g J.parents = [A]
                for (String parentName : currParents) {    //[A]
                    String parentOutcome = currQuery.get(parentName);  //T
                    newQuery.put(parentName, parentOutcome); //e.g {J=F, A=T}
                }
            }
            res *= getProbFromCPT(curr, currOutcome, newQuery);
            multiAct1++;
        }
        multiAct1--; //first multiplication does not count
        return res;
    }

    public double getProbFromCPT(Variable queryVar, String queryRequestedOutcome, HashMap<String, String> evidenceVars) {
        double ans = 0;
        String outcome = "";

        if (queryVar.hasParents()) { //e.g. P(A=T|E=T,B=F)=?
            ArrayList<Variable> queryParents = queryVar.getParentNodes();

            if (evidenceVars.size() - 1 == queryParents.size()) {
                int index = 0;
                int outcomeIndex = 0;

                for (int o = 0; o < queryVar.getOutcomes().size(); o++) {
                    if (queryVar.getOutcomes().get(o).equals(queryRequestedOutcome)) {
                        outcomeIndex = o;
                    }
                }
                index += outcomeIndex;

                int multiply = queryVar.getOutcomes().size();
                for (int p = queryParents.size() - 1; p >= 0; p--) {
                    Variable parent = queryParents.get(p);
                    outcome = evidenceVars.get(parent.getName());
                    for (int o = 0; o < parent.getOutcomes().size(); o++) {
                        if (parent.getOutcomes().get(o).equals(outcome)) {
                            outcomeIndex = o;
                            break;
                        }
                    }
                    index += outcomeIndex * multiply;
                    multiply *= parent.getOutcomes().size();
                }

                String[] probTable = queryVar.getProbTable().get(0).split(" ");
                ans = Double.parseDouble(probTable[index]);
                ans = formatAnswer(ans);
            }

        } else {  //queryVar does not have any parents
            int index = 0;
            for (int i = 0; i < queryVar.getOutcomes().size(); i++) {
                String outcome_ = queryVar.getOutcomes().get(i);
                if (outcome_.equals(queryRequestedOutcome)) {
                    index = i;
                    break;
                }
            }
            String[] probTable = queryVar.getProbTable().get(0).split(" ");
            ans = Double.parseDouble(probTable[index]);
            ans = formatAnswer(ans);
            return ans;
        }
        return ans;
    }

    public boolean checkForCPT(HashMap<String, String> evidenceVars, Variable queryVar) {
        boolean flag = false;
        if (evidenceVars.size() - 1 == queryVar.getParentNodes().size()) { //then we might get the answer from the cpt
            flag = true;
            for (int j = 0; j < queryVar.getParentNodes().size(); j++) {
                Variable parent = queryVar.getParentNodes().get(j);
                if (!evidenceVars.containsKey(parent.getName())) {
                    flag = false;  //we cannot get the answer from the cpt
                    break;
                }
            }
        }
        return flag;
    }

    public double formatAnswer(double ans) {
        double value = ans;
        value = Double.parseDouble(new DecimalFormat("#.#####").format(value));
        return value;
    }

    public double getAnswer() {
        return formatAnswer(this.answer);
    }

    public int getAddActions1() {
        return this.addAct1;
    }

    public int getMultiplyActions1() {
        return this.multiAct1;
    }

    public int getAddActions2() {
        return this.addAct2;
    }

    public int getMultiplyActions2() {
        return this.multiAct2;
    }

    public void addToEvidence(String query) {
        for (Variable currVar : variables) {
            String currVarName = currVar.getName();
            if (query.contains(currVarName) && !this.evidence.contains(currVar)) {
                this.evidence.add(currVar);
            }
        }
    }

    public void addToHidden(String query) {
        for (Variable currVar : variables) {
            String currVarName = currVar.getName();
            if (!query.contains(currVarName) && !hidden.contains(currVar)) {
                hidden.add(currVar);
            }
        }
    }

    public void removeUnnecessaryVars() {

        ArrayList<Variable> leafNodes = new ArrayList<>();

        for (Variable currVar : variables) {
            if (!currVar.hasChildren()) {
                leafNodes.add(currVar);
            }
        }

        for (int i = 0; i < leafNodes.size(); i++) {
            Variable currVar = variables.get(i);
            if (!(hidden.contains(currVar) && evidence.contains(currVar))) {
                leafNodes.remove(currVar);
            }
        }

        for (int i = 0; i < variables.size(); i++) {

        }


    }

    public void dropFromHidden() {
        for (int i = 0; i < hidden.size(); i++) {
            Variable var = hidden.get(i);
            for (Variable evi : evidence) {
                if (evi.getAncestors().contains(var)) {
                    hidden.remove(var);
                }
            }
        }
    }

    public String printHidden() {
        String print = "";
        for (Variable cptNode : this.hidden) {
            print += cptNode.getName() + ", ";
        }
        return print;
    }

    public String printEvidence() {
        String print = "";
        for (Variable cptNode : this.evidence) {
            print += cptNode.getName() + ", ";
        }
        return print;
    }

    public double getAlpha(){
        return alpha;
    }

    public int getNumOfPerms(ArrayList<Variable> variables){
        int numOfPerms = 1;
        for (Variable variable : variables) {
            numOfPerms *= variable.getOutcomes().size();
        }
        return numOfPerms;
    }

}

