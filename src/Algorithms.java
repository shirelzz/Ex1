import java.text.DecimalFormat;
import java.util.ArrayList;

public class Algorithms {

    private String query;
    private double answer;
    private int addAct = 0;
    private int multiAct = 0;
    private ArrayList<CptNode> evidence;
    private ArrayList<CptNode> hidden;
    private ArrayList<CptNode> variables;

//    private ArrayList<Factor> factors;
//    private Hashtable factor;


    Algorithms(String query, BayesianNetwork network, int algo){




        if (algo == 1){
            jointDist();
        }
        if (algo == 2){
            eliminateBySize();
        }
        else if (algo == 3){
            heuristicElimination();
        }
    }

    public void jointDist(){


    }

    public void eliminateBySize(){

    }

    public void heuristicElimination(){

    }

    public double getAnswer() {
        double value = this.answer;
        value =Double.parseDouble(new DecimalFormat("#.#####").format(value));
        return value;
    }

    public int getAddActions() {
        return this.addAct;
    }

    public int getMultiplyActions() {
        return this.multiAct;
    }

    public double calcProb(){
        double ans =0;
//        if (this.query.contains())
        return ans;
    }
}

