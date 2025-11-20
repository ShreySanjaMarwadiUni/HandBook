package com.example.handbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class CoverPageFragment : Fragment() {

    // For external inflated layouts
    private var externalView: View? = null

    companion object {
        private const val ARG_LAYOUT_ID = "layout_id"

        // ✅ Factory for built-in (internal) layouts
        fun newInstance(layoutResId: Int): CoverPageFragment {
            return CoverPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LAYOUT_ID, layoutResId)
                }
            }
        }

        // ✅ Factory for external (file-based) layouts
        fun fromView(view: View?): CoverPageFragment {
            return CoverPageFragment().apply {
                this.externalView = view
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Priority 1: use inflated external view
        externalView?.let { return it }

        // Priority 2: fall back to built-in resource
        val layoutId = arguments?.getInt(ARG_LAYOUT_ID)
        return if (layoutId != null && layoutId != 0) {
            inflater.inflate(layoutId, container, false)
        } else {
            null
        }
    }
}
