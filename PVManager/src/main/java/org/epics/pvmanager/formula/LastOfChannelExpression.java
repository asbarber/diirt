/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.pvmanager.formula;

import java.util.List;
import org.epics.pvmanager.DataRecipe;
import org.epics.pvmanager.Function;
import org.epics.pvmanager.expression.DesiredRateExpression;
import org.epics.pvmanager.expression.DesiredRateExpressionImpl;
import org.epics.pvmanager.expression.DesiredRateExpressionList;
import static org.epics.pvmanager.ExpressionLanguage.*;

/**
 *
 * @author carcassi
 */
public class LastOfChannelExpression<T> implements DesiredRateExpression<T> {
    
    private final DesiredRateExpression<T> expression;
    private final Class<T> clazz;

    public LastOfChannelExpression(String name, Class<T> clazz) {
        this.expression = latestValueOf(channel(name, clazz, Object.class));
        this.clazz = clazz;
    }

    @Override
    public DesiredRateExpression<T> as(String name) {
        return expression.as(name);
    }

    @Override
    public String getName() {
        return expression.getName();
    }

    @Override
    public DataRecipe getDataRecipe() {
        return expression.getDataRecipe();
    }

    @Override
    public Function<T> getFunction() {
        return expression.getFunction();
    }

    @Override
    public DesiredRateExpressionImpl<T> getDesiredRateExpressionImpl() {
        return expression.getDesiredRateExpressionImpl();
    }

    @Override
    public DesiredRateExpressionList<T> and(DesiredRateExpressionList<? extends T> expressions) {
        return expression.and(expressions);
    }

    @Override
    public List<DesiredRateExpression<T>> getDesiredRateExpressions() {
        return expression.getDesiredRateExpressions();
    }
    
    public <N> LastOfChannelExpression<N> cast(Class<N> clazz) {
        if (this.clazz.isAssignableFrom(clazz)) {
            return new LastOfChannelExpression<N>(getName(), clazz);
        }
        return null;
    }

    public Class<T> getType() {
        return clazz;
    }
    
}
