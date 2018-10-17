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
    
    private Vector<float[]> trainData;
    private Vector<float[]> testData;
    
    public ClusteringAlgorithm ca;   //handle to the actual clustering algorithm

    private int dim;                 // dimensionality of the data and clusters
    
    int[] minMaxN;
    int[] minMaxepochs;
    int[] minMaxK;
    double[] minMaxPFT;
    double[] pft; /// Prefetchthreshold: min; max; stepsize
    int repetitions;
    
    private double[][] accuracy;
    private double[][] hitrate;
    
    
    public ParameterSweeper(Vector<float[]> trainData, Vector<float[]> testData, int dim){
        this.trainData = trainData;
        this.testData = testData;
        this.dim = dim;
        readAlgoID();
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
    
    public void test(){
        System.out.println("How many training iterations?");
        this.repetitions = returnInt();
        
        minMaxPFT = askRangePFT();
        for (int i = 0; i < minMaxPFT.length; i++)
            System.out.println(minMaxPFT[i]);
        switch (algoID){
            case 1:
                kmeansInit();
                break;
            case 2:
                leaderFollowerInit();
                break;
            case 3:
                kohonenInit();
                break;
        }
        
        
            /// Alway create new algo to get a new random initialization
            switch (algoID){
            case 1:
                for (int k = minMaxK[0]; k <= minMaxK[1]; k++){
                    
                    int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])); /// Compute nr of thresholds
                    
                    for (int idx = 0; idx < ths; idx++){ /// For each threshold value
                        
                        double threshold = minMaxPFT[0] + minMaxPFT[2]*(double)idx;
                        double acc = 0;
                        double hr = 0;
                        
                        
                        for (int i = 0; i < repetitions; i++){
                            ca = new KMeans(k, new Vector<float[]>(trainData), new Vector<float[]>(testData), dim, threshold);
                            /// Run and evaluate algorithm
                            ca.train();
                            ca.test();
                            /// Add up results
                            acc += ca.getAccuracy();
                            hr  += ca.getHitrate();
                        }
                        
                        accuracy[k][idx] = acc / (double) repetitions;
                        hitrate[k][idx] = hr / (double) repetitions;
                        ca = null;
                    }
                    
                }
                
                break;
            case 2:
                System.err.println("Algorithm not supported.");
                break;
            case 3:
                //ca = new Kohonen(n, epochs, new Vector<float[]>(trainData), new Vector<float[]>(testData), dim);
                break;
            }
        
        
        printResults();
                
    }
    
    
    private void kmeansInit()
    {
            System.out.println("How many clusters (k) ? ");
            minMaxK = askRange();
            System.out.println("... " + ((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2]));
            int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])); /// Compute nr of thresholds
            accuracy = new double[minMaxK[1]+1][ths+1];
            hitrate = new double[minMaxK[1]+1][ths+1];
            System.out.println("ths: " + (ths+1));
    }
    
    private double[] askRangePFT(){ /// Ask for prefetchthreshold
        
        System.out.println("Prefetch threshold range:");
        double[] minMax = new double[3];
        
        System.out.print("From:\t");
        minMax[0] = returnDouble();
        
        System.out.print("To:\t");
        minMax[1] = returnDouble();
        
        System.out.print("Step size:\t");
        minMax[2] = returnDouble();
        return minMax;
    }
    
    private double returnDouble(){
        double k = 0;
        while (true) {  // get int
            try{ 
                    k = (new Double(in.readLine())).doubleValue(); 
                    break;
            }
            catch (Exception e){
                    System.out.println();
            }
        }
        return k;
    }
    private int[] askRange(){
        int[] minMax = new int[2];
        System.out.print("From:\t");
        minMax[0] = returnInt();
        System.out.print("To:\t");
        minMax[1] = returnInt();
        return minMax;
    }
    
    private int returnInt(){
        int k = 0;
        while (true) {  // get int
            try{ 
                    k = (new Integer(in.readLine())).intValue(); 
                    break;
            }
            catch (Exception e){
                    System.out.println();
            }
        }
        return k;
    }
    
    public void leaderFollowerInit()
    {
            System.err.println("This algorithms is not supported yet.");
    }

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
    
    private void printResults(){
        switch (algoID){
            case 1:
                System.out.println("Accuracy and Hitrate are as follows in the KMeans algorithm when varying k between " + minMaxK[0] + " and " + minMaxK[1] + " and running " + repetitions + " repetitions.");
                int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])); /// Compute nr of thresholds
                for (int i = 0; i < ths; i++){
                    double threshold = minMaxPFT[0] + minMaxPFT[2]*(double)i;
                    System.out.println();
                    System.out.println("Threshold: " + threshold);
                    System.out.println();
                    System.out.println("k\tAccuracy:\t\tHitrate:\t\tcombined:\t");

                    for (int k = minMaxK[0]; k <= minMaxK[1]; k++){
                        System.out.println(k + "\t" + accuracy[k][i] + "\t" + hitrate[k][i] + "\t" + (accuracy[k][i]+hitrate[k][i]));
                    }
                }
                break;
            case 2:
                System.err.println("Algorithm not supported.");
                break;
            case 3:
                //ca = new Kohonen(n, epochs, new Vector<float[]>(trainData), new Vector<float[]>(testData), dim);
                break;
            }
    } 
}
