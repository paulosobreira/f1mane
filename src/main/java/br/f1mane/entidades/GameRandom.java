package br.f1mane.entidades;

import java.util.SplittableRandom;

public class GameRandom {

    private final SplittableRandom random;

    public GameRandom(long seed) {
        this.random = new SplittableRandom(seed);
    }

    public double nextDouble() {
        return random.nextDouble();
    }

    public int nextInt(int min, int max) {
        return random.nextInt(min, max + 1);
    }

    public long nextLong() {
        return random.nextLong();
    }

    public int intervalo(int val1, int val2) {

        if (val1 > val2) {
            int tmp = val1;
            val1 = val2;
            val2 = tmp;
        }

        return val1 + random.nextInt(val2 - val1 + 1);
    }

    public double intervalo(double val1, double val2) {

        if (val1 > val2) {
            double tmp = val1;
            val1 = val2;
            val2 = tmp;
        }

        return val1 + (random.nextDouble() * (val2 - val1));
    }
}