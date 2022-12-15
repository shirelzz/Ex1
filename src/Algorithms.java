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

    public double runAlgo(int algo) {
        String q = this.query;
        double ans = 0;
        if (q.contains("|")) {                //e.g. "P(B=T|J=T,M=T)"
            String numeratorStr = q.replace("|", ",");       //"P(B=T,J=T,M=T)"
            numeratorStr = numeratorStr.substring(2, numeratorStr.length() - 1); //"B=T,J=T,M=T"
            String[] numerator = numeratorStr.split(",");                //["B=T","J=T","M=T"]
            String[] queryName_Outcome = numerator[0].split("=");       //e.g. [B,T]
            String queryVarName = queryName_Outcome[0];                      //e.g. "B"
            String queryRequestedOutcome = queryName_Outcome[1];             //e.g. "T"
            //Find query variable
            int index = this.network.find(queryVarName);
            CptNode queryVar = this.network.get(index);

            //Save evidence variables outcomes
            HashMap<String, String> evidenceVars = new HashMap<>();              //at the end, it would look like {B=T, J=T, M=T}
            for (int i = 0; i < numerator.length; i++) {
                String[] varName_outcome = numerator[i].split("=");       //e.g. [B,T] ...
                evidenceVars.put(varName_outcome[0], varName_outcome[1]);       //e.g. {B=T} ...
            }

            ans = getProbFromCPT(queryVar, queryRequestedOutcome, evidenceVars);
            if (ans > 0) {   //or maybe <1 . depends. pay attention to this!!
                ans = formatAnswer(ans);
                this.answer = ans;
                return ans;
            } else {
                /*Hidden variables outcomes.
                This part saves them in a hashmap.
                Later we will get their permutation for the joint distribution algorithm.
                 */
                HashMap<String, String> hiddenVars = new HashMap<>();
                for (int j = 0; j < this.hidden.size(); j++) {
                    CptNode currHidden = this.hidden.get(j);
                    hiddenVars.put(currHidden.getName(), currHidden.getOutcomes().get(0));
                }

                if (algo == 1) {
                    ans = jointProb(queryVar, queryRequestedOutcome, evidenceVars);
                    ans = formatAnswer(ans);
                    this.answer = ans;
                    return ans;
                }
                if (algo == 2) {
                    ans = eliminateBySize(q, evidenceVars);
                    ans = formatAnswer(ans);
                    this.answer = ans;
                    return ans;
                }
            }
        } else { //"P(B=T)"
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
                ans = jointProb(queryVar, outcome, query);
            }
            ans = formatAnswer(ans);
            this.answer = ans;
            return ans;
        }
