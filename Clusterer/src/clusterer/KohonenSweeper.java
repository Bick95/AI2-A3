package clusterer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import static clusterer.RunClustering.in;


public class KohonenSweeper extends Sweeper{

    private int[] minMaxN;
    private int[] minMaxEpochs;
    private double[][][] accuracy;
    private double[][][] hitrate;
    private int totalIterations; /// Used for printing progress bar

    public KohonenSweeper(Vector<float[]> trainData, Vector<float[]> testData, int dim){
        super(trainData, testData, dim);

    }

    @Override
    public void askSpecificParameters(){
        minMaxN = new int[2];
        readminMaxN();
        minMaxEpochs = new int [3];
        readMinMaxEpochs();
    }

    @Override
    public void init(){
        int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])) + 1; /// Compute nr of thresholds
        int gridRange = minMaxN[1] - minMaxN[0] + 1;
        int epochRange = ((minMaxEpochs[1] - minMaxEpochs[0] + 1)/minMaxEpochs[2]) + 1;
        //System.out.println("ranges: n is " + gridRange + " ths is " + ths + " epochs is " + epochRange);
        accuracy = new double[gridRange][ths][epochRange];
        hitrate = new double[gridRange][ths][epochRange];
        totalIterations = (minMaxN[1] - minMaxN[0])*(int)(minMaxEpochs[1] - minMaxEpochs[0] + 1)*((int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])+1) + 1);
    }

    @Override
    public void test(){
        int nIndex = 0;
        int totalCounter = 0;
        for (int n = minMaxN[0]; n <= minMaxN[1]; n++){         ///iterate over grid size
            
            int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])) + 1; /// Compute nr of thresholds

            for (int idx = 0; idx < ths; idx++){ /// For each threshold value       ///iterate over threshold values

                double threshold = minMaxPFT[0] + minMaxPFT[2]*(double)idx;

                int epochIndex = 0;
                for (int epoch = minMaxEpochs[0]; epoch<=minMaxEpochs[1]; epoch+=minMaxEpochs[2]) {      //iterate over epoch number

                    double acc = 0;
                    double hr = 0;
                    
                    totalCounter++;
                    printProgressBar(totalCounter); /// Only serves as a rough indication... Can't cope with exponential scale unfortunately
                    //System.out.println("specs: n is " + n + " ths is " + threshold + " epochs is " + epoch);
                    //System.out.println("indexes: n is " + nIndex + " ths is " + idx + " epochs is " + epochIndex);

                    for (int i = 0; i < iterations; i++) { /// Compute mean values for X iterations ran...
                        ca = new Kohonen(n,epoch, new Vector<float[]>(trainData), new Vector<float[]>(testData), dim, threshold);
                        /// Run and evaluate algorithm
                        ca.train();
                        ca.test();
                        /// Add up results
                        acc += ca.getAccuracy();
                        hr += ca.getHitrate();
                    }
                    /// array[mapSize(n)][threshold][epoch]
                    accuracy[nIndex][idx][epochIndex] = acc / (double) iterations;
                    hitrate[nIndex][idx][epochIndex] = hr / (double) iterations;
                    ca = null;
                    epochIndex++;
                }
            }
            nIndex++;

        }
    }

    @Override
    public void printResults(){
        System.out.println("\nAccuracy and Hitrate are as follows in the Kohonen SOM algorithm when varying n between " + minMaxN[0] + " and " + minMaxN[1] + " and between prefetch threshold values " + minMaxPFT[0] + " and " + minMaxPFT[1] + ", and learning epochs between " + minMaxEpochs[0] + " and " + minMaxEpochs[1] + " with step " + minMaxEpochs[2] + ". For one version, running " + iterations + " repetitions.");
        int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])) + 1; /// Compute nr of thresholds
        int nIndex = 0;

        for (int n = minMaxN[0]; n <= minMaxN[1]; n++) {
            for (int i = 0; i < ths; i++){ /// Threshold-loop
                double threshold = minMaxPFT[0] + minMaxPFT[2]*(double)i;
                int epochIndex = 0;
                
                System.out.println();
                System.out.println("_________________________________________");
                System.out.println("N: " + n);
                System.out.println();
                System.out.println("Threshold: " + threshold);
                System.out.println();
                System.out.println("Epochs:\tAccuracy:\t\tHitrate:\t\tcombined:\t");
                
                for(int epoch = minMaxEpochs[0]; epoch<=minMaxEpochs[1]; epoch+=minMaxEpochs[2]) {
                    /// array[mapSize][threshold][epoch]
                    System.out.println(epoch + "\t" + accuracy[nIndex][i][epochIndex] + "\t" + hitrate[nIndex][i][epochIndex] + "\t" + (accuracy[nIndex][i][epochIndex] + hitrate[nIndex][i][epochIndex]));
                    epochIndex++;
                }
            }
            nIndex++;
        }
        System.out.println();
        System.out.println();
    }

    @Override
    public void saveResults(){
        File output = new File(getPath());
        try {
            output.createNewFile();                  // Create file if necessary
        } catch (IOException ex) {
            Logger.getLogger("Kohonen sweeping save results error").log(Level.SEVERE, null, ex);
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
            writer.print("n");
            for (int i = minMaxN[0]; i <= minMaxN[1]; i++){
                writer.print(";" + i);
            }

            writer.println();
            writer.print("epochs");
            for (int i = minMaxEpochs[0]; i <= minMaxEpochs[1]; i+=minMaxEpochs[2]){
                writer.print(";" + i);
            }
            writer.println("\nAccuracy[row: threshold][column: learning epoch number];...");
            /// Store actual data
            /// Accuracy
            writer.println();
            /*
            for (int k = 0; k <= minMaxN[1] - minMaxN[0]; k++){
                writer.println("Accuracy for N=" + (k + minMaxN[0]) + "...");
                writer.println("Accuracy[row: threshold][column: learning epoch number];...");
                for (int idx = 0; idx < ths; idx++){ /// For each threshold value
                    writer.print(Double.toString(minMaxPFT[0] + minMaxPFT[2]*(double)idx));
                    int epochIndex = 0;
                    for (int epoch = minMaxEpochs[0]; epoch<=minMaxEpochs[1]; epoch+=minMaxEpochs[2] ) {
                        writer.print(";" + accuracy[k][idx][epochIndex]);
                        epochIndex++;
                    }
                    writer.print("\n");
                }
                writer.println();
            }*/
            int epochIndex = 0;
            writer.println("N");
            for (int epoch = minMaxEpochs[0]; epoch<=minMaxEpochs[1]; epoch+=minMaxEpochs[2] ) {
                writer.println("Accuracy for Epochs=" + epoch);
                for (int k = 0; k <= minMaxN[1] - minMaxN[0]; k++){
                    writer.print((minMaxN[0]+k));
                    for (int idx = 0; idx < ths; idx++){ /// For each threshold value
                        writer.print(";" + accuracy[k][idx][epochIndex]);
                        
                    }
                    writer.println();
                }
                epochIndex++;
            }

            /// Hitrate
            writer.println("----------------Hitrates--------------------");
            epochIndex = 0;
            writer.println("N");
            for (int epoch = minMaxEpochs[0]; epoch<=minMaxEpochs[1]; epoch+=minMaxEpochs[2] ) {
                writer.println("Accuracy for Epochs=" + epoch);
                for (int k = 0; k <= minMaxN[1] - minMaxN[0]; k++){
                    writer.print((minMaxN[0]+k));
                    for (int idx = 0; idx < ths; idx++){ /// For each threshold value
                        writer.print(";" + hitrate[k][idx][epochIndex]);
                        
                    }
                    writer.println();
                }
                epochIndex++;
            }

            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println("Error when saving to file! " + e.getMessage());
            e.printStackTrace();
        }
        
    }

    @Override
    public String getFileName(){
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
        return "SaveFile_Kohonen"  + "_" + timeStamp + ".csv";
    }

    private void readminMaxN(){
        //prompt for n
        System.out.println("Define minimum and maximum map size");
        while (true) {
            System.out.print("Minimum map size? :");
            try{
                minMaxN[0] = (new Integer(in.readLine())).intValue();
                break;
            }
            catch (Exception e){
                System.err.println(e);
            }

        }
        while (true) {
            System.out.print("Maximum map size? :");
            try{
                minMaxN[1] = (new Integer(in.readLine())).intValue();

            }
            catch (Exception e){
                System.err.println(e);
            }
            if(minMaxN[1]>minMaxN[0]){
                break;
            }else{
                System.err.println("Max should be bigger than min");
            }

        }
    }

    private void readMinMaxEpochs(){
        //prompt for epoch numbers
        System.out.println("Define minimum and maximum number of training epochs, and step size");
        while (true) {
            System.out.print("Minimum number of training epochs ? :");
            try{
                minMaxEpochs[0] = (new Integer(in.readLine())).intValue();
                break;
            }
            catch(Exception e){
                System.err.println(e);
            }
        }
        while (true) {
            System.out.print("Maximum number of training epochs ? :");
            try{
                minMaxEpochs[1] = (new Integer(in.readLine())).intValue();

            }
            catch(Exception e){
                System.err.println(e);
            }
            if(minMaxEpochs[1]>minMaxEpochs[0]){
                break;
            }else{
                System.err.println("Max should be bigger than min");
            }
        }
        while (true) {
            System.out.print("Step size ? ");
            try{
                minMaxEpochs[2] = (new Integer(in.readLine())).intValue();
                break;
            }
            catch(Exception e){
                System.err.println(e);
            }
        }
    }
    
    private void printProgressBar(int t){
            /// Uncomment following to see development of progress bar
            //System.err.println("Epoch: " + t);
                
            int maxProgress = 10;
            int cntProgress = (int) Math.round((((double)t+(double)1)/(double)totalIterations)*(double)maxProgress);
            
            System.out.print("\rProgress: |");
            for (int prog = 0; prog < cntProgress; prog++){
                System.out.print("=");
            }
            for (int prog = cntProgress; prog < maxProgress; prog++){
                System.out.print(" ");
            }
            System.out.print("|");
        }
}
