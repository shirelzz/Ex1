import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Ex1 {
    public static void main(String[] args) throws IOException {

        String q = "P(B=T|J=T,M=T)";
//        String numerator = q.replace("|", ",");
        String denominator = "P(" +q.substring(6, q.length());
//        System.out.println(numerator);
//        System.out.println(denominator);

        String query = "P(B=T)";

        String numeratorStr = q.replace("|", ",");  //P(B=T,J=T,M=T)
        System.out.println(numeratorStr);
        numeratorStr = numeratorStr.substring(2, numeratorStr.length()-1);
        System.out.println(numeratorStr);
        String[] numerator = numeratorStr.split(",");
        for (int i = 0; i< numerator.length; i++){
            System.out.println(numerator[i]);
        }


//        answerQueries("input.txt");

    }

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
