package com.byteutility.dev.quickfill.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AutofillTrampolineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetPackage = intent.getStringExtra("TARGET_PACKAGE")
        val replyIntent = Intent()
        setResult(RESULT_OK, replyIntent)
        finish()
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                action = "ACTION_ADD_SPECIFIC_SNIPPET"
                putExtra("TARGET_PACKAGE", targetPackage)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        )
    }
}
