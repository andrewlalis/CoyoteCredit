package nl.andrewl.coyotecredit.util;

import java.security.SecureRandom;
import java.util.Random;

public class StringUtils {
	private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

	public static String random(int length) {
		StringBuilder sb = new StringBuilder(length);
		Random rand = new SecureRandom();
		for (int i = 0; i < length; i++) {
			sb.append(ALPHABET.charAt(rand.nextInt(ALPHABET.length())));
		}
		return sb.toString();
	}
}
