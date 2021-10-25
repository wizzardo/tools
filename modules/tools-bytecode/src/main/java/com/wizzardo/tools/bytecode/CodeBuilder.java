package com.wizzardo.tools.bytecode;

import com.wizzardo.tools.misc.Pair;

import java.util.ArrayList;
import java.util.List;

public class CodeBuilder {
    List<Operation> operations = new ArrayList<>();

    public byte[] build() {
        byte[] bytes = new byte[length()];
        int position = 0;
        List<Pair<Integer, BranchingOperation>> branches = new ArrayList<>();

        for (Operation operation : operations) {
            bytes[position] = (byte) operation.instruction.code;
            position++;
            if (operation instanceof BranchingOperation) {
                branches.add(Pair.of(position, (BranchingOperation) operation));
            }
            if (operation.args.length > 0) {
                System.arraycopy(operation.args, 0, bytes, position, operation.args.length);
                position += operation.args.length;
            }
        }

        for (Pair<Integer, BranchingOperation> branch : branches) {
            BranchingOperation operation = branch.value;
            operation.from = branch.key;
            operation.to = position;
            int offset = position - branch.key + 1;
            operation.args[0] = bytes[branch.key] = (byte) ((offset >> 8) & 0xff);
            operation.args[1] = bytes[branch.key + 1] = (byte) (offset & 0xff);
            byte[] b = operation.codeBuilder.build();
            System.arraycopy(b, 0, bytes, position, b.length);
            position += b.length;
        }
        return bytes;
    }

    public CodeBuilder append(Instruction instruction) {
        operations.add(new Operation(instruction));
        return this;
    }

    public CodeBuilder append(CodeBuilder cb) {
        operations.addAll(cb.operations);
        return this;
    }

    public CodeBuilder append(Instruction instruction, byte[] args) {
        operations.add(new Operation(instruction, args));
        return this;
    }

    public CodeBuilder append(Instruction instruction, CodeBuilder cb) {
        if (instruction != Instruction.if_acmpne)
            throw new IllegalArgumentException(instruction + " is not a branching instruction");

        operations.add(new BranchingOperation(instruction, cb));
        return this;
    }

    public CodeBuilder append(Operation operation) {
        operations.add(operation);
        return this;
    }

    public int length() {
        return operations.stream().mapToInt(Operation::length).sum();
    }
}
