# PCG Random Number Generation, Minimal Java Edition

[PCG-Random website]: http://www.pcg-random.org

This code provides a port to Java 8 of the C minimal implementation of one 
member of the PCG family of random number generators, which are fast, 
statistically excellent, and offer a number of useful features.

Full details on the original version of the library can be found at the 
[PCG-Random website]. This is a port to Java 8 of the original C code that 
provides a single family member and skips some useful features (such as 
jump-ahead/jump-back) -- there is no current port to Java 8 of my doing 
or any other that I'm aware of either the full version of the PCG C library, 
or the full feature, the PCG C++ library.

## Documentation and Examples

Visit [PCG-Random website] for information on how to use the C or C++ libraries, 
or look at the (ported to Java 8) sample code/demos -- hopefully it should be 
fairly self explanatory.
 
## Building

A configured Netbeans project is provided. Just 'build' and 'run', the default 
'Main class' is the 'pcg32_demo', with the run arguments '-r' (random seed) 
and 1 (generation rounds). A maven project is also provided cortesy of user 
https://github.com/vborrego.

The code is written in Java 8, and was tested with Oracle Java 8 JRE, in both 
Windows 7 64bits and Linux Mint 17.3 64bits operative systems.

## Testing

To run either of the three demos solely change in the Netbeans provided 
project 'properties' the 'Main class' to the desired ported to Java 8
demo: 'pcg32-global-demo' (which uses the global rng), 'pcg32-demo' (which 
uses a local generator), and pcg32x2-demo (which gangs together two generators, 
showing the usefulness of creating multiple generators).

To run the demos using a fixed seed (same output every time), run the demos 
with no 'Arguments' in the project 'properties' 'Run' option.

To produce different output, the demos with '-r' in the 'Arguments' in the 
project 'properties' 'Run' option.

You can also pass an integer count to specify how may rounds of output you
would like.
