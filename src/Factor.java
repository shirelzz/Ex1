import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Factor {

    private ArrayList<String> names;
    private String main_name;
    private ArrayList<Variable> evidence;
    private ArrayList<Variable> hidden;
    private ArrayList<HashMap<String, String>> factor;


    /**
     * constructor
     * @param hidden list of the hidden variables
     * @param evidence list of the evidence variables
     * @param name list of the names of the variables in this factor
     */
    Factor(ArrayList<Variable> hidden, ArrayList<Variable> evidence, ArrayList<String> name) {
        this.names = name;
        this.factor = new ArrayList<>();
        this.evidence = evidence;
        this.hidden = hidden;
    }

    /**
     * constructor
     * @param f factor to be copied from
     */
    Factor(Factor f) {
        this.names = f.names;
        this.factor = f.factor;
        this.evidence = f.evidence;
        this.hidden = f.hidden;
    }

    /**
     *  define factor
     * @param permutations all the permutations of the variables in this factor
     */
    public void defFactor(ArrayList<HashMap<String, String>> permutations) {
        for (int i = 0; i < permutations.size(); i++) {
            HashMap<String, String> currRow = permutations.get(i);
            factor.add(currRow);
        }
    }

    /**
     * get a specific line of the factor
     * @param line line
     * @param names list of names in this line
     * @return the row in the factor equals to this line
     */
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

    /**
     * add a row to the factor
     * @param row the row we want to add
     */
    public void addRow(HashMap<String, String> row) {
        factor.add(row);
    }

    /**
     * remove all the rows in the factor that are irrelevant to the query
     * @param evidence the evidence variable
     * @param val the known outcome of the evidencce variable
     * @param names the names of the variables in this factor
     * @return a factor without the rows that are irrelevant
     */
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

    /**
     * normalizes the factor
     */
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

    /**
     * checks if the factor contains a specific variable
     * @param varName the name of the variable
     * @return true if the factor contains the variable or false if it doesn't
     */
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

    /**
     * sum out a (hidden) variable from the factor
     * @param variable the variable we want to sum out
     * @return a factor without that variable
     */
    public Factor sumOut(Variable variable) {
        ArrayList<String> name = new ArrayList<>();
        Factor newFactor = new Factor(this.hidden, this.evidence, name);

        if (factor.size() == 2) {
            return null;

        } else {

            int c = 0;
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
                        c++;
                        row_to_add.put("val", String.valueOf(values[l]));
                    }
                }
                variable.setCounter(c);

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

    /**
     * checks if this factor contains this line (except for the "val" key of each row)
     * @param line the line to be searched for in this factor
     * @return true if the factor contains this line or false if ot doesn't
     */
    public boolean containsPerm(HashMap<String, String> line) {
        String val = line.get("val");
        line.remove("val");
        boolean flag = false;
        if (factor.size() == 0) {
            flag = false;
        } else {
            for (int i = 0; i < factor.size(); i++) {
                HashMap<String, String> row = factor.get(i);
                String val_ = row.get("val");
                row.remove("val");
                if (row.equals(line)) {
                    flag = true;
                    row.put("val", val_);
                    line.put("val", val);
                    break;
                }
                row.put("val", val_);
            }
        }
        line.put("val", val);
        return flag;
    }

    /**
     * set factor names
     * @param names all the names of the variables that are in this factor
     */
    public void setNames(ArrayList<String> names) {
        this.names = names;
    }

    /**
     * checks if two lines are similar except for a specific variable key
     * @param variable the variable that its value is irrelevant to us
     * @return true if there's a resemblance or false if there isn't
     */
    public boolean resembling(HashMap<String, String> row, HashMap<String, String> line, Variable variable) {
        boolean flag = true;
        for (int i = 0; i < this.names.size(); i++) {
            String name = names.get(i);
            if (name.equals(variable.getName())) {
                continue;
            } else if (!row.get(name).equals(line.get(name))) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    /**
     * get to a specific line in this factor
     * @param i the index of this line
     * @return the line
     */
    public HashMap<String, String> get(int i) {
        return factor.get(i);
    }

    /**
     * @return number of lines in this factor
     */
    public int size() {
        return factor.size();
    }

    public void setMain_name(String name) {
        this.main_name = name;
    }

    /**
     * @return the names of the variables that are in this factor
     */
    public ArrayList<String> getNames() {
        return this.names;
    }

    /**
     * @param bn network
     * @return hashmap when the keys are the name of the variables that are in the factor
     * and the values are the outcomes of those variables
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

}
