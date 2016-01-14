package com.google.appinventor.components.runtime;

import java.util.Random;

public class RandomNameGenerator {
	private static final String[] colors = {"red", "green", "blue", "grue", "black", "gold", "tan", "rojo", "verde"};
	private static final String[] adjectives  = {"big", "small", "silly", "frugal", "pretentious", "cheap", "fancy", "tall", "short"};
	private static final String[] nouns = { "house", "car", "bike", "shoe", "ski", "snowboard", "jetpack", "hoverboard", "elephant"};
	private static final Random random = new Random();

	private static String GetRandomElementFromArray(String[] theArray){
		int index = random.nextInt(theArray.length);
        return theArray[index];
	}

	public static String GenerateRandomName(){
		return GetRandomElementFromArray(colors) + "-" + GetRandomElementFromArray(adjectives) + "-" + GetRandomElementFromArray(nouns);
	}
}