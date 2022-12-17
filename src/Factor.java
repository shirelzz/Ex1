import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Factor {

    private String evidence;
    private HashMap<String,String>  variables;
    private ArrayList<HashMap<String, String>> factor;
    private HashMap<String, String> factorElem;

    Factor(HashMap<String,String> evidenceVars) {
        this.factor = new ArrayList<>();
        this.factorElem = new HashMap<>();
        this.variables = evidenceVars;
    }

    public void defFactor(ArrayList<HashMap<String,String>> permutations, ArrayList<String> values) {
        for (int i =0; i< permutations.size(); i++){
            HashMap<String,String> currRow = permutations.get(i);
            String val = values.get(i);
            currRow.put("val", val);
            factor.add(currRow);
        }
    }

    public void restrictFactor(Variable variable, String val) {
        for (int i = 0; i < factor.size(); i++) {
            HashMap<String, String> currRow = factor.get(i);
            if (currRow.get(variable.getName()).equals(val)) {
                factor.remove(currRow);
            }
        }

    }

    public void normFactor() {

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

    private boolean contains(String varName) {
        HashMap<String, String> currRow = factor.get(0);
        if (currRow.containsKey(varName)) {
            return true;
        } else {
            return false;
        }
    }

    public Factor sumOut(Variable variable) {
        Factor newFactor = new Factor(this.variables);
        for (int i = 0; i < factor.size(); i++) {
            HashMap<String,String> currRow = factor.get(i);
            double value = Double.parseDouble(currRow.get("val"));


        }
        return newFactor;
    }

    public void multiplyFactors() {

    }

    public void join() {

    }

    public void eliminateVariable() {


    }

    public void removeFactor() {

        //if there is 1 line

    }
}
