package com.example.finalproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddDocumentActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button
    private var imageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance().reference.child("documents")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_document)

        imageView = findViewById(R.id.previewImageView)
        uploadButton = findViewById(R.id.uploadButton)

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            startActivityForResult(intent, 1001)
        }

        uploadButton.setOnClickListener {
            if (imageUri != null) {
                val fileName = UUID.randomUUID().toString() + ".jpg"
                val ref = storageRef.child(fileName)

                ref.putFile(imageUri!!)
                    .addOnSuccessListener {
                        Toast.makeText(this, "המסמך הועלה בהצלחה", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "העלאה נכשלה", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "בחר תמונה קודם", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }
}
