package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class MyDocumentsActivity : BaseActivity() {

    private lateinit var container: LinearLayout
    private lateinit var addButton: Button
    private val storageRef = FirebaseStorage.getInstance().reference.child("documents")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContentLayout(R.layout.activity_my_documents)

        container = findViewById(R.id.documentsContainer)
        addButton = findViewById(R.id.addDocumentButton)

        addButton.setOnClickListener {
            startActivity(Intent(this, AddDocumentActivity::class.java))
        }

        loadDocumentsFromStorage()
    }

    private fun loadDocumentsFromStorage() {
        container.removeAllViews()

        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    item.downloadUrl.addOnSuccessListener { uri ->
                        val imageView = ImageView(this@MyDocumentsActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                600
                            ).apply {
                                setMargins(0, 16, 0, 16)
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                        Glide.with(this@MyDocumentsActivity).load(uri).into(imageView)
                        container.addView(imageView)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "שגיאה בטעינת המסמכים", Toast.LENGTH_SHORT).show()
            }
    }
}
