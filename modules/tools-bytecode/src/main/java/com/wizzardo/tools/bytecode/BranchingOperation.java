package com.wizzardo.tools.bytecode;

class BranchingOperation extends Operation {
    CodeBuilder codeBuilder;
    int from;
    int to;

    public BranchingOperation(Instruction instruction, CodeBuilder codeBuilder) {
        super(instruction, new byte[2]);
        this.codeBuilder = codeBuilder;
    }

    @Override
    public int length() {
        return super.length() + codeBuilder.length();
    }
}
