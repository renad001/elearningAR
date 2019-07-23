package com.example.elearningar.YouTube;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchInput {

    public static String removeSpaces(String input){
        return input.replaceAll("\\s+$", "");
    }

    public static String getYouTubeId(String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if(matcher.find()){
            return matcher.group();
        } else {
            return "error";
        }
    }


}
