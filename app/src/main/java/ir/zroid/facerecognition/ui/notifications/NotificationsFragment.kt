package ir.zroid.facerecognition.ui.notifications

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.paperdb.Paper
import ir.zroid.facerecognition.MainActivity
import ir.zroid.facerecognition.R
import kotlinx.android.synthetic.main.fragment_notifications.*
import java.io.File


class NotificationsFragment : Fragment() {

    var counter = 0
    val root =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() + File.separator + "FaceRecognition" + File.separator + "data"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        constraint_container.setOnClickListener {
            counter++
        }

        constraint_container.setOnLongClickListener {

            if (counter == 3){
                tv_remove_all.visibility = View.VISIBLE
            }

            true
        }

        tv_remove_all.setOnClickListener {
            showDialog()
        }

        tv_backup.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_navigation_notifications_to_backupFragment)
        }

        tv_import.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_navigation_notifications_to_importFragment)
        }
    }





    fun showDialog() {
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        materialAlertDialogBuilder.setTitle("Are you sure ?")
        materialAlertDialogBuilder.setMessage("All data will be deleted")
        materialAlertDialogBuilder.setPositiveButton("DELETE", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                Paper.book("faces").delete("data")
                recursiveDelete(File(root))
                MainActivity.db.Query("DELETE FROM persons")
                Toast.makeText(requireContext(), "ALL DELETED", Toast.LENGTH_LONG).show()
                val intent: Intent = requireActivity().intent

                restartWith(intent)
            }

        })
        materialAlertDialogBuilder.setNegativeButton("بازگشت", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                Log.e("OnClick", "Negative message clicked")
            }

        })
        materialAlertDialogBuilder.show()
    }

    private fun restartWith(intent: Intent) {
        requireActivity().finish()
        requireActivity().overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit)
    }
    fun recursiveDelete(
        file: File) {
        if (file.isDirectory) {
            for (f in file.listFiles()) {
                //call recursively
                recursiveDelete(f)
            }
        }
        file.delete()
        println("Deleted file/folder: " + file.absolutePath)
    }
}