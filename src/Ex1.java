import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;

public class Ex1 {
    public static void main(String[] args) throws IOException, SAXException {

////        BayesianNetwork network = new BayesianNetwork();
////        XmlReader xmlReader = new XmlReader();
////        network = xmlReader.buildNet("/Users/syrlzkryh/Documents/GitHub/Ex1/src/big_net.xml");
////        network.printNet();
//
//        String q = "P(J=T|B=T)";
//
//        HashMap<String, String> evidenceVars = new HashMap<>();
//
//        ArrayList<Variable> hidden = new ArrayList<>();
//        Variable E = new Variable();
//        E.setName("E");
//        E.addOutcome("T");
//        E.addOutcome("F");
//        hidden.add(E);
//
//        Variable P = new Variable();
//        P.setName("M");
//        P.addOutcome("T");
//        P.addOutcome("F");
//        hidden.add(P);
//
//        Variable A = new Variable();
//        A.setName("A");
//        A.addOutcome("T");
//        A.addOutcome("F");
//        hidden.add(A);
//
//        int numOfPerms = 8;
//
//        //start func
//
//        ArrayList<HashMap<String, String>> permutations = new ArrayList<>();
//
//        int[] outcomesSizes = new int[hidden.size()];
//        int hiddenSize = hidden.size();
//
//        for (int i = 0; i < hiddenSize; i++) {
//            Variable curr = hidden.get(i);
//            outcomesSizes[i] = curr.getOutcomes().size();
//        }
//
//        int m = outcomesSizes[0];
//        int temp = outcomesSizes[1];
//        outcomesSizes[1] = m;
//        m = temp;
//        outcomesSizes[0] = 0;
//
//        for (int i = 2; i < outcomesSizes.length; i++) {
//            m *= outcomesSizes[i];
//            outcomesSizes[i] = m;
//        }
//
//        String name;
//        String outcome;
//        int[] outcomes = new int[hiddenSize];
//
//        for (int i = 0; i < numOfPerms; i++) {
//            HashMap<String, String> perm = new HashMap<>();
//            for (int j = 0; j < hiddenSize; j++) {
//                Variable currHidden = hidden.get(j);
//                name = currHidden.getName();
//                int numOfOutcomes = currHidden.getOutcomes().size();
//                if (outcomes[j] >= numOfOutcomes) {
//                    outcomes[j] = 0;
//                }
//                outcome = currHidden.getOutcomes().get(outcomes[j]);
//                if (j == 0) {
//                    outcomes[j]++;
//                } else {
//                    if ((i % outcomesSizes[j] == 0) && (i != 0)) {
//                        if (outcomes[j] + 1 >= numOfOutcomes) {
//                            outcomes[j] = 0;
//                        } else {
//                            outcomes[j]++;
//                        }
//                        outcome = currHidden.getOutcomes().get(outcomes[j]);
//                    }
//                }
//                perm.put(name, outcome);
//
//            }
//            if (!permutations.contains(perm)) {
//                permutations.add(perm);
//            }
//            System.out.println(perm);
//        }
//
//        System.out.println("");
//
//        for (HashMap<String, String> permutation : permutations) {
//            System.out.println(permutation);
//        }
//
//
//
//        Variable queryP = new Variable();
//        queryP.setName("A");
//
//        Variable queryVar = new Variable();
//        queryVar.setName("J");
//        queryVar.addParentNode(queryP);
//
//        evidenceVars.put("B", "T");
//        evidenceVars.put("J", "T");

//        System.out.println(checkForCPT(evidenceVars, queryVar));


        answerQueries("input.txt");

    }


    public static boolean checkForCPT(HashMap<String, String> evidenceVars, Variable queryVar) {
        boolean flag = false;
        if (evidenceVars.size() - 1 == queryVar.getParentNodes().size()) { //then we might get the answer from the cpt
            flag = true;
            for (int j = 0; j < queryVar.getParentNodes().size(); j++) {
                Variable parent = queryVar.getParentNodes().get(j);
                if (!evidenceVars.containsKey(parent.getName())) {
                    flag = false;  //we cannot get the answer from the cpt
                    break;
                }
            }
        }
        return flag;
    }


    public static void answerQueries(String inputFileName) {

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
//            System.out.println(questions);

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

        for (String query : questions) {
            int algoWanted = Integer.parseInt(query.substring(query.length() - 1));
            String newQuery = query.substring(0, query.length() - 2);

            if (algoWanted == 1) {          //1st algorithm: Joint distribution
                Algorithms jd = new Algorithms(newQuery, network);
                jd.addToHidden(newQuery);
                jd.addToEvidence(newQuery);
                jd.runAlgo(1);

                System.out.println("hid: " + jd.printHidden());
                System.out.println("evi: " + jd.printEvidence());


                output += jd.getAnswer() + "," + jd.getAddActions1() + "," + jd.getMultiplyActions1() + "\n";
                System.out.println(query + ":" + "\n" + "Answer = " + jd.getAnswer() + "\n" +
                        "Total number of additions = " + jd.getAddActions1() + "\n" +
                        "Total number of multiplications = " + jd.getMultiplyActions1() + "\n" +
                        "hidden: " + jd.printHidden() + "\n" +
                        "evidence: " + jd.printEvidence() + "\n"
                );

            } else if (algoWanted == 2) {  //2nd algorithm: Variable elimination
                Algorithms ve1 = new Algorithms(newQuery, network);
                ve1.addToHidden(newQuery);
                ve1.addToEvidence(newQuery);
                ve1.runAlgo(2);

                output += (ve1.getAnswer() + "," + ve1.getAddActions2() + "," + ve1.getMultiplyActions2()) + "\n";
                System.out.println(query + ":" + "\n" + "Answer = " + ve1.getAnswer() + "\n" +
                        "Total number of additions = " + ve1.getAddActions2() + "\n" +
                        "Total number of multiplications = " + ve1.getMultiplyActions2() + "\n");
            }
            else if (algoWanted == 3) {  //2nd algorithm: Variable elimination
                Algorithms ve2 = new Algorithms(newQuery, network);
                ve2.addToHidden(newQuery);
                ve2.addToEvidence(newQuery);
                ve2.runAlgo(3);

                output += (ve2.getAnswer() + "," + ve2.getAddActions2() + "," + ve2.getMultiplyActions2()) + "\n";
                System.out.println(query + ":" + "\n" + "Answer = " + ve2.getAnswer() + "\n" +
                        "Total number of additions = " + ve2.getAddActions2() + "\n" +
                        "Total number of multiplications = " + ve2.getMultiplyActions2() + "\n");
            }


        }

        //extract output text file
        try {
            BufferedWriter myWriter = new BufferedWriter(new FileWriter("output.txt"));
            myWriter.write(output);
            myWriter.close();
            System.out.println("Kululu!!");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}