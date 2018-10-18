package clusterer;

import java.util.Vector;

import static clusterer.RunClustering.in;

public class Sweeper {

    public int iterations;
    public double[] minMaxPFT;
    public Vector<float[]> trainData;
    public Vector<float[]> testData;

    public ClusteringAlgorithm ca;   //handle to the actual clustering algorithm

    public int dim;                 // dimensionality of the data and clusters

    public Sweeper(){}

    public Sweeper(Vector<float[]> trainData, Vector<float[]> testData, int dim){
        this.trainData = trainData;
        this.testData = testData;
        this.dim = dim;
        ///general parameters
        this.iterations = askIterations();
        this.minMaxPFT = askRangePFT();
    }

    private int askIterations(){
        System.out.println("How many training iterations?");
        return returnInt();
    }
    private double[] askRangePFT(){ /// Ask for prefetchthreshold

        System.out.println("Prefetch threshold range:");
        double[] minMax = new double[3];

        System.out.print("From:\t\t");
        minMax[0] = returnDouble();

        System.out.print("To:\t\t");
        minMax[1] = returnDouble();

        System.out.print("Step size:\t");
        minMax[2] = returnDouble();
        return minMax;
    }

    public double returnDouble(){
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

    public int returnInt(){
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

    public void init(){}
    public void test(){}
    public void train(){}
    public void printResults(){}
    public void askSpecificParameters(){}
}
