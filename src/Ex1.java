public class Ex1 {
    public static void main(String[] args) {

        XmlReader xmlReader = new XmlReader();
        BayesianNetwork network = xmlReader.buildNet("/Users/syrlzkryh/Documents/GitHub/Ex1/src/alarm_net.xml");
        network.printNet();




    }


}
