package com.oudmon.qc_utils.date;

import java.util.Locale;
public class LanguageUtil {
    public static String localLanguage="nl";
    public static boolean changeDateFormat(){
//        String language = Locale.getDefault().getLanguage();
//        if(localLanguage.contains(language)){
//            return true;
//        }else {
//            return false;
//        }
        return true;
    }

    public static boolean isChina(){
//        String language = Locale.getDefault().getLanguage();
//        if(language.contains("zh")){
//            return true;
//        }else {
//            return false;
//        }
        return false;
    }

    public static boolean isChinaReal(){
        String language = Locale.getDefault().getLanguage();
        if(language.contains("zh")){
            return true;
        }else {
            return false;
        }
    }
}
