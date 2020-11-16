package ir.zroid.facerecognition.ui.notifications

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import io.paperdb.Paper
import ir.hamsaa.persiandatepicker.Listener
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import ir.zroid.facerecognition.R
import ir.zroid.facerecognition.database.PrepairDatabase
import ir.zroid.facerecognition.face.resources.ZRoidFile
import ir.zroid.facerecognition.face.resources.ZipArchive
import kotlinx.android.synthetic.main.fragment_backup.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupFragment : Fragment() {

    val pt = File(Paper.book("faces").getPath("data")).path
    val des = File(ZRoidFile.DATABASE_BACKUP)

    lateinit var DoneDes: File
    var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_backup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_submit.setOnClickListener {
            doExport()
        }
    }


    fun doExport() {
        dialog = ProgressDialog.show(
            requireContext(), "",
            "Loading... Please Wait ...", true
        )

        dialog!!.show()

        Handler().postDelayed({
            CoroutineScope(Dispatchers.Main).launch {

                File(ZRoidFile.DATABASE_BACKUP).mkdir()
                exportDB()


            }.invokeOnCompletion {

                CoroutineScope(Dispatchers.Main).launch {
                    File(pt).copyTo(File(ZRoidFile.root + File.separator + "/room" + File.separator + "/faces" + File.separator + "data.pt"), true)
                    Log.e("PT FIle", pt)
                }.invokeOnCompletion {
                    CoroutineScope(Dispatchers.Main).launch {
                        val zipArchive = ZipArchive()
                        zipArchive.zip(
                            ZRoidFile.root,
                            ZRoidFile.FOLDER_PATH + File.separator + "backup.zip","")
                    }.invokeOnCompletion {
                        File(ZRoidFile.root + File.separator + "/room").deleteRecursively()
                        //File(DoneDes.path).deleteRecursively()
                        File(ZRoidFile.DATABASE_BACKUP).deleteRecursively()

                        Toast.makeText(requireContext(), "Backup completed", Toast.LENGTH_SHORT).show()
                        dialog!!.dismiss()

                        share()

                    }
                }
            }
        }, 150)

    }


    private fun exportDB() {

        try {

            //Existing DB Path
            val DB_PATH = "/data/ir.zroid.facerecognition/databases/facedance.db";//""

            val DATA_DIRECTORY = Environment.getDataDirectory()
            val INITIAL_DB_PATH = File(DATA_DIRECTORY, DB_PATH)

            //COPY DB PATH
            val EXTERNAL_DIRECTORY: File = Environment.getExternalStorageDirectory()
           // val COPY_DB = "/mynewfolder/mydb.db"
            val COPY_DB_PATH = File(ZRoidFile.DATABASE_BACKUP, "facedance.db")//"marzbani.db")

            File(COPY_DB_PATH.parent!!).mkdirs()
            val srcChannel = FileInputStream(INITIAL_DB_PATH).channel

            val dstChannel = FileOutputStream(COPY_DB_PATH).channel
            dstChannel.transferFrom(srcChannel,0,srcChannel.size())
            srcChannel.close()
            dstChannel.close()

        } catch (excep: Exception) {
            Toast.makeText(requireContext(),"ERROR IN COPY $excep",Toast.LENGTH_LONG).show()
            Log.e("FILECOPYERROR>>>>",excep.toString())
            excep.printStackTrace()
        }

    }

    private fun share(){
        val file = File(ZRoidFile.FOLDER_PATH + "/backup.zip")
        if (file.exists()){
            try {
                val intent = ShareCompat.IntentBuilder.from(requireActivity()).setType("application/pdf").setStream(Uri.fromFile(file))
                    .createChooserIntent().addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            }catch (e:java.lang.Exception){
                e.printStackTrace()
            }
        } else {
            Log.e("DoneDes", file.path)
            Toast.makeText(requireContext(), "فایل مورد نظر یافت نشد", Toast.LENGTH_LONG).show()
        }
    }
}