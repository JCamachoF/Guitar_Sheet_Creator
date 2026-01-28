package com.daza.guitarsheetcreator.activities.activities

import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daza.guitarsheetcreator.R
import com.daza.guitarsheetcreator.activities.adapters.TabBarAdapterEdit
import com.daza.guitarsheetcreator.activities.adapters.TablatureRowAdapter
import com.daza.guitarsheetcreator.activities.dataBase.AppDatabase
import com.daza.guitarsheetcreator.activities.models.TablatureBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.daza.guitarsheetcreator.activities.models.Song
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton


class TablatureViewerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var adapter: TabBarAdapterEdit
    private lateinit var db: AppDatabase

    private lateinit var song: Song

    //guardar pdf
    private lateinit var currentSongToExport: Song
    private val CREATE_FILE_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tablature_viewer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerViewBars)
        fab= findViewById(R.id.floatingActionButtonPdf)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = AppDatabase.getDatabase(this)

        val songId = intent.getIntExtra("SONG_ID", -1)
        if (songId == -1) {
            Toast.makeText(this, "No se recibió ID de canción", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            song = db.songDao().getSongById(songId)!!
            withContext(Dispatchers.Main) {
                if (song != null) {
                    val bars = parseTablature(song.tablature)
                    val isLandscape = resources.configuration.orientation == android.content.res.
                    Configuration.ORIENTATION_LANDSCAPE
                    val isTablet = resources.configuration.smallestScreenWidthDp >= 600



                    if (!isTablet) {
                        if (isLandscape) {
                            // Mostrar 2 compases por fila (modo horizontal)
                            val groupedBars = groupBarsIntoRows(bars, barsPerRow = 2)
                            recyclerView.adapter = TablatureRowAdapter(groupedBars)
                        } else {
                            // Mostrar un compás por ítem (modo vertical)

                            val groupedBars = groupBarsIntoRows(bars, barsPerRow = 1)
                            recyclerView.adapter = TablatureRowAdapter(groupedBars)

                        }
                    }else {
                        if (isLandscape) {
                            // Mostrar 2 compases por fila (modo horizontal)
                            val groupedBars = groupBarsIntoRows(bars, barsPerRow = 4)
                            recyclerView.adapter = TablatureRowAdapter(groupedBars)
                        } else {
                            // Mostrar un compás por ítem (modo vertical)
                            val groupedBars = groupBarsIntoRows(bars, barsPerRow = 3)
                            recyclerView.adapter = TablatureRowAdapter(groupedBars)



                        }
                    }



                } else {
                    Toast.makeText(this@TablatureViewerActivity, "Canción no encontrada", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        fab.setOnClickListener {
            val songId = intent.getIntExtra("SONG_ID", -1)
            if (songId == -1) {
                Toast.makeText(this, "No se recibió ID de canción", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }
            promptUserToSavePdf()
        }


    }

    private fun parseTablature(tablatureText: String): List<TablatureBar> {
        val bars = mutableListOf<TablatureBar>()
        val lines = tablatureText.lines().filter { it.isNotBlank() }

        for (i in lines.indices step 6) {
            if (i + 5 >= lines.size) break

            val bar = TablatureBar().apply {
                highE.clear()
                highE.addAll(lines[i].removePrefix("e|").chunked(3))
                B.clear()
                B.addAll(lines[i + 1].removePrefix("B|").chunked(3))
                G.clear()
                G.addAll(lines[i + 2].removePrefix("G|").chunked(3))
                D.clear()
                D.addAll(lines[i + 3].removePrefix("D|").chunked(3))
                A.clear()
                A.addAll(lines[i + 4].removePrefix("A|").chunked(3))
                lowE.clear()
                lowE.addAll(lines[i + 5].removePrefix("E|").chunked(3))
            }

            bars.add(bar)
        }

        return bars
    }

    fun groupBarsIntoRows(bars: List<TablatureBar>, barsPerRow: Int = 2): List<List<TablatureBar>> {
        val rows = mutableListOf<List<TablatureBar>>()
        for (i in bars.indices step barsPerRow) {
            val row = bars.subList(i, minOf(i + barsPerRow, bars.size))
            rows.add(row)
        }
        return rows
    }
    private fun promptUserToSavePdf() {
        currentSongToExport = song

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "${song.title}.pdf")
        }

        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                saveSongAsPdf(uri, currentSongToExport)
            }
        }
    }

    /*private fun saveSongAsPdf(uri: Uri, song: Song) {
        val bars = parseTablature(song.tablature)

        val pdfDocument = PdfDocument()
        val paint = Paint().apply {
            textSize = 12f
            typeface = Typeface.MONOSPACE
        }

        val pageWidth = 595
        val pageHeight = 842
        val lineHeight = 18
        val marginLeft = 40f
        val marginRight = 40f
        val contentWidth = pageWidth - marginLeft - marginRight

        // ✅ Ahora que paint y contentWidth existen, puedes usar measureText
        val maxCharsPerLine = (contentWidth / paint.measureText("-")).toInt()
        val lines = buildLinearTablature(bars, maxCharsPerLine)

        var yPosition = 60
        var currentPageNumber = 1

        var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create())
        val canvas = page.canvas

        // Título
        paint.textSize = 16f
        canvas.drawText("Título: ${song.title}", marginLeft, yPosition.toFloat(), paint)
        yPosition += 30
        paint.textSize = 12f

        for (line in lines) {
            if (yPosition + lineHeight > pageHeight - 40) {
                pdfDocument.finishPage(page)
                currentPageNumber++
                page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create())
                yPosition = 60
            }
            canvas.drawText(line, marginLeft, yPosition.toFloat(), paint)
            yPosition += lineHeight
        }

        pdfDocument.finishPage(page)

        try {
            contentResolver.openOutputStream(uri)?.use { output ->
                pdfDocument.writeTo(output)
            }
            Toast.makeText(this, "PDF guardado correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }*/


    private fun saveSongAsPdf(uri: Uri, song: Song) {
        val bars = parseTablature(song.tablature)

        val pdfDocument = PdfDocument()
        val paint = Paint().apply {
            textSize = 12f
            typeface = Typeface.MONOSPACE
        }

        val pageWidth = 595
        val pageHeight = 842
        val lineHeight = 18
        val marginLeft = 40f
        val marginRight = 40f
        val contentWidth = pageWidth - marginLeft - marginRight

        val maxCharsPerLine = (contentWidth / paint.measureText("-")).toInt()
        val lines = buildLinearTablature(bars, maxCharsPerLine)

        var yPosition = 60f
        var currentPageNumber = 1

        var page = pdfDocument.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
        )
        val canvas = page.canvas

        // Dibujar título
        paint.textSize = 16f
        canvas.drawText("Título: ${song.title}", marginLeft, yPosition, paint)
        yPosition += 30f
        paint.textSize = 12f

        val stringSpacing = lineHeight.toFloat()
        val totalStrings = 6  // número de cuerdas

        var lineIndex = 0
        while (lineIndex < lines.size) {
            // Cambiar página si es necesario
            if (yPosition + stringSpacing * totalStrings > pageHeight - 40) {
                pdfDocument.finishPage(page)
                currentPageNumber++
                page = pdfDocument.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                )
                yPosition = 60f
            }

            // Tomar bloque de 6 líneas (6 cuerdas)
            val currentBlock = lines.subList(lineIndex, minOf(lineIndex + 6, lines.size))
            val maxLength = currentBlock.maxOf { it.length }

            for (charIndex in 0 until maxLength) {
                for ((stringIdx, stringLine) in currentBlock.withIndex()) {
                    val x = marginLeft + charIndex * paint.measureText("-")
                    val y = yPosition + stringIdx * stringSpacing

                    val c = if (charIndex < stringLine.length) stringLine[charIndex] else ' '

                    if (c == '|') {
                        // Dibujar línea vertical atravesando las 6 cuerdas
                        val topY = yPosition
                        val bottomY = yPosition + stringSpacing * (totalStrings - 1)
                        val lineX = x + paint.measureText("|") / 2
                        canvas.drawLine(lineX, topY, lineX, bottomY, paint)
                    } else {
                        canvas.drawText(c.toString(), x, y, paint)
                    }
                }
            }

            yPosition += stringSpacing * totalStrings + 10  // espacio entre bloques
            lineIndex += 7  // 6 cuerdas + 1 línea vacía
        }

        pdfDocument.finishPage(page)

        try {
            contentResolver.openOutputStream(uri)?.use { output ->
                pdfDocument.writeTo(output)
            }
            Toast.makeText(this, "PDF guardado correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun truncateLineToFit(line: String, paint: Paint, maxWidth: Float): String {
        var endIndex = line.length
        while (endIndex > 0 && paint.measureText(line.substring(0, endIndex)) > maxWidth) {
            endIndex--
        }
        return if (endIndex < line.length) line.substring(0, endIndex) + "…" else line
    }


    //construir sheet para pdf
    fun buildLinearTablature(bars: List<TablatureBar>, maxCharsPerLine: Int = 80): List<String> {
        val outputLines = mutableListOf<String>()

        // Unir cada cuerda de todos los compases en una sola línea larga por cuerda
        val fullE = bars.joinToString("") { it.highE.joinToString("") }
        val fullB = bars.joinToString("") { it.B.joinToString("") }
        val fullG = bars.joinToString("") { it.G.joinToString("") }
        val fullD = bars.joinToString("") { it.D.joinToString("") }
        val fullA = bars.joinToString("") { it.A.joinToString("") }
        val fullLowE = bars.joinToString("") { it.lowE.joinToString("") }

        val totalLength = listOf(fullE.length, fullB.length, fullG.length, fullD.length, fullA.length, fullLowE.length).maxOrNull() ?: 0

        var currentIndex = 0
        while (currentIndex < totalLength) {
            val endIndex = minOf(currentIndex + maxCharsPerLine, totalLength)

            outputLines.add("e|" + fullE.safeSubstring(currentIndex, endIndex))
            outputLines.add("B|" + fullB.safeSubstring(currentIndex, endIndex))
            outputLines.add("G|" + fullG.safeSubstring(currentIndex, endIndex))
            outputLines.add("D|" + fullD.safeSubstring(currentIndex, endIndex))
            outputLines.add("A|" + fullA.safeSubstring(currentIndex, endIndex))
            outputLines.add("E|" + fullLowE.safeSubstring(currentIndex, endIndex))
            outputLines.add("") // espacio entre bloques

            currentIndex += maxCharsPerLine
        }

        return outputLines
    }

    fun String.safeSubstring(start: Int, end: Int): String {
        return if (start >= length) "" else this.substring(start, minOf(end, this.length))
    }




}