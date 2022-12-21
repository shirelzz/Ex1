import java.util.ArrayList;

public class BayesianNetwork {

    private final ArrayList<Variable> network;

    public BayesianNetwork(){
        this.network = new ArrayList<>();
    }
    
    public void add(Variable variable){
        network.add(variable);
    }

    public int size(){
       return network.size();
    }

    public Variable get(int i){
        return network.get(i);
    }

    public void printNet(){
        for (Variable variable : network) {
            System.out.println(variable.printVariableDetails());
        }
    }

    public int find(String name){
        int k = 0;
        for (int i = 0; i<this.network.size(); i++){
            Variable curr = this.network.get(i);
            if (curr.getName().equals(name)){
                k = i;
                break;
            }
        }
        return k;
    }

}
