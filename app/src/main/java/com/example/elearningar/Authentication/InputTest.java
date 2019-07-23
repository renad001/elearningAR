package com.example.elearningar.Authentication;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.elearningar.R;


public class InputTest {

    private void shackAnimation(Context context, EditText editText){
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        editText.startAnimation(shake);
    }
    private void radioShackAnimation(Context context, RadioGroup radioGroup){
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        radioGroup.startAnimation(shake);
    }

    public boolean validate(Context context, int id, EditText[] fields){
        for(int i = 0; i < fields.length; i++){
            EditText currentField = fields[i];
            if(currentField.getId() == id){
                if(currentField.getText().toString().length() <= 0 || currentField.getText().toString().length() < 6){
                    Toast.makeText(context, "كلمة السر أكثر من 6 ارقام",
                            Toast.LENGTH_SHORT).show();
                    shackAnimation(context, currentField);
                    return false;
                }
            }
            if(currentField.getText().toString().length() <= 0){
                shackAnimation(context,currentField);
                return false;
            }
        }
        return true;
    }
    public boolean radioBtnTest(Context context, RadioGroup[] groups){
        for(int i = 0; i < groups.length; i++) {
            RadioGroup currentGroup = groups[i];
            if (currentGroup.getCheckedRadioButtonId() == -1) {
                radioShackAnimation(context, currentGroup);
                return false;
            }
        }
        return true;
    }

}
