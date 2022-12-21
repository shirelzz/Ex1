import java.util.ArrayList;
import java.util.HashMap;

public class Factor {

    private ArrayList<String> name;
    private ArrayList<Variable> evidence;
    private ArrayList<Variable> hidden;
    private HashMap<String, String> variables;
    private ArrayList<HashMap<String, String>> factor;
    private HashMap<String, String> factorElem;


    Factor(ArrayList<Variable> hidden, ArrayList<Variable> evidence, ArrayList<String> name) {
        this.name = name;
        this.factor = new ArrayList<>();
        this.factorElem = new HashMap<>();
        this.evidence = evidence;
        this.hidden = hidden;
    }

    public void defFactor(ArrayList<HashMap<String, String>> permutations, ArrayList<String> values) {
        for (int i = 0; i < permutations.size(); i++) {
            HashMap<String, String> currRow = permutations.get(i);
            String val = values.get(i); //how to get values??
            currRow.put("val", val);
            factor.add(currRow);
        }
    }

    public void addRow(HashMap<String, String> row) {
        factor.add(row);
    }

    public void restrictFactor(Variable evidence, String val) {
        for (int i = 0; i < factor.size(); i++) {
            HashMap<String, String> currRow = factor.get(i);
            if (!currRow.get(evidence.getName()).equals(val)) {
                factor.remove(currRow);
            }
        }

    }

    public void normFactor() {
        double sum = 0;
        double alpha = 0;

        for (int i = 0; i<factor.size(); i++){
            HashMap<String,String> row = factor.get(i);
            String value = row.get("val");
            sum += Double.parseDouble(value);
        }

        alpha = 1/sum;

        for (int i = 0; i<factor.size(); i++){
            HashMap<String,String> row = factor.get(i);
            double rowVal = Double.parseDouble(row.get("val"));
            double value = alpha * rowVal;
            String val = String.valueOf(value);
            row.put("val", val);
        }
    }

    //get all the factors containing a specific variable
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

    public boolean contains(String varName) {
        HashMap<String, String> currRow = factor.get(0);
        if (currRow.containsKey(varName)) {
            return true;
        } else {
            return false;
        }
    }

    public Factor sumOut(Variable variable) {
        ArrayList<String> name = new ArrayList<>();
        Factor newFactor = new Factor(this.hidden, this.evidence, name);
        double[] values = new double[factor.size()];

        for (int i = 0; i < factor.size(); i++) {
            HashMap<String, String> currRow = factor.get(i);
            currRow.remove(variable.getName());
            double value1 = Double.parseDouble(currRow.get("val"));
            values[i] += value1;

            for (int j = i; j < factor.size(); j++) {
                HashMap<String, String> row = factor.get(j);
                row.remove(variable.getName());
                if (resembling(row, currRow, variable)) {
                    double value2 = Double.parseDouble(row.get("val"));
                    values[i] += value2;
                    currRow.put("val", String.valueOf(values[i]));
                }
            }
            newFactor.addRow(currRow);
        }
        return newFactor;
    }

    public void setName(ArrayList<String> name){
        this.name = name;
    }


    public boolean resembling(HashMap<String, String> row, HashMap<String, String> currRow, Variable variable) {
        boolean flag = true;
        for (int v = 0; v < evidence.size(); v++) {  //evidence or hidden?
            Variable currVar = evidence.get(v);
            String name = currVar.getName();
            if (name.equals(variable.getName())) {
                continue;
            } else if (!row.get(name).equals(currRow.get(name))) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public Factor multiplyFactors(Factor f2) {
        ArrayList<String> newName = f2.name;
//        if (this.name.length() >= f2.getName().length()){
//            newName = this.name;
//        }
//        else {
//            newName = f2.getName();
//        }

        Factor newFactor = new Factor(hidden,evidence, newName);
        ArrayList<Variable> variables = new ArrayList<>(); //the variables of f1 and f2
        ArrayList<HashMap<String,String>> perms = getPermsG(variables);
        double[] values = new double[perms.size()];

        for (int p = 0; p<perms.size(); p++){
            HashMap<String,String> perm = perms.get(p);
            newFactor.addRow(perm);
            double value;

            for (int i = 0; i<evidence.size(); i++){ //evidence/ size?
                HashMap<String,String> row = factor.get(i);
                Variable variable = evidence.get(i);
                String varName = variable.getName();
                if (resemble(row,perm)){
                    value = Double.parseDouble(row.get(varName));
                    values[p] *= value;
                }
            }

            for (int i = 0; i<f2.getEvidence().size(); i++){
                HashMap<String,String> row = factor.get(i);
                Variable variable = f2.getEvidence().get(i);
                String varName = variable.getName();
                if (resemble(row,perm)){
                    value = Double.parseDouble(row.get(varName));
                    values[p] *= value;
                }
            }

            String valStr = String.valueOf(values[p]);
            perm.put("val", valStr);
            newFactor.addRow(perm);
        }
        return newFactor;
    }

    public boolean resemble(HashMap<String,String> row, HashMap<String,String> perm){
        boolean flag = true;
        for (int v = 0; v < evidence.size(); v++) {  //evidence or hidden?
            Variable currVar = evidence.get(v);
            String name = currVar.getName();
            if (perm.containsKey(name) && row.containsKey(name)){
                if (!row.get(name).equals(perm.get(name))) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }

    public HashMap<String, String> get(int i) {
        return factor.get(i);
    }

    public ArrayList<Variable> getEvidence() {
        return evidence;
    }

    public ArrayList<Variable> getHidden() {
        return hidden;
    }

    public void join() {

    }

    public void eliminateVariable() {


    }

    public void removeFactor() {

        //if there is 1 line
    }

    public int size() {
        return factor.size();
    }

    public ArrayList<String> getName() {
        return this.name;
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
}
