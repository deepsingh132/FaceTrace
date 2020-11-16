package ir.zroid.facerecognition.ui.recognize

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ir.zroid.facerecognition.R
import ir.zroid.facerecognition.activities.AdapterPersons
import ir.zroid.facerecognition.database.MyDatabase
import kotlinx.android.synthetic.main.fragment_persons.*

class PersonsFragment : Fragment() {
    var db: MyDatabase? = null
    var firstName: String? = null
    var familyName: String? = null
    var passportCode: String? = null
    var fileNumber: String? = null
    var codeMeli: String? = null
    var query = "SELECT * FROM persons WHERE"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_persons, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = MyDatabase(requireContext())

        var intent = Bundle()
        intent = requireArguments()

        val llm = LinearLayoutManager(requireContext())
        recPersons!!.layoutManager = llm

        if (!intent.getString("firstName").isNullOrEmpty()) {
            firstName = intent.getString("firstName")
            query = "$query firstName LIKE '%$firstName%'"
        }

        if (!intent.getString("familyName").isNullOrEmpty()) {
            familyName = intent.getString("familyName")

            if (!intent.getString("firstName").isNullOrEmpty()) {
                query = "$query AND lastName LIKE '%$familyName%'"
            } else {
                query = "$query lastName LIKE '%$familyName%'"
            }
        }

        if (!intent.getString("codeMeli").isNullOrEmpty()) {
            codeMeli = intent.getString("codeMeli")
            query = "$query codeMeli = '$codeMeli'"
        }

        adapterPersons =
            AdapterPersons(requireContext(), db!!.getListOfRow(query, "id"))




        recPersons!!.adapter = adapterPersons
    }

    companion object {
        var adapterPersons: AdapterPersons? = null
    }
}