package ir.zroid.facerecognition.ui.recognize

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import ir.hamsaa.persiandatepicker.Listener
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import ir.zroid.facerecognition.R
import kotlinx.android.synthetic.main.fragment_search.*


class SearchFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                if (checkedId == R.id.by_name) {
                    txt_Name.visibility = View.VISIBLE
                    txt_familyName.visibility = View.VISIBLE
                    txt_codeMeli.visibility = View.GONE
                } else if (checkedId == R.id.by_codeMeli) {
                    txt_Name.visibility = View.GONE
                    txt_familyName.visibility = View.GONE
                    txt_codeMeli.visibility = View.VISIBLE
                }
            }
        })

        btn_search.setOnClickListener(View.OnClickListener {
            val bundle = Bundle()
            val codeMeli: String = txt_codeMeli.text.toString()
            val firstName: String = txt_Name.text.toString()
            val familyName: String = txt_familyName.text.toString()




            if (by_codeMeli.isChecked) {
                if (codeMeli.isNotEmpty()) {
                    bundle.putString("codeMeli", codeMeli)
                }
            } else  if (by_name.isChecked) {
                if (firstName.isNotEmpty()) {
                    bundle.putString("firstName", firstName)
                }
                if (familyName.isNotEmpty()) {
                    bundle.putString("familyName", familyName)
                }
            }

            Navigation.findNavController(it).navigate(R.id.action_searchFragment_to_personsFragment,bundle)
        })

    }
}