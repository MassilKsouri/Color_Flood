package p8.demo.colorflood;

/**
 * Class representing a case in the board
 */
class Case {
    int CSTcolor ;
    int x;
    int y;

    /**
     * Case constructor
     * @param CSTcolor the color of the case
     * @param x the x coordinate
     * @param y the y coordinate
     */
    Case(int CSTcolor, int x, int y) {
        this.CSTcolor = CSTcolor;
        this.x = x;
        this.y = y;
    }
}
