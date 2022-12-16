import java.text.DecimalFormat;
import java.util.*;

public class Algorithms {

    private String query;
    private double answer;
    private int addAct1 = 0;
    private int multiAct1 = 0;
    private int addAct2 = 0;
    private int multiAct2 = 0;
    private ArrayList<CptNode> evidence;
    private ArrayList<CptNode> hidden;
    private BayesianNetwork network;
    private ArrayList<CptNode> variables;
    private double alpha = 0;
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
            CptNode queryVar = network.get(index);

            //Save evidence variables outcomes
            HashMap<String, String> evidenceVars = new HashMap<>();              //at the end, it would look like {B=T, J=T, M=T}
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
                    ans = eliminateBySize(q, evidenceVars);
                    ans = formatAnswer(ans);
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
            CptNode queryVar = network.get(index);
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

    public double jointProb(CptNode queryVar, HashMap<String, String> evidenceVars) { //e.g. evidenceVars = {B=T,J=T,M=T}
        double ans;

        //get number of permutations for the hidden variables
        int numOfPerms = 1;
        for (CptNode cptNode : hidden) {
            numOfPerms *= cptNode.getOutcomes().size();
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
            this.alpha = denominatorAns;
            addAct1++;
        }
        addAct1--; //first addition does not count
        double alpha = normalize(denominatorAns);
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

    public ArrayList<HashMap<String, String>> getPerms(int numOfPerms) {

        ArrayList<HashMap<String, String>> permutations = new ArrayList<>();
        int[] outcomesSizes = new int[hidden.size()];
        int hiddenSize = hidden.size();

        for (int i = 0; i < hiddenSize; i++) {
            CptNode curr = hidden.get(i);
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
                CptNode currHidden = hidden.get(j);
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
                if (!permutations.contains(perm)) {
                    permutations.add(perm);
                }
            }
        }
        return permutations;
    }

    public double eliminateBySize(String q, HashMap<String, String> evidenceVars) {
        double ans = 0;

        return ans;
    }

    public double calcProb(int numOfPerms, HashMap<String, String> evidenceVars, CptNode queryVar) {
        double res = 0;

        //Create an array list that contains all permutations on the *hidden* variables
        ArrayList<HashMap<String, String>> perms;
        perms = getPerms(numOfPerms);

        //loop over all hidden permutations
        for (int i = 0; i < numOfPerms; i++) {
            HashMap<String, String> currQuery = new HashMap<>();
            currQuery.putAll(evidenceVars);
            currQuery.putAll(perms.get(i));
            res += calcEachPerm(currQuery);
            addAct1++;
        }

        addAct1--;
        return res;
    }

    public double calcEachPerm(HashMap<String, String> currQuery) {
        double res = 1;

        for (int i = 0; i < network.size(); i++) {
            CptNode curr = network.get(i);
            String currName = curr.getName();
            String currOutcome = currQuery.get(currName);
            HashMap<String,String> newQuery = new HashMap<>();
            newQuery.put(currName, currOutcome);
            if (curr.hasParents()){
                ArrayList<String> currParents = curr.getParents();
                for (String parentName : currParents) {
                    String parentOutcome = currQuery.get(parentName);
                    newQuery.put(parentName, parentOutcome);
                }
            }
            res *= getProbFromCPT(curr, currOutcome, newQuery);
            multiAct1++;
        }
        multiAct1--;
        return res;
    }

