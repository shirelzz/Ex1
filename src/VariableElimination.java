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

    /**
     * constructor
     * @param evidence list of the evidence variables
     * @param hidden list of the hidden variables
     * @param variables list of all the variables
     * @param bn bayesian network
     * @param queryName_Outcome the name and the outcome of query variable
     */
    VariableElimination(ArrayList<Variable> evidence, ArrayList<Variable> hidden, ArrayList<Variable> variables, BayesianNetwork bn, String[] queryName_Outcome) {
        this.evidence = evidence;
        this.variables = variables;
        this.network = bn;
        this.hidden = sortByName(hidden);
        this.add_Act = 0;
        this.mul_Act = 0;
        this.queryName_Outcome = queryName_Outcome;
    }

    /**
     * sorts a list of variables by their names
     * @param list list of variables
     * @return the list sorted
     */
    public ArrayList<Variable> sortByName(ArrayList<Variable> list) {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            names.add(list.get(i).getName());
        }

        Collections.sort(names);
        list.removeAll(list);

        for (String name : names) {
            int index = network.find(name);
            list.add(network.get(index));
        }
        return list;
    }

    /**
     * variable elimination algorithm function
     *
     * @param queryVar the query variable
     * @param evidenceVars list of the evidence variables (that we got their outcome values by the query)
     * @param eliminationOrder the elimination order (as required in the input file)
     * @return the probability of this query
     */
    public double varElm(Variable queryVar, HashMap<String, String> evidenceVars, int eliminationOrder) {

        //define factors
        ArrayList<Factor> factors = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);
            ArrayList<String> names = new ArrayList<>();
            names.add(variable.getName());
            Factor factor = new Factor(hidden, evidence, names);
            factor.setMain_name(variable.getName());

            if (variable.hasParents()) {
                ArrayList<Variable> parents = variable.getParentNodes();
                ArrayList<String> parentsNames = variable.getParents();

                for (int n = 0; n < parentsNames.size(); n++) {
                    names.add(parentsNames.get(n));
                }
                factor.setNames(names);

                parents.add(variable); //add the child
                ArrayList<HashMap<String, String>> perms = getPermsG(parents);
                parents.remove(variable);
                ArrayList<String> values = new ArrayList<>();

                for (int p = 0; p < perms.size(); p++) {
                    HashMap<String, String> currRow = perms.get(p);
                    double value = UtilFunctions.getProbFromCPT(variable, currRow.get(variable.getName()), currRow);
                    String val = String.valueOf(value);
                    currRow.put("val", val);
                }
                factor.defFactor(perms);

            } else {
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
            if (evi.getName().equals(queryVar.getName())) {
                continue;
            }
            String outcome = evidenceVars.get(evi.getName());
            ArrayList<Factor> f_evi = getFactorsConVar(factors, evi);
            for (int j = 0; j < f_evi.size(); j++) {
                Factor factor = f_evi.get(j);
                factor.getNames().remove(evi.getName());
                factors.remove(factor);
                Factor r = factor.restrictFactor(evi, outcome, factor.getNames());
                if (r.size() > 1) {
                    factors.add(r);
                }
            }
        }

        //eliminate hidden variables
        for (int i = 0; i < hidden.size(); i++) {
            Variable hid = hidden.get(i);
            String hidName = hid.getName();

            ArrayList<Factor> f_hid = getFactorsConVar(factors, hid);
            if (eliminationOrder == 2) {  //algo = 2 , second algorithm
                if (f_hid.size() >= 2) {
                    Factor[] f_hidden = sortFactors(f_hid);

                    //multiply factors
                    for (int j = 0; j < f_hid.size() - 1; j++) {
                        Factor factor_1 = new Factor(f_hidden[j]);
                        Factor factor_2 = new Factor(f_hidden[j + 1]);

                        Factor factor_n = joinTwoFactors(factor_1, factor_2);
                        factors.remove(f_hidden[j]);
                        factors.remove(f_hidden[j + 1]);
                        f_hidden[j + 1] = factor_n;
                        ArrayList<String> names = new ArrayList<>();
                        names.addAll(factor_1.getNames());
                        names.addAll(factor_2.getNames());
                        names = UtilFunctions.removeDuplicates(names);
                        factor_n.setNames(names);
                        factors.add(factor_n);

                        if (j == f_hid.size() - 2) {
                            //sum out
                            factors.remove(factor_n);
                            factor_n = factor_n.sumOut(hid);
                            add_Act += hid.getCounter();
                            if (factor_n != null) {
                                ArrayList<String> names_ = new ArrayList<>();
                                names_.addAll(factor_1.getNames());
                                names_.addAll(factor_2.getNames());
                                names_ = UtilFunctions.removeDuplicates(names_);
                                names_.remove(hidName);
                                factor_n.setNames(names_);
                                factors.add(factor_n);
                            }
                        }
                    }

                } else {
                    ArrayList<String> f_names = f_hid.get(0).getNames();
                    f_names.remove(hidName);
                    factors.remove(f_hid.get(0));
                    Factor factor_to_add = f_hid.get(0).sumOut(hid);
                    add_Act+=hid.getCounter();
                    if (factor_to_add != null && factor_to_add.size()>1){
                        factor_to_add.setNames(f_names);
                        factors.add(factor_to_add);
                    }
                }
            }

        }

        //multiply all remaining factors
        if (factors.size() >= 2) { /////???????
            Factor[] sorted = sortFactors(factors);
            Factor f;

            for (int s = 0; s < sorted.length - 1; s++) {
                f = joinTwoFactors(sorted[s], sorted[s + 1]);

                if (s == sorted.length-2){
                    //normalize
                    f.normFactor();

                    //get answer
                    for (int l = 0; l < f.size(); l++) {
                        HashMap<String, String> line = f.get(l);
                        if (line.get(queryName_Outcome[0]).equals(queryName_Outcome[1])) {
                            answer = Double.parseDouble(line.get("val"));
                        }
                    }
                }
            }


        } else {
            Factor f = factors.get(0);

            //normalize
            f.normFactor();

            //get answer
            for (int l = 0; l < f.size(); l++) {
                HashMap<String, String> line = f.get(l);
                if (line.get(queryName_Outcome[0]).equals(queryName_Outcome[1])) {
                    answer = Double.parseDouble(line.get("val"));
                }
            }
        }
        return answer;
    }

    /**
     * joins two factors
     * @param X the first factor
     * @param Y the second factor
     * @return new factor that is a multiplication of X and Y
     */
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
        Factor result = new Factor(hidden, evidence, (ArrayList<String>) X_Y_names_intersection);
        if (X_Y_names_intersection.size() == 1) {
            result.setMain_name(X_Y_names_intersection.get(0));
        }

        ArrayList<Variable> all_vars = new ArrayList<>();
        ArrayList<String> all_vars_names = new ArrayList<>();
        for (int i = 0; i < X_names.size() + Y_names.size(); i++) {
            String name;
            Variable var;
            if (i < X_names.size()) {
                name = X_names.get(i);
            } else {
                name = Y_names.get(i - X_names.size());
            }
            if (!all_vars_names.contains(name)) {
                all_vars_names.add(name);
            }
            var = network.get(network.find(name));
            if (!all_vars.contains(var)) {
                all_vars.add(var);
            }
        }
        result.setNames(all_vars_names);

        ArrayList<Variable> vars = new ArrayList<>();
        for (String name : X_Y_names_intersection) {
            Variable var = network.get(network.find(name));
            vars.add(var);
        }

        if (all_vars.size() >= 1) {
            ArrayList<HashMap<String, String>> perms = getPermsG(all_vars);
            for (HashMap<String, String> perm : perms) {
                for (int i = 0; i < X_Y_names_intersection.size(); i++) {
                    String name = X_Y_names_intersection.get(i);
                    perm.get(name);
                    HashMap<String, String> x_line = X.getLine(perm, X_names);
                    HashMap<String, String> y_line = Y.getLine(perm, Y_names);

                    double u = Double.parseDouble(x_line.get("val"));
                    double v = Double.parseDouble(y_line.get("val"));
                    double r = u * v;
                    mul_Act++;
                    perm.put("val", String.valueOf(r));
                    result.addRow(perm);
                }
            }
        }
        return result;
    }

    /**
     *
     * @param variables list of variables
     * @return all the permutations on the variables
     */
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
        return permutations;
    }

    /**
     *
     * @param factors list of factors
     * @param variable the specific variable we want to find
     * @return all the factors containing a specific variable
     */
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

    /**
     * sorts a list of factors by size. If the factors are in the same size, it sorts by the ASCII code values
     * of the names of the variables in the factors
     *
     * @param factors list of factors
     * @return A sorted list of these factors
     */
    public Factor[] sortFactors(ArrayList<Factor> factors) {

        Factor[] sorted_factors = new Factor[factors.size()];
        for (int i = 0; i < factors.size(); i++) {
            sorted_factors[i] = factors.get(i);
        }

        // using bubble sort algorithm
        for (int i = 0; i < sorted_factors.length; i++) {
            for (int j = 0; j < sorted_factors.length - 1; j++) {
                if (sorted_factors[j + 1] == null) {
                    factors.remove(sorted_factors[j + 1]);
                }
                if (sorted_factors[j + 1] != null) {
                    if (CompareSize(sorted_factors[j], sorted_factors[j + 1])) {

                        // swap factors
                        Factor temp = sorted_factors[j];
                        sorted_factors[j] = sorted_factors[j + 1];
                        sorted_factors[j + 1] = temp;
                    }
                }
            }
        }
        return sorted_factors;
    }

    /**
     * compares the sizes of two factors. If X and Y are of same size it compares them by their ASCII values
     * @param X factor
     * @param Y factor
     * @return true if X is bigger (size/ASCII) than Y, otherwise it returns false.
     */
    public boolean CompareSize(Factor X, Factor Y) {
        if (X.size() > Y.size()) {
            return true;
        } else if (X.size() < Y.size()) {
            return false;
        } else {
            if (X.size() == 0) {
                return false;
            } else {
                // compare by ASCII values
                List<String> X_names_list = X.getNames();
                List<String> Y_names_list = Y.getNames();

                int X_names_ascii = 0;
                for (String name : X_names_list) {
                    for (int i = 0; i < name.length(); i++) {
                        X_names_ascii += name.charAt(i);
                    }
                }
                int Y_names_ascii = 0;
                for (String name : Y_names_list) {
                    for (int i = 0; i < name.length(); i++) {
                        Y_names_ascii += name.charAt(i);
                    }
                }
                return X_names_ascii >= Y_names_ascii;
            }
        }
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
