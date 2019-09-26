package com.example.sony.chatme.ui.people

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.sony.chatme.R

class PeopleFragment : Fragment() {

    private lateinit var peopleViewModel: PoepleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        peopleViewModel =
            ViewModelProviders.of(this).get(PoepleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_people, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        peopleViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}