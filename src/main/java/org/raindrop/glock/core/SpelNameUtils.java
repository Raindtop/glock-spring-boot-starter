package org.raindrop.glock.core;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.raindrop.glock.annotation.Glock;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SpelNameUtils {
    /**
     * Spel 表达式解析
     */
    private SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 获取方法参数名字
     */
    private ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    public String getKeyName(JoinPoint joinPoint, Glock glock) {
        List<String> keyList = new ArrayList();
        Method method = getMethod(joinPoint);
        List<String> definitionKeys = getSpelDefinitionKey(glock.keys(), method, joinPoint.getArgs());
        keyList.addAll(definitionKeys);
        return StringUtils.collectionToDelimitedString(keyList,":","lock:","");
    }

    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        method.getParameterTypes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return method;
    }

    private List<String> getSpelDefinitionKey(String[] definitionKeys, Method method, Object[] parameterValues) {
        List<String> definitionKeyList = new ArrayList<String>();
        for (String definitionKey : definitionKeys) {
            if (!ObjectUtils.isEmpty(definitionKey)) {
                EvaluationContext context = new MethodBasedEvaluationContext(null, method, parameterValues, nameDiscoverer);
                Object objKey = parser.parseExpression(definitionKey).getValue(context);
                definitionKeyList.add(ObjectUtils.nullSafeToString(objKey));
            }
        }
        return definitionKeyList;
    }
}
