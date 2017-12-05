package p8.demo.colorflood;

/**
 * Created by TheNightKing on 04/12/2017.
 */

public class Case {
    Boolean active;
    int CSTcolor ;
    int x;
    int y;

    Case(Boolean active, int CSTcolor, int x, int y) {
        this.active = active;
        this.CSTcolor = CSTcolor;
        this.x = x;
        this.y = y;
    }
}
