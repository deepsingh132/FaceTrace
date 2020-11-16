package ir.zroid.facerecognition.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import ir.zroid.facerecognition.R
import ir.zroid.facerecognition.activities.AdapterPersons
import ir.zroid.facerecognition.database.MyDatabase
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment() {
    var db: MyDatabase? = null
    var isSearch: Boolean? = null
    var query = "SELECT * FROM persons"
    var adapterPersons: AdapterPersons? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        db = MyDatabase(requireContext())

        val llm = LinearLayoutManager(requireContext())
        recPersons!!.layoutManager = llm
        adapterPersons =
            AdapterPersons(requireContext(), db!!.getListOfRow(query, "id"))
        recPersons!!.adapter = adapterPersons


        fab.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_navigation_dashboard_to_searchFragment)
        }
    }


}