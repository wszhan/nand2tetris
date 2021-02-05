// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Optimized original code by making the larger value to be the multiplier
// and the smaller one to be the multiplicand, thus reducing the number
// of ADD operations.

@R0
D=M
@R1
D=D-M // R0-R1
@R1_MULTIPLIER
D;JLT // R0-R1<0 or R0<R1
// @R0_MULTIPLIER
// D;JGE // R0-R1>=0 or R0>=R1 // remove these two lines to make it naturally flow

(R0_MULTIPLIER)
@R0
D=M
@multiplier
M=D

@R1
D=M
@multiplicand
M=D
@BEFORE_LOOP
0;JMP

(R1_MULTIPLIER)
@R1
D=M
@multiplier
M=D

@R0
D=M
@multiplicand
M=D
// @BEFORE_LOOP
// 0;JMP // remove these two lines to make it naturally flow

(BEFORE_LOOP)
@i
M=0
@R2 // init product register in-place
M=0

(LOOP)
@i
D=M
@multiplicand
D=M-D
@END
D;JEQ

@multiplier
D=M
@R2 // update product register in-place
M=M+D
@i 
M=M+1 // i++
@LOOP
0;JMP

(END)
@END
0; JMP
