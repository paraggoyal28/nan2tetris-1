@R0
D=M

@num1             // num1
M=D

@R1
D=M

@num2
M=D             // num2

@i
M=0             // i

@R2
M=0

(LOOP)
@i
D=M

@num2
D=D-M

@EXIT
D;JGE

@R2
D=M

@num1
D=D+M

@R2
M=D

@i
M=M+1

@LOOP
0;JMP

(EXIT)
@EXIT
0;JMP

