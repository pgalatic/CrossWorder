import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Tests functionality of WordFinder, since without WordFinder, CrossWorder
 * will not function at all.
 *
 * @author paul g
 */
public class WordFinderTest {

    private static final String TEST_FILENAME = "resources/test.txt";
    private static final String SAMPLE_REGEX_1 = ".a.\\b";
    private static final String SAMPLE_REGEX_2 = "\\b...\\b";

    private static final String TEST_REGEX_1 = "[aeiou][aeiou][aeiou][aeiou][aeiou]";
    private static final String TEST_REGEX_2 = "[tongues][tongues][tongues][tongues][tongues][tongues][tongues][tongues]";
    private static final String TEST_REGEX_3 = ".(.)(.)\\2.\\1.";

    private WordFinder CuT;

    /**
     * This is more of a self-test, to make sure I know how to correctly set
     * up a Pattern and Matcher.
     */
    @Test
    public void test_matcher(){
        final String EXPECTED_MATCH_1 = "bar";
        final String EXPECTED_MATCH_2 = "car";
        CuT = new WordFinder(TEST_FILENAME);
        ArrayList<String> matches = CuT.findMatches(SAMPLE_REGEX_1);

        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(EXPECTED_MATCH_1, matches.get(0));
        Assert.assertEquals(EXPECTED_MATCH_2, matches.get(1));

        matches = CuT.findMatches(SAMPLE_REGEX_2);
        Assert.assertEquals(4, matches.size());
    }

    /**
     * Tests that the more complex regular expressions, tested against a much
     * larger wordlist, are returning useful results.
     */
    @Test
    public void test_matches(){
        final ArrayList<String> EXPECTED_MATCHES_1 = new ArrayList<>(
                Arrays.asList("ooeei", "iaoue", "iaoui", "ueuei"));

        final ArrayList<String> EXPECTED_MATCHES_2 = new ArrayList<>(
                Arrays.asList(
                        "ententes", "genettes", "gensengs", "goneness",
                        "gossoons", "neustons", "nonsense", "osteoses",
                        "outguess", "outstunt", "sensuous", "snuggest",
                        "stenoses", "stoutens", "stoutest", "suggests",
                        "sunstone", "testoons", "tonettes", "tungsten",
                        "ungotten", "unguents")
        );

        final int EXPECTED_MATCH_LISTSIZE = 274;
        CuT = new WordFinder();
        ArrayList<String> matches;

        matches = CuT.findMatches(TEST_REGEX_1);
        for (String EXPECTED : EXPECTED_MATCHES_1){
            Assert.assertTrue(matches.contains(EXPECTED));
        }
        matches = CuT.findMatches(TEST_REGEX_2);
        for (String EXPECTED : EXPECTED_MATCHES_2){
            Assert.assertTrue(matches.contains(EXPECTED));
        }
        // I would write another test for this, but at this point I'm pretty
        // sure it works, and I have other homework to do. TODO
        matches = CuT.findMatches(TEST_REGEX_3);
        Assert.assertEquals(EXPECTED_MATCH_LISTSIZE, matches.size());
    }


}
