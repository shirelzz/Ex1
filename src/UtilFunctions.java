import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class UtilFunctions {

    /**
     *
     * @param names list of strings
     * @param factors list of factors
     * @return the factor with the same name list as names
     */
    public static Factor find(ArrayList<String> names, ArrayList<Factor> factors){
        for (Factor factor: factors){
            if (factor.getNames().equals(names)){
                return factor;
            }
        }

        return null;
    }

    /**
     *
     * @param list list of strings
     * @return the list without the duplicates
     */
    public static ArrayList<String> removeDuplicates(ArrayList<String> list){
        ArrayList<String> new_list = new ArrayList<>();
        for (int i = 0; i<list.size(); i++){
            String str = list.get(i);
            if (i==0){
                new_list.add(str);
            }
            else if (!new_list.contains(str)){
                new_list.add(str);
            }
        }
        return new_list;
    }

    /**
     *
     * @param queryVar the query variable
     * @param queryRequestedOutcome the outcome of the query variable
     * @param evidenceVars all the variables in the query
     * @return the answer to the query
     */
    public static double getProbFromCPT(Variable queryVar, String queryRequestedOutcome, HashMap<String, String> evidenceVars) {
        double ans = 0;
        String outcome = "";

        if (queryVar.hasParents()) { //e.g. P(A=T|E=T,B=F)=?
            Variable variable = queryVar;
            ArrayList<Variable> queryParents = queryVar.getParentNodes();

            if (evidenceVars.size() -1  == queryParents.size()) {
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
            return ans;
        }
        return ans;
    }

    /**
     * @param X   list of elements
     * @param Y   list of elements
     * @param <T> some value that X and Y are fill with
     * @return intersection of X and Y lists
     */
    public static <T> List<T> intersection(List<T> X, List<T> Y) {
        List<T> result = new ArrayList<>();
        if (X.isEmpty() && Y.isEmpty()) return result;
        else if (X.isEmpty()) return Y;
        else if (Y.isEmpty()) return X;
        else for (T x : X) if (Y.contains(x)) result.add(x);
        return result;
    }

    public static double roundFiveDecimalPlaces(double d) {
        BigDecimal bigDecimal = new BigDecimal(Double.toString(d));
        bigDecimal = bigDecimal.setScale(5, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

}

