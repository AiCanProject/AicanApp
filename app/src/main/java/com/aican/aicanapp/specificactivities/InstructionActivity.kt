package com.aican.aicanapp.specificactivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.aican.aicanapp.R
import com.github.barteksc.pdfviewer.PDFView

class InstructionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction)


        val pdfView= findViewById<PDFView>(R.id.pdfViewer)
        pdfView.fromAsset("IM_ph_portable_1.pdf").load();
    }
}