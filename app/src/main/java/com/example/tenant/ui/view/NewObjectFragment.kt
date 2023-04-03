package com.example.tenant.ui.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tenant.R

class NewObjectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_object, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewObjectFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}