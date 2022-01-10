package us.warframestat.moddetectiongui;

public class RunApplication {
    /**
     * Adds a method to run the application.
     * Had to do this, because the program says that the javafx runtime components are missing when running from a class that extends Application.
     * @param args  default args
     */
    public static void main(String[] args) {
        MainApplication.main(args);
    }
}