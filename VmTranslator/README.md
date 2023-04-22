# VM Translator

## VM Specification

1. There are no statements or expressions outside of functions. 
2. Every function call must return a value. If a function has the 
`void` return type, then the compiler must add a `push constant 0` to
add a value to the stack to be returned (copied to _*ARG_).
3. Return values are copied to _*ARG_ and the stack pointer is set to
ARG+1 (so the last value on the stack is the return value). The return
value must be consumed, either by assigning it to a segment, or by
dropping it from the stack.
4. The starting function must not return. For syntactic reasons it will
need a return statement, but that statement must not be reached. Therefore
an endless loop must be inserted into the code just before the return.

### Explanations
About 3.: Return values are stored in *ARG. But what about functions
that have zero arguments but still return a value? We could try to 
handle all these different cases. Or we can handle them all the same.


## Planned Changes to the VM Specification

1. Allow `push static <name>` and `pop static <name>`.
2. Add instruction to drop a value from the stack `drop`. This is more important
than the dual operation of increasing the stack pointer without acutally pushing
something to the stack.