    public double getProbFromCPT(CptNode queryVar, String queryRequestedOutcome, HashMap<String, String> evidenceVars) {
        double ans = 0;
        String outcome = "";

        if (queryVar.hasParents()) { //e.g. P(A=T|E=T,B=F)=?
            ArrayList<CptNode> queryParents = queryVar.getParentNodes();

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
                    CptNode parent = queryParents.get(p);
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

//            } else if (evidenceVars.size() - 1 < queryParents.size()) {       //e.g. P(A=T|E=F)=? or P(B=T)=?
//                ArrayList<CptNode> hiddenParents = queryVar.getParentNodes();
//                for (int j = 0; j < queryParents.size(); j++) {
//                    CptNode parent = queryParents.get(j);
//                    String parentName = parent.getName();
//                    if (!evidenceVars.containsKey(parentName)) {
//                        hiddenParents.add(parent);
//                    }
//                }
//
//                int index = 0;
//                int outcomeIndex = 0;
//
//                for (int o = 0; o < queryVar.getOutcomes().size(); o++) {
//                    if (queryVar.getOutcomes().get(o).equals(queryRequestedOutcome)) {
//                        outcomeIndex = o;
//                    }
//                }
//                index += outcomeIndex;
//
//                int multiply = queryVar.getOutcomes().size();
//                for (int p = queryParents.size() - 1; p >= 0; p--) {
//                    CptNode parent = queryParents.get(p);
//                    if (!hiddenParents.contains(parent)){
//                        outcome = evidenceVars.get(parent.getName());
//                        for (int o = 0; o < parent.getOutcomes().size(); o++) {
//                            if (parent.getOutcomes().get(o).equals(outcome)) {
//                                outcomeIndex = o;
//                                break;
//                            }
//                        }
//                        index += outcomeIndex * multiply;
//                        multiply *= parent.getOutcomes().size();
//                    }
//
//
//                }
//
//                String[] probTable = queryVar.getProbTable().get(0).split(" ");
//                ans = Double.parseDouble(probTable[index]);
//                ans = formatAnswer(ans);
//                return ans;
//            }


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

    public boolean checkForCPT(HashMap<String, String> evidenceVars, CptNode queryVar) {
        boolean flag = false;
        if (evidenceVars.size() - 1 == queryVar.getParentNodes().size()) { //then we might get the answer from the cpt
            flag = true;
            for (int j = 0; j < queryVar.getParentNodes().size(); j++) {
                CptNode parent = queryVar.getParentNodes().get(j);
                if (!evidenceVars.containsKey(parent.getName())) {
                    flag = false;  //we cannot get the answer from the cpt
                    break;
                }
            }
        }
        return flag;
    }

    //    public void heuristicElimination(){
    //
    //    }

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
        for (CptNode currVar : variables) {
            String currVarName = currVar.getName();
            if (query.contains(currVarName) && !this.evidence.contains(currVar)) {
                this.evidence.add(currVar);
            }
        }
    }

    public void addToHidden(String query) {
        for (CptNode currVar : variables) {
            String currVarName = currVar.getName();
            if (!query.contains(currVarName) && !hidden.contains(currVar)) {
                hidden.add(currVar);
            }
        }
    }

    public void removeUnnecessaryVars() {

        ArrayList<CptNode> leafNodes = new ArrayList<>();

        for (CptNode currVar : variables) {
            if (!currVar.hasChildren()) {
                leafNodes.add(currVar);
            }
        }

        for (int i = 0; i < leafNodes.size(); i++) {
            CptNode currVar = variables.get(i);
            if (!(hidden.contains(currVar) && evidence.contains(currVar))) {
                leafNodes.remove(currVar);
            }
        }

        for (int i = 0; i < variables.size(); i++) {

        }


    }

    public void dropFromHidden() {
        for (int i = 0; i < hidden.size(); i++) {
            CptNode var = hidden.get(i);
            for (CptNode evi : evidence) {
                if (evi.getAncestors().contains(var)) {
                    hidden.remove(var);
                }
            }
        }
    }

    public String printHidden() {
        String print = "";
        for (CptNode cptNode : this.hidden) {
            print += cptNode.getName() + ", ";
        }
        return print;
    }

    public String printEvidence() {
        String print = "";
        for (CptNode cptNode : this.evidence) {
            print += cptNode.getName() + ", ";
        }
        return print;
    }

    public double getAlpha(){
        return alpha;
    }
}

