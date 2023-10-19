package com.example.firebaseagenda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

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
                Log.e("FIREBASE ERROR", "Erro ao tentar recuperar dados: " + error.getMessage());
                showToast("Erro ao tentar recuperar dados");
            }
        });

    }
    /**
     * Este método lê um registro específico no banco de dados Firebase, identificado pelo
     * ID fornecido, e atualiza os campos "nome" e "phone" desse registro com os valores
     * fornecidos pelos EditTexts mEditTextName e mEditTextPhone.
     * @param snapshot The current data at the location
     */
    private void updateAgendaData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        long ID = Long.parseLong(mEditTextID.getText().toString());
        String name = mEditTextName.getText().toString();
        String phone = mEditTextPhone.getText().toString();
        /*
          Realiza uma consulta no nó "agenda" do banco de dados Firebase, onde o
          campo "id" é igual a ID (o valor obtido do mEditTextID).
          addListenerForSingleValueEvent é usado para escutar eventos de dados uma vez.
         */
        reference.child("agenda").orderByChild("id").equalTo(ID).addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Este método é chamado quando os dados na referência são alterados.
             * snapshot contém os dados atuais no local especificado.
             * @param snapshot The current data at the location
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Para todos os filhos de snapshot utilizando o nome "data" do tipo DataSnapshot
                // Espera-se que haja apenas um filho correspondente ao ID fornecido.
                for(DataSnapshot data : snapshot.getChildren()) {
                    Map<String,Object> valoresAtualizados = new HashMap<>();
                    valoresAtualizados.put("nome", name);
                    valoresAtualizados.put("telefone", phone);
                    // É usado para realizar a atualização no banco de dados Firebase. Ele atualiza
                    // os campos especificados no mapa valoresAtualizados para o nó
                    // específico identificado pelo data.getRef().
                    data.getRef().updateChildren(valoresAtualizados).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.i("FIREBASE UPDATE", "Atualização bem-sucedida");
                            showToast("Atualização bem-sucedida");
                        } else {
                            Log.e("FIREBASE ERROR", "Erro ao tentar atualizar dados: " + task.getException().getMessage());
                            showToast("Erro ao tentar atualizar dados");
                        }
                    });                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE ERROR", "Erro ao tentar consultar dados: " + error.getMessage());
                showToast("Erro ao tentar consultar dados");
            }
        });
    }

    private void deleteAgendaData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        long ID = Long.parseLong(mEditTextID.getText().toString());

    /*
        Realiza uma consulta no nó "agenda" do banco de dados Firebase, onde o
        campo "id" é igual a ID (o valor obtido do mEditTextID).
        addListenerForSingleValueEvent é usado para escutar eventos de dados uma vez.
    */
        reference.child("agenda").orderByChild("id").equalTo(ID).addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Este método é chamado quando os dados na referência são alterados.
             * snapshot contém os dados atuais no local especificado.
             * @param snapshot The current data at the location
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    data.getRef().removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.i("FIREBASE DELETE", "Exclusão bem-sucedida");
                            showToast("Exclusão bem-sucedida");
                            clearView();
                        } else {
                            Log.e("FIREBASE ERROR", "Erro ao tentar excluir dados: " + task.getException().getMessage());
                            showToast("Erro ao tentar excluir dados");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Lida com erros, se necessário.
                Log.e("FIREBASE ERROR", "Erro ao tentar consultar dados: " + error.getMessage());
            }
        });
    }

    private void clearView(){
        mEditTextID.setText("");
        mEditTextName.setText("");
        mEditTextPhone.setText("");
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}