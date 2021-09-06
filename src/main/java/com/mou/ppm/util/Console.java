package com.mou.ppm.util;

import picocli.CommandLine;

import java.io.IOException;
import java.util.Scanner;

public final class Console {
    private static final CommandLine.Help.Ansi ANSI = CommandLine.Help.Ansi.AUTO;

    private Console() {
    }

    public static void error(String message) {
        System.out.println(ANSI.string("@|bold,red ERROR: " + message + "|@"));
    }

    public static void warning(String message) {
        System.out.println(ANSI.string("@|bold,yellow WARNING: " + message + "|@"));
    }

    public static void success(String message) {
        System.out.println(ANSI.string("@|bold,green SUCCESS: " + message + "|@"));
    }
    public static void info(String message) {
        System.out.println(ANSI.string("@|bold,cyan INFO: " + message + "|@"));
    }

    public static void action(String actionName, String on, String color) {
        if (on == null)
            System.out.println(ANSI.string("@|bold," + color + " " + actionName + "|@"));
        else
            System.out.println(ANSI.string("@|bold," + color + " " + actionName + "|@\t@|white " + on + "|@"));
    }

    public static void action(String actionName, String color) {
        action(color, actionName, null);
    }

    public static void actionSuccess(String actionName, String on) {
        action(actionName, on, "green");
    }

    public static void actionWarning(String actionName, String on) {
        action(actionName, on, "yellow");
    }

    public static void actionDanger(String actionName, String on) {
        action(actionName, on, "red");
    }


    public static void actionSuccess(String actionName) {
        action(actionName, "green");
    }

    public static void actionWarning(String actionName) {
        action(actionName, "yellow");
    }

    public static void actionDanger(String actionName) {
        action(actionName, "red");
    }

    public static void prompt(String question, Runnable runnable) {
        System.out.print(ANSI.string("@|yellow >|@ " + question + "\t@|yellow (y/n)|@"));
        if (getScanner().next().trim().equals("y")) {

            System.out.println(ANSI.string(" @|green permitted|@"));
            runnable.run();
        } else {
            System.out.println(ANSI.string(" @|red prohibited|@"));
        }
    }
    private static Scanner scanner;
    private static Scanner getScanner(){
        if(scanner == null){
            scanner = new Scanner(System.in);
        }
        return scanner;
    }
}
