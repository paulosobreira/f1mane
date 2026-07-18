package br.flmane.entidades;

import java.util.SplittableRandom;

public class GameRandom {

    private final SplittableRandom random;

    private long seed;

    public GameRandom(long seed) {
        this.random = new SplittableRandom(seed);
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }

    public double nextDouble() {
        if (seed == 1) {
            return 0.5;
        }
        return random.nextDouble();
    }

    public int intervalo(int val1, int val2) {

        if (seed == 1) {
            return (val1 + val2) / 2;
        }

        if (val1 > val2) {
            int tmp = val1;
            val1 = val2;
            val2 = tmp;
        }

        return val1 + random.nextInt(val2 - val1 + 1);
    }

    public double intervalo(double val1, double val2) {
        if (seed == 1) {
            return (val1 + val2) / 2;
        }
        if (val1 > val2) {
            double tmp = val1;
            val1 = val2;
            val2 = tmp;
        }
        return val1 + (random.nextDouble() * (val2 - val1));
    }
}