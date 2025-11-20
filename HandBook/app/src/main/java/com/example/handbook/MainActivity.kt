package com.example.handbook

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var bookList: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private val books = mutableListOf<File>()

    private val openBookLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { handleBookImport(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnNew = findViewById<Button>(R.id.btnNew)
        val btnOpen = findViewById<Button>(R.id.btnOpen)
        bookList = findViewById(R.id.bookList)

        val dbHelper = TemplateDbHelper(this)
        dbHelper.ensureDefaultTemplates(this)

        // ✅ Initialize adapter *before* refreshing
        bookList.layoutManager = GridLayoutManager(this, 2)
        bookAdapter = BookAdapter(books, dbHelper, this)
        bookList.adapter = bookAdapter

        // ✅ Now safe to call
        refreshBookList()

        btnNew.setOnClickListener { showTemplateDialog(dbHelper) }
        btnOpen.setOnClickListener { openBookLauncher.launch("*/*") }
    }


    private fun refreshBookList() {
        books.clear()
        val booksDir = File(getExternalFilesDir(null), "books")
        if (!booksDir.exists()) booksDir.mkdirs()

        books.addAll(booksDir.listFiles { file -> file.extension == "book" } ?: emptyArray())
        bookAdapter.notifyDataSetChanged()
    }

    private fun showTemplateDialog(dbHelper: TemplateDbHelper) {
        val templates = dbHelper.getAllTemplates()

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_template, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.templateSpinner)
        val preview = dialogView.findViewById<ImageView>(R.id.templatePreview)
        val okBtn = dialogView.findViewById<Button>(R.id.btnOk)

        if (templates.isEmpty()) {
            Toast.makeText(this, "No templates found!", Toast.LENGTH_LONG).show()
            return
        }

        val names = templates.map { it.name }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val imagePath = templates[position].imagePath
                if (!imagePath.isNullOrBlank()) {
                    val resId = resources.getIdentifier(imagePath, "drawable", packageName)
                    when {
                        resId != 0 -> preview.setImageResource(resId) // ✅ Built-in drawable
                        File(imagePath).exists() -> preview.setImageURI(Uri.fromFile(File(imagePath))) // ✅ External image
                        else -> preview.setImageResource(android.R.drawable.ic_menu_gallery) // fallback
                    }
                } else {
                    preview.setImageResource(android.R.drawable.ic_menu_gallery)
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        okBtn.setOnClickListener {
            dialog.dismiss()
            val selectedTemplate = templates[spinner.selectedItemPosition]
            Toast.makeText(this, "Selected Template: ${selectedTemplate.name}", Toast.LENGTH_SHORT)
                .show()

            // ✅ Launch NewReportActivity with selected template name
            val intent = Intent(this, NewReportActivity::class.java)
            intent.putExtra("TEMPLATE_NAME", selectedTemplate.name)
            startActivity(intent)
        }

        dialog.show()
    }

    private fun handleBookImport(uri: Uri) {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val fileName = getFileName(uri)
        val destDir = File(getExternalFilesDir(null), "books")
        if (!destDir.exists()) destDir.mkdirs()

        val destFile = File(destDir, fileName)
        inputStream?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        Toast.makeText(this, "Book imported: $fileName", Toast.LENGTH_SHORT).show()
        refreshBookList()
        Toast.makeText(this, "Opening $fileName ...", Toast.LENGTH_SHORT).show()
    }

    private fun getFileName(uri: Uri): String {
        var name = "new_book.book"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name
    }

    class BookAdapter(
        private val books: List<File>,
        private val dbHelper: TemplateDbHelper,
        private val context: Context
    ) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

        class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val preview: ImageView = view.findViewById(R.id.bookPreview)
            val title: TextView = view.findViewById(R.id.bookTitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_book_card, parent, false)
            return BookViewHolder(view)
        }

        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            val file = books[position]
            holder.title.text = file.nameWithoutExtension

            try {
                val json = JSONObject(file.readText())
                val templateId = json.optInt("template_id", -1)
                val template = dbHelper.getAllTemplates().find { it.id == templateId }

                val imagePath = template?.imagePath
                if (imagePath != null) {
                    val resId = context.resources.getIdentifier(imagePath, "drawable", context.packageName)
                    if (resId != 0) {
                        holder.preview.setImageResource(resId)
                    } else if (File(imagePath).exists()) {
                        holder.preview.setImageURI(Uri.fromFile(File(imagePath)))
                    } else {
                        holder.preview.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                } else {
                    holder.preview.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                holder.preview.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }

        override fun getItemCount(): Int = books.size
    }

}
