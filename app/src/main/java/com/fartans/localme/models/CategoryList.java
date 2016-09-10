package com.fartans.localme.models;

/**
 * Created by ARavi on 3/5/2015.
 */
public class CategoryList {
    public final String[] CATEGORIES = new String[] {
            "Physics", "Chemistry", "Math", "Biology", "Literature","Science","Travel","Cuisine","Technology","Programming","Music"
    };

   /* public static String[] getCategories() {
        return CATEGORIES;
    }*/
    public boolean checkinarray(String[] arr,String targetvalue){
        for(String check:arr){
            if(check.equals(targetvalue))
                return true;
        }
        return false;


    }
}
