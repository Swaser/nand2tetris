// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

	// start with 24576-1 in R1
	@24575
	D=A
	@R1
	M=D
(LOOP)
	// R1 = R1 + 1
	@R1
	M=M+1
	// if (R1 < 24576) goto KEYBOARD
	D=M
	@24576
	D=D-A
	@KEYBOARD
	D;JLT
	// R1 = SCREEN
	@SCREEN
	D=A
	@R1
	M=D
(KEYBOARD)
	// read keyboard; if nothing pressed whiten the screen
	@KBD
	D=M
	@BLACKEN
	D;JNE
(WHITEN)
	@R1
	A=M
	M=0
	@LOOP
	0;JEQ
(BLACKEN)
	@R1
	A=M
	M=-1
	@LOOP
	0;JEQ
