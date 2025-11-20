package com.example.handbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.core.widget.doAfterTextChanged

class HeadingPopup(
    private val editBlock: Block?,
    private val onOk: (Block, Boolean) -> Unit
) : DialogFragment() {

    private lateinit var headingInput: EditText
    private lateinit var preview: TextView
    private lateinit var indexInput: EditText
    private lateinit var fontSpinner: Spinner
    private var headingSize = 1
    private var fontStyle = "sans-serif"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.popup_heading, container, false)

        headingInput = root.findViewById(R.id.heading_input)
        preview = root.findViewById(R.id.preview)
        indexInput = root.findViewById(R.id.index_input)
        fontSpinner = root.findViewById(R.id.font_spinner)

        // Setup font spinner
        fontSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("sans-serif","serif","monospace"))
        fontSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                fontStyle = fontSpinner.selectedItem.toString()
                updatePreview()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // H1-H6 buttons
        val hLayout = root.findViewById<LinearLayout>(R.id.heading_buttons)
        (1..6).forEach { i ->
            val btn = Button(requireContext())
            btn.text = "H$i"
            btn.setOnClickListener { headingSize = i; updatePreview() }
            hLayout.addView(btn)
        }

        headingInput.doAfterTextChanged { updatePreview() }

        // Fill data if editing
        editBlock?.let {
            headingSize = it.headingSize
            fontStyle = it.fontStyle
            headingInput.setText(it.content)
            indexInput.setText(it.index.toString())
            updatePreview()
        }

        root.findViewById<Button>(R.id.ok_btn).setOnClickListener {
            val index = indexInput.text.toString().toIntOrNull() ?: (editBlock?.index ?: 0)
            val newBlock = Block(BlockType.HEADING, headingInput.text.toString(), fontStyle=fontStyle, headingSize=headingSize, index=index)
            onOk(newBlock, editBlock != null)
            dismiss()
        }

        root.findViewById<Button>(R.id.del_btn).setOnClickListener {
            if (editBlock != null) onOk(editBlock, true) // deletion handled in activity
            dismiss()
        }

        return root
    }

    private fun updatePreview() {
        preview.textSize = when(headingSize){1->32f;2->28f;3->24f;4->20f;5->18f;else->16f}
        preview.typeface = android.graphics.Typeface.create(fontStyle, android.graphics.Typeface.BOLD)
        preview.text = headingInput.text.toString()
    }
}
