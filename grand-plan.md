# The Grand Plan

## Possibilities
* Make Jack into an interpreted platform using C analog Lox
* Make Jack into a compiled platform using a C transpiler
* Port the interpreter platform for microcontroller (ARM or RISC-V)
* Write a Jack compiler including assembler for microcontroller that
runs on the microcontroller itself
* Write a code editor integrated acting like a command line, like C64 Basic
* Write a compiler for Jack on Microcontroller

Should I really write an interpreter? It would almost certainly be fast
enough on Raspberry Pi or Teensy. And probably also on RP 2040.

Having an interpreter would probably make things easier and I could refrain
from using Assemby. But Assembly is what I want to learn (together with C).

Or do the VM (not the interpreter). See crafting interpreters for that.