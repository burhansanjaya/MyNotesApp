package com.dicoding.mynotesapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.dicoding.mynotesapp.adapter.NoteAdapter;
import com.dicoding.mynotesapp.db.NoteHelper;
import com.dicoding.mynotesapp.entity.Note;
import com.dicoding.mynotesapp.helper.MappingHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.bluetooth.BluetoothAdapter.EXTRA_STATE;

/*
    * Menampilkan data dari database pada table note secara ascending
    * Menerima umpan balik setiap aksi (Intent dengan Request Code) dan proses yang dilakukan di class NoteAddUpdateActivity.
 */
public class MainActivity extends AppCompatActivity implements LoadNotesCallback {

    private ProgressBar progressBar;
    private RecyclerView rvNotes;
    private NoteAdapter adapter;
    private FloatingActionButton fabAdd;
    //mengambil data dari database menggunakan class NoteHelper
    private NoteHelper noteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Notes");

            //setting adapter
            progressBar = findViewById(R.id.progressbar);
            rvNotes = findViewById(R.id.rv_notes);
            rvNotes.setLayoutManager(new LinearLayoutManager(this));
            rvNotes.setHasFixedSize(true);
            adapter = new NoteAdapter(this);
            rvNotes.setAdapter(adapter);
        }
        //create tombol add
        fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent pindah ke NoteAddUpdateActivity dengan membawa ID request_add
                Intent intent = new Intent(MainActivity.this, NoteAddUpdateActivity.class);
                startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD);
                Log.d("Main - REQ_ADD", "Debug : "+String.valueOf(NoteAddUpdateActivity.REQUEST_ADD));
            }
        });

        noteHelper = NoteHelper.getInstance(getApplicationContext());
        //membuka database
        noteHelper.open();

        /*
            * Methode onSaveInstanceState bakal menyimpan arraylist
            * Ketika layar dirotasi maka aplikasi tidak memanggil ulang proses dari database
            * Pada methode onCreate, kita ambil saveInstanceState jika tersedia
         */
        if (savedInstanceState == null){
            //memulai proses ambil data dari database menggunakan teknik background thread
            new LoadNotesAsync(noteHelper, this).execute();
        }else{
            ArrayList<Note> list = savedInstanceState.getParcelableArrayList(EXTRA_STATE);
            if (list != null){
                adapter.setListNotes(list);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null){
            /*
                * Akan dipanggil jika request codenya ADD
                * Dijalankan ketika berhasil insert data
             */
            if (requestCode == NoteAddUpdateActivity.REQUEST_ADD){
                Log.d("Main - REQ_ADD", "Debug REQUEST_ADD : " + NoteAddUpdateActivity.REQUEST_ADD + "RequestCode : " + requestCode);
                if (resultCode == NoteAddUpdateActivity.RESULT_ADD){
                    Log.d("Main - RES_ADD", "Debug RESULT_ADD : " + NoteAddUpdateActivity.RESULT_ADD + "resultCode : " + resultCode);
                    Note note = data.getParcelableExtra(NoteAddUpdateActivity.EXTRA_NOTE);

                    //memasukkan data note ke dalam adapater
                    adapter.addItem(note);
                    rvNotes.smoothScrollToPosition(adapter.getItemCount() -1);

                    //menampilkan data seperti toast
                    showSnackbarMessage("Satu Item Berhasil Ditambahkan");
                }
            }
            //update dan delete memiliki request code sama akan tetapi result codenya berbeda
            else if(requestCode == NoteAddUpdateActivity.REQUEST_UPDATE){
                Log.d("Main - REQ_UPDATE", "Debug REQUEST_UPDATE: "+String.valueOf(NoteAddUpdateActivity.REQUEST_UPDATE + "RequestCode : " + requestCode));
                if (resultCode == NoteAddUpdateActivity.RESULT_UPDATE){

                    Note note = data.getParcelableExtra(NoteAddUpdateActivity.EXTRA_NOTE);
                    int position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0);
                    Log.d("Main - RES_UPDATE", "Debug : "+NoteAddUpdateActivity.RESULT_UPDATE);

                    adapter.updateItem(position, note);
                    rvNotes.smoothScrollToPosition(position);

                    showSnackbarMessage("Satu Item Berhasil Diubah");
                }else if(resultCode == NoteAddUpdateActivity.RESULT_DELETE){
                    int position  = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0);
                    Log.d("Main - RES_DELETE", "Debug : "+NoteAddUpdateActivity.RESULT_DELETE);
                    adapter.removeItem(position);
                    showSnackbarMessage("Satu Item Barhasil Dihapus");
                }
            }
        }
    }

    private void showSnackbarMessage(String message){
        Snackbar.make(rvNotes, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //menutup database
        noteHelper.close();
    }

    @Override
    public void preExecute() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void postExecute(ArrayList<Note> notes) {
        progressBar.setVisibility(View.INVISIBLE);
        if (notes.size() > 0){
            adapter.setListNotes(notes);
        }else{
            adapter.setListNotes(new ArrayList<Note>());
            showSnackbarMessage("Tidak Ada Saat Ini");
        }
    }

    private static class LoadNotesAsync extends AsyncTask<Void, Void, ArrayList<Note>> {

        private final WeakReference<NoteHelper> weakNoteHelper;
        private final WeakReference<LoadNotesCallback> weakCallback;

        private LoadNotesAsync(NoteHelper noteHelper, LoadNotesCallback callback){
            weakNoteHelper = new WeakReference<>(noteHelper);
            weakCallback = new WeakReference<>(callback);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weakCallback.get().preExecute();
        }

        @Override
        protected ArrayList<Note> doInBackground(Void... voids) {
            Cursor dataCursor = weakNoteHelper.get().queryAll();
            return MappingHelper.mapCursorToArrayList(dataCursor);
        }

        @Override
        protected void onPostExecute(ArrayList<Note> notes) {
            super.onPostExecute(notes);

            weakCallback.get().postExecute(notes);
        }
    }
}

interface LoadNotesCallback{
    void preExecute();
    void postExecute(ArrayList<Note> notes);
}
