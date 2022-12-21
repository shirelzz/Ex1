
import java.util.*;

public class CPTBuilder {

    public static LinkedHashMap<String, Double> buildCPTLinkedHashMap(double[] values, List<List<String>> outcomes, List<String> names) {
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();

        System.out.println("values: " + Arrays.toString(values));
        for (List<String> o : outcomes) {
            System.out.println("outcome: " + o);
        }
        System.out.println("names: " + names);


        String[] outputs = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            outputs[i] = "";
        }

        int exp = values.length;
        for (int i = 0; i < outcomes.size(); i++) {
            List<String> o = outcomes.get(i);
            exp = exp / o.size();
            int k = 0, sum = 0;
            for (int j = 0; j < values.length; j++) {
                sum++;
                outputs[j] += o.get(k);
                if (i != outcomes.size() - 1) outputs[j] += ",";
                if (sum >= exp) {
                    k++;
                    sum = 0;
                    if (k >= o.size()) {
                        k = 0;
                    }
                }
            }
        }

        List<String> keys = new ArrayList<>();
        for (String output : outputs) {
            String[] split_key = output.split(",");
            StringBuilder key_line = new StringBuilder();
            for (int j = 0; j < split_key.length; j++) {
                String key = names.get(j) + "=" + split_key[j];
                key_line.append(key);
                if (j != split_key.length - 1) key_line.append(",");
            }
            keys.add(key_line.toString());
        }

        for (int i = 0; i < values.length; i++) {
            result.put(keys.get(i), values[i]);
        }

