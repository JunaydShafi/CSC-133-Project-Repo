package pkgDriverA5;

import org.lwjgl.Version;

public class A5SlSpot {
    public static int WIN_WIDTH = 1600, WIN_HEIGHT = 1600;
    public static String WINDOW_TITLE = "CSC 133";

    public static final int TILE_LENGTH = 50;
    public static final int TILE_OFFSET = 50;
    public static final int TILE_PADDING = 50;

    // A5SlSpot.java — change these lines
    public static final float FRUSTUM_LEFT   = -1.0f;
    public static final float FRUSTUM_RIGHT  =  1.0f;
    public static final float FRUSTUM_BOTTOM = -1.0f;
    public static final float FRUSTUM_TOP    =  1.0f;
    public static final float Z_NEAR = -1.0f;
    public static final float Z_FAR  =  1.0f;


    public static void print_legalese() {
        System.out.println("(c) California State University at Sacramento, Computer Science Department");
        System.out.println("It is illegal to host this code anywhere outside of csus.edu websites");
        System.out.print("EXCEPT FOR THE PURPOSE OF COURSE WORK AT CSUS BY THE STUDENTS REGISTERED AT THE UNIVERSITY\n");
        System.out.println("LWJGL Version: " + Version.getVersion());
    }  //  public static void legalese()

}
