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

// total number of registers dedicated for SCREEN memory maps
@8192
D=A
// store with variable symbols for readability purpose
@screen_limit
M=D

(LISTENING)
@0
D=A
@i
M=D
@KBD
D=M
@CLEAR
D;JEQ
@FILL
D;JNE

// whenever a key is pressed down
(FILL)
@i
D=M
@screen_limit
D=M-D
@LISTENING
D;JEQ

@i
D=M
@SCREEN
A=A+D
M=-1
@i
M=M+1
@FILL
0;JMP

// whenever no key is pressed down
(CLEAR)
@i
D=M
@screen_limit
D=M-D
@LISTENING
D;JEQ

@i
D=M
@SCREEN
A=A+D
M=0
@i
M=M+1
@CLEAR
0;JMP

