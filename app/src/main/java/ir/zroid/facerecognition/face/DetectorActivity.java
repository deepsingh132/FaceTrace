/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ir.zroid.facerecognition.face;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.camera2.CameraCharacteristics;
import android.inputmethodservice.Keyboard;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ir.zroid.facerecognition.MainActivity;
import ir.zroid.facerecognition.R;
import ir.zroid.facerecognition.database.MyDatabase;
import ir.zroid.facerecognition.face.adapters.DetectedAdapter;
import ir.zroid.facerecognition.face.customview.OverlayView;
import ir.zroid.facerecognition.face.customview.OverlayView.DrawCallback;
import ir.zroid.facerecognition.face.env.BorderedText;
import ir.zroid.facerecognition.face.env.ImageUtils;
import ir.zroid.facerecognition.face.env.Logger;
import ir.zroid.facerecognition.face.tflite.SimilarityClassifier;
import ir.zroid.facerecognition.face.tflite.TFLiteObjectDetectionAPIModel;
import ir.zroid.facerecognition.face.tracking.AddDetectedActivity;
import ir.zroid.facerecognition.face.tracking.MultiBoxTracker;
import ir.zroid.facerecognition.studentlist.StudentListActivity;


/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  public static final Logger LOGGER = new Logger();
  public static ArrayList<String> studentNames = new ArrayList<String>();
  private static final String path = "/Attendance";
  ArrayList<String> studentUids = new ArrayList<String>();


  // FaceNet
