package ir.zroid.facerecognition.face.tracking

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.zroid.facerecognition.MainActivity
import ir.zroid.facerecognition.R
import ir.zroid.facerecognition.face.env.ImageUtils
import kotlinx.android.synthetic.main.activity_add_detected.*

class AddDetectedActivity : AppCompatActivity() {
    private var codeMeli: String? = null
    private var firstName: String? = null
    private var lastName: String? = null


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_detected)


        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)




        btn_add.setOnClickListener {
            start()
        }
    }

    fun start() {



        codeMeli = txt_codeMeli!!.text.toString()
        firstName = txt_Name!!.text.toString()
        lastName = txt_familyName!!.text.toString()


        if (firstName.isNullOrEmpty() || lastName.isNullOrEmpty()) {
            Toast.makeText(this, "Fill all fields please", Toast.LENGTH_LONG).show()
        } else {
            if (!codeMeli.isNullOrEmpty()) {
                doNext()
            } else {
                Toast.makeText(this, "کد ملی وارد نشده است", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun doNext() {
        MainActivity.db.Query(
            "INSERT INTO persons (firstName, lastName, codeMeli) VALUES ('" + firstName + "', '" + lastName + "', '" + codeMeli + "')"
        )

        MainActivity.detector.register(codeMeli, MainActivity.savedRec)
        ImageUtils.saveBitmap(MainActivity.savedRec.crop, "$codeMeli.png")
        finish()
    }

}