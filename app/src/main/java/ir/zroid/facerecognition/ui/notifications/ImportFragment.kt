package ir.zroid.facerecognition.ui.notifications

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.paperdb.Paper
import ir.zroid.facerecognition.MainActivity
import ir.zroid.facerecognition.R
import ir.zroid.facerecognition.database.MyExternalDatabase
import ir.zroid.facerecognition.face.resources.ZRoidFile
import ir.zroid.facerecognition.face.resources.ZipArchive
import ir.zroid.facerecognition.face.tflite.SimilarityClassifier
import kotlinx.android.synthetic.main.fragment_import.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ImportFragment : Fragment() {
    val des = File(ZRoidFile.DATABASE_BACKUP)
    val databaseFile = File(ZRoidFile.FOLDER_PATH + "/backup.zip")
    //val pt = File(Paper.book("faces").getPath("data")).path
    lateinit var externalDB: MyExternalDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (databaseFile.exists()){
            tv_status.text = "File found!"
            tv_status.setTextColor(resources.getColor(R.color.colorGreen))
            btn_submit.isEnabled = true
        } else {
            tv_status.text = "File not found"
            tv_status.setTextColor(resources.getColor(R.color.colorRed))
            btn_submit.isEnabled = false
        }

        btn_submit.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            btn_submit.isEnabled = false
            Handler().postDelayed({
                doImport()
            }, 300)
        }
    }



    fun doImport(){

        CoroutineScope(Dispatchers.Main).launch {
            val zipArchive = ZipArchive()
            zipArchive.unzip(ZRoidFile.FOLDER_PATH + File.separator + "backup.zip",ZRoidFile.FOLDER_PATH + File.separator + "backup/","marz4030")
            //File(ZRoidFile.root,ZRoidFile.FOLDER_PATH + File.separator + "backup.zip").deleteRecursively()
        }.invokeOnCompletion {
            CoroutineScope(Dispatchers.Main).launch {
                externalDB = MyExternalDatabase(requireContext())
                externalDB.getListOfRow("SELECT * FROM persons","codeMeli").forEachIndexed { index, it ->

                    Log.e("OnCount", "Item : " + index)

                    val firstName = externalDB.getString("SELECT * FROM persons WHERE codeMeli = '$it'", "firstName")
                    val lastName = externalDB.getString("SELECT * FROM persons WHERE codeMeli = '$it'", "lastName")

                    MainActivity.db.Query(
                        "INSERT INTO persons (firstName, lastName, codeMeli) VALUES ('" + firstName + "', '" + lastName + "', '" + it + "')"
                    )

                    File(ZRoidFile.BACKUP_IMAGES + it + ".png").copyTo(File(ZRoidFile.root + File.separator + it + ".png"), true)

                    val backupPaper = Paper.bookOn(ZRoidFile.BACKUP_ROOM, "faces")
                    val paper = Paper.book("faces")
                    var registered: HashMap<String, SimilarityClassifier.Recognition> = backupPaper.read("data")


                    if (backupPaper.contains("data")){

                        if (registered.containsKey(it)){
                            MainActivity.registered.put(it, registered.get(it)!!)
                            MainActivity.saveMap()
                        }

                    }

                }
            }.invokeOnCompletion {
                File(ZRoidFile.BACKUP_FOLDER).deleteRecursively()
                File(ZRoidFile.FOLDER_PATH + "backup.zip").deleteRecursively()
                progressBar.visibility = View.GONE
                btn_submit.isEnabled = true
                Toast.makeText(requireContext(), "Import Success", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun step_one(){

    }
}