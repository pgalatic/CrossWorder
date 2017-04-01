import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * Code utilizing methods found on www.java-tips.org to apply regular
 * expressions to a file.
 *
 * @author java-tips.org
 * @author paul g
 */
public class Matcher {

    public Matcher(){}

    // Converts the contents of a file into a CharSequence
    // @author: java-tips.org
    public CharSequence toCharSequence(String filename) throws IOException {
        FileInputStream input = new FileInputStream(filename);
        FileChannel channel = input.getChannel();

        // Create a read-only CharBuffer on the file
        ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int) channel.size());
        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
        return cbuf;
    }


}
