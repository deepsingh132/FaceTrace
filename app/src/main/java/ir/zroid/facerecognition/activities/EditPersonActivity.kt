package ir.zroid.facerecognition.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import ir.zroid.facerecognition.MainActivity
import ir.zroid.facerecognition.R
import ir.zroid.facerecognition.database.MyDatabase
import kotlinx.android.synthetic.main.activity_edit_person.btn_add
import kotlinx.android.synthetic.main.activity_edit_person.txt_Name
import kotlinx.android.synthetic.main.activity_edit_person.txt_familyName

class EditPersonActivity : AppCompatActivity() {
    var db: MyDatabase? = null
    var codeMeli: String? = null
    var firstName: String? = null
    var lastName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_person)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)


        db = MyDatabase(this)
        val intent = intent
        codeMeli = intent.getStringExtra("codeMeli")


        getData(txt_Name, "firstName")
        getData(txt_familyName, "lastName")


        btn_add.setOnClickListener {
            start()
        }
    }

    fun start() {

        firstName = txt_Name!!.text.toString()
        lastName = txt_familyName!!.text.toString()


        if (firstName.isNullOrEmpty() || lastName.isNullOrEmpty()) {
            Toast.makeText(this, "Fill All Fields", Toast.LENGTH_LONG).show()
        } else {
            doNext()
        }
    }

    private fun doNext() {

        MainActivity.db.Query(
            "UPDATE persons SET firstName = '$firstName' , lastName = '$lastName' WHERE codeMeli = '$codeMeli'"
        )

        finish()
    }

    fun getData(textView: AppCompatEditText, Column: String) {
        val text =
            db!!.getString("SELECT * FROM persons WHERE codeMeli = '$codeMeli' ", Column)


        textView.setText(text)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}