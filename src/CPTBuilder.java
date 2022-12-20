import java.util.*;

//this class would contain a several functions to build a CPT table for the bayesian network variables
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


    public static LinkedHashMap<String, Double> joinFactors(List<LinkedHashMap<String, Double>> cpt_to_join, Factor factorCounter) {

        LinkedHashMap<String, Double> factor = cpt_to_join.get(0);
        List<LinkedHashMap<String, Double>> new_cpt_to_join = new ArrayList<>();
        for (int i = 1; i < cpt_to_join.size(); i++) {
            new_cpt_to_join.add(cpt_to_join.get(i));
        }
        return joinFactors(new_cpt_to_join, factor, factorCounter);
    }

    private static LinkedHashMap<String, Double> joinFactors(List<LinkedHashMap<String, Double>> cpt_to_join, LinkedHashMap<String, Double> factor, Factor factorCounter) {

        if (cpt_to_join.isEmpty()) return factor;

        LinkedHashMap<String, Double> joined = new LinkedHashMap<>();
        /////////////////////////////////////////////////

        return joined;
    }


    public static List<String> getNames(LinkedHashMap<String, Double> factor) {
        List<String> names = new ArrayList<>();
//        LinkedHashMap<String, List<String>> names_and_outcomes = getNamesAndOutcomes(factor); //write this function
//        for (Map.Entry<String, List<String>> entry : names_and_outcomes.entrySet()) {
//            names.add(entry.getKey());
//        }
        return names;
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

