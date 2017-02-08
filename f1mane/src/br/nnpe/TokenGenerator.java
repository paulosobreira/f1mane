package br.nnpe;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class TokenGenerator {
	private SecureRandom random = new SecureRandom();

	public String nextSessionId() {
		return new BigInteger(130, random).toString(32);
	}

	public static void main(String[] args) {
		TokenGenerator sessionIdentifierGenerator = new TokenGenerator();
		System.out.println(sessionIdentifierGenerator.nextSessionId());
	}
}