package com.example.firebaseagenda;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //Login
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 123; // Um código de solicitação para identificar a resposta do login
    private boolean isUserLoggedIn = false;
    EditText mEditTextID, mEditTextName, mEditTextPhone;
    Button mButtonCreate, mButtonRetrieve, mButtonUpdate, mButtonDelete, mButtonGoogleSignIn;
    ImageView avatarImageView;

    // Recursos String
    private String loginSuccessMessage;
    private String loginFailureMessage;
    private String logoutSuccessMessage;
    private String logoutFailureMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ImageView
        avatarImageView = findViewById(R.id.avatarImageView);
        oneTapClient = Identity.getSignInClient(this);

        //EditText
        mEditTextID = findViewById(R.id.editTextID);
        mEditTextName = findViewById(R.id.editTextName);
        mEditTextPhone = findViewById(R.id.editTextPhone);

        //Buttons
        mButtonCreate = findViewById(R.id.buttonCreate);
        mButtonRetrieve = findViewById(R.id.buttonRetrieve);
        mButtonUpdate = findViewById(R.id.buttonUpdate);
        mButtonDelete = findViewById(R.id.buttonDelete);
        mButtonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);

        // Inicialização dos recursos de string
        loginSuccessMessage = getString(R.string.login_success_message);
        loginFailureMessage = getString(R.string.login_failure_message);
        logoutSuccessMessage = getString(R.string.logout_success_message);
        //TODO implementar o Logout
//        logoutFailureMessage = getString(R.string.logout_failure_message);

        mButtonCreate.setEnabled(false);
        mButtonRetrieve.setEnabled(false);
        mButtonUpdate.setEnabled(false);
        mButtonDelete.setEnabled(false);
        mButtonGoogleSignIn.setEnabled(true);

        //Login
        mAuth = FirebaseAuth.getInstance();
        oneTapClient = Identity.getSignInClient(this);

        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();

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
        mButtonGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserLoggedIn) {
                    // If the user is logged in, perform logoff
                    mAuth.signOut();
                    showToast(logoutSuccessMessage);
                    // Update the user state to disconnected
                    isUserLoggedIn = false;
                    // Update the button text
                    updateGoogleSignInButtonText();
                    // Disable buttons after logoff
                    disableButtons();
                    // Hide avatar
                    avatarImageView.setVisibility(View.GONE);
                } else {
                    // If the user is not logged in, start the login flow
                    startGoogleSignInFlow();
                }
            }
        });
    }

    private void startGoogleSignInFlow() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado retornado da intent de login do Google
        if (requestCode == RC_SIGN_IN) {
            try {
                // Tarefa completa com a intenção de login do Google
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);

                // Autenticar com o Firebase usando o token de ID do Google
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login bem-sucedido
                        FirebaseUser user = mAuth.getCurrentUser();
                        showToast(loginSuccessMessage + user.getDisplayName());

                        // Atualizar o estado do usuário como logado
                        isUserLoggedIn = true;

                        // Atualizar o texto do botão
                        updateGoogleSignInButtonText();

                        // Habilitar botões
                        enableButtons();

                        // Carregar a imagem do avatar
                        loadAvatarImage(user);
                    } else {
                        // Se o login falhar, exiba uma mensagem ao usuário.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        showToast(loginFailureMessage);
                    }
                });
    }

    private void loadAvatarImage(FirebaseUser user) {
        if (user != null) {
            String photoUrl = user.getPhotoUrl().toString();
            // Use Glide para carregar a imagem do avatar
            avatarImageView.setVisibility(View.VISIBLE);
            Glide.with(getBaseContext())
                    .load(photoUrl)
                    .placeholder(R.drawable.placeholder_image) // Recurso de imagem padrão
                    .into(avatarImageView);
        }
    }

    private void updateGoogleSignInButtonText() {
        mButtonGoogleSignIn.setText(isUserLoggedIn ? "Logoff" : "Login");
    }

    private void enableButtons() {
        mButtonCreate.setEnabled(true);
        mButtonRetrieve.setEnabled(true);
        mButtonUpdate.setEnabled(true);
        mButtonDelete.setEnabled(true);
        mButtonGoogleSignIn.setEnabled(true);
    }

    private void disableButtons() {
        mButtonCreate.setEnabled(false);
        mButtonRetrieve.setEnabled(false);
        mButtonUpdate.setEnabled(false);
        mButtonDelete.setEnabled(false);
        mButtonGoogleSignIn.setEnabled(true);
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

        int ID = Integer.parseInt(mEditTextID.getText().toString());

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