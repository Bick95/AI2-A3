package clusterer;

import static clusterer.RunClustering.in;


public class KohonenSweeper extends Sweeper{

    @Override
    public void askSpecificParameters(){}

    @Override
    public void init(){
        //TODO
    }

    @Override
    public void test(){}

    @Override
    public void printResults(){}

    public void kohonenInit()
    {
        int n = 0;
        int epochs = 0;
        while (true) {
            System.out.print("Map size (N*N) ? ");
            try{
                n = (new Integer(in.readLine())).intValue();
                break;
            }
            catch (Exception e){
                System.out.println();
            }

        }

        while (true) {
            System.out.print("Number of training epochs ? ");
            try{
                epochs = (new Integer(in.readLine())).intValue();
                break;
            }
            catch(Exception e){
                System.out.println();
            }
        }

        // this.n = n;
        // this.epochs = epochs;
    }
}
