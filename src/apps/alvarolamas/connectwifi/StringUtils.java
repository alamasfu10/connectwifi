package apps.alvarolamas.connectwifi;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;


public class StringUtils {

	private static String join(Collection<String> collection, String delimiter, boolean reversed) {
        if (collection != null) {
            StringBuffer buffer = new StringBuffer();
            Iterator<String> iter = collection.iterator();
            while (iter.hasNext()) {
                if (!reversed) {
                    buffer.append(iter.next());
                    if (iter.hasNext()) {
                        buffer.append(delimiter);

                    }
                } else {
                    buffer.insert(0, iter.next());
                    if (iter.hasNext()) {
                        buffer.insert(0, delimiter);

                    }
                }
            }
            return buffer.toString();
        } else {
            return null;
        }
    }


    private static byte[] int_to_byte(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

    public static String int_to_ip(int value) {
        byte[] b = int_to_byte(value);
        Stack<String> stack = new Stack<String>();
        for (byte c : b) {
            stack.push(String.valueOf(0xFF & c));
        }

        return (join(stack, ".", true));
    }
	
}
