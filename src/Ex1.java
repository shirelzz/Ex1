import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Ex1 {
    public static void main(String[] args) throws IOException, SAXException {

        BayesianNetwork network = new BayesianNetwork();
        XmlReader xmlReader = new XmlReader();
        network = xmlReader.buildNet("/Users/syrlzkryh/Documents/GitHub/Ex1/src/big_net.xml");
        network.printNet();

        String q = "P(B=T|J=T,M=T)";
//        String numerator = q.replace("|", ",");
        String denominator = "P(" +q.substring(6, q.length());
//        System.out.println(numerator);
//        System.out.println(denominator);

        String query = "P(B=T)";



//        String numeratorStr = q.replace("|", ",");  //P(B=T,J=T,M=T)
//        System.out.println(numeratorStr);
//        numeratorStr = numeratorStr.substring(2, numeratorStr.length()-1);
//        System.out.println(numeratorStr);
//        String[] numerator = numeratorStr.split(",");
//        for (int i = 0; i< numerator.length; i++){
//            System.out.println(numerator[i]);
//        }
        HashMap<String, String> evidenceVars = new HashMap<>();
//
//        for (int i = 0; i<numerator.length; i++){
//            String[] varName_outcome = numerator[i].split("=");
//            System.out.println(varName_outcome[0]);//e.g. [B,T]
//            System.out.println(varName_outcome[1]);//e.g. [B,T]
//
//            evidenceVars.put(varName_outcome[0], varName_outcome[1]);
//        }
//        System.out.println(evidenceVars);
//        System.out.println(evidenceVars.get("J"));
//        HashMap<String, String> vars = new HashMap<>();
//        vars.putAll(evidenceVars);
//        System.out.println(vars.get("B"));
//        vars.put("J","F");
//        System.out.println(vars);
        ArrayList<CptNode> hidden = new ArrayList<>();
        CptNode E = new CptNode();
        E.setName("E");
        E.addOutcome("T");
        E.addOutcome("F");
        hidden.add(E);

        CptNode P = new CptNode();
        P.setName("P");
        P.addOutcome("T");
        P.addOutcome("F");
        hidden.add(P);

        CptNode A = new CptNode();
        A.setName("A");
        A.addOutcome("T");
        A.addOutcome("F");
        hidden.add(A);


        HashMap<String, String> hiddenVars = new HashMap<>();
        hiddenVars.put("E", "T");
        hiddenVars.put("A", "T");

        ArrayList<HashMap<String,String>> permutions = new ArrayList<>();

        int numOfPerms = 8;
        int arr[] = new int[hidden.size()];
        for (int k = 0; k<numOfPerms; k++){
            HashMap<String, String> perm = new HashMap<>();
            for (int i=0; i<hidden.size(); i++){
                CptNode currHidden = hidden.get(i);
                String name = currHidden.getName();
                if (arr[i]<currHidden.getOutcomes().size()){
                    String outcome = currHidden.getOutcomes().get(arr[i]);
                    perm.put(name,outcome);

                    if (i==0){
                        arr[0]++;
                        if (arr[0] + 1 == hidden.get(0).getOutcomes().size()){
                            arr[0] = 0;
                        }
                    }
                    else if (i>0){
                        if (arr[i-1] +1  == hidden.get(i-1).getOutcomes().size()){
                            arr[i-1] = 0;
                        }
                        if (arr[i-1] +1 == hidden.get(i-1).getOutcomes().size()){
                            arr[i-1] = 0;
                        }

                        if (arr[i]+1<currHidden.getOutcomes().size()) {
                            arr[i]++;
                        }
                        else {
                            arr[i] = 0;
                        }
                    }
                }
            }
            System.out.println(perm);
            permutions.add(perm);
        }

    }



//        HashMap<String, String> all = new HashMap<>();
////        all.putAll(evidenceVars);
//        all.putAll(perm);
//        System.out.println(all);

//        Iterator<String> iterator = perm.keySet().iterator();

//        while (iterator.hasNext()){
//            String key = iterator.next();
//            System.out.println(key);   //name
//            System.out.println(perm.get(key));  //outcome
//        }
//        System.out.println(perm);

//        for (Map.Entry<String, String> entry : perm.entrySet()) {
//            entry.setValue("F");
//        }

//        for (Map.Entry<String, String> entry: perm.entrySet()){
//            String key = entry.getKey();
////            perm.put(key, "F");
//            System.out.println(entry.getKey());
//            System.out.println(entry.getValue());
//        }









//        answerQueries("input.txt");

    public static void answerQueries(String inputFileName){

        //input file
        String xmlFileName = "";
        String queries = "";
        ArrayList<String> questions = new ArrayList<>();

        try {
            File inputFile = new File(inputFileName);
            Scanner myReader = new Scanner(inputFile);
            xmlFileName = myReader.nextLine();

            while (myReader.hasNextLine()) {
                questions.add(myReader.nextLine());
//                queries = myReader.nextLine();
//                System.out.println(queries);
            }
            System.out.println(questions);

            System.out.println("");
            myReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // xml file
        XmlReader xmlReader = new XmlReader();
        BayesianNetwork network = xmlReader.buildNet("/Users/syrlzkryh/Documents/GitHub/Ex1/src/" + xmlFileName);
//        network.printNet();

        //answers
        String output = "";

        //1st algorithm: Bayes rule || Joint distribution
        for (String query : questions) {
            if (query.equals(null)){
                System.out.println("Error: null query");
            }
            else {
                int algoWanted = Integer.parseInt(query.substring(query.length()-1));
                String newQuery = query.substring(0,query.length()-1);
                if (algoWanted == 1){
                    Algorithms jd = new Algorithms(newQuery, network);
                    jd.addToHidden(newQuery);
                    jd.addToEvidence(newQuery);
                    jd.runAlgo(1);

                    output += jd.runAlgo(1) + "," + jd.getAddActions() + "," + jd.getMultiplyActions() + "\n";
                    System.out.println(query + ":" + "\n" + "Answer = " + jd.runAlgo(1) + "\n" +
                            "Total number of additions = " + jd.getAddActions() + "\n" +
                            "Total number of multiplications = " + jd.getMultiplyActions() +"\n" +
                            "hidden: " + jd.printHidden() +"\n" +
                            "evidence: " + jd.printEvidence() +"\n");
                }
                else if (algoWanted == 2){
                    Algorithms ve1 = new Algorithms(newQuery, network);
                    ve1.addToHidden(newQuery);
                    ve1.addToEvidence(newQuery);
                    ve1.runAlgo(2);

                    output += (ve1.getAnswer() + "," + ve1.getAddActions() + "," + ve1.getMultiplyActions()) + "\n";
                    System.out.println(query + ":" + "\n" + "Answer = " + ve1.getAnswer() + "\n" +
                            "Total number of additions = " + ve1.getAddActions() + "\n" +
                            "Total number of multiplications = " + ve1.getMultiplyActions() +"\n");
                }
            }

        }



        //2nd algorithm: variable elimination #1
//        output = "";
//        for (String query : questions) {
//            Algorithms ve1 = new Algorithms(query, network);
//            ve1.addToHidden(query);
//            ve1.addToEvidence(query);
//            ve1.runAlgo(2);
//
//            output += (ve1.getAnswer() + "," + ve1.getAddActions() + "," + ve1.getMultiplyActions()) + "\n";
//            System.out.println(query + ":" + "\n" + "Answer = " + ve1.getAnswer() + "\n" +
//                    "Total number of additions = " + ve1.getAddActions() + "\n" +
//                    "Total number of multiplications = " + ve1.getMultiplyActions() +"\n");
//
//        }



        //3rd algorithm: variable elimination #2
//        output = "";
//        for (String query : questions) {
//            Algorithms ve2 = new Algorithms(query, network);
//            output += (ve2.getAnswer() + "," + ve2.getAddActions() + "," + ve2.getMultiplyActions()) + "\n";
//            System.out.println(query + ":" + "\n" + "Answer = " + ve2.getAnswer() + "\n" +
//                    "Total number of additions = " + ve2.getAddActions() +"\n" +
//                    "Total number of multiplications = " + ve2.getMultiplyActions()+"\n");
//        }


        //extract output text file
        try {
            FileWriter myWriter = new FileWriter("output.txt");
            myWriter.write(output);
            myWriter.close();
            System.out.println("Kululu!!");

        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
