# Nand2Tetris Projects

A repo consisting of projects from the course [*From Nand to Tetris: Building a Modern Computer From First Principles*](https://www.nand2tetris.org/).

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

This is basically pretty easy, but it is a bit tricky to optimize the algorithm, especially given the restriction of 20 clock cycles. Two important tips:
- use as less user-defined symbols as possible to reduce the number of clock cycles to finish one mulitplication
- arrange jumps and label symbols in a way to remove as many of them as possible.

### Fill

It is extremely important to differentiante D register and A register as well as their usage in the code while writing the machine code. Addresses are calculated, and as per the computed result manipulations are done.