        return result;
    }

    public static LinkedHashMap<String, Double> joinFactors(List<LinkedHashMap<String, Double>> cpt_to_join, FactorCounter factorCounter) {

        LinkedHashMap<String, Double> factor = cpt_to_join.get(0);
        List<LinkedHashMap<String, Double>> new_cpt_to_join = new ArrayList<>();
        for (int i = 1; i < cpt_to_join.size(); i++) {
            new_cpt_to_join.add(cpt_to_join.get(i));
        }
        return joinFactors(new_cpt_to_join, factor, factorCounter);
    }

    private static LinkedHashMap<String, Double> joinFactors(List<LinkedHashMap<String, Double>> cpt_to_join, LinkedHashMap<String, Double> factor, FactorCounter factorCounter) {

        if (cpt_to_join.isEmpty()) return factor;

        cpt_to_join.add(factor);
        cpt_to_join = sortFactors(cpt_to_join);

        factor = cpt_to_join.get(0);
        factor = joinTwoFactors(factor, cpt_to_join.get(1), factorCounter);
        factor = UtilFunctions.fixingDuplicatesValuesInKeys(factor);

        List<LinkedHashMap<String, Double>> new_cpt_to_join = new ArrayList<>();
        for (int i = 2; i < cpt_to_join.size(); i++) {
            new_cpt_to_join.add(cpt_to_join.get(i));
        }

        return joinFactors(new_cpt_to_join, factor, factorCounter);
    }


    public static LinkedHashMap<String, Double> joinTwoFactors(LinkedHashMap<String, Double> X, LinkedHashMap<String, Double> Y, FactorCounter factorCounter) {

        System.out.println("//////////////// JOIN //////////////////////");
        System.out.println("X:");
        System.out.println(UtilFunctions.hashMapToString(X));
        System.out.println("Y:");
        System.out.println(UtilFunctions.hashMapToString(Y));
        System.out.println("////////////////////////////////////////////");

        // get the outcome hashmaps for X and Y
        HashMap<String, List<String>> X_outcomes = getNamesAndOutcomes(X);
        HashMap<String, List<String>> Y_outcomes = getNamesAndOutcomes(Y);

        Set<String> X_names_set = X_outcomes.keySet();
        List<String> X_names = new ArrayList<>(X_names_set);
        Set<String> Y_names_set = Y_outcomes.keySet();
        List<String> Y_names = new ArrayList<>(Y_names_set);

        // get the names of all the variables that the factor will contain
        List<String> X_Y_names_intersection = UtilFunctions.intersection(X_names, Y_names); // ["C1", "C2", "C3"]
//        System.out.println("intersection: " + X_Y_names_intersection);

        // joined factor to return
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();

        for (Map.Entry<String, Double> y : Y.entrySet()) {
            LinkedHashMap<String, String> values_of_line = UtilFunctions.splitKeysToVariablesAndOutcomes(y.getKey());
            List<String> values_of_intersection_variables = new ArrayList<>();

            for (String name : X_Y_names_intersection) {
                values_of_intersection_variables.add(name + "=" + values_of_line.get(name));
            }
            for (Map.Entry<String, Double> x : X.entrySet()) {
                boolean b = true;

                for (String name : values_of_intersection_variables) {
                    if (!x.getKey().contains(name)) {
                        b = false;
                        break;
                    }
                }
                if (b) {

                    double u = y.getValue();
                    double v = x.getValue();
                    double r = u * v;

                    String[] Y_split = y.getKey().split(",");
                    List<String> Y_split_list = new ArrayList<>();
                    Collections.addAll(Y_split_list, Y_split);

                    String[] X_split = x.getKey().split(",");
                    List<String> X_split_list = new ArrayList<>();
                    Collections.addAll(X_split_list, X_split);

                    List<String> new_key_split = UtilFunctions.union(X_split_list, Y_split_list);
                    Collections.sort(new_key_split);
                    String new_key = UtilFunctions.combineWithCommas(new_key_split);
                    result.put(new_key, r);
                }
            }
        }

        System.out.println("\nRESULT AFTER JOIN:");
        System.out.println(UtilFunctions.hashMapToString(result));
        System.out.println();

        factorCounter.mulAdd(result.size());

        return result;
    }

    public static LinkedHashMap<String, List<String>> getNamesAndOutcomes(LinkedHashMap<String, Double> cpt) {

        LinkedHashMap<String, List<String>> outcomes = new LinkedHashMap<>();

        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Double> line : cpt.entrySet()) {
            LinkedHashMap<String, String> line_split = UtilFunctions.splitKeysToVariablesAndOutcomes(line.getKey());
            for (Map.Entry<String, String> inner : line_split.entrySet()) {
                names.add(inner.getKey());
            }
            break;
        }

        for (String name : names) {
            outcomes.put(name, new ArrayList<>());
        }

        for (Map.Entry<String, Double> line : cpt.entrySet()) {
            LinkedHashMap<String, String> line_split = UtilFunctions.splitKeysToVariablesAndOutcomes(line.getKey());
            for (Map.Entry<String, String> inner : line_split.entrySet()) {
                if (!outcomes.get(inner.getKey()).contains(inner.getValue())) {
                    outcomes.get(inner.getKey()).add(inner.getValue());
                }
            }
        }
        return outcomes;
    }

    public static List<String> getNames(LinkedHashMap<String, Double> factor) {
        List<String> names = new ArrayList<>();
        LinkedHashMap<String, List<String>> names_and_outcomes = getNamesAndOutcomes(factor);
        for (Map.Entry<String, List<String>> entry : names_and_outcomes.entrySet()) {
            names.add(entry.getKey());
        }
        return names;
    }


    public static LinkedHashMap<String, Double> eliminate(LinkedHashMap<String, Double> factor, Variable hidden, FactorCounter factorCounter) {

        LinkedHashMap<String, Double> result = new LinkedHashMap<>();

        List<String> names = getNames(factor);

        if (names.size() <= 1) return result;

        // build list with all the outcomes of hidden and his name, for example: {"A=T", "A=F"}
        List<String> outcomes = hidden.getOutcomes();
        List<String> values = new ArrayList<>();
        for (String outcome : outcomes) {
            values.add(hidden.getName() + "=" + outcome);
        }

        for (Map.Entry<String, Double> y : factor.entrySet()) {
            for (String value : values) {
                if (y.getKey().contains(value)) {

                    // build the new key without the value
                    List<String> split_new_key = new ArrayList<>(Arrays.asList(y.getKey().split(",")));
                    split_new_key.remove(value);
                    String new_key = UtilFunctions.combineWithCommas(split_new_key);

                    for (Map.Entry<String, Double> x : factor.entrySet()) {

                        boolean b = true;
                        List<String> new_key_values = UtilFunctions.separateByCommas(new_key);
                        for (String new_key_value : new_key_values) {
                            if (!x.getKey().contains(new_key_value)) {
                                b = false;
                                break;
                            }
                        }

                        if (x.getKey().equals(y.getKey())) {
                            b = false;
                        }

                        if (b) {
                            double u = y.getValue();
                            double v = x.getValue();
                            double r = u + v;

                            if (!result.containsKey(new_key)) {
                                factorCounter.sumAdd(1);
                                result.put(new_key, r);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }


    public static List<LinkedHashMap<String, Double>> sortFactors(List<LinkedHashMap<String, Double>> factors) {

        LinkedHashMap<String, Double>[] sorted_factors = new LinkedHashMap[factors.size()];
        for (int i = 0; i < factors.size(); i++) {
            sorted_factors[i] = factors.get(i);
        }
//        List<LinkedHashMap<String, Double>> sorted_factors = new ArrayList<>(factors);

        // using bubble sort algorithm
        for (int i = 0; i < sorted_factors.length; i++) {
            for (int j = 0; j < sorted_factors.length - 1; j++) {
                if (CPTCompare(sorted_factors[j], sorted_factors[j + 1])) {

                    // swap factors
                    LinkedHashMap<String, Double> temp = sorted_factors[j];
                    sorted_factors[j] = sorted_factors[j + 1];
                    sorted_factors[j + 1] = temp;

                }
            }
        }

        return new ArrayList<>(Arrays.asList(sorted_factors));
    }


    private static boolean CPTCompare(LinkedHashMap<String, Double> X, LinkedHashMap<String, Double> Y) {
        if (X.size() < Y.size()) {
            return false;
        } else if (X.size() > Y.size()) {
            return true;
        } else {
            // compare by ASCII values
            List<String> X_names_list = getNames(X);
            List<String> Y_names_list = getNames(Y);

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
