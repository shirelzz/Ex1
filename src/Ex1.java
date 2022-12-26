import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;

public class Ex1 {
    public static void main(String[] args) throws IOException, SAXException {

        answerQueries("input.txt");

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
            }

            System.out.println("");
            myReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // xml file
        XmlReader xmlReader = new XmlReader();
//        xmlReader.readXMLFile(xmlFileName);
        BayesianNetwork network = xmlReader.buildNet(xmlFileName);
//        network.printNet();

        //answers
        String output = "";

        for (String query : questions) {
            int algoWanted = Integer.parseInt(query.substring(query.length() - 1));
            String newQuery = query.substring(0, query.length() - 2);

            if (algoWanted == 1) {          //1st algorithm: Joint distribution
                Algorithms jd = new Algorithms(newQuery, network);
                jd.runAlgo(1);

                output += jd.getAnswer() + "," + jd.getAddActions1() + "," + jd.getMultiplyActions1() + "\n";
                System.out.println(query + ":" + "\n" + "Answer = " + jd.getAnswer() + "\n" +
                        "Total number of additions = " + jd.getAddActions1() + "\n" +
                        "Total number of multiplications = " + jd.getMultiplyActions1() + "\n"
                );

            } else if (algoWanted == 2) {  //2nd algorithm: Variable elimination
                Algorithms ve1 = new Algorithms(newQuery, network);
                ve1.runAlgo(2);

                output += (ve1.getAnswer() + "," + ve1.getAddActions2() + "," + ve1.getMultiplyActions2()) + "\n";
                System.out.println(query + ":" + "\n" + "Answer = " + ve1.getAnswer() + "\n" +
                        "Total number of additions = " + ve1.getAddActions2() + "\n" +
                        "Total number of multiplications = " + ve1.getMultiplyActions2() + "\n");
            }
            else {
                Algorithms ve2 = new Algorithms(newQuery, network);
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

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}