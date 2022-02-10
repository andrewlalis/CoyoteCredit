package nl.andrewl.coyotecredit.util;

import java.security.SecureRandom;
import java.util.Random;

public class AccountNumberUtils {
	public static String generate() {
		StringBuilder sb = new StringBuilder(19);
		Random rand = new SecureRandom();
		for (int i = 0; i < 16; i++) {
			if (i % 4 == 0) sb.append('-');
			sb.append(rand.nextInt(0, 10));
		}
		return sb.toString();
	}

	public static boolean isValid(String num) {
		return num.matches("\\d{4}-\\d{4}-\\d{4}-\\d{4}");
	}
}
