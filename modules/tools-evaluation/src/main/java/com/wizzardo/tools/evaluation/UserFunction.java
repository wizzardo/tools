package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.misc.Unchecked;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: moxa
 * Date: 11/5/12
 */
public class UserFunction extends Expression {
    private Expression eh;
    private String[] argsNames;
    private String name;
    private Expression[] args;
    private Map<String, UserFunction> functions;
    private boolean notCreateOwnModel = false;

    public UserFunction(String name, String exp, String... argsNames) {
        this.exp = exp;
        this.name = name;
        this.argsNames = argsNames;
    }

    public UserFunction(String name, Expression eh, String... argsNames) {
        this.eh = eh;
        this.name = name;
        this.argsNames = argsNames;
    }

    @Override
    public void setVariable(Variable v) {
        if (eh != null)
            eh.setVariable(v);
        if (args != null)
            for (Expression e : args)
                e.setVariable(v);
    }

    public void prepare(Map<String, Object> model) {
        if (eh == null)
            eh = EvalTools.prepare(exp, model, functions);
    }

    @Override
    public UserFunction clone() {
        String[] args = null;
        if (argsNames != null) {
            args = new String[argsNames.length];
            System.arraycopy(argsNames, 0, args, 0, args.length);
        }
        if (eh == null) {
            return new UserFunction(name, exp, args);
        } else {
            return new UserFunction(name, eh.clone(), args);
        }
    }

    public void setArgs(Expression[] args) {
        this.args = args;
    }

    public void setUserFunctions(Map<String, UserFunction> functions) {
        this.functions = functions;
    }

    public boolean isNotCreateOwnModel() {
        return notCreateOwnModel;
    }

    public void setNotCreateOwnModel(boolean b) {
        notCreateOwnModel = b;
    }

    public String getArgumentName(int i) {
        if (argsNames == null) {
            return null;
        }
        return argsNames[i];
    }

    public Object get(Map<String, Object> model) {
        Map<String, Object> m;
        if (notCreateOwnModel) {
            m = model;
        } else if (model != null) {
            m = new HashMap<String, Object>(model);
        } else {
            m = new HashMap<String, Object>();
        }
//        System.out.println("get user function: "+exp);
//        System.out.println("argsNames: " + Arrays.toString(argsNames));
//        System.out.println("args: " + Arrays.toString(args));
        try {
            if (argsNames != null && args != null)
                for (int i = 0; i < argsNames.length; i++) {
                    m.put(argsNames[i], args[i].get(model));
                }
            if (eh == null) {
                eh = EvalTools.prepare(exp, m, functions);
            }
//            System.out.println(m);
            Object r = eh.get(m);
            if (model != null)
                for (Map.Entry<String, Object> entry : model.entrySet()) {
                    if (m.get(entry.getKey()) != entry.getValue()) {
                        entry.setValue(m.get(entry.getKey()));
                    }
                }
            return r;
        } catch (Exception e) {
            throw Unchecked.rethrow(e);
        }
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name + " = " + exp;
    }
}
