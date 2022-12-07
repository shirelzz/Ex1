import java.util.ArrayList;

public class BayesianNetwork {

    private final ArrayList<CptNode> network;

    public BayesianNetwork(){
        this.network = new ArrayList<>();
    }
    
    public void add(CptNode cptNode){
        network.add(cptNode);
    }

    public int size(){
       return network.size();
    }

    public CptNode get(int i){
        return network.get(i);
    }

    public void printNet(){
        for (int i = 0; i < network.size(); i++) {
            System.out.println(network.get(i).printVariableDetails());
        }
    }

    public int find(String s){
        int k = 0;
        for (int i = 0; i<this.network.size(); i++){
            CptNode curr = this.network.get(i);
            if (curr.getName().equals(s)){
                k = i;
                break;
            }
        }
        return k;
    }

}
