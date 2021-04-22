# Nand2Tetris Projects

A repo consisting of projects from the course [*From Nand to Tetris: Building a Modern Computer From First Principles*](https://www.nand2tetris.org/).

## Course Overview

If you are taking the course on Coursera where deadlines apply, it should be expected that the second part of the course requires much more commitment than the first one. The lectures are longer, concepts more mind-bending, and projects more time-consuming, exactly like an intermediate level undergraduate course.

Besides, although it is said programming background is not necessary, the course could be a headache with no prior knowledge of programming or computer science. However, picking up and cramming for relevant topics during the course could be possible too.

## Project 2

Two improved implementation for ADD16 compared to the default one in the course, as well as relevant resources:

**Carry Select Adder**

[8.2.2 Carry-select Adders](https://www.youtube.com/watch?v=S2c7pAFdP84&ab_channel=MITOpenCourseWare)

**Carry Lookahead Adder**

[8.2.3 Carry-lookahead Adders](https://www.youtube.com/watch?v=i1tUBZLWD3o&ab_channel=MITOpenCourseWare)

[Carry Lookahead Adder (Part 1) | CLA Generator](https://www.youtube.com/watch?v=6Z1WikEWxH0&ab_channel=NesoAcademy)

[Carry Lookahead Adder (Part 2) | CLA Adder](https://www.youtube.com/watch?v=9lyqSVKbyz8&ab_channel=NesoAcadem)

## Project 3

DFF, which comes as a primitive chip in this course, could be confusing, especially when clock is not very intuitive. These are some useful resources to understand DFF.

[Latches and Flip-Flops](https://www.youtube.com/playlist?list=PLTd6ceoshpreKyY55hA4vpzAUv9hSut1H)

[D Latch](https://www.youtube.com/watch?v=peCh_859q7Q&list=PLIAZKm9GwZo4XsZ0iD7k9n6n1LBIBTBwh&index=3&ab_channel=BenEater)

[D flip-flop](https://www.youtube.com/watch?v=YW-_GkUguMM&list=PLIAZKm9GwZo4XsZ0iD7k9n6n1LBIBTBwh&index=2&ab_channel=BenEaterBenEaterVerified)

## Project 4

### Multiplication

This is not difficult, but it is a bit tricky to optimize the algorithm, especially given the restriction of 20 clock cycles. Two important tips:
- use as less user-defined symbols as possible to reduce the number of clock cycles to finish one mulitplication
- arrange jumps and label symbols in a way to remove as many of them as possible.

### Fill

It is extremely important to differentiante D register and A register as well as their usage in the code while writing the machine code. Addresses are calculated, and as per the computed result manipulations are done.

## Project 7 & 8: VM Translator

Leave a note here to save someone's life probably: if you are uploading week 8's project file and the grader keeps failing your assignment, try rename the file to "project7.zip". It worked for me.

## Project 10 & 11: Compiler

This assignment is not difficult; the syntax of Jack language is already pretty simplified for educational purposes by the professors. However, it can be very time-consuming, because the compiler needs to cope with all syntactically legitimate arrangement. Following the steps proposed by the professor is a good way to go.

Since the test cases are guaranteed to be error-free and the Jack language is designed simple for educational purposes, the most important task here is to take into consideration all possible **valid** cases.

Although the VM code generated does not have to be identical to the test cases, it will be much easier to base the test on the provided files, since comparison could simply do the job.