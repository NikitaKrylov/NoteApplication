package com.example.noteapplication

import com.example.noteapplication.dock_instrument.ColorPickerFragment
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.noteapplication.databinding.ActivityNoteBinding
import com.example.noteapplication.model.Note
import com.example.noteapplication.tools.NoteDate
import com.example.noteapplication.viewmodel.NoteViewModel

class NoteActivity : AppCompatActivity() {

    private lateinit var mNoteViewModel : NoteViewModel
    private lateinit var titleInput : EditText
    private lateinit var textInput : EditText
    private var IS_SAVE_MOD : Boolean = false
    private var NOTE_ID : Int ?= null
    private lateinit var backgroundSurface : View
    private var currentNote : Note ?= null
    private lateinit var binding:ActivityNoteBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)

        overridePendingTransition(R.anim.slide_to_top, R.anim.no_animation)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        mNoteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        titleInput = findViewById(R.id.noteTitle)
        textInput = findViewById(R.id.noteText)
        backgroundSurface = findViewById(R.id.note_background)

        val arguments = intent.extras
        if (arguments != null) {
            IS_SAVE_MOD = arguments.getBoolean("IS_SAVE_MOD")
            if (IS_SAVE_MOD) {
                NOTE_ID = arguments.getInt("NOTE_ID")
                currentNote = mNoteViewModel.getById(NOTE_ID!!)
                setDataToView(currentNote!!)
            }
        } else {
            backgroundSurface.setBackgroundColor(getColor(R.color.default_note_background))
            supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.default_note_background)))
        }
    }

    //Set up data into fields
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setDataToView(note:Note){
        ColorPickerFragment.setBackgroundColor(this, note.backgroundColorId)
        titleInput.setText(note.title)
        textInput.setText(note.text)
        backgroundSurface.setBackgroundColor(getColor(note.backgroundColorId))
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(note.backgroundColorId)))
    }

    //Main save note function
    private fun saveNote(){
        val title = titleInput.text
        val text = textInput.text

        var backgroundColorId : Int =  R.color.default_note_background
        if (ColorPickerFragment.backgroundColorId != null)
            backgroundColorId = ColorPickerFragment.backgroundColorId!!

        if (title.isEmpty() && text.isEmpty()) return

        if (IS_SAVE_MOD){
            updateNote(NOTE_ID, title, text, backgroundColorId)
        }
        else{
            createNote(title, text, backgroundColorId)
        }
        ColorPickerFragment.backgroundColorId = null
    }


    private fun updateNote(noteId:Int?, title : Editable, text:Editable, backgroundColorId:Int){
        if (noteId != null){
            currentNote = Note(noteId, title.toString(), text.toString(), backgroundColorId, NoteDate().toString(), 0)
            if (mNoteViewModel.getById(noteId) == currentNote) return
            mNoteViewModel.update(currentNote!!)
        }
    }

    private fun createNote(title : Editable, text:Editable, backgroundColorId:Int){
        currentNote = Note(0, title.toString(), text.toString(), backgroundColorId, NoteDate().toString(), 0)
        mNoteViewModel.add(currentNote!!)
    }

    private fun deleteNote(note: Note){
        mNoteViewModel.delete(note)
        notificateSaveAction("Deleted successfully")
        finish()
        overridePendingTransition(R.anim.slide_to_bottom, R.anim.no_animation)
    }

    private fun createDeleteDialog(){
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Do you want to delete note?")
            setPositiveButton("Ok", DialogInterface.OnClickListener { _, _ ->
                if (IS_SAVE_MOD) deleteNote(mNoteViewModel.getById(NOTE_ID!!))
                else
                    finish()
                    overridePendingTransition(R.anim.no_animation, R.anim.slide_to_bottom)
            })
            setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, _ ->  dialogInterface.cancel()})
        }
        builder.create().show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        saveNote()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.updater_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> onBackPressed()
            R.id.delete_menu_btn -> {
                createDeleteDialog()
                return true
            }
            R.id.share_note_btn ->{
                intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, titleInput.text.toString() + "\n" + textInput.text.toString())
                    type = "text/plain"
                }
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun notificateSaveAction(message:String){
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun setNavigationBarColor(activity: Activity, color:Int) {
            activity.window.navigationBarColor = color
        }

    }


}