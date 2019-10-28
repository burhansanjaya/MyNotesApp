package com.dicoding.mynotesapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dicoding.mynotesapp.db.NoteHelper;
import com.dicoding.mynotesapp.entity.Note;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.dicoding.mynotesapp.db.DatabaseContract.NoteColumns.TITLE;
import static com.dicoding.mynotesapp.db.DatabaseContract.NoteColumns.DESCRIPTION;
import static com.dicoding.mynotesapp.db.DatabaseContract.NoteColumns.DATE;

/*
    * Menyediakan form untuk melakukan proses input data
    * Menyediakan form untuk proses pembaruan data
    * Bila user berada proses pembaruan data maka setiap kolom pada form sudah terisi otomatis dan ikon hapus berada sudut kanan atas actionbar ditampilkan untuk menghapus data
    * Sebelum menghapus data, dialog konfirmasi akan tampil. User akan ditanya terkait penghapusan yang akan dilakukan.
    * Bila user menekan tombol back pada ActionBar maupun piranti, maka akan tampil dialog konfirmasi sebelum menutup halaman.
    *
 */
public class NoteAddUpdateActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtTitle, edtDescription;
    private Button btnSubmit;

    private boolean isEdit = false;
    private Note note;
    private int position;
    private NoteHelper noteHelper;

    public static final String EXTRA_NOTE = "extra_note";
    public static final String EXTRA_POSITION = "extra_position";

    public static final int REQUEST_ADD = 100;
    public static final int RESULT_ADD = 101;

    public static final int REQUEST_UPDATE = 200;
    public static final int RESULT_UPDATE = 201;

    public static final int RESULT_DELETE = 301;

    private final int ALERT_DIALOG_CLOSE = 10;
    private final int ALERT_DIALOG_DELETE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_add_update);

        edtTitle = findViewById(R.id.edt_title);
        edtDescription = findViewById(R.id.edt_description);
        btnSubmit = findViewById(R.id.btn_submit);

        //objek untuk menginisiasi Database
        noteHelper = NoteHelper.getInstance(getApplicationContext());

        //mengambil data dari inten
        note = getIntent().getParcelableExtra(EXTRA_NOTE);
        Log.d("AddUpdate - EXTRA_NOTE", "Debug : "+EXTRA_NOTE);

        //bila data note ada isinya maka update bila tidak maka tambah data baru
        if (note != null){
            position = getIntent().getIntExtra(EXTRA_POSITION, 0);
            isEdit = true;
        }else{
            note = new Note();
        }

        String actionBarTitle;
        String btnTitle;

        if (isEdit){
            actionBarTitle = "Ubah";
            btnTitle = "Update";

            if (note != null){
                edtTitle.setText(note.getTitle());
                edtDescription.setText(note.getDescription());
            }
        }else{
            actionBarTitle = "Tambah";
            btnTitle = "Simpan";
        }

        //setting Home Tittle
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(actionBarTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnSubmit.setText(btnTitle);

        btnSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_submit){
            String title = edtTitle.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title)){
                edtTitle.setError("Field Can Not Blank");
                return;
            }

            note.setTitle(title);
            note.setDescription(description);

            Intent intent = new Intent();
            intent.putExtra(EXTRA_NOTE, note);
            intent.putExtra(EXTRA_POSITION, position);
            Log.d("AddUpdate - EX_POSITION", "Debug : "+EXTRA_POSITION);

            ContentValues values = new ContentValues();
            values.put(TITLE, title);
            values.put(DESCRIPTION, description);

            if (isEdit){
                long result = noteHelper.update(String.valueOf(note.getId()), values);
                if (result > 0){
                    //set nilai intent update
                    setResult(RESULT_UPDATE, intent);
                    Log.d("AddUpdate - RES_UPDATE", "Debug : "+ String.valueOf(RESULT_UPDATE));
                    finish();
                }else{
                    Toast.makeText(NoteAddUpdateActivity.this, "Gagal Mengupdate Data", Toast.LENGTH_SHORT).show();
                }
            }else{
                //mengambil tanggal dan jam
                note.setDate(getCurrentDate());
                //insert data ke database
                values.put(DATE, getCurrentDate());
                long result = noteHelper.insert(values);

                if (result > 0){
                    note.setId((int) result);
                    //set nilai intent ke dalam variable result_add
                    setResult(RESULT_ADD, intent);
                    Log.d("AddUpdate - RESULT_ADD", "Debug : "+String.valueOf(RESULT_ADD));
                    finish();
                }else{
                    Toast.makeText(NoteAddUpdateActivity.this, "Gagal Menambah Data", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getCurrentDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();

        return dateFormat.format(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isEdit){
            getMenuInflater().inflate(R.menu.menu_form, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    //memberikan fungsi ketika menu diklik
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                showAlertDialog(ALERT_DIALOG_DELETE);
                break;
            case android.R.id.home:
                showAlertDialog(ALERT_DIALOG_CLOSE);
                Log.d("AddUpdate - AL_DEL ", "Debug : "+ String.valueOf(ALERT_DIALOG_DELETE));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //ketika menekan tombol back(kembali), memunculkan kembali AlertDialog. Panggil metode onBackPress

    @Override
    public void onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE);
    }

    //memunculkan dialog dan mengembalikan nilai result untuk diterima MainActivity nantinya
    private void showAlertDialog(int type){
        final boolean isDialogClose = type == ALERT_DIALOG_CLOSE;
        String dialogTitle, dialogMessage;

        if (isDialogClose){
            //batal mengapus data
            dialogTitle = "Batal";
            dialogMessage = "Apakah Anda Ingin Membatalkan Perubahan Pada Form?";
        }else{
            dialogMessage = "Apakah Anda Yakin Ingin Menghapus Item Ini ?";
            dialogTitle = "Hapus Note";
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(dialogTitle);
        alertDialogBuilder
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        // batal ketika mengklik hapus data
                        if (isDialogClose){
                            finish();
                        }else{

                            // ketika menghapus data
                            long result = noteHelper.deleteById(String.valueOf(note.getId()));
                            if (result > 0){
                                Intent intent = new Intent();
                                intent.putExtra(EXTRA_POSITION, position);
                                setResult(RESULT_DELETE, intent);
                                Log.d("AddUpdate - RES_DEL ", "Debug : "+ String.valueOf(RESULT_DELETE));
                                finish();
                            }else{
                                Toast.makeText(NoteAddUpdateActivity.this, "Gagal Menghapus Data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
