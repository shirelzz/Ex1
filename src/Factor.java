import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Factor {

    private ArrayList<String> names;
    private String main_name;
    private ArrayList<Variable> evidence;
    private ArrayList<Variable> hidden;
    //    private HashMap<String, String> variables;
    private ArrayList<HashMap<String, String>> factor;


    Factor(ArrayList<Variable> hidden, ArrayList<Variable> evidence, ArrayList<String> name) {
        this.names = name;
        this.factor = new ArrayList<>();
        this.evidence = evidence;
        this.hidden = hidden;
    }

    Factor(Factor f) {
        this.names = f.names;
        this.factor = f.factor;
        this.evidence = f.evidence;
        this.hidden = f.hidden;
    }

    public Factor find(String main_name, ArrayList<Factor> factors) {
        for (Factor factor : factors) {
            if (factor.main_name.equals(main_name)) {
                return factor;
            }
        }
        return null;
    }

    public void defFactor(ArrayList<HashMap<String, String>> permutations) {
        for (int i = 0; i < permutations.size(); i++) {
            HashMap<String, String> currRow = permutations.get(i);
            factor.add(currRow);
        }
    }

    public HashMap<String, String> getLine(HashMap<String, String> line, List<String> names) {
        for (int i = 0; i < factor.size(); i++) {
            HashMap<String, String> row = factor.get(i);
            for (int j = 0; j < names.size(); j++) {
                String name = names.get(j);
                if (!row.get(name).equals(line.get(name))) {
                    break;
                }
                if (j == names.size() - 1) {
                    return row;
                }
            }
        }
        return null;
    }

    public void addRow(HashMap<String, String> row) {
        factor.add(row);
    }

    public Factor restrictFactor(Variable evidence, String val, ArrayList<String> names) {
        Factor f = new Factor(this);
        Factor factor_to_return = new Factor(this.hidden, this.evidence, names);
        factor_to_return.setMain_name(evidence.getName());
        String evi_name = evidence.getName();

        for (int i = 0; i < f.size(); i++) {
            HashMap<String, String> currRow = f.get(i);
            if (currRow.get(evi_name).equals(val)) {
                currRow.remove(evi_name);
                factor_to_return.addRow(currRow);
            }
        }
        return factor_to_return;
    }

    public void normFactor() {
        double sum = 0;
        double alpha = 0;

        for (int i = 0; i < factor.size(); i++) {
            HashMap<String, String> row = factor.get(i);
            String value = row.get("val");
            sum += Double.parseDouble(value);
        }

        alpha = 1 / sum;

        for (int i = 0; i < factor.size(); i++) {
            HashMap<String, String> row = factor.get(i);
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
        if (factor.size() > 0) {
            HashMap<String, String> currRow = factor.get(0);
            if (currRow.containsKey(varName)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public Factor sumOut(Variable variable) {
        ArrayList<String> name = new ArrayList<>();
        Factor newFactor = new Factor(this.hidden, this.evidence, name);

        //?
        if (factor.size() == 2) {
            return null;
//            newFactor.setMain_name(variable.getName());
//            HashMap<String, String> new_row = new HashMap<>();
//            new_row.put("val", "1.0");
//            newFactor.addRow(new_row);
        } else {

            double[] values = new double[factor.size() / variable.getOutcomes().size()];
            HashMap<String, String> row_to_add;

            int l = 0;
            for (int i = 0; i < factor.size() - 1; i++) {
                HashMap<String, String> currRow = factor.get(i);
                if (newFactor.containsPerm(currRow)) {
                    continue;
                }
                currRow.remove(variable.getName());
                row_to_add = currRow;
                double value1 = Double.parseDouble(currRow.get("val"));
                if (l >= values.length) {
                    l = 0;
                }
                values[l] += value1;

                for (int j = i + 1; j < factor.size(); j++) {
                    HashMap<String, String> row = factor.get(j);
                    row.remove(variable.getName());
                    if (resembling(row, currRow, variable)) {
                        double value2 = Double.parseDouble(row.get("val"));
                        if (l >= values.length) {
                            l = 0;
                        }
                        values[l] += value2;
                        row_to_add.put("val", String.valueOf(values[l]));
                    }
                }
                if (i == 0) {
                    newFactor.addRow(row_to_add);
                } else if (!newFactor.containsPerm(row_to_add)) {
                    newFactor.addRow(row_to_add);
                }
                l++;
            }
        }
        return newFactor;
    }

    private boolean containsPerm(HashMap<String, String> row_to_add) {
        String val = row_to_add.get("val");
        row_to_add.remove("val");
        boolean flag = false;
        if (factor.size() == 0) {
            flag = false;
        } else {
            for (int i = 0; i < factor.size(); i++) {
                HashMap<String, String> row = factor.get(i);
                String val_ = row.get("val");
                row.remove("val");
                if (row.equals(row_to_add)) {
                    flag = true;
                    row.put("val", val_);
                    row_to_add.put("val", val);
                    break;
                }
                row.put("val", val_);
            }
        }
        row_to_add.put("val", val);
        return flag;
    }

    public void setNames(ArrayList<String> names) {
        this.names = names;
    }

    public boolean resembling(HashMap<String, String> row, HashMap<String, String> currRow, Variable variable) {
        boolean flag = true;
        for (int i = 0; i < this.names.size(); i++) {
            String name = names.get(i);
            if (name.equals(variable.getName())) {
                continue;
            } else if (!row.get(name).equals(currRow.get(name))) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public boolean resemble(HashMap<String, String> row, HashMap<String, String> perm) {
        boolean flag = true;
        for (int v = 0; v < evidence.size(); v++) {  //evidence or hidden?
            Variable currVar = evidence.get(v);
            String name = currVar.getName();
            if (perm.containsKey(name) && row.containsKey(name)) {
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

    public int size() {
        return factor.size();
    }

    public void setMain_name(String name) {
        this.main_name = name;
    }

    public String getMain_name() {
        return this.main_name;
    }

    public ArrayList<String> getNames() {
        return this.names;
    }

    /**
     * @param bn network
     * @return hashmap when the keys are the name of the variables that are in the factor and the values are the outcomes of those variables
     */
    public HashMap<String, List<String>> getNamesAndOutcomes(BayesianNetwork bn) {
        HashMap<String, List<String>> names_outcomes = new HashMap<>();
        ArrayList<String> names = this.getNames();
        for (String name : names) {
            ArrayList<String> outcomes = new ArrayList<>();
            int index = bn.find(name);
            Variable variable = bn.get(index);
            outcomes = variable.getOutcomes();
            names_outcomes.put(name, outcomes);
        }
        return names_outcomes;
    }

    public boolean isEmpty() {
        return this.factor.size() > 1;
    }

}
