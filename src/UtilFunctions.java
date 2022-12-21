import java.util.*;

public class UtilFunctions {

    public static double getProbFromCPT(Variable queryVar, String queryRequestedOutcome, HashMap<String, String> evidenceVars) {
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
     * @param keys is a key string from a CPT table, for example: "A=T,B=F,C=v1"
     * @return hashmap when the keys are the variables name ("A", "B", "C") and the values of them are the outcomes ("T", "F", "v1")
     */
    public static LinkedHashMap<String, String> splitKeysToVariablesAndOutcomes(String keys) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        String[] keys_split = keys.split(",");
        for (String key : keys_split) {
            String[] key_split = key.split("=");
            result.put(key_split[0], key_split[1]);
        }
        return result;
    }

    /**
     * @param X   list of elements
     * @param Y   list of elements
     * @param <T> some value that X and Y are fill with
     * @return union of X and Y lists
     */
    public static <T> List<T> union(List<T> X, List<T> Y) {
        Set<T> result = new HashSet<>();
        result.addAll(X);
        result.addAll(Y);
        return new ArrayList<>(result);
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

    /**
     * @param list of strings
     * @return string of the list strings seperated by commas
     */
    public static String combineWithCommas(List<String> list) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            result.append(list.get(i));
            if (i != list.size() - 1) result.append(",");
        }
        return result.toString();
    }


    public static List<String> separateByCommas(String string) {
        return new ArrayList<>(Arrays.asList(string.split(",")));
    }

}

