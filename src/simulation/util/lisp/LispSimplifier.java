package simulation.util.lisp;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

/**
 * Created by YiMei on 1/10/16.
 */
public class LispSimplifier {

    public static String simplifyExpression(String expression) {
        expression = expression.trim();

        if (expression.charAt(0) == '(') {
            int nextWhiteSpaceIdx = expression.indexOf(' ');
            String func = expression.substring(1, nextWhiteSpaceIdx);
            String argsString = expression.substring(nextWhiteSpaceIdx + 1,
                    expression.length() - 1);
            List<String> args = LispUtil.splitArguments(argsString);

            args.replaceAll(LispSimplifier::simplifyExpression);

            switch (func) {
                case "-":
                    if (args.get(0).equals(args.get(1))) {
                        // a - a = 0
                        return "0";
                    }

                    if (args.get(1).equals("0")) {
                        // a - 0 = a
                        return args.get(0);
                    }

                    if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                        // do the calculation
                        double a = Double.parseDouble(args.get(0));
                        double b = Double.parseDouble(args.get(1));
                        return String.valueOf(a - b);
                    }
                    break;
                case "/":
                    if (args.get(0).equals(args.get(1))) {
                        // a / a = 1
                        return "1";
                    }

                    if (args.get(1).equals("0")) {
                        // a / 0 = 1 (protective division)
                        return "1";
                    }

                    if (args.get(1).equals("1")) {
                        // a / 1 = a
                        return args.get(0);
                    }

                    if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                        // do the calculation
                        double a = Double.parseDouble(args.get(0));
                        double b = Double.parseDouble(args.get(1));
                        return String.valueOf(a / b);
                    }
                    break;
                case "+":
                    if (args.get(0).equals("0")) {
                        // 0 + a = a
                        return args.get(1);
                    }

                    if (args.get(1).equals("0")) {
                        // a + 0 = a
                        return args.get(0);
                    }

                    if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                        // do the calculation
                        double a = Double.parseDouble(args.get(0));
                        double b = Double.parseDouble(args.get(1));
                        return String.valueOf(a + b);
                    }
                    break;
                case "*":
                    if (args.get(0).equals("1")) {
                        // 1 * a = a
                        return args.get(1);
                    }

                    if (args.get(1).equals("1")) {
                        // a * 1 = a
                        return args.get(0);
                    }

                    if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                        // do the calculation
                        double a = Double.parseDouble(args.get(0));
                        double b = Double.parseDouble(args.get(1));
                        return String.valueOf(a * b);
                    }
                    break;
                case "Max":
                    if (args.get(0).equals(args.get(1))) {
                        // max(a, a) = a
                        return args.get(0);
                    }

                    if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                        // do the calculation
                        double a = Double.parseDouble(args.get(0));
                        double b = Double.parseDouble(args.get(1));
                        double c = a;
                        if (c < b)
                            c = b;
                        return String.valueOf(c);
                    }
                    break;
                case "Min":
                    if (args.get(0).equals(args.get(1))) {
                        // min(a, a) = a
                        return args.get(0);
                    }

                    if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                        // do the calculation
                        double a = Double.parseDouble(args.get(0));
                        double b = Double.parseDouble(args.get(1));
                        double c = a;
                        if (c > b)
                            c = b;
                        return String.valueOf(c);
                    }
                    break;
                case "if":
                    if (args.get(1).equals(args.get(2))) {
                        // the second and third arguments are the same
                        return args.get(1);
                    }

                    if (NumberUtils.isNumber(args.get(0))) {
                        // the first argument is a number
                        double num = Double.parseDouble(args.get(0));
                        if (num > 0) {
                            // always positive
                            return args.get(1);
                        } else {
                            // always non-positive
                            return args.get(2);
                        }
                    }
                    break;
            }

            StringBuilder simplifiedExpression = new StringBuilder("(" + func);
            for (String arg : args) {
                simplifiedExpression.append(" ").append(arg);
            }
            simplifiedExpression.append(")");

            return simplifiedExpression.toString();
        } else {
            return expression;
        }
    }

    public static void main(String[] args) {
        String expression = "(+ (max (+ (+ (+ (+ (max OWT TIS) OWT) OWT) (+ TIS OWT)) TIS) (max (* (max OWT TIS) (min OWT NOR)) (+ TIS OWT))) (- (+ (+ TIS OWT) (+ TIS OWT)) (* PT W)))";

        String sexp = simplifyExpression(expression);
        System.out.println(sexp);
    }
}
