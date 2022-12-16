import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Ex1 {
    public static void main(String[] args) throws IOException, SAXException {

        Variable queryP = new Variable();
        queryP.setName("A");

        Variable queryVar = new Variable();
        queryVar.setName("J");
        queryVar.addParentNode(queryP);

        HashMap<String, String> evidenceVars = new HashMap<>();
        evidenceVars.put("B", "T");
        evidenceVars.put("J", "T");
        evidenceVars.put("E", "T");


        System.out.println(checkForCPT(evidenceVars, queryVar));




//        BayesianNetwork network = new BayesianNetwork();
//        XmlReader xmlReader = new XmlReader();
//        network = xmlReader.buildNet("/Users/syrlzkryh/Documents/GitHub/Ex1/src/alarm_net.xml");
//
//        ArrayList<String> questions = new ArrayList<>();
//        questions.add("P(B=T|J=T,M=T),1");
//
//
//        //answers
//        String output = "";
//
//
//        //1st algorithm: Joint distribution
//        for (String query : questions) {
//            int algoWanted = Integer.parseInt(query.substring(query.length() - 1));
//            String newQuery = query.substring(0, query.length() - 1);
//            if (algoWanted == 1) {
//                Algorithms jd = new Algorithms(newQuery, network);
//                System.out.println(jd.printHidden());
//                jd.addToHidden(newQuery);
//                System.out.println(jd.printHidden());
//                System.out.println(jd.printEvidence());
//                jd.addToEvidence(newQuery);
//                System.out.println(jd.printEvidence());
//                jd.runAlgo(1);
//
//                output += jd.getAnswer() + "," + jd.getAddActions1() + "," + jd.getMultiplyActions1() + "\n";
//                System.out.println(query + ":" + "\n" + "Answer = " + jd.getAnswer() + "\n" +
//                        "Total number of additions = " + jd.getAddActions1() + "\n" +
//                        "Total number of multiplications = " + jd.getMultiplyActions1() + "\n" +
//                        "hidden: " + jd.printHidden() + "\n" +
//                        "evidence: " + jd.printEvidence() + "\n");
//            }
//        }

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

        //1st algorithm: Joint distribution
        for (String query : questions) {
            int algoWanted = Integer.parseInt(query.substring(query.length() - 1));
            String newQuery = query.substring(0, query.length() - 1);
            if (algoWanted == 1) {
                Algorithms jd = new Algorithms(newQuery, network);
                jd.addToHidden(newQuery);
                jd.addToEvidence(newQuery);
                jd.runAlgo(1);

                output += jd.getAnswer() + "," + jd.getAddActions1() + "," + jd.getMultiplyActions1() + "\n";
                System.out.println(query + ":" + "\n" + "Answer = " + jd.getAnswer() + "\n" +
                        "Total number of additions = " + jd.getAddActions1() + "\n" +
                        "Total number of multiplications = " + jd.getMultiplyActions1() + "\n" +
                        "hidden: " + jd.printHidden() + "\n" +
                        "evidence: " + jd.printEvidence() + "\n" +
                        "alpha: = " + jd.getAlpha() + "\n");
            } else if (algoWanted == 2) {
                Algorithms ve1 = new Algorithms(newQuery, network);
                ve1.addToHidden(newQuery);
                ve1.addToEvidence(newQuery);
                ve1.runAlgo(2);

                output += (ve1.getAnswer() + "," + ve1.getAddActions2() + "," + ve1.getMultiplyActions2()) + "\n";
                System.out.println(query + ":" + "\n" + "Answer = " + ve1.getAnswer() + "\n" +
                        "Total number of additions = " + ve1.getAddActions2() + "\n" +
                        "Total number of multiplications = " + ve1.getMultiplyActions2() + "\n" );
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