//  private static final int TF_OD_API_INPUT_SIZE = 160;
//  private static final boolean TF_OD_API_IS_QUANTIZED = false;
//  private static final String TF_OD_API_MODEL_FILE = "facenet.tflite";
//  //private static final String TF_OD_API_MODEL_FILE = "facenet_hiroki.tflite";

  // MobileFaceNet
  private static final int TF_OD_API_INPUT_SIZE = 112;
  private static final boolean TF_OD_API_IS_QUANTIZED = false;
  private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";


  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  private static final boolean MAINTAIN_ASPECT = false;

  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  public boolean flag = false;
  //private static final int CROP_SIZE = 320;
  //private static final Size CROP_SIZE = new Size(320, 320);


  private static final boolean SAVE_PREVIEW_BITMAP = true;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  //private SimilarityClassifier detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;
  public static boolean addPending = false;
  //private boolean adding = false;

  private long timestamp = 0;
  public static String Uid = "";
  private MyDatabase db;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;
  //private Matrix cropToPortraitTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;

  // Face detector
  //private FaceDetector faceDetector;

  // here the preview image is drawn in portrait way
  private Bitmap portraitBmp = null;
  // here the face is cropped and drawn
  private Bitmap faceBmp = null;

  private ExtendedFloatingActionButton fabAdd;
  private CardView studentAddList;

  //public static SimilarityClassifier.Recognition savedRec;

  Boolean scanned = false;

  public String query = "SELECT * FROM persons";
  DetectedAdapter detectedAdapter;


  ArrayList<String> listToLoad = new ArrayList<String>();

  RecyclerView rec;

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        this.onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    this.db = new MyDatabase(getApplicationContext());

    SimpleDateFormat s = new SimpleDateFormat("dd:MM:yyyy:hh:mm");
    String format = s.format(new Date());
    DatabaseReference reference = database.getReference("Attendance");

    System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
    System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
    System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");


    

    fabAdd = findViewById(R.id.fab_add);
    rec = findViewById(R.id.rec_detected);
    studentAddList = findViewById(R.id.btn_addStudentlist);

    fabAdd.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onAddClick();
      }
    });

    studentAddList.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        generate();
      }
    });



    // Real-time contour detection of multiple faces
    FaceDetectorOptions options =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .build();


    FaceDetector detector = FaceDetection.getClient(options);

    MainActivity.faceDetector = detector;

  }

  private void generate(){
    //flag=true;
    Toast.makeText(
            getApplicationContext(),
            Uid+ " Added to list",
            Toast.LENGTH_SHORT).show();

    //final CharSequence[] items = studentUids.;
    AlertDialog.Builder builder = new AlertDialog.Builder(DetectorActivity.this);
    builder.setTitle("Student Uid's");
    builder.setItems(studentUids.toArray(new String[0]), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {

      }
    });
    builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {

        try {
          XSSFWorkbook workbook = new XSSFWorkbook();
          XSSFSheet sheet = workbook.createSheet("attendance");
          //int rownum = 0;

          Row rowId = sheet.createRow(0);
          rowId.createCell(0).setCellValue("Name");

          rowId.createCell(1).setCellValue("Uid");

          int count=1;
          String rowval="";


          for(int k=0; k< studentUids.size(); k++){
            Row row = sheet.createRow(count);
            rowval = String.valueOf(sheet.getRow(k));
            Log.e("Row Value: ",rowval);
            row.createCell(1).setCellValue(studentUids.get(k));
            row.createCell(0).setCellValue(studentNames.get(k));
            count++;
            //createList(studentUids,row);
          }




          /*for (int j=0; j< studentNames.size(); j++) {
            Row rowCol = sheet.createRow(countName);
            rowval = String.valueOf(sheet.getRow(j));
            Log.e("Row Value: ",rowval);
            rowCol.createCell(2).setCellValue(studentNames.get(j));
            countName++;
          }*/

          for (int l = 0; l < studentNames.size(); l++) {
            Log.e("Names: ", studentNames.get(l));
          }





          File file = new File(Environment.getExternalStorageDirectory() + path);
          if (!file.exists()){
            //Log.d("dir:" , file.mkdirs());
            file.mkdirs();
          }
          try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            String fileSuffix = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
            File f = new File(file,"Attendance"+ fileSuffix.trim() + ".xlsx");
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            workbook.write(out);
            out.write(bytes.toByteArray());
            Toast.makeText(DetectorActivity.this,"Done ! File Saved to: " + f.getAbsolutePath(),Toast.LENGTH_LONG).show();
            uploadFile(f,fileSuffix);
            out.close();
          } catch (IOException e){
            e.printStackTrace();
          }


        }
        catch (Exception e){
          e.printStackTrace();
        }

        //Upload uids to db
        /*for (String uid: studentUids) {
          reference.child(format).setValue(studentUids.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
              Toast.makeText(DetectorActivity.this,"Uploaded !",Toast.LENGTH_LONG).show();
            }
          }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              Toast.makeText(DetectorActivity.this,"Upload Failed !",Toast.LENGTH_LONG).show();
            }
          });
        }*/

      }
    });
    builder.create().show();
  }

  private void uploadFile(File file,String time){
    Uri f = Uri.fromFile(file);
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference reference = storage.getReferenceFromUrl("gs://facetrace132.appspot.com");
    StorageReference storageReference = reference.child("Attendance" + time.trim() + ".xlsx");

    UploadTask task  = storageReference.putFile(f);
    task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
      @Override
      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        Log.w(String.valueOf(task.getResult()),"Uploaded to Database !");
        Toast.makeText(DetectorActivity.this,"Uploaded to Database !",Toast.LENGTH_SHORT).show();
      }
    }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        Log.e(String.valueOf(task.getException()),"Failed to Upload to Database !");
        Toast.makeText(DetectorActivity.this,"Upload to Database Failed !",Toast.LENGTH_SHORT).show();
      }
    });

  }


  private void onAddClick() {
    addPending = true;
  }

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);


    try {
      MainActivity.detector =
              TFLiteObjectDetectionAPIModel.create(
                      getAssets(),
                      TF_OD_API_MODEL_FILE,
                      TF_OD_API_LABELS_FILE,
                      TF_OD_API_INPUT_SIZE,
                      TF_OD_API_IS_QUANTIZED);
      //cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing classifier!");
      Toast toast =
              Toast.makeText(
                      getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);


    int targetW, targetH;
    if (sensorOrientation == 90 || sensorOrientation == 270) {
      targetH = previewWidth;
      targetW = previewHeight;
    } else {
      targetW = previewWidth;
      targetH = previewHeight;
    }
    int cropW = (int) (targetW / 2.0);
    int cropH = (int) (targetH / 2.0);

    croppedBitmap = Bitmap.createBitmap(cropW, cropH, Config.ARGB_8888);

    portraitBmp = Bitmap.createBitmap(targetW, targetH, Config.ARGB_8888);
    faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Config.ARGB_8888);

    frameToCropTransform =
            ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight,
                    cropW, cropH,
                    sensorOrientation, MAINTAIN_ASPECT);

