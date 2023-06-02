# VM Translator



## VM Specification

1. No code outside of functions.
2. Every function call must return a value. It's the compilers job
to make sure that functions that do not return a value will return one 
(_push const 0_).
3. Upon return the return value is popped to _ARG 0_. The other registers
are restored and the stack pointer is set to _*ARG + 1_
4. The return value is normally consumed through a _pop_ command. This should
be ensured by the compiler.
5. The starting function must not return. For syntactic reasons it will
need a return statement, but that statement must not be reached. Therefore,
an endless loop must be inserted into the code just before the return.
6. Static variables are special: If the argument to _push/pop static_ is 
alphanumeric, then this is to be the name of the static variable. If it is
numeric only, then the name of the variable should be _filename.number_. 
This is to work around a quirk of the Jack platform which aims to simplify
the exercises.


## Planned Changes to the VM Specification

1. Allow `push static <name>` and `pop static <name>`.
2. Add instruction to drop a value from the stack `drop`. This is more important
than the dual operation of increasing the stack pointer without actually pushing
something to the stack.