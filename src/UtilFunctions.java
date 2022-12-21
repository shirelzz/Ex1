import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class UtilFunctions {
    /**
     * this generic function returns a string of a given hashMap (for printing)
     *
     * @param hashmap - given hashmap
     * @return hashmap to string
     */
    public static <K, V> String hashMapToString(LinkedHashMap<K, V> hashmap) {
        if (hashmap.isEmpty()) return "";
        StringBuilder output = new StringBuilder();
        hashmap.forEach((key, value) -> {
            output.append(key);
            output.append(" : ");
            output.append(value);
            output.append("\n");
        });
        return output.toString();
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

    /**
     * @param string given string
     * @return list of the string split by commas (",")
     */
    public static List<String> separateByCommas(String string) {
        return new ArrayList<>(Arrays.asList(string.split(",")));
    }

    /**
     * this function get a factor and return a new factor with without duplicates values
     * for example if our input factor will be:
     * K=T,A=T,MANGO=TASTY,B=T,C=T,G=F : 0.1
     * K=T,A=T,MANGO=TASTY,B=F,C=T,G=F : 0.2
     * K=T,A=F,MANGO=TASTY,B=T,C=T,G=F : 0.3
     * K=T,A=F,MANGO=TASTY,B=F,C=T,G=F : 0.4
     * the output factor will be:
     * A=T,B=T, : 0.1
     * A=T,B=F, : 0.2
     * A=F,B=T, : 0.3
     * A=F,B=F, : 0.4
     *
     * @param factor input factor
     * @return output factor without duplicate values
     */
    public static LinkedHashMap<String, Double> fixingDuplicatesValuesInKeys(LinkedHashMap<String, Double> factor) {

        // result factor
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();

        if(factor.size() == 1 && CPTBuilder.getNames(factor).size() == 1) {
            return factor;
        }

        // all outcomes linked hash map in factor
        LinkedHashMap<String, List<String>> outcomes = CPTBuilder.getNamesAndOutcomes(factor);

        if (outcomes.size() == 0) {
            return result;
        }

        if (outcomes.size() == 1) {
            for (Map.Entry<String, List<String>> entry : outcomes.entrySet()) {
                if (entry.getValue().size() == 1) {
                    return result;
                }
            }
        }

        // list with the un welcome values - that we want to delete
        List<String> unWelcomeValues = new ArrayList<>();

        // for each outcome in outcomes if outcome of a values has only one values - we want to delete it
        for (Map.Entry<String, List<String>> entry : outcomes.entrySet()) {
            if (entry.getValue().size() == 1) {
                String value = entry.getKey() + "=" + entry.getValue().get(0);
                unWelcomeValues.add(value);
            }
        }

        for (Map.Entry<String, Double> entry : factor.entrySet()) {
            StringBuilder new_key = new StringBuilder();
            List<String> new_key_split = separateByCommas(entry.getKey());

            for (String key : new_key_split) {

                // create new key for the result linked hash map
                if (!unWelcomeValues.contains(key)) {
                    new_key.append(key).append(",");
                }
            }
            result.put(new_key.substring(0, new_key.length() - 1), entry.getValue());
        }

        return result;
    }

    /**
     * @param d a double value
     * @return the double value only with 6 decimal places after the decimal point
     */
    public static double roundFiveDecimalPlaces(double d) {
        BigDecimal bigDecimal = new BigDecimal(Double.toString(d));
        bigDecimal = bigDecimal.setScale(5, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
}