//    frameToCropTransform =
//            ImageUtils.getTransformationMatrix(
//                    previewWidth, previewHeight,
//                    previewWidth, previewHeight,
//                    sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);


    Matrix frameToPortraitTransform =
            ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight,
                    targetW, targetH,
                    sensorOrientation, MAINTAIN_ASPECT);


    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
            new DrawCallback() {
              @Override
              public void drawCallback(final Canvas canvas) {
                tracker.draw(canvas);
                if (isDebug()) {
                  tracker.drawDebug(canvas);
                }
              }
            });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }


  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;

    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    /*if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    } */

    InputImage image = InputImage.fromBitmap(croppedBitmap, 0);
    MainActivity.faceDetector
            .process(image)
            .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
              @Override
              public void onSuccess(List<Face> faces) {
                if (faces.size() == 0) {
                  updateResults(currTimestamp, new LinkedList<>());
                  return;
                }
                runInBackground(
                        new Runnable() {
                          @Override
                          public void run() {
                            onFacesDetected(currTimestamp, faces, addPending);
                            addPending = false;
                          }
                        });
              }

            });


  }

  @Override
  protected int getLayoutId() {
    return R.layout.tfe_od_camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(() -> MainActivity.detector.setUseNNAPI(isChecked));
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> MainActivity.detector.setNumThreads(numThreads));
  }


  // Face Processing
  private Matrix createTransform(
          final int srcWidth,
          final int srcHeight,
          final int dstWidth,
          final int dstHeight,
          final int applyRotation) {

    Matrix matrix = new Matrix();
    if (applyRotation != 0) {
      if (applyRotation % 90 != 0) {
        LOGGER.w("Rotation of %d % 90 != 0", applyRotation);
      }

      // Translate so center of image is at origin.
      matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

      // Rotate around origin.
      matrix.postRotate(applyRotation);
    }

//        // Account for the already applied rotation, if any, and then determine how
//        // much scaling is needed for each axis.
//        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;
//        final int inWidth = transpose ? srcHeight : srcWidth;
//        final int inHeight = transpose ? srcWidth : srcHeight;

    if (applyRotation != 0) {

      // Translate back from origin centered reference to destination frame.
      matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
    }

    return matrix;

  }

  private void showAddFaceDialog(SimilarityClassifier.Recognition rec) {
    MainActivity.savedRec = rec;
    Intent n = new Intent(DetectorActivity.this, AddDetectedActivity.class);
    startActivity(n);
    finish();
  }

  private void updateResults(long currTimestamp, final List<SimilarityClassifier.Recognition> mappedRecognitions) {

    tracker.trackResults(mappedRecognitions, currTimestamp);
    trackingOverlay.postInvalidate();
    computingDetection = false;

    if (mappedRecognitions.size() > 0) {
      LOGGER.i("Adding results");
      SimilarityClassifier.Recognition rec = mappedRecognitions.get(0);


      //Showing Add Face Dialog
      if (rec.getExtra() != null) {
        showAddFaceDialog(rec);

      }

    }

    /*runOnUiThread(
            new Runnable() {
              @Override
              public void run() {
                showFrameInfo(previewWidth + "x" + previewHeight);
                showCropInfo(croppedBitmap.getWidth() + "x" + croppedBitmap.getHeight());
                showInference(lastProcessingTimeMs + "ms");
              }
            });
 */
  }

  private void onFacesDetected(long currTimestamp, List<Face> faces, boolean add) {

    cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
    final Canvas canvas = new Canvas(cropCopyBitmap);
    final Paint paint = new Paint();
    paint.setColor(Color.RED);
    paint.setStyle(Style.STROKE);
    paint.setStrokeWidth(2.0f);

    float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
    switch (MODE) {
      case TF_OD_API:
        minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
        break;
    }

    final List<SimilarityClassifier.Recognition> mappedRecognitions =
            new LinkedList<SimilarityClassifier.Recognition>();


    //final List<Classifier.Recognition> results = new ArrayList<>();

    // Note this can be done only once
    int sourceW = rgbFrameBitmap.getWidth();
    int sourceH = rgbFrameBitmap.getHeight();
    int targetW = portraitBmp.getWidth();
    int targetH = portraitBmp.getHeight();
    Matrix transform = createTransform(
            sourceW,
            sourceH,
            targetW,
            targetH,
            sensorOrientation);
    final Canvas cv = new Canvas(portraitBmp);

    // draws the original image in portrait mode.
    cv.drawBitmap(rgbFrameBitmap, transform, null);

    final Canvas cvFace = new Canvas(faceBmp);

    boolean saved = false;

    listToLoad.clear();
    for (Face face : faces) {

      LOGGER.i("FACE" + face.toString());
      LOGGER.i("Running detection on face " + currTimestamp);
      //results = detector.recognizeImage(croppedBitmap);

      final RectF boundingBox = new RectF(face.getBoundingBox());

      //final boolean goodConfidence = result.getConfidence() >= minimumConfidence;
      final boolean goodConfidence = true; //face.get;
      if (boundingBox != null && goodConfidence) {

        // maps crop coordinates to original
        cropToFrameTransform.mapRect(boundingBox);

        // maps original coordinates to portrait coordinates
        RectF faceBB = new RectF(boundingBox);
        transform.mapRect(faceBB);

        // translates portrait to origin and scales to fit input inference size
        //cv.drawRect(faceBB, paint);
        float sx = ((float) TF_OD_API_INPUT_SIZE) / faceBB.width();
        float sy = ((float) TF_OD_API_INPUT_SIZE) / faceBB.height();
        Matrix matrix = new Matrix();
        matrix.postTranslate(-faceBB.left, -faceBB.top);
        matrix.postScale(sx, sy);

        cvFace.drawBitmap(portraitBmp, matrix, null);

        //canvas.drawRect(faceBB, paint);

        String label = "";
        float confidence = -1f;
        Integer color = Color.BLUE;
        Object extra = null;
        Bitmap crop = null;

        if (add) {
          crop = Bitmap.createBitmap(portraitBmp,
                  (int) faceBB.left,
                  (int) faceBB.top,
                  (int) faceBB.width(),
                  (int) faceBB.height());

        }

        final long startTime = SystemClock.uptimeMillis();
        MainActivity.resultsAux = MainActivity.detector.recognizeImage(faceBmp, add);
        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;


        if (MainActivity.resultsAux.size() > 0) {

          SimilarityClassifier.Recognition result = MainActivity.resultsAux.get(0);

          extra = result.getExtra();
//          Object extra = result.getExtra();
          if (extra != null) {
            LOGGER.e("embeeding retrieved " + extra.toString());
          }

          float conf = result.getDistance();
          if (conf < 1.0f) {

            confidence = conf;
            label = result.getTitle();
            if (result.getId().equals("0")) {
              color = Color.GREEN;
            } else {
              color = Color.RED;
            }
          }

        }

        if (getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT) {

          // camera is frontal so the image is flipped horizontally
          // flips horizontally
          Matrix flip = new Matrix();
          if (sensorOrientation == 90 || sensorOrientation == 270) {
            flip.postScale(1, -1, previewWidth / 2.0f, previewHeight / 2.0f);
          } else {
            flip.postScale(-1, 1, previewWidth / 2.0f, previewHeight / 2.0f);
          }
          //flip.postScale(1, -1, targetW / 2.0f, targetH / 2.0f);
          flip.mapRect(boundingBox);

        }

        //After Recognition//
        final SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                "0", label, confidence, boundingBox);

        //SimilarityClassifier.Recognition recognition= new SimilarityClassifier.Recognition()

        Log.e("LabelDetected", label);
        if (!label.isEmpty()) {
          listToLoad.add(label);

          if (!studentUids.contains(label)){
            studentUids.add(label);
            String search = "WHERE codeMeli IN ('" + label + "')";
            String no = MainActivity.db.getString(query + " " + search, "id");  //getListOfRow(query + " " + search, "id")
            studentNames.add(db.getString(
                    "SELECT * FROM persons WHERE id = '"
                            + no +
                            "'", "firstName") +
                    " " + db.getString(
                            "SELECT * FROM persons WHERE id = '"
                                    + no
                                    + "'", "lastName"));
            Log.e("SearchLabel:",search);
            Log.e("Label No:",no);

          }

          //generateXL(listToLoad);
          Uid = label;


          result.setColor(color);
          result.setLocation(boundingBox);
          result.setExtra(extra);
          result.setCrop(crop);
          mappedRecognitions.add(result);

        }


      }

      updateResults(currTimestamp, mappedRecognitions);


     // if (!scanned) {
        String text = "";
        for (int i = 0; i < listToLoad.size(); i++) {
          if (i == 0) {
            text = listToLoad.get(i);
          } else {
            text = text + "," + listToLoad.get(i);
          }
        }
        String search = "WHERE codeMeli IN ('" + text + "')";

        Log.e("SearchText", search);
      for (int i = 0; i < studentUids.size(); i++) {
        Log.e("Uids: ",studentUids.get(i));
        Log.e("Names: ",studentNames.get(i));
      }

        initRecyclerView(search);

     // }
    }
  }

  /*public void generateXL(ArrayList<String> studentUids){
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Students"); //Creating a sheet

    for(int  i=0; i<listToLoad.size(); i++){

      Row row = sheet.createRow(i);
      row.createCell(CELL_INDEX_0).setCellValue(VALUE_YOU_WANT_TO_KEEP_ON_1ST_COLUMN);
      row.createCell(CELL_INDEX_1).setCellValue(VALUE_YOU_WANT_TO_KEEP_ON_2ND_COLUMN);
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
    Date now = new Date();


    String fileName = "studentAttendance:" + formatter.format(now) +  ".xlsx"; //Name of the file

    String extStorageDirectory = Environment.getExternalStorageDirectory()
            .toString();
    File folder = new File(extStorageDirectory, "Attendance");// Name of the folder you want to keep your file in the local storage.
    folder.mkdir(); //creating the folder
    File file = new File(folder, fileName);
    try {
      file.createNewFile(); // creating the file inside the folder
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    try {
      FileOutputStream fileOut = new FileOutputStream(file); //Opening the file
      workbook.write(fileOut); //Writing all your row column inside the file
      fileOut.close(); //closing the file and done

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }*/


  public void initRecyclerView(String searchQuery) {
    scanned = true;

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        LinearLayoutManager llm = new LinearLayoutManager(DetectorActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rec.setLayoutManager(llm);
        detectedAdapter = new DetectedAdapter(DetectorActivity.this, MainActivity.db.getListOfRow(query + " " + searchQuery, "id"));
        rec.setAdapter(detectedAdapter);


        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            rec.setVisibility(View.VISIBLE);
            studentAddList.setVisibility(View.VISIBLE);
          }
        }, 300);

        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
              scanned = false;
          }
        }, 3000);
      }
    });

  }


  }
