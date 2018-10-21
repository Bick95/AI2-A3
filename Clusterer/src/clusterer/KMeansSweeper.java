package clusterer;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KMeansSweeper extends Sweeper {

    int[] minMaxK;
    private double[][] accuracy;
    private double[][] hitrate;

    public KMeansSweeper(Vector<float[]> trainData, Vector<float[]> testData, int dim){
        super(trainData, testData, dim);
    }

    @Override
    public void askSpecificParameters(){
        System.out.println("How many clusters (k) ? ");
        int[] minMax = new int[2];
        System.out.print("From:\t");
        minMax[0] = returnInt();
        System.out.print("To:\t");
        minMax[1] = returnInt();
        this.minMaxK =  minMax;
    }


    @Override
    public void init(){
        int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])); /// Compute nr of thresholds
        int ks = minMaxK[1] - minMaxK[0] + 1;
        accuracy = new double[ks][ths+1];
        hitrate = new double[ks][ths+1];

    }

    @Override
    public void test(){
        for (int k = 0; k <= minMaxK[1] - minMaxK[0]; k++){

            int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])) + 1; /// Compute nr of thresholds

            for (int idx = 0; idx < ths; idx++){ /// For each threshold value

                double threshold = minMaxPFT[0] + minMaxPFT[2]*(double)idx;
                double acc = 0;
                double hr = 0;

                for (int i = 0; i < iterations; i++){
                    ca = new KMeans(k+minMaxK[0], new Vector<float[]>(trainData), new Vector<float[]>(testData), dim, threshold);
                    /// Run and evaluate algorithm
                    ca.train();
                    ca.test();
                    /// Add up results
                    acc += ca.getAccuracy();
                    hr  += ca.getHitrate();
                }

                accuracy[k][idx] = acc / (double) iterations;
                hitrate[k][idx] = hr / (double) iterations;
                ca = null;
            }

        }
    }

    @Override
    public void printResults(){
        System.out.println("Accuracy and Hitrate are as follows in the KMeans algorithm when varying k between " + minMaxK[0] + " and " + minMaxK[1] + " and running " + iterations + " repetitions.");
        int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])) + 1; /// Compute nr of thresholds
        for (int i = 0; i < ths; i++){
            double threshold = minMaxPFT[0] + minMaxPFT[2]*(double)i;
            System.out.println();
            System.out.println("Threshold: " + threshold);
            System.out.println();
            System.out.println("k\tAccuracy:\t\tHitrate:\t\tcombined:\t");

            for (int k = 0; k <= minMaxK[1] - minMaxK[0]; k++){
                System.out.println((k + minMaxK[0]) + "\t" + accuracy[k][i] + "\t" + hitrate[k][i] + "\t" + (accuracy[k][i]+hitrate[k][i]));
            }
        }
    }
    @Override
    public void saveResults(){
        System.out.println("starting the saveresults");
        File output = new File(getPath());
        try {
            output.createNewFile();                  // Create file if necessary
        } catch (IOException ex) {
            Logger.getLogger("Kmeans sweeping save results file creation error").log(Level.SEVERE, null, ex);
        }

        int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])) + 1; /// Compute nr of thresholds

        try {
            PrintWriter writer = new PrintWriter(output);

            /// Store meta-data
            writer.print("Thresholds");
            for (int i = 0; i < ths; i++){
                writer.print(";" + (minMaxPFT[0] + minMaxPFT[2]*(double)i));
            }

            writer.println();
            writer.print("k");
            for (int i = minMaxK[0]; i <= minMaxK[1]; i++){
                writer.print(";" + i);
            }

            /// Store actual data
            /// Accuracy
            writer.println();
            writer.println("Accuracy[row: k][column: threshold];...");
            for (int k = 0; k <= minMaxK[1] - minMaxK[0]; k++){
                writer.print((k + minMaxK[0]));
                for (int idx = 0; idx < ths; idx++){ /// For each threshold value
                    writer.print(";" + accuracy[k][idx]);
                }
                writer.println();
            }

            /// Hitrate
            writer.println();
            writer.println("Hitrate[row: k][column: threshold];...");
            for (int k = 0; k <= minMaxK[1] - minMaxK[0]; k++){
                writer.print((k + minMaxK[0]));
                for (int idx = 0; idx < ths; idx++){ /// For each threshold value
                    writer.print(";" + hitrate[k][idx]);
                }
                writer.println();
            }

            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println("Error when saving to file! " + e.getMessage());
        }
    }

    @Override
    public String getFileName(){
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
        return "SaveFile_Kmeans"  + "_" + timeStamp + ".csv";
    }

}

