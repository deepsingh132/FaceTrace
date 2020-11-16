package ir.zroid.facerecognition.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.zroid.facerecognition.R;
import ir.zroid.facerecognition.database.MyDatabase;

public class PersonDetailsActivity extends AppCompatActivity {
    MyDatabase db;
    AppCompatTextView tv_firstName, tv_lastName, tv_codeMeli, btn_edit;
    CircleImageView img_person;
    String codeMeli;

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
        setContentView(R.layout.activity_person_details);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);



        Intent intent = getIntent();

        db = new MyDatabase(PersonDetailsActivity.this);
        codeMeli = intent.getStringExtra("codeMeli");

        initUi();





        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonDetailsActivity.this, EditPersonActivity.class);
                intent.putExtra("codeMeli", codeMeli);
                startActivity(intent);
            }
        });




        final String root =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "FaceRecognition" + File.separator + "data";

        Picasso.get().load(new File(root + File.separator + codeMeli + ".png")).into(img_person);

        Log.e("FileData", root + File.separator + codeMeli + ".png");

    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getData(tv_firstName, "firstName");
                getData(tv_lastName, "lastName");
                getData(tv_codeMeli, "codeMeli");
            }
        }, 150);

    }

    public void getData(AppCompatTextView textView, String Column){
        String text = db.getString("SELECT * FROM persons WHERE codeMeli = '" + codeMeli + "' ", Column);
        textView.setText(text);
    }

    public void initUi(){
        tv_firstName = (AppCompatTextView)findViewById(R.id.tv_firstName);
        tv_lastName = (AppCompatTextView)findViewById(R.id.tv_lastName);
        tv_codeMeli = (AppCompatTextView)findViewById(R.id.tv_codeMeli);
        btn_edit = (AppCompatTextView)findViewById(R.id.btn_edit);
        img_person = (CircleImageView)findViewById(R.id.img_person);
    }
}