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
package demos;

import static java.lang.Math.abs;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.identityHashCode;
import java.util.Random;
import pcg.pcg32_random_t;
import static pcg.pcg_basic.pcg32_boundedrand_r;
import static pcg.pcg_basic.pcg32_random_r;
import static pcg.pcg_basic.pcg32_srandom_r;

/**
 * This file was manually ported from C to Java 8 from the mechanically
 * generated from tests/check-pcg32_demo.c
 *
 * @author G. Amador
 */
public class pcg32_demo {

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

        // In this version of the code, we'll use a local rng, rather than the
        // global one.
        pcg32_random_t rng = new pcg32_random_t();

        // You should *always* seed the RNG.  The usual time to do it is the
        // point in time when you create RNG (typically at the beginning of the
        // program).
        //
        // pcg32_srandom_r takes two 64-bit constants (the initial state, and the
        // rng sequence selector; rngs with different sequence selectors will
        // *never* have random sequences that coincide, at all) - the code below
        // shows three possible ways to do so.
        if (nondeterministic_seed) {
            // Seed with external entropy -- the time and some the next random 
            // long generated after a few rounds
            // (which will actually be somewhat random on most modern systems).                       
            pcg32_srandom_r(rng,
                    (currentTimeMillis() / 1000) ^ (identityHashCode(System.out) / 100),
                    (abs(new Random().nextLong()) & 0xfffffffffffL) | 0x811111111111L);
        } else {
            // Seed with a fixed constant
            pcg32_srandom_r(rng, Integer.toUnsignedLong(42), Integer.toUnsignedLong(54));
        }

        System.out.printf("pcg32_random_r:\n"
                + "      -  result:      32-bit unsigned int (uint32_t)\n"
                + "      -  period:      2^64   (* 2^63 streams)\n"
                + "      -  state type:  pcg32_random_t (%d bytes)\n"
                + "      -  output func: XSH-RR\n"
                + "\n",
                pcg32_random_t.sizeof());

        for (round = 1; round <= rounds; ++round) {
            System.out.printf("Round %d:\n", round);
            /* Make some 32-bit numbers */
            System.out.printf("  32bit:");
            for (i = 0; i < 6; ++i) {
                System.out.printf(" 0x%08x", pcg32_random_r(rng));
            }
            System.out.println("");

            /* Toss some coins */
            System.out.printf("  Coins: ");
            for (i = 0; i < 65; ++i) {
                System.out.printf("%c", pcg32_boundedrand_r(rng, 2) != 0 ? 'H' : 'T');
            }
            System.out.println("");

            /* Roll some dice */
            System.out.printf("  Rolls:");
            for (i = 0; i < 33; ++i) {
                System.out.printf(" %d", (int) pcg32_boundedrand_r(rng, 6) + 1);
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
                int chosen = pcg32_boundedrand_r(rng, i);
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
