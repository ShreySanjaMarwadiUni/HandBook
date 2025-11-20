package com.example.handbook

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment

class TextPopup(
    private val editBlock: Block?,
    private val onOk: (Block, Boolean) -> Unit
) : DialogFragment() {

    private lateinit var inputEditText: EditText
    private lateinit var previewTextView: TextView
    private lateinit var indexInput: EditText
    private lateinit var fontSpinner: Spinner
    private lateinit var styleButtonsLayout: LinearLayout

    private var bold = false
    private var italic = false
    private var underline = false
    private var color = "#000000"
    private var fontStyle = "sans-serif"

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.popup_text, container, false)

        inputEditText = root.findViewById(R.id.input_edit_text)
        previewTextView = root.findViewById(R.id.preview_text)
        indexInput = root.findViewById(R.id.index_input)
        fontSpinner = root.findViewById(R.id.font_spinner)
        styleButtonsLayout = root.findViewById(R.id.style_buttons)

        // Style buttons
        val btnBold = Button(requireContext()).apply { text = "B" }
        val btnItalic = Button(requireContext()).apply { text = "I" }
        val btnUnderline = Button(requireContext()).apply { text = "U" }
        val btnColor = Button(requireContext()).apply { text = "C" }
        styleButtonsLayout.addView(btnBold)
        styleButtonsLayout.addView(btnItalic)
        styleButtonsLayout.addView(btnUnderline)
        styleButtonsLayout.addView(btnColor)

        // Font spinner
        fontSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("sans-serif","serif","monospace"))
        fontSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                fontStyle = fontSpinner.selectedItem.toString()
                wrapSelectedTag("<f=\"$fontStyle\">","</f>")
                updatePreview()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        btnBold.setOnClickListener { bold = !bold; wrapSelectedTag("<b>", "</b>"); updatePreview() }
        btnItalic.setOnClickListener { italic = !italic; wrapSelectedTag("<i>", "</i>"); updatePreview() }
        btnUnderline.setOnClickListener { underline = !underline; wrapSelectedTag("<u>", "</u>"); updatePreview() }
        btnColor.setOnClickListener {
            color = if (color=="#000000") "#FF0000" else "#000000"
            wrapSelectedTag("<c=\"$color\">", "</c>")
            updatePreview()
        }

        inputEditText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updatePreview() }
            override fun afterTextChanged(s: Editable?) {}
        })

        editBlock?.let {
            inputEditText.setText(it.content)
            indexInput.setText(it.index.toString())
            fontStyle = it.fontStyle
            color = it.color ?: "#000000"
            updatePreview()
        }

        root.findViewById<Button>(R.id.ok_btn).setOnClickListener {
            val index = indexInput.text.toString().toIntOrNull() ?: (editBlock?.index ?: 0)
            val newBlock = Block(BlockType.TEXT, inputEditText.text.toString(), fontStyle, color, index=index)
            onOk(newBlock, editBlock != null)
            dismiss()
        }

        root.findViewById<Button>(R.id.del_btn).setOnClickListener { dismiss() }

        return root
    }

    private fun updatePreview() {
        TextRenderer.renderTags(inputEditText.text.toString(), previewTextView)
    }

    private fun wrapSelectedTag(startTag: String, endTag: String) {
        val selStart = inputEditText.selectionStart
        val selEnd = inputEditText.selectionEnd
        if (selStart >= 0 && selEnd > selStart) {
            val text = inputEditText.text.toString()
            val newText = text.substring(0, selStart) + startTag + text.substring(selStart, selEnd) + endTag + text.substring(selEnd)
            inputEditText.setText(newText)
            inputEditText.setSelection(selEnd + startTag.length + endTag.length)
        }
    }
}
