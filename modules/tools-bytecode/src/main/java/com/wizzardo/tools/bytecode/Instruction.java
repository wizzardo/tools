package com.wizzardo.tools.bytecode;

public enum Instruction {
    aaload(0x32),
    aastore(0x53),
    aload(0x19, 1),
    aload_0(0x2a),
    aload_1(0x2b),
    iconst_0(0x03),
    iconst_1(0x04),
    iload(0x15,1),
    iload_1(0x1b),
    iload_2(0x1c),
    iload_3(0x1d),
    lload(0x16,1),
    lload_1(0x1f),
    lload_2(0x20),
    lload_3(0x21),
    fload(0x17,1),
    dload(0x18,1),
    invokevirtual(0xb6, 2),
    invokespecial(0xb7, 2),
    invokestatic(0xb8, 2),
    nop(0x00),
    new_(0xbb, 2),
    bipush(0x10, 1),
    anewarray(0xbd, 2),
    dup(0x59),
    aconst_null(0x01),
    return_(0xb1),
    areturn(0xb0),
    ireturn(0xac),
    freturn(0xae),
    dreturn(0xaf),
    lreturn(0xad),
    getfield(0xb4, 2),
    putfield(0xb5, 2),
    getstatic(0xb2, 2),
    invokeinterface(0xb9, 4),
    iconst_m1(0x02),
    astore(0x3a,1),
    astore_1(0x4c),
    if_acmpne(0xa6, 2),
    checkcast(0xc0, 2),
    ldc(0x12, 1),
    ldc_w(0x13, 2),
    ldc2_w(0x14, 2),
    ;

    public final int code;
    public final int operands;

    Instruction(int code) {
        this(code, 0);
    }

    Instruction(int code, int operands) {
        this.code = code;
        this.operands = operands;
    }

    final static Instruction[] instructions;

    static {
        instructions = new Instruction[256];
        for (Instruction instruction : values()) {
            if (instructions[instruction.code] != null)
                throw new IllegalStateException("Instructions with same code! " + instructions[instruction.code] + " and " + instruction);
            instructions[instruction.code] = instruction;
        }
    }

    public static Instruction byCode(int code) {
        Instruction instruction = instructions[code];
        if (instruction == null)
            throw new IllegalArgumentException("Unknown instruction 0x" + Integer.toHexString(code));
        return instruction;
    }

    @Override
    public String toString() {
        return "Instruction{" +
                "code=" + code +
                ", operands=" + operands +
                '}';
    }
}
