/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clusterer;

import static clusterer.RunClustering.ca;
import static clusterer.RunClustering.in;
import java.util.Vector;

/**
 *
 * @author daniel
 */
public class ParameterSweeper {
    
    private int algoID;
    public Sweeper sweeper;

    public ParameterSweeper(Vector<float[]> trainData, Vector<float[]> testData, int dim){
        readAlgoID();
        switch (algoID){
            case 1:
                sweeper = new KMeansSweeper(trainData, testData, dim);
                break;
            default:
                System.out.println("not implemented yet");

        }

    }

    public void runParameterSweeping(){
        this.sweeper.askSpecificParameters();
        this.sweeper.init();
        this.sweeper.test();
        this.sweeper.printResults();
    }
    
    private void readAlgoID(){
        int algID = 0;
        while (true) {
            System.out.print("Run K-means (1), Leader-Follower(2) or Kohonen SOM (3) ? ");
            String line="";
            try {
                    if ((line = in.readLine()) == null)
                            break;

                    algID = (new Integer(line)).intValue();

                    if ((algID > 0) && (algID < 4))
                            break;
            }
            catch (Exception e) {
                    System.out.println();
            }
        }
        this.algoID = algID;
    }


    


}
