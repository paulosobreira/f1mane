package br.nnpe;

import java.util.Random;

/**
 * Random Password Generator Generate aleatory alphaNumeric password
 * 
 * @version 1.1 02/01/1999
 * 
 * 
 */
public class PassGenerator {
	final String[] UPPER = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
			"K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
			"Y", "Z" };
	final String[] LOWER = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
			"k", "l", "m", "n", "p", "q", "r", "s", "t", "u", "v", "w", "x",
			"y", "z" };
	final String[] DIGIT = { "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	private Random genNum = new Random();
	private int arrTam = 0;
	private int contL = 0;
	private int contU = 0;
	private int contD = 0;
	private String res = "";
	private String pass;
	private int iGeRaNu;
	private String sTam;
	private String sChar;

	public PassGenerator() {
	}

	public String generateIt() {
		pass = "";

		for (int i = 0; i < 6; i++) {

			arrTam = 9;

			pass = pass + getDigit(genNum());
			contL = 0;
			contU = 0;
		}

		return pass;
	}

	int genNum() {
		iGeRaNu = genNum.nextInt() % arrTam;
		sTam = Integer.toString(iGeRaNu);
		sChar = new String(String.valueOf(sTam.charAt(0)));

		// Converting the possible negative number to positive
		if (sChar.equals("-")) {
			iGeRaNu = iGeRaNu * -1;
		}

		return iGeRaNu;
	}

	String getUpper(int val) {
		res = UPPER[val];

		return res;
	}

	String getLower(int val) {
		res = LOWER[val];

		return res;
	}

	String getDigit(int val) {
		res = DIGIT[val];

		return res;
	}
}
