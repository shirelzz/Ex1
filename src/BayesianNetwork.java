import org.w3c.dom.Node;

import java.util.ArrayList;

public class BayesianNetwork {

    private final ArrayList<CptNode> network;

    public BayesianNetwork(){
        this.network = new ArrayList<>();
    }
    
    public void add(CptNode cptNode){
        network.add(cptNode);
    }

//    public String toString(BayesianNetwork network){
//
//    }



//
//    DataSet ds = new DataSet();
//    // load network and data here
//    DataMatch[] matching = ds.matchNetwork();
//    Validator validator = new Validator(ds, net, matching);
//    int classNodehandle = net.getNode("someNodeId");
//    validator.addClassNode(classNodeHandle);
//    EM em = new EM();
//// optionally tweak EM options here
//    validator.kFold(em, 5);
//    double acc = validator.getAccuracy(classNodeHandle, 0);




//    int e = createCptNode(net,
//            "Economy", "State of the economy",
//            new String[] {"Up","Flat","Down"},
//            160, 40);
//    int s = createCptNode(net,
//            "Success", "Success of the venture",
//            new String[] {"Success","Failure"},
//            60, 40);
//    int f = createCptNode(net,
//            "Forecast", "Expert forecast",
//            new String[] {"Good","Moderate","Poor"},
//            110, 140);

//    private static int createCptNode( BayesianNetwork net, String id, String name,
//            String[] outcomes, int xPos, int yPos) {
//        int handle = net.addNode(BayesianNetwork.NodeType.CPT, id);
//        net.setNodeName(handle, name);
//        net.setNodePosition(handle, xPos, yPos, 85, 55);
//        int initialOutcomeCount = net.getOutcomeCount(handle);
//        for (int i = 0; i < initialOutcomeCount; i ++) {
//            net.setOutcomeId(handle, i, outcomes[i]);
//        }
//        for (int i = initialOutcomeCount; i < outcomes.length; i ++) {
//            net.addOutcome(handle, outcomes[i]);
//        }
//        return handle;
//    }

//    net.addArc(e, s);
//    net.addArc(s, f);
//    net.addArc("Economy", "Forecast");
//
//    double[] successDef = new double[] {
//            0.3, // P(Success=S|Economy=U)
//            0.7, // P(Success=F|Economy=U)
//            0.2, // P(Success=S|Economy=F)
//            0.8, // P(Success=F|Economy=F)
//            0.1, // P(Success=S|Economy=D)
//            0.9  // P(Success=F|Economy=D)
//    };
//    net.setNodeDefinition(s, successDef);
//
//    net.writeFile("tutorial1.xdsl");
//
//    private Node addNode(BayesianNetwork net, Node n){
//        Node newNode = net.getFactory().createNode(n.getId(), n.getCoord());
//        net.addNode(newNode);
//        return newNode;
//    }
//
//    private void addNode(Node newNode) {
//
//    }
//
//}





}