//        else if (algo == 3){
//            ans = heuristicElimination(q);
//        }
        ans = formatAnswer(ans);
        this.answer = ans;
        return ans;
    }

    public double jointProb(CptNode queryVar, String queryRequestedOutcome, HashMap evidenceVars) { //e.g. evidenceVars = {B=T,J=T,M=T}
        double ans = 0;

        //get number of permutations for the hidden variables
        int numOfPerms = 1;
        for (int i = 0; i < this.hidden.size(); i++) {
            numOfPerms *= this.hidden.get(i).getOutcomes().size();
        }

        String newQ = "";
//        if (q.contains("|")) {  //then we need to calculate. Else we surely(yael?) can get the answer from the CPT

            /* To answer a query like P(B=T|J=T,M=T) we need to calculate:
            P(B=T|J=T,M=T) = P(B=T,J=T,M=T)/P(J=T,M=T)
            We can also write that as:
            P(B=T|J=T,M=T) = P(B=T,J=T,M=T) / (P(B=T,J=T,M=T) + P(B=F,J=T,M=T))
            */
//            String numeratorQ = q.replace("|", ",");                   //e.g. numeratorQ = "P(B=T,J=T,M=T)"
//            String tempDenominator = numeratorQ.substring(2, numeratorQ.length()-1);     //e.g. tempDenominator = "B=T,J=T,M=T"
//            String[] tempDenominatorArr = tempDenominator.split(",");             //e.g. tempDenominatorArr = ["B=T"], ["J=T"], ["M=T"]
//            String denominatorQ = "P(" ;
//            for (int j = 1; j<tempDenominatorArr.length; j++){
//                denominatorQ += tempDenominatorArr[j];
//                if (j == tempDenominatorArr.length-1){
//                    denominatorQ += ")";
//                }
//                else {
//                    denominatorQ += ",";
//                }
//            }
        double numeratorAns = calcProb(numOfPerms, evidenceVars, queryVar);
        double denominatorAns = 0;


            /*
            For the denominator we will sum all possible outcomes of the query variable
             */
        int counter = 0;
        for (int i = 0; i < queryVar.getOutcomes().size(); i++) {
            HashMap<String, String> queryVarOutcome = new HashMap<>();
            queryVarOutcome.putAll(evidenceVars);            //copy all evidence variables including the query variable
            queryVarOutcome.put(queryVar.getName(), queryVar.getOutcomes().get(i)); //set different outcome to the query variable
            denominatorAns += calcProb(numOfPerms, queryVarOutcome, queryVar);
            counter++;
        }
//            evidenceVars.get(queryVarName).
        ans = numeratorAns;
        double alpha = normalize(denominatorAns);
        ans = alpha * ans;
        ans = formatAnswer(ans);
        return ans;
//        } else {
//            HashMap<String,String> evidence = new HashMap<>();
//            evidence.put(queryVar.getName(), queryRequestedOutcome);
//            ans = getProbFromCPT(queryVar, queryRequestedOutcome, evidenceVars);
//            ans = formatAnswer(ans);
//            this.answer = ans;
//        }
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
        int outcomesSizes[] = new int[this.hidden.size()];
        int hiddenSize = this.hidden.size();

        for (int i = 0; i < hiddenSize; i++) {
            CptNode curr = this.hidden.get(i);
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

        String name = "";
        String outcome = "";
        int outcomes[] = new int[hiddenSize];

        for (int i = 0; i < numOfPerms; i++) {
            HashMap<String, String> perm = new HashMap<>();
            for (int j = 0; j < hiddenSize; j++) {
                CptNode currHidden = this.hidden.get(j);
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

    public double eliminateBySize(String q, HashMap evidenceVars) {
        double ans = 0;

        return ans;
    }

    public double calcProb(int numOfPerms, HashMap evidenceVars, CptNode queryVar) {
        double ans = 0;

        //Create an array list that contains all permutations on the hidden variables
        ArrayList<HashMap<String, String>> perms;
        perms = getPerms(numOfPerms);
        for (int i = 0; i < numOfPerms; i++) {
            HashMap<String, String> currQuery = new HashMap<>();
            currQuery.putAll(evidenceVars);
            currQuery.putAll(perms.get(i));
            ans += calcEachPerm(currQuery, queryVar);
            this.addAct1++;
        }
        return ans;
    }

//    public void heuristicElimination(){
//
//    }

    public double calcEachPerm(HashMap<String, String> currQuery, CptNode queryVar) {
        double ans = 1;
        String requestedOutcome = currQuery.get(queryVar.getName());

        for (int i = 0; i < this.network.size(); i++) {
            CptNode curr = this.network.get(i);
            String currName = curr.getName();
            if (curr.hasParents()) {

            } else {
                ans *= getProbFromCPT(queryVar, requestedOutcome, currQuery);
            }
            currQuery.get(currName);
        }

        return ans;
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
        for (int i = 0; i < variables.size(); i++) {
            CptNode currVar = variables.get(i);
            String currVarName = currVar.getName();
            if (query.contains(currVarName) && !this.evidence.contains(currVarName)) {
                this.evidence.add(currVar);
            }
        }
    }

    public void addToHidden(String query) {
        for (int i = 0; i < this.variables.size(); i++) {
            CptNode currVar = this.variables.get(i);
            String currVarName = currVar.getName();
            if (!query.contains(currVarName) && !this.hidden.contains(currVarName)) {
                this.hidden.add(currVar);
            }
        }
    }

    public void removeUnnecessaryVars() {

        ArrayList<CptNode> leafNodes = new ArrayList<>();

        for (CptNode currVar : this.variables) {
            if (!currVar.hasChildren()) {
                leafNodes.add(currVar);
            }
        }

        for (int i = 0; i < leafNodes.size(); i++) {
            CptNode currVar = this.variables.get(i);
            if (!(this.hidden.contains(currVar) && this.evidence.contains(currVar))) {
                leafNodes.remove(currVar);
            }
        }

        for (int i = 0; i < this.variables.size(); i++) {

        }


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

    public void dropFromHidden() {
        for (int i = 0; i < this.hidden.size(); i++) {
            CptNode var = this.hidden.get(i);
            for (int j = 0; j < this.evidence.size(); j++) {
                CptNode evi = this.evidence.get(j);
                if (evi.getAncestors().contains(var.getName())) {
                    this.hidden.remove(var);
                }
            }
        }
    }

    public String printHidden() {
        String print = "";
        for (int i = 0; i < this.hidden.size(); i++) {
            print += this.hidden.get(i).getName() + ", ";
        }
        return print;
    }

    public String printEvidence() {
        String print = "";
        for (int i = 0; i < this.evidence.size(); i++) {
            print += this.evidence.get(i).getName() + ", ";
        }
        return print;
    }
}

