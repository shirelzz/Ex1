import java.util.HashMap;
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

    /**
     * constructor
     *
     * @param query   the query string as shown in the input file
     * @param network the BN for the given variables
     */
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

    /**
     * runs the algorithms
     *
     * @param algo the number of the algorithm required in the input file
     */
    public void runAlgo(int algo) {
        String q = query;
        double ans = 0;
        if (q.contains("|")) {                //e.g. "P(B=T|J=T,M=T)"
            String numeratorStr = q.replace("|", ",");       //"P(B=T,J=T,M=T)"
            numeratorStr = numeratorStr.substring(2, numeratorStr.length() - 1); //"B=T,J=T,M=T"
            String[] numerator = numeratorStr.split(",");                 //["B=T","J=T","M=T"]
            addToEvidence(numerator);
            addToHidden();
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
                if (algo == 2 || algo == 3) {
                    VariableElimination ve = new VariableElimination(evidence, hidden, variables, network, queryName_Outcome);
                    ve.varElm(queryVar, evidenceVars, 2);
                    ans = ve.getAnswer();
                    ans = formatAnswer(ans);
                    this.multiAct2 = ve.getMul_Act();
                    this.addAct2 = ve.getAdd_Act();
                    answer = ans;
                }
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

        ans = formatAnswer(ans);
        answer = ans;
    }

    private double calc(HashMap<String, String> evidenceVars) {
        double res = 0;

        return res;
    }


    /**
     * The first algorithm
     *
     * @param queryVar     the query variable
     * @param evidenceVars all the variables in this query
     * @return the probability of this query
     */
    public double jointProb(Variable queryVar, HashMap<String, String> evidenceVars) { //e.g. evidenceVars = {B=T,J=T,M=T}
        double ans;

        //get number of permutations for the hidden variables
        int numOfPerms = 1;
        for (Variable variable : hidden) {
            numOfPerms *= variable.getOutcomes().size();
        }

        double numeratorAns = calcProb(numOfPerms, evidenceVars);

            String orgQueryOutcome = evidenceVars.get(queryVar.getName());
            double denominatorAns = 0;

            //For the denominator we will sum all possible outcomes of the query variable
            for (int i = 0; i < queryVar.getOutcomes().size(); i++) {
                HashMap<String, String> queryVarOutcome = new HashMap<>();
                queryVarOutcome.putAll(evidenceVars);            //copy all evidence variables including the query variable
                queryVarOutcome.put(queryVar.getName(), queryVar.getOutcomes().get(i)); //set different outcome to the query variable
                if (orgQueryOutcome.equals(queryVar.getOutcomes().get(i))) {
                    denominatorAns += numeratorAns;
                } else {
                    denominatorAns += calcProb(numOfPerms, queryVarOutcome);
                }
                addAct1++;
            }
            addAct1--; //first addition does not count
            double alpha = normalize(denominatorAns);
            ans = alpha * numeratorAns;
            ans = formatAnswer(ans);

        return ans;
    }

    /**
     * @param res the result probability
     * @return normalized result
     */
    public double normalize(double res) {
        double alpha = 0;
        if (res > 0) {
            alpha = 1 / res;
        }
        return alpha;
    }

    /**
     * @param variables a list of variables
     * @return a list of all the permutations on this variables
     */
    public ArrayList<HashMap<String, String>> getPermsG(ArrayList<Variable> variables) {

        ArrayList<HashMap<String, String>> permutations = new ArrayList<>();
        if (variables.size() == 0) {
            return permutations;
        } else {

            int numOfPerms = 1;
            for (Variable variable : variables) {
                numOfPerms *= variable.getOutcomes().size();
            }

            int[] outcomesSizes = new int[variables.size()];
            int varSize = variables.size();

            for (int i = 0; i < varSize; i++) {
                Variable curr = variables.get(i);
                outcomesSizes[i] = curr.getOutcomes().size();
            }

            if (variables.size() == 1) {
                ArrayList<String> outcomes = variables.get(0).getOutcomes();
                String name = variables.get(0).getName();
                for (String outcome : outcomes) {
                    HashMap<String, String> perm = new HashMap<>();
                    perm.put(name, outcome);
                    permutations.add(perm);
                }
            } else {

                int[] outcomeSizes_new = new int[varSize];
                int m = outcomesSizes[0];
                outcomeSizes_new[0] = 0;
                outcomeSizes_new[1] = outcomesSizes[0];

                for (int i = 2; i < outcomesSizes.length; i++) {
                    m *= outcomesSizes[i - 1];
                    outcomeSizes_new[i] = m;
                }

                String name;
                String outcome;
                int[] outcomes = new int[varSize];

                for (int i = 0; i < numOfPerms; i++) {
                    HashMap<String, String> perm = new HashMap<>();
                    for (int j = 0; j < varSize; j++) {
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
                            if ((i % outcomeSizes_new[j] == 0) && (i != 0)) {
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
            }
        }
        return permutations;
    }

    /**
     * @param numOfPerms   number of permutations we should have
     * @param evidenceVars names and outcomes of some variables
     * @return the probability of the evidenceVars
     */
    public double calcProb(int numOfPerms, HashMap<String, String> evidenceVars) {
        double res = 0;

        if (hidden.size() == 0) {
            HashMap<String, String> currQuery = new HashMap<>();
            currQuery.putAll(evidenceVars);
            res += calcEachPerm(currQuery);
            addAct1++;

        } else {
            //Create an array list that contains all permutations on the *hidden* variables
            ArrayList<HashMap<String, String>> perms;
            perms = getPermsG(hidden);

            //loop over all hidden permutations
            for (int i = 0; i < numOfPerms; i++) {
                HashMap<String, String> currQuery = new HashMap<>();
                currQuery.putAll(evidenceVars);
                currQuery.putAll(perms.get(i));
                res += calcEachPerm(currQuery);
                addAct1++;
            }
        }
        addAct1--; //first addition does not count
        return res;
    }

    /**
     * @param currQuery a permutation on the variables that needs to be calculated
     * @return the probability of this currQuery
     */
    public double calcEachPerm(HashMap<String, String> currQuery) { //e.g. currQuery = {J=F, A=T, M=t, B=F, E=T}}
        double res = 1;

        for (int i = 0; i < network.size(); i++) {
            Variable curr = network.get(i);
            String currName = curr.getName();  //e.g J
            String currOutcome = currQuery.get(currName);  //e.g F
            HashMap<String, String> newQuery = new HashMap<>();
            newQuery.put(currName, currOutcome);   //e.g {J=F}
            if (curr.hasParents()) {
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

    /**
     * get the answer directly from the CPT
     *
     * @param queryVar              the query variable
     * @param queryRequestedOutcome the query variables outcome as it shows in the query
     * @param evidenceVars          all the variables names and outcomes as they're shown in the query
     * @return the probability from the CPT
     */
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

    /**
     * checks if we can get the answer from the CPT
     *
     * @param evidenceVars the names and the outcomes of the variables in the query
     * @param queryVar     the query variable
     * @return true if we can get the answer from the CPT, false otherwise
     */
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

    public void addToEvidence(String[] query) {

        for (int i =0; i<query.length; i++){
            String[] evi_name_outcome_split = query[i].split("=");
            String evi_name = evi_name_outcome_split[0];
            int index = network.find(evi_name);
            evidence.add(network.get(index));
        }
    }

    public void addToHidden() {
        for (int i = 0; i<network.size(); i++){
            Variable variable = network.get(i);
            if (!evidence.contains(variable)){
                hidden.add(variable);
            }
        }
    }

}