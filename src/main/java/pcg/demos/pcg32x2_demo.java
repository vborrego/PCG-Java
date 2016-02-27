/*
 * PCG Random Number Generation for C port to Java.
 *
 * Copyright 2016 Gonçalo Amador <g.n.p.amador@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For additional information about the PCG random number generation scheme,
 * including its license and other licensing options, visit
 *
 *     http://www.pcg-random.org
 */
package pcg.demos;

import static java.lang.Math.abs;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.identityHashCode;
import java.util.Random;
import pcg.pcg32_random_t;
import static pcg.pcg_basic.pcg32_random_r;
import static pcg.pcg_basic.pcg32_srandom_r;

/**
 * This file was manually ported from C to Java 8 from the mechanically
 * generated from tests/check-pcg32.c
 *
 * This code shows how you can cope if you're on a 32-bit platform (or a 64-bit
 * platform with a mediocre compiler) that doesn't support 128-bit math, or if
 * you're using the basic version of the library which only provides 32-bit
 * generation.
 *
 * Here we build a 64-bit generator by tying together two 32-bit generators.
 * Note that we can do this because we set up the generators so that each 32-bit
 * generator has a *totally different* different output sequence -- if you tied
 * together two identical generators, that wouldn't be nearly as good.
 *
 * For simplicity, we keep the period fixed at 2^64. The state space is
 * approximately 2^254 (actually 2^64 * 2^64 * 2^63 * (2^63 - 1)), which is
 * huge.
 *
 * @author G. Amador
 */
public class pcg32x2_demo {

    static class pcg32x2_random_t {

        pcg32_random_t[] gen = new pcg32_random_t[2];

        public pcg32x2_random_t() {
            gen[0] = new pcg32_random_t();
            gen[1] = new pcg32_random_t();
        }

        public static int sizeof() {
            return pcg32_random_t.sizeof() * 2;
        }
    }

    static void pcg32x2_srandom_r(pcg32x2_random_t rng, long seed1, long seed2,
            long seq1, long seq2) {
        long mask = ~0l >>> 1;
        // The stream for each of the two generators *must* be distinct
        if ((seq1 & mask) == (seq2 & mask)) {
            seq2 = ~seq2;
        }
        pcg32_srandom_r(rng.gen[0], seed1, seq1);
        pcg32_srandom_r(rng.gen[1], seed2, seq2);
    }

    static long pcg32x2_random_r(pcg32x2_random_t rng) {
        return ((long) (pcg32_random_r(rng.gen[0])) << 32)
                | pcg32_random_r(rng.gen[1]);
    }

    /* See other definitons of ..._boundedrand_r for an explanation of this code. */
    static long pcg32x2_boundedrand_r(pcg32x2_random_t rng, long bound) {
        long threshold = -bound % bound;
        for (;;) {
            long r = pcg32x2_random_r(rng);
            if (r >= threshold) {
                return r % bound;
            }
        }
    }

    static int dummy_global;

    public static void main(String[] args) {
        // Read command-line options
        int rounds = 5;
        boolean nondeterministic_seed = false;
        int round, i;

        int argv = 0;
        int argc = args.length;
        if (argc > 0 && args[argv].equalsIgnoreCase("-r")) {
            nondeterministic_seed = true;
            ++argv;
            --argc;
        }
        if (argc > 0) {
            rounds = Integer.parseInt(args[argv]);
        }

        // In this version of the code, we'll use our custom rng rather than
        // one of the provided ones.
        pcg32x2_random_t rng = new pcg32x2_random_t();

        // You should *always* seed the RNG.  The usual time to do it is the
        // point in time when you create RNG (typically at the beginning of the
        // program).
        //
        // pcg32x2_srandom_r takes four 64-bit constants (the initial state, and 
        // the rng sequence selector; rngs with different sequence selectors will
        // *never* have random sequences that coincide, at all) - the code below
        // shows three possible ways to do so.
        if (nondeterministic_seed) {
            // Seed with external entropy -- the time and some program addresses
            // (which will actually be somewhat random on most modern systems).          
            pcg32x2_srandom_r(rng,
                    (currentTimeMillis() / 1000) ^ (identityHashCode(System.out) / 100),
                    ~(currentTimeMillis() / 1000) ^ (identityHashCode(pcg32_random_r(new pcg32_random_t())) / 100),
                    (abs(new Random().nextLong()) & 0xfffffffffffL) | 0x811111111111L,
                    identityHashCode(dummy_global) >> 8 & 0xfe45174L);

        } else {
            // Seed with a fixed constant
            pcg32x2_srandom_r(rng,
                    Integer.toUnsignedLong(42), Integer.toUnsignedLong(42),
                    Integer.toUnsignedLong(54), Integer.toUnsignedLong(54));
        }

        System.out.printf("pcg32x2_random_r:\n"
                + "      -  result:      64-bit unsigned int (long)\n"
                + "      -  period:      2^64   (* ~2^126 streams)\n"
                + "      -  state space: ~2^254\n"
                + "      -  state type:  pcg32x2_random_t (%d bytes)\n"
                + "      -  output func: XSH-RR (x 2)\n"
                + "\n",
                pcg32x2_random_t.sizeof());

        for (round = 1; round <= rounds; ++round) {
            System.out.printf("Round %d:\n", round);
            /* Make some 32-bit numbers */
            System.out.printf("  64bit:");
            for (i = 0; i < 6; ++i) {
                if (i > 0 && i % 3 == 0) {
                    System.out.printf("\n\t");
                }
                System.out.printf(" 0x%016x", pcg32x2_random_r(rng));
            }
            System.out.println("");

            /* Toss some coins */
            System.out.printf("  Coins: ");
            for (i = 0; i < 65; ++i) {
                System.out.printf("%c", pcg32x2_boundedrand_r(rng, 2) != 0 ? 'H' : 'T');
            }
            System.out.println("");

            /* Roll some dice */
            System.out.printf("  Rolls:");
            for (i = 0; i < 33; ++i) {
                System.out.printf(" %d", (int) pcg32x2_boundedrand_r(rng, 6) + 1);
            }
            System.out.println("");

            /* Deal some cards */
            int SUITS = 4;
            int NUMBERS = 13;
            int CARDS = 52;
            char cards[] = new char[CARDS];

            for (i = 0; i < CARDS; ++i) {
                cards[i] = (char) i;
            }

            for (i = CARDS; i > 1; --i) {
                int chosen = (int) pcg32x2_boundedrand_r(rng, i);
                char card = cards[chosen];
                cards[chosen] = cards[i - 1];
                cards[i - 1] = card;
            }

            System.out.printf("  Cards:");
            char number[] = {'A', '2', '3', '4', '5', '6', '7', '8', '9', 'T',
                'J', 'Q', 'K'};
            char suit[] = {'h', 'c', 'd', 's'};
            for (i = 0; i < CARDS; ++i) {
                System.out.printf(" %c%c", number[cards[i] / SUITS],
                        suit[cards[i] % SUITS]);
                if ((i + 1) % 22 == 0) {
                    System.out.printf("\n\t");
                }
            }

            System.out.println("");
            System.out.println("");
        }
    }
}
