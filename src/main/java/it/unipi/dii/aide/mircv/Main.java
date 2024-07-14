package it.unipi.dii.aide.mircv;

public class Main {
    public static void main(String[] args) {
        String input = "your/example/string/with/multiple/slashes";
        String[] parts = input.split("(?<=/)(?!.*?/)");
        System.out.println("Before last /: " + parts[0]);
        System.out.println("After last /: " + parts[1]);
    }
}