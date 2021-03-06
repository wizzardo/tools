package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.io.FileTools;
import com.wizzardo.tools.misc.Pair;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptEngine {
    public static final FileFilter NOOP_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return true;
        }
    };

    protected final File root;
    protected FileFilter fileFilter = NOOP_FILTER;

    public ScriptEngine(File root) {
        this.root = root;
    }

    public Object run(String pathToScript) {
        String script = FileTools.text(new File(root, pathToScript));

        List<String> imports = new ArrayList<String>();
        String pack = EvalTools.readPackage(script);
        script = EvalTools.readImports(script, imports);

        Binding binding = new Binding(root, pack, new ArrayList<String>(imports));
        Expression expression = EvalTools.prepare(script, binding, new HashMap<String, UserFunction>(), imports);
        return expression.get(binding);
    }

    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    public static class Binding extends HashMap<String, Object> {
        protected File root;
        protected String pack;
        protected List<String> imports;
        protected List<String> dependencies = new ArrayList<String>();
        protected FileFilter fileFilter;
        protected Map<String, Class> classCache = new HashMap<String, Class>();
        protected long findClassDuration = 0;
        protected long findClassCount = 0;

        public Binding(File root, String pack, List<String> imports) {
            this(root, pack, imports, NOOP_FILTER);
        }

        public Binding(File root, String pack, List<String> imports, FileFilter filter) {
            this.root = root;
            this.pack = pack.replace('.', '/') + '/';
            this.imports = imports;
            this.fileFilter = filter;
        }

        public List<String> getDependencies() {
            return dependencies;
        }

        @Override
        public Object get(Object key) {
            if (this.containsKey(key))
                return super.get(key);

            String k = key.toString();
            if (k.startsWith("class ")) {
                Pair<String, ClassExpression> resolved = resolve(k.substring(6));
                if (resolved != null) {
                    put(k, resolved.value);
                    dependencies.add(resolved.key);
                    return resolved.value;
                }
            }

            return null;
        }

        public Pair<String, ClassExpression> resolve(String name) {
            String path = resolveFile(name);
            if (path != null) {
                ClassExpression classExpression = resolveClassExpression(name, path);
                if (classExpression != null)
                    return Pair.of(path, classExpression);
            }

            return null;
        }

        public String resolveFile(String name) {
            for (String imp : imports) {
                if (imp.endsWith(".*")) {
                    File file = new File(root, imp.substring(0, imp.length() - 1).replace('.', '/') + name + ".groovy");
                    if (!file.exists())
                        file = new File(root, imp.substring(0, imp.length() - 1).replace('.', '/') + name + ".java");

                    if (file.exists() && fileFilter.accept(file)) {
                        return file.getPath();
                    }
                } else if (imp.endsWith(name) && imp.charAt(imp.length() - 1 - name.length()) == '.') {
                    File file = new File(root, imp.replace('.', '/') + ".groovy");
                    if (!file.exists())
                        file = new File(root, imp.replace('.', '/') + ".java");

                    if (file.exists() && fileFilter.accept(file)) {
                        return file.getPath();
                    }
                }
            }

            {
                File file = new File(root, pack + name + ".groovy");
                if (!file.exists())
                    file = new File(root, pack + name + ".java");

                if (file.exists() && fileFilter.accept(file)) {
                    return file.getPath();
                }
            }

            return null;
        }

        protected ClassExpression resolveClassExpression(String name, String path) {
            String script = FileTools.text(path);
            return resolveClassExpression(name, path, script);
        }

        protected ClassExpression resolveClassExpression(String name, String path, String script) {
            List<String> imports = new ArrayList<String>();
            String pack = EvalTools.readPackage(script);
            script = EvalTools.readImports(script, imports);

            Binding binding = createBidding(root, pack, imports, fileFilter);
            Expression expression = EvalTools.prepare(script, binding, new HashMap<String, UserFunction>(), imports);

//            System.out.println("resolveClassExpression: " + name + ", " + file);
//            System.out.println("binding.findClassDuration: " + (binding.findClassDuration / 1000 / 1000f) + "ms");
//            System.out.println("binding.findClassCount: " + (binding.findClassCount));
//            System.out.println("binding.classCache.size(): " + (binding.classCache.size()));

            if (binding.containsKey("class " + name)) {
                return (ClassExpression) binding.get("class " + name);
            }
            return null;
        }

        protected Binding createBidding(File root, String pack, List<String> imports, FileFilter fileFilter) {
            return new Binding(root, pack, imports, fileFilter);
        }
    }
}
