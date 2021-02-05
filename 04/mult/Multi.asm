// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

/***
Assume R0 holds the multiplier and R1 holds the multiplicand
Then we just get the data from these two registers

Performance can be optimized by assigning the smaller value to be the 
multiplicand so as to reduce the number of operations. Such optimization
is impossible here since test case requires multiplication is done in 
20 clock cycles.

Note that:
- product can be updated in-place
- i must be incremented before going into the next loop
***/

@0
D=A
@i
M=D
@R2
M=D // product 

(LOOP)
@R1
D=M
@i
D=M-D // i - multiplicand/M[R1]
@END
D; JEQ

@R0
D=M
@R2
M=M+D
@i
M=M+1
@LOOP
0; JMP

(END)
@END
0; JMP
