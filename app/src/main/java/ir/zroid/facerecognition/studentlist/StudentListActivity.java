package ir.zroid.facerecognition.studentlist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import ir.zroid.facerecognition.R;
import android.widget.Toast;

import com.airbnb.lottie.L;

import java.util.ArrayList;

import ir.zroid.facerecognition.MainActivity;
import ir.zroid.facerecognition.face.DetectorActivity;
import ir.zroid.facerecognition.face.adapters.DetectedAdapter;
import ir.zroid.facerecognition.face.adapters.StudentListAdapter;
import ir.zroid.facerecognition.face.tflite.SimilarityClassifier;

public class StudentListActivity extends AppCompatActivity {

   public RecyclerView studentListrec;
   public StudentListAdapter detectedAdapter;
   public DetectedAdapter detectedStudents;
    public String query = "SELECT * FROM persons";
   Boolean scanned=false;
    ArrayList<String> listToLoad = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        studentListrec = findViewById(R.id.studentList);



        listToLoad.clear();


        String label = "";
        float confidence = -1f;
        Object extra = null;

        //Get Student List here and set names and images from DetectedAdapter.java

        if (MainActivity.resultsAux.size() > 0) {

            SimilarityClassifier.Recognition result = MainActivity.resultsAux.get(0);

            extra = result.getExtra();

            listToLoad = (ArrayList<String>) getIntent().getSerializableExtra("key");

//          Object extra = result.getExtra();
            if (extra != null) {
                //LOGGER.e("embeeding retrieved " + extra.toString());
            }

            float conf = result.getDistance();
            if (conf < 1.0f) {

                confidence = conf;
                label = result.getTitle();
            }

        }

        String name= label+"Added To List";

        Log.e("LabelDetected", label);
        if (!label.isEmpty()){
            listToLoad.add(label);
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), name, Toast.LENGTH_LONG);
            toast.show();
        }
            String text = "";
            for (int i = 0; i < listToLoad.size(); i++) {
                if (i == 0){
                    text = listToLoad.get(i);
                } else {
                }
            }
            String search = "WHERE codeMeli IN ('" + text + "')";

            Log.e("SearchText", search);

        for (int i = 0; i < listToLoad.size(); i++) {
            Log.e("List: ",listToLoad.get(i));
        }




        //LinearLayoutManager llm = new LinearLayoutManager(StudentListActivity.this, LinearLayoutManager.VERTICAL, false);
        //studentListrec.setLayoutManager(llm);
        //detectedAdapter = new StudentListAdapter(StudentListActivity.this, MainActivity.db.getListOfRow(query + " " + search, "id"));
        //studentListrec.setAdapter(detectedStudents);

            initRecyclerView(search);




    }

    public void uploadAttendance(View view) {
        //Put Backend Code here for Getting Student list and uploading to firebase db
    }

    public void initRecyclerView(String search) {
        //scanned = true;
        studentListrec.setVisibility(View.VISIBLE);

                String text = "";
                for (int i = 0; i < listToLoad.size(); i++) {
                    if (i == 0){
                        text = listToLoad.get(i);
                    } else {
                        text = text + "," + listToLoad.get(i);
                    }
                }

               // int i=0;
                //for (i=0;i<DetectorActivity.studentUids.size(); i++){
                    //studentListrec.setVisibility(View.VISIBLE);
                    //String search = "WHERE codeMeli IN ('" + text + "')";

                    Log.e("SearchText", search);



                    LinearLayoutManager llm = new LinearLayoutManager(StudentListActivity.this, LinearLayoutManager.HORIZONTAL, false);
                    studentListrec.setLayoutManager(llm);
                    detectedStudents = new DetectedAdapter(StudentListActivity.this, MainActivity.db.getListOfRow(query + " " + search, "id"));
                    studentListrec.setAdapter(detectedStudents);

                //    detectedAdapter.notifyDataSetChanged();
                //}



       /* String name= Uid +" Added To List";
        if(!Uid.isEmpty()) {
          Toast toast = Toast.makeText(getApplicationContext(), name, Toast.LENGTH_LONG);
          toast.show();
        }*/
      /*  new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            scanned = false;
          }
        }, 3000);*/


    }

}