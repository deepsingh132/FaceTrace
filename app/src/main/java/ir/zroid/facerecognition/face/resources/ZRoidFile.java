package ir.zroid.facerecognition.face.resources;

import android.graphics.Bitmap;
import android.os.Environment;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * This class is used to access the file system where the training and test data is stored
 */

public class ZRoidFile {
    public static String getFolderPath() {
        return FOLDER_PATH;
    }
    public static final String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "FaceRecognition" + File.separator + "data";
    public static final String FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/FaceRecognition";
    public static final String TRAINING_PATH = FOLDER_PATH + "/training/";
    public static final String TEST_PATH = FOLDER_PATH + "/test/";
    public static final String DETECTION_TEST_PATH = FOLDER_PATH + "/detection_test/";
    public static final String DATA_PATH = FOLDER_PATH + "/data/";
    public static final String DATABASE_BACKUP = FOLDER_PATH + "/data/databases/";
    public static final String BACKUP_PATH = FOLDER_PATH + "/backup/data/databases";
    public static final String BACKUP_IMAGES = FOLDER_PATH + "/backup/data/";
    public static final String BACKUP_ROOM = FOLDER_PATH + "/backup/data/room/";
    public static final String BACKUP_FOLDER = FOLDER_PATH + "/backup/";

    public static final String RESULTS_PATH = FOLDER_PATH + "/results/";
    public static final String EIGENFACES_PATH = DATA_PATH + "Eigenfaces/";
    public static final String SVM_PATH = DATA_PATH + "SVM/";
    public static final String KNN_PATH = DATA_PATH + "KNN/";
    public static final String CAFFE_PATH = DATA_PATH + "Caffe/";
    public static final String TENSORFLOW_PATH = DATA_PATH + "TensorFlow/";
    private static final String SEPARATOR = ";";
    /**
     * Name of the person (subdirectory)
     */
    private String name = "";

    public ZRoidFile(String name) {
        this.name = name;
    }

    public ZRoidFile(){}

    public void createDataFolderIfNotExsiting(){
        File folder = new File(DATA_PATH);
        folder.mkdir();
    }

    private void createFolderIfNotExisting(String path){
        File folder = new File(path);
        folder.mkdir();
    }

    public static boolean isFileAnImage(File file){
        if (file.toString().endsWith(".jpg") || file.toString().endsWith(".jpeg") || file.toString().endsWith(".gif") || file.toString().endsWith(".png")){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns an array of all files in the specified directory
     * @param path of the directory
     * @return
     */
    private File[] getListOfFiles(String path){
        File directory = new File(path + name);
        if(directory.exists()){
            return directory.listFiles();
        } else {
            return new File[]{};
        }
    }

    /**
     * Returns an array of all training files in the specified person directory
     * @return
     */
    public File[] getTrainingList(){
        return getListOfFiles(TRAINING_PATH);
    }

    /**
     * Returns an array of all test files in the specified person directory
     * @return
     */
    public File[] getTestList(){
        return getListOfFiles(TEST_PATH);
    }

    public File[] getDetectionTestList() {
        return getListOfFiles(DETECTION_TEST_PATH);
    }






    public void saveBitmapToImage(Bitmap bmp, String name){
        File file = new File(DATA_PATH + name +".png");
        try {
            FileOutputStream os = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File createSvmTrainingFile(){
        createFolderIfNotExisting(SVM_PATH);
        String filepath = SVM_PATH + "svm_train";
        File trainingFile = new File(filepath);
        return trainingFile;
    }

    public File createSvmPredictionFile(){
        String filepath = SVM_PATH + "svm_predict";
        File predictionFile = new File(filepath);
        return predictionFile;
    }

    public File createSvmTestFile(){
        String filepath = SVM_PATH + "svm_test";
        File testFile = new File(filepath);
        return testFile;
    }

    public File createLabelFile(String path, String name){
        createFolderIfNotExisting(path);
        String filepath = path + "label_" + name;
        File trainingFile = new File(filepath);
        return trainingFile;
    }


    public void saveResultsToFile(Map<String, ?> map, double accuracy, double accuracy_reference, double accuracy_deviation, double robustness, int duration, List<String> results){
        String timestamp = new SimpleDateFormat("ddMMyyyyHHmm").format(new java.util.Date());
        createFolderIfNotExisting(RESULTS_PATH);
        String filepath = RESULTS_PATH + "Accuracy_" + String.format("%.2f", accuracy * 100) + "_" + timestamp + ".txt";
        try {
            FileWriter fw = new FileWriter(filepath);
            for (Map.Entry entry : map.entrySet()){
                fw.append(entry.getKey() + ": " + entry.getValue() + "\n");
            }
            fw.append("Accuracy: " + accuracy * 100 + "%\n");
            fw.append("Accuracy reference: " + accuracy_reference * 100 + "%\n");
            fw.append("Accuracy deviation: " + accuracy_deviation * 100 + "%\n");
            fw.append("Robustness: " + robustness * 100 + "%\n");
            fw.append("Duration per image: " + duration + "ms\n");
            for (String result : results){
                fw.append(result + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveResultsToFile(Map<String, ?> map, double accuracy, int duration, List<String> results){
        String timestamp = new SimpleDateFormat("ddMMyyyyHHmm").format(new java.util.Date());
        createFolderIfNotExisting(RESULTS_PATH);
        String filepath = RESULTS_PATH + "Accuracy_" + String.format("%.2f", accuracy * 100) + "_" + timestamp + ".txt";
        try {
            FileWriter fw = new FileWriter(filepath);
            for (Map.Entry entry : map.entrySet()){
                fw.append(entry.getKey() + ": " + entry.getValue() + "\n");
            }
            fw.append("Accuracy: " + accuracy * 100 + "%\n");
            fw.append("Duration per image: " + duration + "ms\n");
            for (String result : results){
                fw.append(result + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void saveStringList(List<String> list, File file){
        try {
            FileWriter fw = new FileWriter(file, false);
            for (String line : list){
                fw.append(line + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void saveIntegerList(List<Integer> list, File file){
        try {
            FileWriter fw = new FileWriter(file, false);
            for (int line : list){
                fw.append(Integer.toString(line)+ "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<String> loadStringList(File file){
        List<String> list = new ArrayList<>();
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null){
                list.add(line);
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Integer> loadIntegerList(File file){
        List<Integer> list = new ArrayList<>();
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            Integer line = 0;
            String sLine;
            while ((sLine = br.readLine()) != null){
                line = Integer.parseInt(sLine);
                list.add(line);
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Boolean copy(File src, String dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                return true;
            }
        }
    }
}
