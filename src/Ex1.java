import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Ex1 {
    public static void main(String[] args) throws IOException {

        answerQueries("input.txt");
    }

    public static void answerQueries(String inputFileName){

        //input file
        String xmlFileName = "";
        try {
            File inputFile = new File(inputFileName);
            Scanner myReader = new Scanner(inputFile);
            xmlFileName = myReader.nextLine();

            while (myReader.hasNextLine()) {
                String queries = myReader.nextLine();
                System.out.println(queries);
            }
            System.out.println("");
            myReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // xml file
        XmlReader xmlReader = new XmlReader();
        BayesianNetwork network = xmlReader.buildNet("/Users/syrlzkryh/Documents/GitHub/Ex1/src/" + xmlFileName);
        network.printNet();

        //answers
        String output = "";

        //Bayes rule || Joint distribution


        // variable elimination #1


        // variable elimination #2




        //extract output
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
