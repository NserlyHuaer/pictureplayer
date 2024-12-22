package Tools.String;

import java.security.SecureRandom;
import java.util.stream.IntStream;

public class RandomString {
    private RandomString() {

    }

    public static String getRandomString(int length) {
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        return IntStream.generate(() -> random.nextInt(characters.length()))
                .limit(length)
                .mapToObj(i -> characters.charAt(i) + "")
                .reduce("", String::concat);
    }
}
