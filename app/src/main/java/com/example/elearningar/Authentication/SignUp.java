package com.example.elearningar.Authentication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.elearningar.MainActivity;
import com.example.elearningar.Module.User;
import com.example.elearningar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SignUp extends AppCompatActivity {

    RadioGroup radioGenderGroup, radioWorkGroup;
    RadioButton radioGenderButton, radioWorkButton;
    int selectedWorkId, selectedGenderId;
    Button btnSignIn, btnSignUp;
    EditText inputuserName, inputfirstName, inputlastName, inputEmail, inputPassword;
    ProgressBar progressBar;
    String username, email, password, firstname, lastname, work, gender;
    private FirebaseAuth auth;
    public static final String TAG = SignUp.class.getSimpleName();
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseUser currentuser;
    InputTest inputTest;
    CurrentUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        inputTest = new InputTest();
        auth = FirebaseAuth.getInstance();

        currentUser = new CurrentUser(this);

        radioGenderGroup = (RadioGroup) findViewById(R.id.workGroup);
        radioWorkGroup = (RadioGroup) findViewById(R.id.genderGroup);
        selectedWorkId= radioWorkGroup.getCheckedRadioButtonId();
        selectedGenderId= radioGenderGroup.getCheckedRadioButtonId();
        radioWorkButton = (RadioButton)findViewById(selectedWorkId);
        radioGenderButton =(RadioButton)findViewById(selectedGenderId);
        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputuserName = (EditText) findViewById(R.id.username);
        inputfirstName = (EditText) findViewById(R.id.firstname);
        inputlastName = (EditText) findViewById(R.id.lastname);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Open SignIn Activity
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //When you press the button SignUp Check the User Data and Creat new User
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a = findViewById(R.id.password).getId();
                if(inputTest.validate(SignUp.this, a,new  EditText[] { inputuserName, inputfirstName, inputlastName, inputEmail, inputPassword})&&
                        inputTest.radioBtnTest(SignUp.this, new RadioGroup[] {radioWorkGroup, radioGenderGroup})){
                    setData();
                }
            }
        });

    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_teacher:
                if (checked)
                    radioWorkButton = (RadioButton)findViewById(view.getId());
                work = radioWorkButton.getText().toString().trim();
                break;
            case R.id.radio_steudent:
                if (checked)
                    radioWorkButton = (RadioButton)findViewById(view.getId());
                work = radioWorkButton.getText().toString().trim();
                break;
        }
        switch (view.getId()) {
            case R.id.radio_female:
                if (checked)
                    radioGenderButton = (RadioButton)findViewById(view.getId());
                gender = radioGenderButton.getText().toString().trim();

                break;
            case R.id.radio_male:
                if (checked)
                    radioGenderButton = (RadioButton)findViewById(view.getId());
                gender = radioGenderButton.getText().toString().trim();
                break;
        }
    }

    private void setData() {
        progressBar.setVisibility(View.INVISIBLE);
        email = inputEmail.getText().toString().trim();
        password = inputPassword.getText().toString().trim();
        username = inputuserName.getText().toString().trim();
        firstname = inputfirstName.getText().toString().trim();
        lastname = inputlastName.getText().toString().trim();
        createAccount(email, password, username, firstname, lastname);
    }

    // Creat new Firebase Account with Email and password
    private void createAccount(final String email, String password, final String username, final String firstname, final String lastname) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "createAccount:" + email);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            ActivityCompat.requestPermissions(SignUp.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(SignUp.this, "لقد تم انشاء الحساب بنجاح",
                                    Toast.LENGTH_LONG).show();
                            currentuser = auth.getCurrentUser();
                            String userId = currentuser.getUid();
                            //Creat new User.
                            User user = new User();
                            user.setUserId(currentuser.getUid());
                            Log.d("userID", currentuser.getUid());
                            user.setUserName(username);
                            user.setFirstName(firstname);
                            user.setLastName(lastname);
                            user.setEmail(email);
                            user.setGender(gender);
                            user.setWork(work);
                            user.setImageURL("https://firebasestorage.googleapis.com/v0/b/augmentedbook-4c666.appspot.com/o/profile_icon.jpg?alt=media&token=bd714743-5a02-4820-8173-6f8e4eed600a");
                            //save the User Data in firebase Realtime Database. and in SQLite Database
                            long data = currentUser.insertUserToFB_DB(user);
                            Log.e("insertUserToFB_DB", String.valueOf(data));

                            List<User> userList = new ArrayList<>();
                            if (data<0){
                                userList = currentUser.getDataFromDB();
                                Log.d("listsize", String.valueOf(userList.size()));
                                Toast.makeText(SignUp.this, "Error", Toast.LENGTH_LONG).show();

                            }else {
                                Toast.makeText(SignUp.this, "success", Toast.LENGTH_LONG).show();
                                Log.d("listsize", String.valueOf(userList.size()));
                            }
                        }
                        else {
                            Intent intent = new Intent(SignUp.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }


                    }
                });
        // [END create_user_with_email]
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "error Permission failed ", Toast.LENGTH_LONG).show();
            }
        }
    }
}
