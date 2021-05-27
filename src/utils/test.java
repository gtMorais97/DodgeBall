package utils;

import java.util.ArrayList;
import java.util.List;

public class test {

    public static void main(String[] args){
        String s1 = "a";
        String s2 = "b";
        String s3 = "c";
        String s4 = "d";

        List<String> l1 = new ArrayList<>();
        l1.add(s1); l1.add(s2); l1.add(s3); l1.add(s4);

        List<String> l2 = new ArrayList<>();
        l2.add(s1); l2.add(s2);

        for(String s: l1){
            l1.remove(s);
        }

        System.out.println(l1);
    }
}