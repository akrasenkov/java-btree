package edu.mirea;

/**
 * Created by senik11 on 21.03.17.
 */
public class App implements Runnable {

    private static Runnable instance;

    public App() {
    }

    public static void main(String[] args) {
        instance = new App();
        instance.run();
    }

    public void run() {

    }
}
