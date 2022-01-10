package us.warframestat.moddetection.gui;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.global.opencv_core;
import us.warframestat.moddetection.api.App;

import java.io.IOException;

public class RunApplication {
  /**
   * Adds a method to run the application. Had to do this, because the program says that the javafx
   * runtime components are missing when running from a class that extends Application.
   *
   * @param args default args
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    try {
      Loader.load(opencv_core.class);
    } catch (UnsatisfiedLinkError e) {
      String path = Loader.cacheResource(opencv_core.class, "windows-x86_64/jni<module>.dll").getPath();
      new ProcessBuilder("c:/Users/tiebe/Downloads/Dependencies_x64_Release/DependenciesGui.exe", path).start().waitFor();
    }
    MainApplication.main(args);
  }
}
