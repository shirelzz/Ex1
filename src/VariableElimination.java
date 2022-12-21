import java.util.*;

public class VariableElimination {

    private ArrayList<Variable> evidence;
    private ArrayList<Variable> hidden;
    private BayesianNetwork network;
    private ArrayList<Variable> variables;
    private String[] queryName_Outcome;
    private double answer;
    private int mul_Act;
    private int add_Act;



    VariableElimination(ArrayList<Variable> evidence, ArrayList<Variable> hidden, ArrayList<Variable> variables, BayesianNetwork bn,String[] queryName_Outcome) {
        this.evidence = evidence;
        this.variables = variables;
        this.hidden = hidden;
        this.network = bn;
        this.add_Act = 0;
        this.mul_Act = 0;
        this.queryName_Outcome = queryName_Outcome;
    }

    public double varElm(Variable queryVar, HashMap<String, String> evidenceVars, int eliminationOrder) {

        //define factors
        ArrayList<Factor> factors = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);
            ArrayList<String> names = new ArrayList<>();
            names.add(variable.getName());
            Factor factor = new Factor(hidden, evidence, names);

            if (variable.hasParents()) {
                ArrayList<Variable> parents = variable.getParentNodes();
                ArrayList<String> parentsNames = variable.getParents();

                for (int n = 0; n < parentsNames.size(); n++) {
                    names.add(parentsNames.get(n));
                }
                factor.setNames(names);

                parents.add(variable); //add the child
                ArrayList<HashMap<String, String>> perms = getPermsG(parents);
                ArrayList<String> values = new ArrayList<>();

                for (int p = 0; p < perms.size(); p++) {
                    HashMap<String, String> currRow = perms.get(p);
                    double value = UtilFunctions.getProbFromCPT(variable, currRow.get(variable.getName()), currRow);
                    String val = String.valueOf(value);
                    values.add(val);
                }

                factor.defFactor(perms, values);
            } else {
                ArrayList<HashMap<String, String>> perm = new ArrayList<>();
                for (int j = 0; j < variable.getOutcomes().size(); j++) {
                    HashMap<String, String> row = new HashMap<>();
                    String outcome = variable.getOutcomes().get(j);
                    double value = UtilFunctions.getProbFromCPT(variable, outcome, evidenceVars);
                    String val = String.valueOf(value);
                    row.put(variable.getName(), outcome);
                    row.put("val", val);
                    factor.addRow(row);
                }
            }
            factors.add(factor);
        }

        //restrict factors
        for (int i = 0; i < evidence.size(); i++) {
            Variable evi = evidence.get(i);
            String outcome = evidenceVars.get(evi.getName());
            ArrayList<Factor> f_evi = getFactorsConVar(factors, evi);
            for (int j = 0; j < f_evi.size(); j++) {
                Factor factor = f_evi.get(j);
                factor.restrictFactor(evi, outcome);
            }
        }

        //eliminate hidden variables
        ArrayList<Factor> hiddenFsAfterMulti = new ArrayList<>();
        ArrayList<Factor> sumOutHiddenFs = new ArrayList<>();

        for (int i = 0; i < hidden.size(); i++) {
            Variable hid = hidden.get(i);
            String hidName = hid.getName();
            ArrayList<String> newName = new ArrayList<>();
            newName.add(hidName);

            String outcome = evidenceVars.get(hid.getName());
            ArrayList<Factor> f_hid = getFactorsConVar(factors, hid); //find factors
            if (eliminationOrder == 2) {  //algo = 2 , second algorithm
                if (f_hid.size() >= 2) {
                    Factor[] f_hidden= sortFactors(f_hid);

                    //multiply factors
                    Factor factor_j = joinTwoFactors(f_hidden[0], f_hidden[1]);
                    factor_j.setNames(newName);
                    hiddenFsAfterMulti.add(factor_j);
                    mul_Act++;

                    if (f_hid.size() > 2) {
                        for (int j = 2; j < f_hid.size() - 1; j++) {
                            factor_j = joinTwoFactors(factor_j, f_hidden[j]);
                            hiddenFsAfterMulti.add(factor_j);
                            mul_Act++;
                        }
                    }

                    //sum out
                    factor_j.sumOut(hid);
                    sumOutHiddenFs.add(factor_j);

                }
            } else {
                continue; //? 3rd algorithm
            }


        }

        //multiply all remaining factors
        if (factors.size()>=2){ /////???????
            Factor[] sorted = sortFactors(factors);
            Factor f = joinTwoFactors(sorted[0], sorted[1]);

            if (factors.size()>2){
                for (int s= 1; s<sorted.length; s++){
                    f = joinTwoFactors(f, sorted[s]);
                }
            }

            //normalize
            f.normFactor();
            for (int l = 0; l<f.size(); l++){
                HashMap<String,String> line = f.get(l);
                if (line.get(queryName_Outcome[0]).equals(queryName_Outcome[1])){
                    answer = Double.parseDouble(line.get("val"));
                }
            }
        }
        return answer;
    }

    public Factor joinTwoFactors(Factor X, Factor Y) {

        // get the outcome hashmaps for X and Y
        HashMap<String, List<String>> X_outcomes = X.getNamesAndOutcomes(network);
        HashMap<String, List<String>> Y_outcomes = Y.getNamesAndOutcomes(network);

        Set<String> X_names_set = X_outcomes.keySet();
        List<String> X_names = new ArrayList<>(X_names_set);
        Set<String> Y_names_set = Y_outcomes.keySet();
        List<String> Y_names = new ArrayList<>(Y_names_set);

        // get the names of all the variables that the factor will contain
        List<String> X_Y_names_intersection = UtilFunctions.intersection(X_names, Y_names); // ["C1", "C2", "C3"]
//        System.out.println("intersection: " + X_Y_names_intersection);

        // joined factor to return
        Factor result = new Factor(hidden,evidence, (ArrayList<String>) X_Y_names_intersection);

        ArrayList<Variable> vars = new ArrayList<>();
        for (String name: X_Y_names_intersection){
            Variable var = network.get(network.find(name));
            vars.add(var);
        }
        if (vars.size()>1) {
            ArrayList<HashMap<String, String>> perms = getPermsG(vars);

            for (HashMap<String, String> perm : perms) {
                for (int i = 0; i < X_Y_names_intersection.size(); i++) {
                    String name = X_Y_names_intersection.get(i);
                    perm.get(name);
                    HashMap<String, String> x_line = X.getLine(perm, X_Y_names_intersection);
                    HashMap<String, String> y_line = Y.getLine(perm, X_Y_names_intersection);

                    double u = Double.parseDouble(x_line.get("val"));
                    double v = Double.parseDouble(y_line.get("val"));
                    double r = u * v;
                    mul_Act++;
                    perm.put(name, String.valueOf(r));
                    result.addRow(perm);
                }
            }
        }

        System.out.println("\nRESULT AFTER JOIN:");
        System.out.println(result);
        System.out.println();

        return result;
    }

    public ArrayList<HashMap<String, String>> getPermsG(ArrayList<Variable> variables) {

        int numOfPerms = 1;
        for (Variable variable : variables) {
            numOfPerms *= variable.getOutcomes().size();
        }

        ArrayList<HashMap<String, String>> permutations = new ArrayList<>();
        int[] outcomesSizes = new int[variables.size()];
        int varSize = variables.size();

        for (int i = 0; i < varSize; i++) {
            Variable curr = variables.get(i);
            outcomesSizes[i] = curr.getOutcomes().size();
        }

        int[] outcomeSizes_new = new int[varSize];
        int m = outcomesSizes[0];
        outcomesSizes[0] = 0;
        int temp = outcomesSizes[1];
        outcomeSizes_new[1] = m;
        m = temp;

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

    public static Factor[] sortFactors(ArrayList<Factor> factors) {

        Factor[] sorted_factors = new Factor[factors.size()];
        for (int i = 0; i < factors.size(); i++) {
            sorted_factors[i] = factors.get(i);
        }

        // using bubble sort algorithm
        for (int i = 0; i < sorted_factors.length; i++) {
            for (int j = 0; j < sorted_factors.length - 1; j++) {
                if (CPTBuilder.CPTCompare(sorted_factors[j], sorted_factors[j + 1])) {

                    // swap factors
                    Factor temp = sorted_factors[j];
                    sorted_factors[j] = sorted_factors[j + 1];
                    sorted_factors[j + 1] = temp;
                }
            }
        }
        return sorted_factors;
    }

    public int getMul_Act() {
        return this.mul_Act;
    }

    public int getAdd_Act() {
        return this.add_Act;
    }

    public double getAnswer() {
        return this.answer;
    }
}
