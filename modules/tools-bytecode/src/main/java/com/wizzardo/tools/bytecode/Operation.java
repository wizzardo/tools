package com.wizzardo.tools.bytecode;

import java.util.Arrays;

class Operation {
    private final static byte[] EMPTY = new byte[0];

    Instruction instruction;
    byte[] args;

    public Operation(Instruction instruction) {
        this(instruction, EMPTY);
    }

    public Operation(Instruction instruction, byte[] args) {
        if (args.length != instruction.operands)
            throw new IllegalArgumentException("Instruction " + instruction + " should have " + instruction.operands + " bytes of args");

        this.instruction = instruction;
        this.args = args;
    }

    public int length() {
        return 1 + instruction.operands;
    }

    @Override
    public String toString() {
        if (args.length == 0)
            return instruction.toString();
        else
            return instruction.toString() + " " + Arrays.toString(args);
    }
}
