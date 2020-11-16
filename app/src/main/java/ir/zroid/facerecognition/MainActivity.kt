package ir.zroid.facerecognition

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.mlkit.vision.face.FaceDetector
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import io.paperdb.Paper
import ir.zroid.facerecognition.database.MyDatabase
import ir.zroid.facerecognition.face.tflite.SimilarityClassifier
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    var a = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = MyDatabase(this)
        context = this
        Paper.init(context)


        if (getMap().size != 0) {
            registered = getMap()
        } else {
            registered = HashMap()
        }



        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)



        val permissionlistener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(this@MainActivity, "Permissions Granted", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(
                    this@MainActivity,
                    "Permissions Not Granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("We" +
                    " need you to accept permissions to run app")
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .check()

    }

    override fun onSupportNavigateUp() : Boolean {
        onBackPressed()
        return true
    }


    companion object {
        lateinit var context: Context
        lateinit var db: MyDatabase
        lateinit var registered: HashMap<String, SimilarityClassifier.Recognition>
        lateinit var detector: SimilarityClassifier
        lateinit var faceDetector: FaceDetector
        lateinit var savedRec: SimilarityClassifier.Recognition
        lateinit var resultsAux: List<SimilarityClassifier.Recognition>
        lateinit var recognitions: ArrayList<SimilarityClassifier.Recognition>


        fun saveMap() {
            Paper.book("faces").write("data", registered)
            getMap()
        }

        fun getMap() : HashMap<String, SimilarityClassifier.Recognition> {
            if (Paper.book("faces").contains("data")) {
                registered = Paper.book("faces").read("data")
            } else {
                registered = HashMap()
            }
            return registered
        }


    }


}