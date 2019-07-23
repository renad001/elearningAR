package com.example.elearningar.Authentication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.elearningar.Module.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class CurrentUser {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    UserDB userDB;
    Context context;
    public CurrentUser(Context context) {
        userDB = new UserDB(context);
        this.context =context;
    }


    public long insertUserToDB( User user ) {
        SQLiteDatabase sqLiteDatabase = userDB.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(userDB.UID, user.getUserId());
        contentValues.put(userDB.userName, user.getUserName());
        contentValues.put(userDB.firstName, user.getFirstName());
        contentValues.put(userDB.lastName, user.getLastName());
        contentValues.put(userDB.email, user.getEmail());
        contentValues.put(userDB.gender, user.getGender());
        contentValues.put(userDB.work, user.getWork());
        contentValues.put(userDB.imageUrl, user.getImageURL());
        long data = sqLiteDatabase.insert(userDB.tableName, null, contentValues);
        return data;
    }

    //Save the user data in the Firebase database.
    public long insertUserToFB_DB(User user) {
        myRef = FirebaseDatabase.getInstance().getReference();

        myRef.child("users").child(user.getUserId()).setValue(user);

        long data = insertUserToDB(user);
        return data;
    }

    public List<User> getDataFromDB() {
        List<User> userList = new ArrayList<User>();
        SQLiteDatabase sqLiteDatabase = userDB.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + UserDB.tableName, null);
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setUserId(cursor.getString(0));
                user.setUserName(cursor.getString(1));
                user.setFirstName(cursor.getString(2));
                user.setLastName(cursor.getString(3));
                user.setEmail(cursor.getString(4));
                user.setGender(cursor.getString(5));
                user.setWork(cursor.getString(6));
                user.setImageURL(cursor.getString(7));
                userList.add(user);
            } while (cursor.moveToNext());
        }
        return userList;
    }


    public String getUserFromDB(String userID) {
        SQLiteDatabase sqLiteDatabase = userDB.getWritableDatabase();
        String[] columns = {UserDB.UID, UserDB.userName, UserDB.firstName, UserDB.lastName,
                UserDB.email, UserDB.gender, UserDB.work, UserDB.imageUrl};
        Cursor cursor = sqLiteDatabase.query(UserDB.tableName, columns, UserDB.UID + " = '" + userID + "'", null, null, null, null);
        StringBuffer stringBuffer = new StringBuffer();
        while (cursor.moveToFirst()) {
            int indexUID = cursor.getColumnIndex(UserDB.UID);
            int indexUserName = cursor.getColumnIndex(UserDB.userName);
            int indexFirstName = cursor.getColumnIndex(UserDB.firstName);
            int indexLastName = cursor.getColumnIndex(UserDB.lastName);
            int indexEmail = cursor.getColumnIndex(UserDB.email);
            int indexGender = cursor.getColumnIndex(UserDB.email);
            int indexWork = cursor.getColumnIndex(UserDB.email);
            int indexImage = cursor.getColumnIndex(UserDB.email);
            String imageurl = cursor.getString(indexImage);
            stringBuffer.append(imageurl);
        }
        return stringBuffer.toString();
    }

    //Save the user data in the Firebase database.
    public void getUserFromFBtoDB(String userID) {
        database = FirebaseDatabase.getInstance();

        Log.e("getUserFromFBtoDB", userID);
        myRef = database.getReference().child("users").child(userID);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User user = new User();

                    user = dataSnapshot.getValue(User.class);

                    long data = insertUserToDB(user);

                    List<User> userList = new ArrayList<>();
                    if (data<0){
                        userList = getDataFromDB();
                        Log.d("listsize", String.valueOf(userList.size()));
                        Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();

                    }else {
                        Toast.makeText(context, "success", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public int updateimageUrlDB(String oldUrl, String newUrl) {
        SQLiteDatabase sqLiteDatabase = userDB.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserDB.imageUrl, newUrl);
        String[] whereArgs = {oldUrl};
        int count = sqLiteDatabase.update(UserDB.tableName, contentValues, UserDB.imageUrl + " LIKE ? ", whereArgs);
        return count;
    }

    public int deleteDataDB(String userid) {
        Log.e("insertUserToFB_DB  in", userid);
        SQLiteDatabase sqLiteDatabase = userDB.getWritableDatabase();
        String[] whereArgs = {userid};
        int count = sqLiteDatabase.delete(UserDB.tableName, UserDB.UID + "=? ", whereArgs);
        return count;
    }

    public int deleteDataDB() {
        SQLiteDatabase sqLiteDatabase = userDB.getWritableDatabase();
        int count = sqLiteDatabase.delete(UserDB.tableName, "1", null);
        return count;
    }


    static class UserDB extends SQLiteOpenHelper {

        private static final String dataBase_Name = "userdata";
        private static final String tableName = "USER";
        private static final int dataBase_Version = 1;
        private static final String UID = "id";
        private static final String userName = "UserName";
        private static final String firstName = "Firstname";
        private static final String lastName = "Lastname";
        private static final String email = "Email";
        private static final String gender = "Gender";
        private static final String work = "Work";
        private static final String imageUrl = "ImageURL";
        private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + tableName;
        private static final String CREATE_TABLE = "CREATE TABLE " + tableName +
                " (" + UID + " VARCHER(255), " + userName + " VARCHER(255), " + firstName + " VARCHER(255), " + lastName + " VARCHER(255), " + email + " VARCHER(255), " + gender + " VARCHER(255), " + work + " VARCHER(255), " + imageUrl + " VARCHER(255));";
        private Context context;

        public UserDB(Context context) {
            super(context, dataBase_Name, null, dataBase_Version);
            this.context = context;
            Toast.makeText(context, "this Constructor", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            try {
                sqLiteDatabase.execSQL(CREATE_TABLE);
                Toast.makeText(context, "onCreat Method", Toast.LENGTH_LONG).show();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(context, "failure in onCreat: " + e, Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            try {
                Toast.makeText(context, "onUpgrade Methode", Toast.LENGTH_LONG).show();
                sqLiteDatabase.execSQL(DROP_TABLE);
                onCreate(sqLiteDatabase);
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(context, "failure in onUpgrade: " + e, Toast.LENGTH_LONG).show();
            }
        }
    }


}

