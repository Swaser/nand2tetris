
// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768. 2^15 wegen signed int

	
	//n=R1
	@R1
	D=M
	@n
	M=D
	//R2=0
	@R2
	M=0
	@n
	D=M
(LOOP)
	//     if (n == 0) goto END
	@END
	D;JEQ
	//     R2=R2+R0
	@R0
	D=M
	@R2
	M=D+M
	//     n=n-1
	@n
	M=M-1
	D=M
	//     goto LOOP
	@LOOP
	0;JEQ
(END)
	@END
	0;JEQ