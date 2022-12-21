
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

    public static Factor[] sortFactors(ArrayList<Factor> factors) {

        Factor[] sorted_factors = new Factor[factors.size()];
        for (int i = 0; i < factors.size(); i++) {
            sorted_factors[i] = factors.get(i);
        }

        // using bubble sort algorithm
        for (int i = 0; i < sorted_factors.length; i++) {
            for (int j = 0; j < sorted_factors.length - 1; j++) {
                if (CPTCompare(sorted_factors[j], sorted_factors[j + 1])) {

                    // swap factors
                    Factor temp = sorted_factors[j];
                    sorted_factors[j] = sorted_factors[j + 1];
                    sorted_factors[j + 1] = temp;
                }
            }
        }
        return sorted_factors;
    }

    public static boolean CPTCompare(Factor X, Factor Y) {
        if (X.size() < Y.size()) {
            return false;
        } else if (X.size() > Y.size()) {
            return true;
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
