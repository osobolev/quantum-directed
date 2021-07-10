package directed.util;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public final class JSRunner {

    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    public JSRunner(Arithmetic a) {
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("arithmetic", a);
    }

    public Number evaluate(String text) {
        try {
            engine.eval("function sqrt(x) { return arithmetic.sqrt(x); }");
            Object res = engine.eval(text);
            if (res instanceof Number) {
                return (Number) res;
            }
            System.out.println(res == null ? null : res.getClass() + " " + res);
        } catch (ScriptException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
