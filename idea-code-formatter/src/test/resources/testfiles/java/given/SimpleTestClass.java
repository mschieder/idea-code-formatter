package foo.bar;


import java.util.List;

import static java.lang.Character.isUpperCase;

public      class      SimpleTestClass {


    public List<String> fetchResult   (){
        return    List.of( "A","b", "C").stream()
                .filter(s ->     isUpperCase( s.charAt(0))).toList();







    }
}