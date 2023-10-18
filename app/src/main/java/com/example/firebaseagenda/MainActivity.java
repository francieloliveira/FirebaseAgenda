package com.example.firebaseagenda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    EditText mEditTextID, mEditTextName, mEditTextPhone;
    Button mButtonCreate, mButtonRetrieve, mButtonUpdate, mButtonDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //EditText
        mEditTextID = findViewById(R.id.editTextID);
        mEditTextName = findViewById(R.id.editTextName);
        mEditTextPhone = findViewById(R.id.editTextPhone);

        //Buttons
        mButtonCreate = findViewById(R.id.buttonCreate);
        mButtonRetrieve = findViewById(R.id.buttonRetrieve);
        mButtonUpdate = findViewById(R.id.buttonUpdate);
        mButtonDelete = findViewById(R.id.buttonDelete);

        //Buttons Listners
        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAgendaData();
            }
        });
        mButtonRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveAgendaData();
            }
        });
        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAgendaData();
            }
        });
        mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAgendaData();
            }
        });
    }

    private void createAgendaData() {
        long ID = Long.parseLong(mEditTextID.getText().toString());
        String name = mEditTextName.getText().toString();
        String phone = mEditTextPhone.getText().toString();

        Agenda agenda = new Agenda(ID, name, phone);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();
        reference.child("agenda").push().setValue(agenda);
//        reference.child("agenda").child("agendaTrabalho").push().setValue(agenda);
        clearView();
    }

    private void retrieveAgendaData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        long ID = Long.parseLong(mEditTextID.getText().toString());

        reference.child("agenda").orderByChild("id").equalTo(ID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Agenda agenda = snapshot.getValue(Agenda.class);
                Log.i("FIREBASE CONSULTA", snapshot.getValue().toString());
                mEditTextName.setText(agenda.getNome());
                mEditTextPhone.setText(agenda.getTelefone());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updateAgendaData() {
    }

    private void deleteAgendaData() {
    }

    private void clearView(){
        mEditTextID.setText("");
        mEditTextName.setText("");
        mEditTextPhone.setText("");
    }

}