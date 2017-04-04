import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

/**
 * Created specifically to test isGoal() and nothing else, yet.
 *
 * @author paul g
 */
public class BoardTest {

    private static final String FILENAME = "resources/board0.txt";

    private Board CuT;

    /** This test will break once isGoal() tests for duplicate words. */
    @Test
    public void isGoal(){
        CuT = new Board(FILENAME);

        Space A = new Space(0, 0); A.push('v'); CuT.addPOI(A, Board.Direction.ACROSS); CuT.addPOI(A, Board.Direction.DOWN);
        Space B = new Space(0, 1); B.push('a'); CuT.addPOI(B, Board.Direction.DOWN);
        Space C = new Space(0, 2); C.push('n'); CuT.addPOI(A, Board.Direction.DOWN);
        Space D = new Space(1, 0); D.push('a'); CuT.addPOI(D, Board.Direction.ACROSS);
        Space E = new Space(1, 1); E.push('l');
        Space F = new Space(1, 2); F.push('e');
        Space G = new Space(2, 0); G.push('n'); CuT.addPOI(G, Board.Direction.ACROSS);
        Space H = new Space(2, 1); H.push('e');
        Space I = new Space(2, 2); I.push('w');

        Space[] ROW0 = new Space[]{A, B, C};
        Space[] ROW1 = new Space[]{D, E, F};
        Space[] ROW2 = new Space[]{G, H, I};

        CuT.setRow(0, ROW0);
        CuT.setRow(1, ROW1);
        CuT.setRow(2, ROW2);

        Assert.assertTrue(CuT.isGoal());
    }
}
