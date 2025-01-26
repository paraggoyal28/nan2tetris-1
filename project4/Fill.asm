// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, 
// the screen should be cleared.

//// Replace this comment with your code.
(LOOP)
@KBD
D=M

@BLACK
D;JNE

@WHITE
D;JEQ

(BLACK)

@SCREEN
D=A

@i
M=D

(INNER)
@i
D=M

@KBD
D=D-A

@EXIT
D;JEQ

@i
A=M
M=-1

@i
M=M+1

@INNER
0;JMP

(WHITE)

@SCREEN
D=A

@i
M=D

(NEWINNER)
@i
D=M

@KBD
D=D-A

@EXIT
D;JEQ

@i
A=M
M=0

@i
M=M+1

@NEWINNER
0;JMP

(EXIT)
@LOOP
0;JMP



