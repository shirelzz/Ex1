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
        for (CptNode cptNode : network) {
            System.out.println(cptNode.printVariableDetails());
        }
    }

    public int find(String name){
        int k = 0;
        for (int i = 0; i<this.network.size(); i++){
            CptNode curr = this.network.get(i);
            if (curr.getName().equals(name)){
                k = i;
                break;
            }
        }
        return k;
    }

}
